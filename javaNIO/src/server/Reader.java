package server;


import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

import inputport.nio.manager.NIOManagerFactory;

public class Reader implements Runnable {

	private ArrayBlockingQueue<Map<SocketChannel, ByteBuffer>> rQeueue;
	private List<SocketChannel> clientList;
	private NIOServer server;

	public Reader(NIOServer server, ArrayBlockingQueue<Map<SocketChannel, ByteBuffer>> readQueue, List<SocketChannel> clients) {
		this.server = server;
		this.rQeueue = readQueue;
		this.clientList = clients;
	}

	@Override
	public void run() {
		Map<SocketChannel, ByteBuffer> message = null;
		while (true) {
			try {
				message = rQeueue.take();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
;
			for (SocketChannel client : clientList) {
				if (!server.isAtomic() && client.equals(message.keySet().toArray()[0])) {
					continue;
				}
				NIOManagerFactory.getSingleton().write(client, message.get(message.keySet().toArray()[0]));

			}
		}
	}

}