package edu.brown.lcamery.server;

import java.util.Scanner;

import org.bitcoinj.core.Wallet.BalanceType;

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
				if (command.length == 3) {
					server.serveContracts(command[1], command[2]);
				} else {
					server.serveContracts(command[1], "full");
				}
			} else if (command[0].equals("exit")) {
				break;
			} else if (command[0].equals("balance")) {
				System.out.println(server.btckit.wallet().getBalance(BalanceType.ESTIMATED));
			} else if (command[0].equals("address")) {
				System.out.println(server.btckit.wallet().currentReceiveAddress());
			}
		}
		scan.close();
	}

}
