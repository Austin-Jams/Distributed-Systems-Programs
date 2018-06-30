package simulations;

import inputport.datacomm.AReceiveRegistrarAndNotifier;
import util.trace.port.objects.ReceivedMessageQueued;

public class ACustomClientReceiveNotifier extends AReceiveRegistrarAndNotifier {
	
	@Override
	public void notifyPortReceive(String aSource, Object aMessage) {
		super.notifyPortReceive(aSource, aMessage);
		ACustomDuplexObjectClientInputPort.getPort().offer(aMessage);
		ReceivedMessageQueued.newCase(this, ACustomDuplexObjectClientInputPort.getPort(), aMessage);
	}
}