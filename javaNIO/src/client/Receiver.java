package client;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ArrayBlockingQueue;

import assignments.util.MiscAssignmentUtils;
import inputport.nio.manager.listeners.SocketChannelReadListener;

public class Receiver implements SocketChannelReadListener{

	private ArrayBlockingQueue<ByteBuffer> readRequests;
	
	public Receiver(ArrayBlockingQueue<ByteBuffer> readRequests) {
		this.readRequests = readRequests;

	}


	@Override
	public void socketChannelRead(SocketChannel socketChannel, ByteBuffer msg, int length) {
		try {
			readRequests.add(MiscAssignmentUtils.deepDuplicate(msg));	
		} catch (IllegalStateException e) {
			System.err.print("Error, Read queue is full!");
		}
		
	}

}