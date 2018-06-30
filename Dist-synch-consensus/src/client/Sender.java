
package client;

import inputport.nio.manager.NIOManagerFactory;
import util.interactiveMethodInvocation.IPCMechanism;
import util.trace.bean.NotifiedPropertyChangeEvent;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;


public class Sender implements PropertyChangeListener {
	SocketChannel socketChannel;
	String name;
	NIOClient client;

	public Sender(SocketChannel socketChannel, String name, NIOClient client) {
		this.socketChannel = socketChannel;
		this.name = name;
		this.client = client;
	}

	@Override
	public void propertyChange(PropertyChangeEvent anEvent) {
		if (!anEvent.getPropertyName().equals("InputString") || !Client.getSingleton().getIPCMechanism().equals(IPCMechanism.NIO)) {
			return;
		}
		NotifiedPropertyChangeEvent.newCase(this, anEvent, new PropertyChangeListener[]{});
		String cmd = anEvent.getNewValue().toString();
		if (!Client.getSingleton().isLocalProcessingOnly()) {
			if (!Client.getSingleton().isAtomicBroadcast()) {
				Client.getSingleton().getCommandProcessor().processCommand(cmd);
			} 
			ByteBuffer aMeaningByteBuffer = ByteBuffer.wrap((cmd.getBytes()));
			NIOManagerFactory.getSingleton().write(socketChannel, aMeaningByteBuffer);
		} else {
			Client.getSingleton().getCommandProcessor().processCommand(cmd);
		}
	}

}