package edu.brown.lcamery.server;

import edu.brown.lcamery.server.support.Network;

public class Launch {
	public static void main(String[] args) {
		Network net;
		if (args.length != 1) {
			System.out.println("args: <network>");
			return;
		} else if (args[0].equals("test")) {
			net = Network.TEST;
		} else {
			net = Network.PROD;
		}
		
		new Command(net)
			.run();
	}
}
