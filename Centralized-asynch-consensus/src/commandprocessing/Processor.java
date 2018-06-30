package commandprocessing;

import java.rmi.Remote;
import java.rmi.RemoteException;

import util.interactiveMethodInvocation.IPCMechanism;
import util.trace.port.consensus.communication.CommunicationStateNames;
public interface Processor extends Remote {
	public void submitRemoteCmd(String command) throws RemoteException;
	public void setRemoteAtomic(boolean newValue) throws RemoteException;
	public void rSetIPCMechanism(IPCMechanism newValue) throws RemoteException;
	public boolean rProposal(String communicationState, Object value) throws RemoteException;
}