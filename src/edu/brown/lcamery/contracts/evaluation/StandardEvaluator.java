package edu.brown.lcamery.contracts.evaluation;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.Callable;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;

import edu.brown.lcamery.server.support.ContractMethods;
import edu.brown.lcamery.server.support.DispatchException;

public class StandardEvaluator implements Callable<Map<Address, Coin>> {
	private final Map<ContractMethods, Method> methods;

	public StandardEvaluator(Map<ContractMethods, Method> methods) {
		this.methods = methods;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Map<Address, Coin> call() throws Exception {
		try {
			Map<Address, Coin> btc = null;
			if ((boolean) methods.get(ContractMethods.EVALUATE).invoke(null)) {
				btc = (Map<Address, Coin>) methods.get(ContractMethods.ONTRUE).invoke(null);
			} else {
				btc = (Map<Address, Coin>) methods.get(ContractMethods.ONFALSE).invoke(null);
			}
			
			return btc;
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			new DispatchException("[failure] on evaluate: " + e.getMessage());
		} catch (SecurityException e) {
			new DispatchException("[failure] security failure");
		}
		return null;
	}
	
}