package server;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import assignments.util.inputParameters.SimulationParametersListener;
import assignments.util.mainArgs.ServerArgsProcessor;
import client.Client;
import commandprocessing.Processor;
import consensus.ProposalFeedbackKind;
import inputport.nio.manager.NIOManagerFactory;
import util.annotations.Tags;
import util.interactiveMethodInvocation.ConsensusAlgorithm;
import util.interactiveMethodInvocation.IPCMechanism;
import util.misc.ThreadSupport;
import util.trace.bean.BeanTraceUtility;
import util.trace.factories.FactoryTraceUtility;
import util.trace.misc.ThreadDelayed;
import util.trace.port.consensus.ConsensusTraceUtility;
import util.trace.port.consensus.ProposalAcceptRequestSent;
import util.trace.port.consensus.ProposalAcceptedNotificationReceived;
import util.trace.port.consensus.ProposalLearnedNotificationSent;
import util.trace.port.consensus.RemoteProposeRequestReceived;
import util.trace.port.consensus.communication.CommunicationStateNames;
import util.trace.port.nio.NIOTraceUtility;
import util.trace.port.rpc.rmi.RMIObjectLookedUp;
import util.trace.port.rpc.rmi.RMIObjectRegistered;
import util.trace.port.rpc.rmi.RMITraceUtility;

import java.nio.channels.SocketChannel;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import util.tags.DistributedTags;

public class RemoteServer implements RemoteServerInterface {
	public static final String RMI_SERVER_NAME = "RMI_SERVER";

	private Map<String, Processor> clients;
	private Registry rmiRegistry;

	public RemoteServer(Registry rmiRegistry) {
		this.rmiRegistry = rmiRegistry;
		clients = new HashMap<String, Processor>();
		
		Server.getSingleton().setAtomicBroadcast(false);
		Server.getSingleton().localProcessingOnly(false);
	}

	public static void main(String[] args) {
		FactoryTraceUtility.setTracing();
		BeanTraceUtility.setTracing();
		NIOTraceUtility.setTracing();
		RMITraceUtility.setTracing();
		ConsensusTraceUtility.setTracing();
		ThreadDelayed.enablePrint();

		try {
			Registry rmiRegistry = LocateRegistry.getRegistry(ServerArgsProcessor.getRegistryHost(args));
			RemoteServer server = new RemoteServer(rmiRegistry);
			UnicastRemoteObject.exportObject(server, 0);
			RMIObjectRegistered.newCase(RemoteServer.class, server.toString(), server, rmiRegistry);
			rmiRegistry.rebind(RMI_SERVER_NAME, server);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void join(String name, Processor callback) throws RemoteException {
		System.out.println(name + " has connected!");
		clients.put(name, callback);
	}

	@Override
	public void submitCommand(String invokerName, String command) throws RemoteException {
		RemoteProposeRequestReceived.newCase(this, CommunicationStateNames.COMMAND, -1, command);
		RemoteProposeRequestReceived.newCase(this, CommunicationStateNames.COMMAND, -1, command);
		if (Server.getSingleton().getConsensusAlgorithm() == ConsensusAlgorithm.CENTRALIZED_SYNCHRONOUS) {
			boolean accept = true;
			for (String proxyName : clients.keySet()) {
				Processor clientProxy = null;
				try {
					clientProxy = (Processor) rmiRegistry.lookup(proxyName);
					RMIObjectLookedUp.newCase(this, clientProxy, clientProxy.toString(), rmiRegistry);
				} catch (NotBoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				ProposalAcceptRequestSent.newCase(this, CommunicationStateNames.COMMAND, -1, command);
				boolean clientAccept = clientProxy.rProposal(CommunicationStateNames.COMMAND, command);
				ProposalAcceptedNotificationReceived.newCase(this, CommunicationStateNames.COMMAND, -1, command,
						ProposalFeedbackKind.SUCCESS);
				if (!clientAccept) {
					accept = false;
				}
			}
			if (!accept) {
		
				command = "";
			}
		}
		System.out.println("Command: " + command + " by " + invokerName + " successfully sent to server");
		for (String proxyName : clients.keySet()) {
			if (!Server.getSingleton().isAtomicBroadcast() && invokerName.equals(proxyName)) {
				continue;
			}
	
			System.out.println("Attempting to invoke " + command + " on " + proxyName);
			try {
				ThreadSupport.sleep(Server.getSingleton().getDelay());
				Processor clientProxy = (Processor) rmiRegistry.lookup(proxyName);
				ProposalLearnedNotificationSent.newCase(this, CommunicationStateNames.COMMAND, -1, command);
				clientProxy.submitRemoteCmd(command);
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}

	public void quit(int aCode) {
		System.exit(aCode);
	}

	@Override
	public void broadcastAtomic(Boolean newValue) throws RemoteException {
		RemoteProposeRequestReceived.newCase(this, CommunicationStateNames.BROADCAST_MODE, -1, newValue);
		if (Server.getSingleton().getConsensusAlgorithm() == ConsensusAlgorithm.CENTRALIZED_SYNCHRONOUS) {
			RemoteProposeRequestReceived.newCase(this, CommunicationStateNames.BROADCAST_MODE, -1, newValue);
			boolean accept = true;
			for (String proxyName : clients.keySet()) {
				Processor clientProxy = null;
				try {
					clientProxy = (Processor) rmiRegistry.lookup(proxyName);
				} catch (NotBoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				ProposalAcceptRequestSent.newCase(this, CommunicationStateNames.BROADCAST_MODE, -1, newValue);
				boolean clientAccept = clientProxy.rProposal(CommunicationStateNames.BROADCAST_MODE, newValue);
				ProposalAcceptedNotificationReceived.newCase(this, CommunicationStateNames.BROADCAST_MODE, -1, newValue,
						ProposalFeedbackKind.SUCCESS);
				if (!clientAccept) {
					accept = false;
				}
			}
			if (!accept) {
			
				newValue = null;
			}
		}
		for (String proxyName : clients.keySet()) {
			Processor clientProxy = null;
			try {
				clientProxy = (Processor) rmiRegistry.lookup(proxyName);
			} catch (NotBoundException e) {
				e.printStackTrace();
			}
			if (Server.getSingleton().getConsensusAlgorithm() == ConsensusAlgorithm.CENTRALIZED_SYNCHRONOUS) {
				ProposalLearnedNotificationSent.newCase(this, CommunicationStateNames.BROADCAST_MODE, -1, newValue);
			}
			clientProxy.setRemoteAtomic(newValue);
		}
	}

	@Override
	public void broadcastIPC(IPCMechanism state) throws RemoteException {
		RemoteProposeRequestReceived.newCase(this, CommunicationStateNames.IPC_MECHANISM, -1, state);
		if (Server.getSingleton().getConsensusAlgorithm() == ConsensusAlgorithm.CENTRALIZED_SYNCHRONOUS) {
			RemoteProposeRequestReceived.newCase(this, CommunicationStateNames.IPC_MECHANISM, -1, state);
			boolean accept = true;
			for (String proxyName : clients.keySet()) {
				Processor clientProxy = null;
				try {
					clientProxy = (Processor) rmiRegistry.lookup(proxyName);
				} catch (NotBoundException e) {
					e.printStackTrace();
				}
				ProposalAcceptRequestSent.newCase(this, CommunicationStateNames.IPC_MECHANISM, -1, state);
				boolean clientAccept = clientProxy.rProposal(CommunicationStateNames.IPC_MECHANISM, state);
				ProposalAcceptedNotificationReceived.newCase(this, CommunicationStateNames.IPC_MECHANISM, -1, state,
						ProposalFeedbackKind.SUCCESS);
				if (!clientAccept) {
					accept = false;
				}
			}
			if (!accept) {
				state = null;
			}
		}
		for (String proxyName : clients.keySet()) {
			Processor clientProxy = null;
			try {
				clientProxy = (Processor) rmiRegistry.lookup(proxyName);
			} catch (NotBoundException e) {
				e.printStackTrace();
			}
			if (Server.getSingleton().getConsensusAlgorithm() == ConsensusAlgorithm.CENTRALIZED_SYNCHRONOUS) {
				ProposalLearnedNotificationSent.newCase(this, CommunicationStateNames.IPC_MECHANISM, -1, state);
			}
			clientProxy.rSetIPCMechanism(state);
		}
	}
}