package commandprocessing;

import java.rmi.Remote;
import java.rmi.RemoteException;

import client.Client;
import client.GIPCClientHandler;
import client.RemoteClient;
import consensus.ProposalFeedbackKind;
import main.BeauAndersonFinalProject;
import stringProcessors.AHalloweenCommandProcessor;
import stringProcessors.HalloweenCommandProcessor;
import util.interactiveMethodInvocation.ConsensusAlgorithm;
import util.interactiveMethodInvocation.IPCMechanism;
import util.trace.port.consensus.ProposalAcceptRequestReceived;
import util.trace.port.consensus.ProposalAcceptedNotificationSent;
import util.trace.port.consensus.ProposalLearnedNotificationReceived;
import util.trace.port.consensus.ProposedStateSet;
import util.trace.port.consensus.communication.CommunicationStateNames;

public class CommandProcessor extends AHalloweenCommandProcessor implements Processor {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private HalloweenCommandProcessor commandProcessor;

	public CommandProcessor(RemoteClient client) {
		commandProcessor = Client.getCommandProcessor();
		commandProcessor.addPropertyChangeListener(client);
	}
	public CommandProcessor(GIPCClientHandler client) {
		commandProcessor = Client.getCommandProcessor();
		commandProcessor.addPropertyChangeListener(client);
	}

	public HalloweenCommandProcessor getCommandProcessor() {
		return commandProcessor;
	}

	public void submitRemoteCmd(String cmd) throws RemoteException {
		ProposalLearnedNotificationReceived.newCase(this, CommunicationStateNames.COMMAND, -1, cmd);
		ProposedStateSet.newCase(this, CommunicationStateNames.COMMAND, -1, cmd);
		commandProcessor.processCommand(cmd);
	}
	@Override
	public boolean rProposal(String communicationState, Object value) throws RemoteException {
		ProposalAcceptRequestReceived.newCase(this, communicationState, -1, value);
		ProposalAcceptedNotificationSent.newCase(this, communicationState, -1, value, ProposalFeedbackKind.SUCCESS); 
		return !Client.getSingleton().isRejectMetaStateChange();
	}
	@Override
	public void setRemoteAtomic(boolean state) throws RemoteException {
		ProposalLearnedNotificationReceived.newCase(this, CommunicationStateNames.BROADCAST_MODE, -1, state);
		Client.getSingleton().setAtomicBroadcastAfterConsensus(state);
	}

	@Override
	public void rSetIPCMechanism(IPCMechanism state) throws RemoteException {
		ProposalLearnedNotificationReceived.newCase(this, CommunicationStateNames.IPC_MECHANISM, -1, state);
		Client.getSingleton().setIPCMechanismAfterConsensus(state);

	}

	
}