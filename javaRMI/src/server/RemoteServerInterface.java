package server;
import java.rmi.Remote;
import java.rmi.RemoteException;

import misc.Processor;
import util.interactiveMethodInvocation.IPCMechanism;

public interface RemoteServerInterface extends Remote {
	public void join(String name, Processor callback) throws RemoteException;
	public void executeCommand(String clientName, String command) throws RemoteException;
	public void broadcastAtomic(Boolean newValue) throws RemoteException;
	public void broadcastIPC(IPCMechanism newValue) throws RemoteException;
}