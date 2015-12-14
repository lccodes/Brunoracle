package edu.brown.lcamery.contracts.instances.swaps;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.params.TestNet3Params;

import edu.brown.lcamery.contracts.StandardContract;

public class TwoPartyLotteryContract extends StandardContract {
	public static ECKey key1 = new ECKey(), key2 = new ECKey();
	public static Coin dep1 = Coin.valueOf(5000);
	public static Coin dep2 = Coin.valueOf(5000);
	
	public static boolean evaluate() {
		SecureRandom sr = new SecureRandom();
		if (sr.nextDouble() <= .5) {
			return true;
		} else {
			return false;
		}
	}
	public static Map<Address, Coin> onTrue() {
		Coin random = Coin.valueOf((long) (750 + ((dep1.value+dep2.value - 750) * Math.random())));
		System.out.println("Player 1 won!");
		Map<Address,Coin> map = new HashMap<Address,Coin>();
		try {
			map.put(new Address(TestNet3Params.get(),"2N9SNXNhsv7WgA6AoSb1LQ8fSaCrRBgjoxP"),
					random);
		} catch (AddressFormatException e) {
			System.out.println("error");
		}
		return map;
	}
	public static Map<Address, Coin> onFalse() {
		Coin random = Coin.valueOf((long) (750 + ((dep2.value+dep2.value - 750) * Math.random())));
		System.out.println("Player 2 won!");
		Map<Address,Coin> map = new HashMap<Address,Coin>();
		try {
			map.put(new Address(TestNet3Params.get(),"2NEx1nRJRqjY89tpuBKBNTyYYcjS7nydxFs"),
					random);
		} catch (AddressFormatException e) {
			System.out.println("ERROR");
		}
		return map;
	}
	public static boolean firstResponse(String question) {
		return false;
	}
	public static boolean secondResponse(String question) {
		return false;
	}
}
