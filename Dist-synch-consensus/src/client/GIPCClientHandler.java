package client;

import java.beans.PropertyChangeEvent;
import util.trace.port.consensus.*;
import util.tags.DistributedTags;
import java.beans.PropertyChangeListener;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import assignments.util.inputParameters.SimulationParametersListener;
import assignments.util.mainArgs.ClientArgsProcessor;
import commandprocessing.CommandProcessor;
import commandprocessing.Processor;
import examples.mvc.rmi.duplex.DistributedRMICounter;
import inputport.nio.manager.NIOManagerFactory;
import inputport.rpc.GIPCLocateRegistry;
import inputport.rpc.GIPCRegistry;
import server.GIPCServerHandler;

import server.RemoteServerInterface;
import util.annotations.Tags;
import util.interactiveMethodInvocation.ConsensusAlgorithm;
import util.interactiveMethodInvocation.IPCMechanism;
import util.misc.ThreadSupport;
import util.trace.bean.BeanTraceUtility;
import util.trace.bean.NotifiedPropertyChangeEvent;
import util.trace.factories.FactoryTraceUtility;
import util.trace.misc.ThreadDelayed;
import util.trace.port.consensus.ConsensusTraceUtility;
import util.trace.port.consensus.communication.CommunicationStateNames;
import util.trace.port.nio.NIOTraceUtility;
import util.trace.port.rpc.rmi.RMIObjectRegistered;
import util.trace.port.rpc.rmi.RMIRegistryLocated;
import util.trace.port.rpc.rmi.RMITraceUtility;

public class GIPCClientHandler implements PropertyChangeListener {
	final String lexicon = "ABCDEFGHIJKLMNOPQRSTUVWXYZ12345674890";

	final java.util.Random rand = new java.util.Random();
	final Set<String> identifiers = new HashSet<String>();
	private Processor cpProxy;
	private String name;


	private RemoteServerInterface serverProxy;

	public GIPCClientHandler(RemoteServerInterface serverProxy, String name) {
		this.serverProxy = serverProxy;
		cpProxy = new CommandProcessor(this);
		this.name = name;
	}

	@Override
	public void propertyChange(PropertyChangeEvent anEvent) {
		if (!Client.getSingleton().getIPCMechanism().equals(IPCMechanism.GIPC) || !anEvent.getPropertyName().equals("InputString")) {
			return;
		}
		NotifiedPropertyChangeEvent.newCase(this, anEvent, new PropertyChangeListener[] {});
		String cmd =  anEvent.getNewValue().toString();
		if (!Client.getSingleton().isLocalProcessingOnly()) {
			if (!Client.getSingleton().isAtomicBroadcast()) {
				try {
					cpProxy.submitRemoteCmd(cmd);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		
			try {
				ThreadSupport.sleep(Client.getSingleton().getDelay());
				if (Client.getSingleton().isAtomicBroadcast()) {
					ProposalMade.newCase(this, CommunicationStateNames.COMMAND, -1, cmd);

				}
				RemoteProposeRequestSent.newCase(this, CommunicationStateNames.COMMAND, -1, cmd);
				serverProxy.submitCommand(this.name, cmd);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		} else {
			try {
				cpProxy.submitRemoteCmd(cmd);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
		FactoryTraceUtility.setTracing();
		BeanTraceUtility.setTracing();
		RMITraceUtility.setTracing();
		ConsensusTraceUtility.setTracing();
		ThreadDelayed.enablePrint();
		//
		try {
			byte[] array = new byte[10];
			new Random().nextBytes(array);
			String name = new String(array, Charset.forName("UTF-8"));
			GIPCRegistry gipcRegistry= GIPCLocateRegistry.getRegistry(ClientArgsProcessor.getRegistryHost(args), ClientArgsProcessor.getGIPCPort(args), name);
			RemoteServerInterface serverProxy = (RemoteServerInterface) gipcRegistry.lookup(RemoteServerInterface.class, GIPCServerHandler.GIPC_SERVER_NAME);
			GIPCClientHandler client = new GIPCClientHandler(serverProxy, name);
			gipcRegistry.rebind(client.getName(), client.getProxy());
			serverProxy.join(client.getName(), client.getProxy());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public String getName() {
		return name;
	}

	public Processor getProxy() {
		return cpProxy;
	}

	// @Override
	public void experimentInput() {
		// TODO Auto-generated method stub

	}

	// @Override
	public void quit(int aCode) {
		System.exit(aCode);

	}

	// @Override
	public void simulationCommand(String aCommand) {
		// TODO Auto-generated method stub

	}

}