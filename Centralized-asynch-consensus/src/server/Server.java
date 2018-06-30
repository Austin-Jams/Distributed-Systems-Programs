package server;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import assignments.util.inputParameters.ASimulationParametersController;
import assignments.util.inputParameters.AnAbstractSimulationParametersBean;
import assignments.util.mainArgs.ClientArgsProcessor;
import assignments.util.mainArgs.ServerArgsProcessor;
import inputport.rpc.ACachingAbstractRPCProxyInvocationHandler;
import inputport.rpc.GIPCLocateRegistry;
import inputport.rpc.GIPCRegistry;
import main.BeauAndersonFinalProject;
import port.ATracingConnectionListener;
import stringProcessors.HalloweenCommandProcessor;
import util.annotations.Tags;
import util.interactiveMethodInvocation.IPCMechanism;
import util.interactiveMethodInvocation.SimulationParametersController;
import util.tags.DistributedTags;
import util.trace.bean.BeanTraceUtility;
import util.trace.factories.FactoryTraceUtility;
import util.trace.misc.ThreadDelayed;
import util.trace.port.consensus.ConsensusTraceUtility;
import util.trace.port.consensus.ProposedStateSet;
import util.trace.port.consensus.communication.CommunicationStateNames;
import util.trace.port.nio.NIOTraceUtility;
import util.trace.port.rpc.gipc.GIPCRPCTraceUtility;
import util.trace.port.rpc.rmi.RMIObjectRegistered;
import util.trace.port.rpc.rmi.RMIRegistryLocated;
import util.trace.port.rpc.rmi.RMITraceUtility;

@Tags({DistributedTags.SERVER, DistributedTags.RMI, DistributedTags.NIO, DistributedTags.GIPC})
public class Server extends AnAbstractSimulationParametersBean {

	private static Server serverInfo;
	private static HalloweenCommandProcessor commandProcessor;
	public static RemoteServer rmiServer;

	public static void buildNIOServer(String[] args) {
		NIOServer aServer = new NIOServer();
		SimulationParametersController aSimulationParametersController = new ASimulationParametersController();
		aSimulationParametersController.addSimulationParameterListener(Server.getSingleton());
		aServer.initialize(ServerArgsProcessor.getServerPort(args));
		aSimulationParametersController.processCommands();
	}
	
	public static void buildGIPCServer(String[] args) {
		GIPCRegistry gipcRegistry = GIPCLocateRegistry.createRegistry(ServerArgsProcessor.getGIPCServerPort(args));
		GIPCServerHandler gipcServer = new GIPCServerHandler(gipcRegistry);
		gipcRegistry.rebind(GIPCServerHandler.GIPC_SERVER_NAME, gipcServer);
		gipcRegistry.getInputPort().addConnectionListener(new ATracingConnectionListener(gipcRegistry.getInputPort()));
	
	}
	public static void main(String[] args) {
		args = ServerArgsProcessor.removeEmpty(args);
		FactoryTraceUtility.setTracing();
		NIOTraceUtility.setTracing();
		BeanTraceUtility.setTracing();
		RMITraceUtility.setTracing();
		ConsensusTraceUtility.setTracing();
		ThreadDelayed.enablePrint();
		GIPCRPCTraceUtility.setTracing();
		GIPCRegistry gipcRegistry = GIPCLocateRegistry.createRegistry(ServerArgsProcessor.getGIPCServerPort(args));
		GIPCServerHandler gipcServer = new GIPCServerHandler(gipcRegistry);
		gipcRegistry.rebind(GIPCServerHandler.GIPC_SERVER_NAME, gipcServer);
		gipcRegistry.getInputPort().addConnectionListener(new ATracingConnectionListener(gipcRegistry.getInputPort()));
		try {
			Registry rmiRegistry = LocateRegistry.getRegistry(ServerArgsProcessor.getRegistryPort(args));
			RMIRegistryLocated.newCase(Server.getSingleton(), ServerArgsProcessor.getRegistryHost(args), ServerArgsProcessor.getRegistryPort(args), rmiRegistry);
			rmiServer = new RemoteServer(rmiRegistry);
			UnicastRemoteObject.exportObject(rmiServer, 0);
			rmiRegistry.rebind(RemoteServer.RMI_SERVER_NAME, rmiServer);
			RMIObjectRegistered.newCase(RemoteServer.class, rmiServer.toString(), rmiServer, rmiRegistry);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		buildNIOServer(args);
	}
	
	public static Server getSingleton() {
		if ( serverInfo == null) {
			serverInfo = new Server();
		}
		return serverInfo;
	}
	
	public static HalloweenCommandProcessor getCommandProcessor() {
		if (commandProcessor == null) {
			commandProcessor = BeauAndersonFinalProject.createSimulation(
					"Austin Schaefers", 0 , 0 , 1000, 700,100,100);
		}
		return commandProcessor;
	}
	
	@Override
	public synchronized void setAtomicBroadcast(Boolean state) {
		ProposedStateSet.newCase(this, CommunicationStateNames.BROADCAST_MODE, -1, state);
		if (this.broadcastMetaState) {
			try {
				rmiServer.broadcastAtomic(state);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		atomicBroadcast = state;
	}
	
	@Override
	public synchronized void setIPCMechanism(IPCMechanism state) {
		if (this.broadcastMetaState) {
			try {
				rmiServer.broadcastIPC(state);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		ipcMechanism = state;
	}
}