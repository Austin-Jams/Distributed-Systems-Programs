package client;

import inputport.nio.manager.NIOManagerFactory;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * Listens to model changes and sends them to the connected server through the
 * NIO manager.
 * 
 * @author Dewan
 *
 */
public class Sender implements PropertyChangeListener {
	SocketChannel socketChannel;
	String clientID;
	NIOClient client;

	public Sender(SocketChannel aSocketChannel, String aClientName, NIOClient client) { 
		socketChannel = aSocketChannel;
		clientID = aClientName;
		this.client = client;
	}
	
	public void local(String command) {
		client.commandProcessor.processCommand(command);
	}
	@Override
	public void propertyChange(PropertyChangeEvent anEvent) {
		if (!anEvent.getPropertyName().equals("InputString"))
			return;
		String command = (String) anEvent.getNewValue();
		System.out.println("Client command:" + command);
		
		if (!client.localProcessing()) {
			if (!client.isAtomic()) {
				client.commandProcessor.processCommand(command);
			} 
			ByteBuffer aMeaningByteBuffer = ByteBuffer.wrap((command.getBytes()));
			NIOManagerFactory.getSingleton().write(socketChannel, aMeaningByteBuffer);
		} else {
				client.commandProcessor.processCommand(command);
		}
	}
}

