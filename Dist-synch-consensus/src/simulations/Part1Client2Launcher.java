package simulations;

import util.annotations.Tags;
import util.annotations.Comp533Tags;

@Tags({ Comp533Tags.EXPLICIT_RECEIVE_CLIENT2 })
public class Part1Client2Launcher {

	public static void main(String[] args) {
		assignments.util.A4TraceUtility.setTracing();
		Part1Client1.launch("Client 2");

	}

}