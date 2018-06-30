package server;

import java.nio.ByteBuffer;
import assignments.util.MiscAssignmentUtils;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

import inputport.nio.manager.listeners.SocketChannelReadListener;

public class Receiver implements SocketChannelReadListener {
	
	private ArrayBlockingQueue<Map<SocketChannel, ByteBuffer>> rQeueue;
	
	public Receiver(ArrayBlockingQueue<Map<SocketChannel, ByteBuffer>> readQueue) {
		this.rQeueue = readQueue;

	}

	@Override
	public void socketChannelRead(SocketChannel aSocketChannel,
			ByteBuffer aMessage, int aLength) {
		try {
			Map<SocketChannel, ByteBuffer> aMap = new HashMap<SocketChannel, ByteBuffer>();
			aMap.put(aSocketChannel, MiscAssignmentUtils.deepDuplicate(aMessage));
			rQeueue.add(aMap);	
		} catch (IllegalStateException e) {
			System.err.print("Read queue is full.");
		}
  
	}

}
