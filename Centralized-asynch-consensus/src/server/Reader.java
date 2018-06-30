package server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

import inputport.nio.manager.NIOManagerFactory;
import util.interactiveMethodInvocation.IPCMechanism;

public class Reader implements Runnable {

	private ArrayBlockingQueue<Map<SocketChannel, ByteBuffer>> readRequests;
	private List<SocketChannel> clientList;
	private NIOServer server;

	public Reader(NIOServer server, ArrayBlockingQueue<Map<SocketChannel, ByteBuffer>> readRequests, List<SocketChannel> clients) {
		this.server = server;
		this.readRequests = readRequests;
		this.clientList = clients;
	}

	@Override
	public void run() {
		Map<SocketChannel, ByteBuffer> msg = null;
		while (true) {
			try {
				msg = readRequests.take();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			for (int i = 0; i < clientList.size(); i++) {
				if (!Server.getSingleton().isAtomicBroadcast() && clientList.get(i).equals(msg.keySet().toArray()[0])) {
					continue;
				}
				NIOManagerFactory.getSingleton().write(clientList.get(i), msg.get(msg.keySet().toArray()[0]));

			}
		}
	}

}