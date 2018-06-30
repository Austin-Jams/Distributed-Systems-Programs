package server;

import java.nio.ByteBuffer;
import assignments.util.MiscAssignmentUtils;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

import inputport.nio.manager.listeners.SocketChannelReadListener;

public class Receiver implements SocketChannelReadListener {
	
	private ArrayBlockingQueue<Map<SocketChannel, ByteBuffer>> readRequests;
	
	public Receiver(ArrayBlockingQueue<Map<SocketChannel, ByteBuffer>> readRequests) {
		this.readRequests = readRequests;

	}

	@Override
	public void socketChannelRead(SocketChannel socketChannel,
			ByteBuffer msg, int length) {
		String command = new String(msg.array(), msg.position(),
				length);
		try {
			Map<SocketChannel, ByteBuffer> socketBufferMap = new HashMap<SocketChannel, ByteBuffer>();
			socketBufferMap.put(socketChannel, MiscAssignmentUtils.deepDuplicate(msg));
			readRequests.add(socketBufferMap);	
		} catch (IllegalStateException e) {
			System.err.print("----full----");
		}
	}

}