package client;

import java.beans.PropertyChangeEvent;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import server.RemoteServer;
import server.RemoteServerInterface;
import assignments.util.MiscAssignmentUtils;
import assignments.util.inputParameters.ASimulationParametersController;
import assignments.util.inputParameters.AnAbstractSimulationParametersBean;
import assignments.util.mainArgs.ClientArgsProcessor;
import main.BeauAndersonFinalProject;
import stringProcessors.HalloweenCommandProcessor;
import util.annotations.Tags;
import util.interactiveMethodInvocation.IPCMechanism;
import util.interactiveMethodInvocation.SimulationParametersController;
import util.tags.DistributedTags;
import util.trace.bean.BeanTraceUtility;
import util.trace.factories.FactoryTraceUtility;
import util.trace.misc.ThreadDelayed;
import util.trace.port.PerformanceExperimentEnded;
import util.trace.port.PerformanceExperimentStarted;
import util.trace.port.consensus.ConsensusTraceUtility;
import util.trace.port.consensus.ProposalMade;
import util.trace.port.consensus.ProposedStateSet;
import util.trace.port.consensus.RemoteProposeRequestSent;
import util.trace.port.consensus.communication.CommunicationStateNames;
import util.trace.port.nio.NIOTraceUtility;
import util.trace.port.rpc.gipc.GIPCRPCTraceUtility;
import util.trace.port.rpc.rmi.RMIObjectLookedUp;
import util.trace.port.rpc.rmi.RMIObjectRegistered;
import util.trace.port.rpc.rmi.RMIRegistryLocated;
import util.trace.port.rpc.rmi.RMITraceUtility;

@Tags({DistributedTags.CLIENT, DistributedTags.RMI, DistributedTags.NIO})
public class Client  extends AnAbstractSimulationParametersBean {
	

	private static Client aClientInfo;
	private static HalloweenCommandProcessor commandProcessor;
	
	
	private static RemoteClient rClient;
	private static NIOClient nClient;
	private static RemoteServerInterface proxy;

	public Client() {
		FactoryTraceUtility.setTracing();
		BeanTraceUtility.setTracing();
		NIOTraceUtility.setTracing();
		RMITraceUtility.setTracing();
		ConsensusTraceUtility.setTracing();
		ThreadDelayed.enablePrint();
		GIPCRPCTraceUtility.setTracing();		
		
	}	
	
	public static Client getSingleton() {
		return aClientInfo;
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
		if (this.broadcastMetaState) {
			ProposalMade.newCase(this, CommunicationStateNames.BROADCAST_MODE, -1, state);
		
				try {
					proxy.broadcastAtomic(state);
					RemoteProposeRequestSent.newCase(this, CommunicationStateNames.BROADCAST_MODE, -1, state);

				} catch (RemoteException e) {
					e.printStackTrace();
				}
			
		}
		ProposedStateSet.newCase(this, CommunicationStateNames.BROADCAST_MODE, -1, state);
		atomicBroadcast = state;
	}
	
	@Override
	public synchronized void setIPCMechanism(IPCMechanism state) {
		if (this.broadcastMetaState) {
			try {
				proxy.broadcastIPC(state);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		ProposedStateSet.newCase(this, CommunicationStateNames.IPC_MECHANISM, -1, state);
		ipcMechanism = state;
	}
	
	@Override
	public void simulationCommand(String cmd) {
		//Proposal
		ProposalMade.newCase(this, CommunicationStateNames.COMMAND, -1, cmd);
		//Nio
		nClient.getNIOSender().propertyChange(new PropertyChangeEvent(this, "InputString", null, cmd));
		//Rmi
		rClient.propertyChange(new PropertyChangeEvent(this, "InputString", null, cmd));
	}
	
	public synchronized void setAtomicBroadcastAfterConsensus(Boolean state) {
		ProposedStateSet.newCase(this, CommunicationStateNames.BROADCAST_MODE, -1, state);
		atomicBroadcast = state == null ? atomicBroadcast : state;
		}
	
	
	public synchronized void setIPCMechanismAfterConsensus(IPCMechanism state) {
		ProposedStateSet.newCase(this, CommunicationStateNames.IPC_MECHANISM, -1, state);
		
		ipcMechanism = state == null ? ipcMechanism : state;
		
	}
	public static void buildNIO(String[] args) {
		SimulationParametersController aSimulationParametersController = 
				new ASimulationParametersController();
		NIOClient.launchClient(ClientArgsProcessor.getServerHost(args),
				ClientArgsProcessor.getServerPort(args),
				ClientArgsProcessor.getClientName(args), aSimulationParametersController);
	}
	
	public static void main(String[] args) {
		args = ClientArgsProcessor.removeEmpty(args);
		MiscAssignmentUtils.setHeadless(ClientArgsProcessor.getHeadless(args));

		aClientInfo = new Client();
		
		// RMI
				try {
					Registry rmiRegistry = LocateRegistry.getRegistry(ClientArgsProcessor.getRegistryPort(args));
					RMIRegistryLocated.newCase(Client.getSingleton(), ClientArgsProcessor.getRegistryHost(args), ClientArgsProcessor.getRegistryPort(args), rmiRegistry);
					proxy = (RemoteServerInterface) rmiRegistry.lookup(RemoteServer.RMI_SERVER_NAME);
					RMIObjectLookedUp.newCase(Client.getSingleton(), proxy, proxy.toString(), rmiRegistry);
					rClient = new RemoteClient(proxy);
					//export
					UnicastRemoteObject.exportObject(rClient.getProxy(), 0);
					RMIObjectRegistered.newCase(RemoteClient.class, rClient.getClientName(), rClient, rmiRegistry);
					rmiRegistry.rebind(rClient.getClientName(), rClient.getProxy());
					proxy.join(rClient.getClientName(), rClient.getProxy());

				} catch (Exception e) {
					e.printStackTrace();
				}
				
				//NIO
				buildNIO(args);
	}

	public static void initializeNIOClient(NIOClient aNIOClient) {
		nClient = aNIOClient;
	}
	public void experimentInput() {
		long start = System.currentTimeMillis();
		PerformanceExperimentStarted.newCase(this, start, 1000);
		int i = 0;
		while (i < 1000) {
			commandProcessor.setInputString("move 1 1");
			i += 1;
		}
		long end = System.currentTimeMillis();
		long total = end-start;
		PerformanceExperimentEnded pfe = PerformanceExperimentEnded.newCase(this, start, end, end - start, 1000);
		System.out.println("experimentInput: " + total + "ms");			
	}
}