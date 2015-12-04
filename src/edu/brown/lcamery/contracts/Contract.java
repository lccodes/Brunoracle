package edu.brown.lcamery.contracts;

import java.util.Map;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.ECKey;

public final class Contract {
	public static ECKey key1, key2;
	public static Coin dep1, dep2;
	
	public static boolean evaluate() {
		return false;
	}
	public static Map<Address, Coin> onTrue() {
		return null;
	}
	public static Map<Address, Coin> onFalse() {
		return null;
	}
	public static boolean firstResponse(String question) {
		return false;
	}
	public static boolean secondResponse(String question) {
		return false;
	}
}
