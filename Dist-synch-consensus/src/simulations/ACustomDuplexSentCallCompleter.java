package simulations;

import inputport.InputPort;
import inputport.datacomm.duplex.object.explicitreceive.ReceiveReturnMessage;
import inputport.rpc.duplex.ADuplexSentCallCompleter;
import inputport.rpc.duplex.DuplexRPCInputPort;
import inputport.rpc.duplex.LocalRemoteReferenceTranslator;
import inputport.rpc.duplex.RPCReturnValue;
import util.trace.port.rpc.ReceivedObjectTransformed;
import util.trace.port.rpc.RemoteCallReceivedReturnValue;
import util.trace.port.rpc.RemoteCallWaitingForReturnValue;

public class ACustomDuplexSentCallCompleter extends ADuplexSentCallCompleter {

	private DuplexRPCInputPort port;

	public ACustomDuplexSentCallCompleter(InputPort anInputPort, LocalRemoteReferenceTranslator aRemoteHandler) {
		super(anInputPort, aRemoteHandler);
		// TODO Auto-generated constructor stub
		port = (DuplexRPCInputPort) anInputPort;
	}

	@Override
	public Object waitForReturnValue(String aRemoteEndPoint) {
		ReceiveReturnMessage<Object> message = port.receive(aRemoteEndPoint);
		while (!(message.getMessage() instanceof RPCReturnValue)) {
			message = port.receive(aRemoteEndPoint);
		}
		return ((RPCReturnValue) message.getMessage()).getReturnValue();
	}

	@Override
	public Object getReturnValueOfRemoteProcedureCall(String aRemoteEndPoint, Object aMessage) {
		// lol just reuse function call semantics
		return super.getReturnValueOfRemoteFunctionCall(aRemoteEndPoint, aMessage);
	}

	@Override
	protected void returnValueReceived(String aRemoteEndPoint, Object message) {

	}
}
