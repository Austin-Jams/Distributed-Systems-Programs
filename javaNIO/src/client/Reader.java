package client;

import java.nio.ByteBuffer;
import java.util.concurrent.ArrayBlockingQueue;

import stringProcessors.HalloweenCommandProcessor;

public class Reader implements Runnable {
	private ArrayBlockingQueue<ByteBuffer> rQeueue;
	private HalloweenCommandProcessor commandProcessor;
	
	public Reader(ArrayBlockingQueue<ByteBuffer> readQueue, HalloweenCommandProcessor commandProcessor) {
		this.rQeueue = readQueue;
		this.commandProcessor = commandProcessor;
		
	}

	@Override
	public void run() {
		ByteBuffer message = null;
		while (true) {
			try {
				message = rQeueue.take();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			String command = new String(message.array(), message.position(), message.array().length);
			System.out.println("Client receives command: " + command);
			commandProcessor.processCommand(command);
		}
	}
}