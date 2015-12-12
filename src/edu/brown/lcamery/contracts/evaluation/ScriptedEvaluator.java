package edu.brown.lcamery.contracts.evaluation;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.Callable;

import org.bitcoinj.core.Transaction;

import edu.brown.lcamery.server.support.ContractMethods;
import edu.brown.lcamery.server.support.DispatchException;

public class ScriptedEvaluator implements Callable<Transaction> {
	private final Map<ContractMethods, Method> methods;

	public ScriptedEvaluator(Map<ContractMethods, Method> methods) {
		this.methods = methods;
	}
	
	@Override
	public Transaction call() throws Exception {
		try {
			Transaction btc = null;
			if ((boolean) methods.get(ContractMethods.EVALUATE).invoke(null)) {
				btc = (Transaction) methods.get(ContractMethods.ONTRUE).invoke(null);
			} else {
				btc = (Transaction) methods.get(ContractMethods.ONFALSE).invoke(null);
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
