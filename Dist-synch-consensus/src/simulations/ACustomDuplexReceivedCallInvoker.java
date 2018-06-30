package simulations;

import inputport.datacomm.duplex.DuplexInputPort;
import inputport.rpc.RPCRegistry;
import inputport.rpc.duplex.ADuplexReceivedCallInvoker;
import inputport.rpc.duplex.LocalRemoteReferenceTranslator;

public class ACustomDuplexReceivedCallInvoker extends ADuplexReceivedCallInvoker {

	public ACustomDuplexReceivedCallInvoker(LocalRemoteReferenceTranslator aLocalRemoteReferenceTranslator,
			DuplexInputPort<Object> aReplier, RPCRegistry theRPCRegistry) {
		super(aLocalRemoteReferenceTranslator, aReplier, theRPCRegistry);
		// TODO Auto-generated constructor stub
	}
	@Override
	protected void handleProcedureReturn(String aSender, Exception e) {
		// clever hack - just use what is already there
		super.handleFunctionReturn(aSender, null, null, e);
	}
	
	

}