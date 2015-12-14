package edu.brown.lcamery.server;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.InsufficientMoneyException;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.Wallet;
import org.bitcoinj.core.Wallet.BalanceType;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.RegTestParams;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.utils.BriefLogFormatter;
import org.bitcoinj.utils.Threading;

import com.google.common.util.concurrent.ListenableFuture;

import edu.brown.lcamery.contracts.ContractType;
import edu.brown.lcamery.server.security.SecurityType;
import edu.brown.lcamery.server.support.DispatchException;
import edu.brown.lcamery.server.support.FieldTypes;
import edu.brown.lcamery.server.support.Network;
import edu.brown.lcamery.server.support.Tuple;
import edu.brown.lcamery.support.VerificationException;
import edu.brown.lcamery.wallet.VerboseWallet;

public class Server {
	public final WalletAppKit btckit;
	public static String LOG = "./logs";
	public static final int UPDATE_ATTEMPTS = 5;
	private final static boolean TEST = false;
	private static final Coin DUST = Coin.valueOf(750), CHARGE = Coin.valueOf(0);
	
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
        VerboseWallet wListener = new VerboseWallet();
        btckit.wallet().addEventListener(wListener);
	}
	
	/*
	 * Server evaluation of the standard contract
	 */
	private void executeStandard(Dispatch d, List<ListenableFuture<Transaction>> contractOutputs) {
		try {
			Tuple<Map<FieldTypes, Coin>, Map<FieldTypes, ECKey>> verification = d.getNextKeys();
			try {
				for (FieldTypes deps : FieldTypes.getDeposits()) {
					validateDeposits(verification, deps);
				}
			} catch (VerificationException e) {
				System.out.print("[server] contract failed verification");
				d.skipNext();
				return;
			}
			
			Map<Address, Coin> outputs = d.executeStandardContract();
			
			if (outputs == null) {
				System.out.println("[server] contract killed by security");
				return;
			}

			for (Map.Entry<Address, Coin> out : outputs.entrySet()) {
				if (out.getValue().isLessThan(DUST)) {
					System.out.println("[server] skipped due to dusty send");
					continue;
				}
				final Wallet.SendResult result = 
						btckit.wallet().sendCoins(btckit.peerGroup(), out.getKey(), out.getValue());
				result.broadcastComplete.addListener(new Runnable() {
				    @Override
				    public void run() {
				         System.out.println("[server] contract complete: " + result.tx.getHashAsString());
				    }
				}, Threading.USER_THREAD);
				
				contractOutputs.add(result.broadcastComplete);
			}
		} catch (InsufficientMoneyException e1) {
			//This should never occur
			e1.printStackTrace();
			return;
		} catch (DispatchException e1) {
			//This may occur
			System.out.println("[server] contract failed" + e1.getMessage());
		}
	}
	
	/*
	 * Ensures that there are sufficient accessible funds to proceed with transaction
	 * @param deposits: map of key to amount
	 */
	private void validateDeposits(Tuple<Map<FieldTypes, Coin>, Map<FieldTypes, ECKey>> verification, FieldTypes which) throws VerificationException {
		if (Server.TEST) {
			return;
		}
		
		final Coin balance = this.btckit.wallet().getBalance(BalanceType.ESTIMATED);
		this.btckit.wallet().importKey(verification.two.get(FieldTypes.getOther(which)));
		int tries = Server.UPDATE_ATTEMPTS;
		boolean enoughOne = false;
		while (tries-- > 0) {
			Coin newBalance = this.btckit.wallet().getBalance(BalanceType.ESTIMATED);
			//Include minimum transaction fee
			if (newBalance.subtract(balance)
					.subtract(Coin.valueOf(1000))
					.subtract(CHARGE)
					.isGreaterThan(verification.one.get(which))) {
				enoughOne = true;
				break;
			}
		}
		if (!enoughOne) {
			throw new VerificationException("[server] insufficient deposit by " + which);
		}
	}
	
	/*
	 * Sends off the bitcoins to the proper place
	 * @param Map<Address, Coin> : what to send where
	 */
	
	/*
	 * Launches the server
	 */
	public void serveContracts(String location, String security) {
		List<ListenableFuture<Transaction>> contractOutputs = 
				new LinkedList<ListenableFuture<Transaction>>();
		SecurityType type = SecurityType.FULL;
		if (security.equals("internet")) {
			type = SecurityType.INTERNET;
		}
		Dispatch d;
		try {
			d = new Dispatch(location, type);
		} catch (DispatchException e2) {
			System.out.println("[server] failed to load " + location);
			return;
		}
		
		while(d.hasNext()) {
			try {
				if (d.getNextType().equals(ContractType.STANDARD)) {
					executeStandard(d, contractOutputs);
				} else if (d.getNextType().equals(ContractType.SCRIPTED)) {
					executeScripted(d, contractOutputs);
				}
			} catch (DispatchException e) {
				d.skipNext();
				System.out.println("[server] unknown contract type");
			}
		}
		
		//Lets go of the threadpool
		d.destruct();
		
		System.out.println("\ndone: " + location);
	}
	
	private void executeScripted(Dispatch d, List<ListenableFuture<Transaction>> contractOutputs) {
		try {
			Transaction t = d.evaluateScripted();
			btckit.peerGroup().broadcastTransaction(t);
		} catch (DispatchException e) {
			System.out.println("[server] contract failed " + e.getMessage());
		}
	}

}
