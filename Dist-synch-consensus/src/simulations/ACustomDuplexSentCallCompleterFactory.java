package simulations;

import inputport.rpc.duplex.ADuplexSentCallCompleterFactory;
import inputport.rpc.duplex.DuplexRPCInputPort;
import inputport.rpc.duplex.DuplexSentCallCompleter;
import inputport.rpc.duplex.LocalRemoteReferenceTranslator;

public class ACustomDuplexSentCallCompleterFactory extends ADuplexSentCallCompleterFactory {
	@Override
	public DuplexSentCallCompleter createDuplexSentCallCompleter(DuplexRPCInputPort aPort, LocalRemoteReferenceTranslator aTranslator) {
		return new ACustomDuplexSentCallCompleter(aPort, aTranslator);
	}
}