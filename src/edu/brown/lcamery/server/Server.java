package edu.brown.lcamery.server;

import java.io.File;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import org.bitcoinj.core.AbstractWalletEventListener;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.Base58;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionConfidence;
import org.bitcoinj.core.Wallet;
import org.bitcoinj.core.Wallet.BalanceType;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.RegTestParams;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.script.Script;
import org.bitcoinj.utils.BriefLogFormatter;
import org.spongycastle.math.ec.ECPoint;

import edu.brown.lcamery.server.support.DispatchException;
import edu.brown.lcamery.server.support.FieldTypes;
import edu.brown.lcamery.server.support.Network;
import edu.brown.lcamery.server.support.Tuple;

public class Server {
	public final WalletAppKit btckit;
	public static String LOG = "./logs";
	
	public Server(Network network) {
		BriefLogFormatter.init();
		
		// Figure out which network we should connect to. Each one gets its own set of files.
		NetworkParameters params = null;
		String filePrefix = null;
		if (network.equals(Network.TEST)) {
		    params = TestNet3Params.get();
		    filePrefix = "forwarding-service-testnet";
		} else if (network.equals(Network.REGTEST)) {
		    params = RegTestParams.get();
		    filePrefix = "forwarding-service-regtest";
		} else if (network.equals(Network.PROD)){
		    params = MainNetParams.get();
		    filePrefix = "forwarding-service";
		}
		
		// Start up a basic app using a class that automates some boilerplate. Ensure we always have at least one key.
		btckit = new WalletAppKit(params, new File(LOG), filePrefix);

		if (params == RegTestParams.get()) {
		    // Regression test mode is designed for testing and development only, so there's no public network for it.
		    // If you pick this mode, you're expected to be running a local "bitcoind -regtest" instance.
		    btckit.connectToLocalHost();
		}
		
		btckit.startAsync();
        btckit.awaitRunning();

        // To observe wallet events (like coins received) we implement a EventListener class 
        //that extends the AbstractWalletEventListener bitcoinj then calls the different functions from the EventListener class
        WalletListener wListener = new WalletListener();
        btckit.wallet().addEventListener(wListener);
	}
	
	/*
	 * Ensures that there are sufficient accessible funds to proceed with transaction
	 * @param deposits: map of key to amount
	 */
	public boolean validateDeposits(Map<String, Integer> deposits) {
		Coin balance = Coin.valueOf(0);
		for (Map.Entry<String, Integer> deposit : deposits.entrySet()) {
			this.btckit.wallet().importKey(ECKey.fromPrivate(new BigInteger(deposit.getKey())));
			Coin bal = this.btckit.wallet().getBalance(BalanceType.AVAILABLE_SPENDABLE);
			if (bal.getValue() - balance.getValue() > deposit.getValue()) {
				balance = bal;
			} else {
				System.out.println("[failure] key " + deposit.getValue() + " has insufficient funds");
				return false;
			}
		}
		
		return true;
	}
	
	/*
	 * Sends off the bitcoins to the proper place
	 * @param Map<Address, Coin> : what to send where
	 */
	
	/*
	 * Launches the server
	 */
	public void launch() {
		try {
			Dispatch d = new Dispatch(".\\contracts");
			while(d.hasNext()) {
				Tuple<Map<FieldTypes, ECKey>, Map<Address, Coin>> outputs = d.executeNext();
				//TODO:
				//1. Ensure that keys have sufficient balance
				//2. Evaluate contract
				//3. SendResults
				//4. Decide what to do on listener
			}
			System.out.println("done");
		} catch (DispatchException e) {
			System.out.println(e.getMessage());
		}
	}
	
	public static void main(String[] args) {
		Server s = new Server(Network.TEST);
		System.out.println(s.btckit.wallet().currentReceiveAddress());
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		/*try {
			final Wallet.SendResult result = s.btckit.wallet().sendCoins(s.btckit.peerGroup(), s.btckit.wallet().freshReceiveAddress(), Coin.valueOf(1000));
			result.broadcastComplete.addListener(new Runnable() {
			    @Override
			    public void run() {
			         // The wallet has changed now, it'll get auto saved shortly or when the app shuts down.
			         System.out.println("Sent coins onwards! Transaction hash is " + result.tx.getHashAsString());
			    }
			}, Threading.USER_THREAD);
		} catch (InsufficientMoneyException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}*/
		
		//while(true){
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.println("estimate:"+s.btckit.wallet().getBalance(BalanceType.ESTIMATED));
			System.out.println("spendable:"+s.btckit.wallet().getBalance(BalanceType.AVAILABLE_SPENDABLE));
		//}
	}
	
    // The Wallet event listener its implementations get called on wallet changes.
    static class WalletListener extends AbstractWalletEventListener {

        @Override
        public void onCoinsReceived(Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance) {
            System.out.println("-----> coins resceived: " + tx.getHashAsString());
            System.out.println("received: " + tx.getValue(wallet));
        }

        @Override
        public void onTransactionConfidenceChanged(Wallet wallet, Transaction tx) {
            System.out.println("-----> confidence changed: " + tx.getHashAsString());
            TransactionConfidence confidence = tx.getConfidence();
            System.out.println("new block depth: " + confidence.getDepthInBlocks());
        }

        @Override
        public void onCoinsSent(Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance) {
            System.out.println("coins sent");
        }

        @Override
        public void onReorganize(Wallet wallet) {
        }

        @Override
        public void onWalletChanged(Wallet wallet) {
        	System.out.println("changed");
        }

        @Override
        public void onKeysAdded(List<ECKey> keys) {
            System.out.println("new key added");
        }

        @Override
        public void onScriptsChanged(Wallet wallet, List<Script> scripts, boolean isAddingScripts) {
        	for (Script s : scripts) {
        		System.out.println(s);
        	}
            System.out.println("new script added");
        }
    }

}
