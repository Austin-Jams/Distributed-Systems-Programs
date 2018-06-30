package simulations;

import util.annotations.Comp533Tags;
import util.annotations.Tags;

@Tags({Comp533Tags.EXPLICIT_RECEIVE_SERVER})
public class Part1ServerLauncher {

	public static void main(String[] args) {
		assignments.util.A4TraceUtility.setTracing();
		Part1Server.launch();
	}

}