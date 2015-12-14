package edu.brown.lcamery.contracts.instances.derivatives;

import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.script.Script;

public class HedgeContract {
	public static final float ORIGINAL = 375.6f; //TODO: Set exchange
	public static final Coin A = Coin.valueOf(1000); //TODO: Set A's value
	public static final Coin B = Coin.valueOf(1000); //TODO: Set B's value
	public static final Script SA = null, SB = null; //TODO: Set Scripts
	
	public static boolean evaluate() {
		return false;
	}
	
	public static Transaction onFalse() {
		final float CURRENT = 400; //Get current USD/BTC
		final Transaction t = new Transaction(MainNetParams.get()); //TODO: Construct transaction
		final Coin TOTAL = A.add(B);
		final Coin NEWA = Coin.valueOf((long) (((float) A.getValue()) * (ORIGINAL/CURRENT)));
		final Coin NEWB = TOTAL.subtract(NEWA);
		t.addOutput(NEWA, SA);
		t.addOutput(NEWB, SB);
		
		return t;
	}
	
	public static Transaction onTrue() {return null;}

}
