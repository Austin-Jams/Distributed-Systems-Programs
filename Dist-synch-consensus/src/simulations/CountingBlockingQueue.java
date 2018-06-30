package simulations;

import java.util.concurrent.ArrayBlockingQueue;

public class CountingBlockingQueue<Object> extends ArrayBlockingQueue<Object> {

	private int blocked;
	
	public CountingBlockingQueue(int capacity) {
		super(capacity);
		// TODO Auto-generated constructor stub
		blocked= 0;
	}
	
	public Object take() throws InterruptedException {
		blocked++;
		return super.take();
		
	}
	
	public boolean offer(Object o) {
		blocked--;
		return super.offer(o);
	}
	
	public int getBlocked() {
		return blocked;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}