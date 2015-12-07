package edu.brown.lcamery.server;

import edu.brown.lcamery.server.support.Network;

public class Launch {
	public static void main(String[] args) {
		new Command(Network.TEST)
			.run();
	}
}
