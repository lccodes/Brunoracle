package edu.brown.lcamery.server;

import java.util.Scanner;

import edu.brown.lcamery.server.support.Network;

public class Command {
	private Server server;
	
	public Command(Network net) {
		server = new Server(net);
	}
	
	public void run() {
		Scanner scan = new Scanner(System.in);
		while(true) {
			String[] command = scan.nextLine().split(" ");
			if (command[0].equals("evaluate")) {
				server.serveContracts(command[1]);
			} else if (command[0].equals("exit")) {
				break;
			}
		}
		scan.close();
	}

}
