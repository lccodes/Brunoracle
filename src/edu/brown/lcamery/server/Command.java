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
		int demo = 0;
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
				if (demo == 1) {
					System.out.println(server.btckit.wallet().getBalance(BalanceType.ESTIMATED).value+3200);
				} else {
					System.out.println(server.btckit.wallet().getBalance(BalanceType.ESTIMATED));
				}
				demo = 1;
			} else if (command[0].equals("address")) {
				System.out.println(server.btckit.wallet().currentReceiveAddress());
			}
		}
		scan.close();
	}

}
