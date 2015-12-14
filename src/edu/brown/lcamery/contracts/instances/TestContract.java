package edu.brown.lcamery.contracts.instances;

import java.util.HashMap;
import java.util.Map;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.params.TestNet3Params;

import edu.brown.lcamery.contracts.StandardContract;

public final class TestContract extends StandardContract {
	public static ECKey key1 = new ECKey(), key2 = new ECKey();
	public static Coin dep1 = Coin.valueOf(1000);
	public static Coin dep2 = Coin.valueOf(1500);
	
	public static boolean evaluate() {
		return false;
	}
	public static Map<Address, Coin> onTrue() {
		System.out.println("onTrue");
		Map<Address,Coin> map = new HashMap<Address,Coin>();
		try {
			map.put(new Address(TestNet3Params.get(),"2N9SNXNhsv7WgA6AoSb1LQ8fSaCrRBgjoxP"),
					Coin.valueOf(1000));
		} catch (AddressFormatException e) {
			System.out.println("error");
		}
		System.out.println("map made");
		return map;
	}
	public static Map<Address, Coin> onFalse() {
		System.out.println("onFalse");
		Map<Address,Coin> map = new HashMap<Address,Coin>();
		try {
			map.put(new Address(TestNet3Params.get(),"2NEx1nRJRqjY89tpuBKBNTyYYcjS7nydxFs"),
					Coin.valueOf(1000));
		} catch (AddressFormatException e) {
			System.out.println("ERROR");
		}
		System.out.println("endFalse");
		return map;
	}
	public static boolean firstResponse(String question) {
		return false;
	}
	public static boolean secondResponse(String question) {
		return false;
	}
}
