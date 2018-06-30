package client;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ArrayBlockingQueue;

import assignments.util.MiscAssignmentUtils;
import assignments.util.inputParameters.ASimulationParametersController;
import assignments.util.inputParameters.SimulationParametersListener;
import assignments.util.mainArgs.ClientArgsProcessor;
import util.trace.bean.BeanTraceUtility;
import util.trace.factories.FactoryTraceUtility;
import util.trace.port.PerformanceExperimentEnded;
import util.trace.port.PerformanceExperimentStarted;
import util.trace.port.nio.NIOTraceUtility;
import inputport.nio.manager.NIOManagerFactory;
import inputport.nio.manager.factories.classes.AReadingWritingConnectCommandFactory;
import inputport.nio.manager.factories.selectors.ConnectCommandFactorySelector;
import inputport.nio.manager.listeners.SocketChannelConnectListener;
import main.BeauAndersonFinalProject;
import server.NIOServer;
import stringProcessors.HalloweenCommandProcessor;

import util.annotations.Tags;
import util.interactiveMethodInvocation.ConsensusAlgorithm;
import util.interactiveMethodInvocation.IPCMechanism;
import util.interactiveMethodInvocation.SimulationParametersController;
import util.tags.DistributedTags;
@Tags({DistributedTags.CLIENT})
public class NIOClient implements SocketChannelConnectListener, SimulationParametersListener {
	String clientID;
	HalloweenCommandProcessor commandProcessor;
	Sender cSender;
	SocketChannel socketChannel;
	Receiver cReceiver;
	private ArrayBlockingQueue<ByteBuffer> readQueue;
	
	private boolean atomic;
	private boolean localProcessing;
	
	public NIOClient(String aClientName) {
		clientID = aClientName;
		readQueue = new ArrayBlockingQueue<ByteBuffer>(4096);
		cReceiver = new Receiver(readQueue);
		atomic = false;
		localProcessing = false;
		
	}
	protected void setFactories() {		
		ConnectCommandFactorySelector.setFactory(new AReadingWritingConnectCommandFactory());
		
	}
	public void initialize(String aServerHost, int aServerPort) {
		setFactories();
		socketChannel = createSocketChannel();
		createSimulation();
		createCommunicationObjects();
		addListeners();
		connectToServer(aServerHost, aServerPort);
	}
	public HalloweenCommandProcessor startSimulation(String prefix, int simX, int simY, int simWidth,int simHeight, int commandX, int commandY) {
		return BeauAndersonFinalProject.createSimulation(prefix, simX, simY, simWidth, simHeight, commandX, commandY);
	}
	public void createSimulation() {
		commandProcessor = startSimulation("Austin Schaefers", 0 , 0 , 1000, 700,100,100);
		Reader reader = new Reader(readQueue, commandProcessor);
		Thread readThread = new Thread(reader);
		readThread.setName(NIOServer.READ_THREAD_NAME);
		readThread.start();
	}
	public void createCommunicationObjects() {
		createSender();
	}

	public void connectToServer(String aServerHost, int aServerPort) {
		createSender();
		connectToSocketChannel(aServerHost, aServerPort);

	}

	protected void connectToSocketChannel(String aServerHost, int aServerPort) {
		try {
			InetAddress aServerAddress = InetAddress.getByName(aServerHost);
			NIOManagerFactory.getSingleton().connect(socketChannel,
					aServerAddress, aServerPort, this);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected SocketChannel createSocketChannel() {
		try {
			SocketChannel retVal = SocketChannel.open();
			return retVal;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public void connected(SocketChannel aSocketChannel) {
		System.out.println("Ready to send messages to server");
		NIOManagerFactory.getSingleton().addReadListener(socketChannel, cReceiver);
	}
	
	
	protected void createSender() {
		cSender = new Sender(socketChannel,
				clientID, this);
	}
	
	protected void addListeners() {
		addModelListener();
	}
	protected void addModelListener(){
		commandProcessor.addPropertyChangeListener(cSender);
	}
	@Override
	public void notConnected(SocketChannel aSocketChannel, Exception e) {
		System.err.println("Could not connect:" +aSocketChannel);
		if (e != null)
		   e.printStackTrace();
	}
	/**
	 * Connect the client with the specified name to the specified server.
	 */
	public static void launchClient(String aServerHost, int aServerPort,
			String aClientName, SimulationParametersController aSimulationParametersController) {
		FactoryTraceUtility.setTracing();
		BeanTraceUtility.setTracing();
		NIOTraceUtility.setTracing();

		NIOClient aClient = new NIOClient(
				aClientName);
		aSimulationParametersController.addSimulationParameterListener(aClient);
		aClient.initialize(aServerHost, aServerPort);	
		aSimulationParametersController.processCommands(); // start the console loop
	}

	public static void main(String[] args) {
		args = ClientArgsProcessor.removeEmpty(args);
		MiscAssignmentUtils.setHeadless(ClientArgsProcessor.getHeadless(args));
		SimulationParametersController aSimulationParametersController = 
				new ASimulationParametersController();
		launchClient(ClientArgsProcessor.getServerHost(args),
				ClientArgsProcessor.getServerPort(args),
				ClientArgsProcessor.getClientName(args), aSimulationParametersController);	
	}
	public boolean isAtomic() {
		return atomic;
	}
	
	public boolean localProcessing() {
		return localProcessing;
	}
	
	@Override
	public void atomicBroadcast(boolean newValue) {
		System.out.println("atomicBroadcast " + newValue);
		atomic = newValue;
	}

	@Override
	public void ipcMechanism(IPCMechanism newValue) {
		System.out.println("ipcMechanism " + newValue);	
	}

	@Override
	public void experimentInput() {
		long start = System.currentTimeMillis();
		PerformanceExperimentStarted.newCase(this, start, 1000);
		int i = 0;
		while (i < 1000) {
			if (i % 2 == 0) {
				commandProcessor.setInputString("move 5 0");
			}else {
				commandProcessor.setInputString("move -5 0");
			}
			i++;
		}
		long end = System.currentTimeMillis();
		long time = end - start;
		PerformanceExperimentEnded pfe = PerformanceExperimentEnded.newCase(this, start, end, time, 1000);
		System.out.println("Duration in Milliseconds:"+ time);			
	}
	@Override
	public void localProcessingOnly(boolean newValue) {
		System.out.println("localProcessingOnly " + newValue);
		localProcessing = newValue;
	}

	@Override
	public void waitForBroadcastConsensus(boolean newValue) {
		System.out.println("waitForBroadcastConsensus " + newValue);

		
	}

	@Override
	public void waitForIPCMechanismConsensus(boolean newValue) {
		System.out.println("waitForIPCMechanismConsensus " + newValue);		
	}

	@Override
	public void consensusAlgorithm(ConsensusAlgorithm newValue) {
		System.out.println("consensusAlgorithm " + newValue);		
	}
	@Override
	public void quit(int aCode) {
		System.exit(aCode);
		
	}
	@Override
	public void simulationCommand(String aCommand) {
		commandProcessor.setInputString(aCommand);
		
	}
}