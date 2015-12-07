package edu.brown.lcamery.server;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.ECKey;

import edu.brown.lcamery.contracts.ContractType;
import edu.brown.lcamery.server.security.ContractManager;
import edu.brown.lcamery.server.support.ContractMethods;
import edu.brown.lcamery.server.support.DispatchException;
import edu.brown.lcamery.server.support.FieldTypes;
import edu.brown.lcamery.server.support.Tuple;

public class Dispatch {
	private final String LOCATION;
	private ArrayList<Class<?>> theContracts;
	private final int pass;
	public final static String EVAL_TYPE_STANDARD = "java.util.Map<org.bitcoinj.core.Address, org.bitcoinj.core.Coin>";
	
	/*
	 * Inits dispatch object 
	 */
	public Dispatch(String location) throws DispatchException {
		this.LOCATION = String.copyValueOf(location.toCharArray());
		this.theContracts = new ArrayList<Class<?>>();
		this.loadContracts();
		this.pass = (int) (Math.random() * 1000000000);
		
		System.setSecurityManager(new ContractManager(this.pass));
		ContractManager sm = (ContractManager) System.getSecurityManager();
		sm.toggle(this.pass);
	}
	
	/*
	 * Returns the type of the next contract so that the server can execute correctly
	 */
	public ContractType getNextType() throws DispatchException {
		if (!this.hasNext()) {
			throw new DispatchException("[failure] dispatch invoked without contracts");
		}
		
		if (this.theContracts.get(0).getSuperclass().getName().contains("Standard")) {
			return ContractType.STANDARD;
		} else if (this.theContracts.get(0).getSuperclass().getName().contains("Scripted")) {
			return ContractType.SCRIPTED;
		}
		
		throw new DispatchException("[dispatch] contract of illegal class");
	}
	
	/*
	 * Loads the contracts as Class files awaiting method invocation
	 */
	private void loadContracts() throws DispatchException {
		File dir = new File(this.LOCATION);
		if (dir.isDirectory()) {
			File[] contracts = dir.listFiles();
			for (int i = 0; i < contracts.length; i++) {
				if (contracts[i].isFile()) {
					try {
						JarFile jar = new JarFile(contracts[i]);
						Enumeration<JarEntry> e = jar .entries();
						if (e.hasMoreElements()) {
							JarEntry j = (JarEntry) e.nextElement();
							while(!j.getName().contains("contract")
									|| j.getName().contains("type")
									|| j.getName().contains("standard")
									|| j.getName().contains("scripted")) {
								j = (JarEntry) e.nextElement();
							}
							String name = j.getName();
							jar.close();
							URL url = contracts[i].toURI().toURL();
							URL[] urls = new URL[]{url};
							@SuppressWarnings("resource")
							ClassLoader cl = new URLClassLoader(urls);

							Class<?> cls = cl.loadClass(name.replace("/", ".").substring(0, name.length()-6));
							this.theContracts.add(cls);
						} else {
							System.out.println("[warning] skipped due to no entry");
						}
					} catch (ClassNotFoundException | IOException e) {
						System.out.println("[warning] skipped due to IO: " + contracts[i].getName());
					}
				} else {
					System.out.println("[warning] skipped due to dir: " + contracts[i].getName());
				}
			}
		} else {
			throw new DispatchException("[failure] not a directory");
		}
	}
	
	/*
	 * Allows command and control to poll if there are contracts left
	 */
	public boolean hasNext() {
		return theContracts.size() != 0;
	}
	
	/*
	 * Evaluates a standard contract
	 * @param Class<? extends StandardContract>
	 */
	@SuppressWarnings("unchecked")
	public Map<Address, Coin> executeStandardContract() throws DispatchException {
		if (!this.hasNext()) {
			throw new DispatchException("[failure] dispatch invoked without contracts");
		}
		
		Class<?> contract = this.theContracts.get(0);
		this.theContracts.remove(0);
		Map<ContractMethods, Method> safeMethods = verifyAndParse(contract.getMethods());
		try {
			ContractManager sm = (ContractManager) System.getSecurityManager();
			sm.toggle(this.pass);
			Map<Address, Coin> btc = null;
			if ((boolean) safeMethods.get(ContractMethods.EVALUATE).invoke(null)) {
				btc = (Map<Address, Coin>) safeMethods.get(ContractMethods.ONTRUE).invoke(null);
			} else {
				btc = (Map<Address, Coin>) safeMethods.get(ContractMethods.ONFALSE).invoke(null);
			}
			sm.toggle(this.pass);
			
			return btc;
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			new DispatchException("[failure] on evaluate: " + e.getMessage());
		} catch (SecurityException e) {
			new DispatchException("[failure] security failure");
		}
		
		return null;
	}
	
	/*
	 * Evaluates scripted contract
	 * @param Class<? extends ScriptedContract>
	 */
	
	
	/*
	 * Gets the keys for the next verification
	 */
	public Tuple<Map<FieldTypes,Coin>, Map<FieldTypes, ECKey>> getNextKeys() throws DispatchException {
		if (!this.hasNext()) {
			throw new DispatchException("[failure] dispatch invoked without contracts");
		}
		
		return verifyAndParseFields(this.theContracts.get(0).getFields());
	}
	
	/*
	 * Verifies the correct fields
	 * @param Field[] : all the variables
	 * @return <FieldType,ECKey>
	 */
	private static Tuple<Map<FieldTypes,Coin>, Map<FieldTypes, ECKey>> verifyAndParseFields(Field[] fields) throws DispatchException {
		Map<FieldTypes, ECKey> finalfields = new HashMap<FieldTypes, ECKey>();
		Map<FieldTypes, Coin> coins = new HashMap<FieldTypes, Coin>();
		
		Boolean[] bools = new Boolean[4];
		for (int i = 0; i < bools.length; i++) {
			bools[i] = false;
		}
		for (Field f : fields) {
			Object o;
			try {
				o = f.get(null);
				if (o instanceof ECKey) {
					ECKey key = (ECKey) o;
					if (f.getName().equals("key1")) {
						bools[0] = true;
						finalfields.put(FieldTypes.KEY1, key);
					} else if (f.getName().equals("key2")) {
						bools[1] = true;
						finalfields.put(FieldTypes.KEY2, key);
					} else {
						new DispatchException("[failure] extraneous field " + f.getName());
					}
						
				} else if (o instanceof Coin) {
					Coin c = (Coin) o;
					if (f.getName().equals("dep1")) {
						bools[2] = true;
						coins.put(FieldTypes.DEP1, c);
					} else if (f.getName().equals("dep2")) {
						bools[3] = true;
						coins.put(FieldTypes.DEP2, c);
					} else {
						new DispatchException("[failure] extraneous field " + f.getName());
					}
				}
			} catch (IllegalArgumentException | IllegalAccessException e) {
				new DispatchException("[failure] coult not parse field " + f.getName());
			}
		}
		
		for (boolean b : bools) {
			if (!b) {
				new DispatchException("[failure] missing field");
			}
		}
		
		return new Tuple<Map<FieldTypes,Coin>, Map<FieldTypes, ECKey>>(coins,finalfields);
	}
	
	/*
	 * Verifies the correct structure of the contract
	 * Returns an accessible map
	 * @param Method[] : raw contract structure
	 * @return Map<String, Method> : accessible and safe contract structure
	 */
	private static Map<ContractMethods, Method> verifyAndParse(Method[] methods) throws DispatchException {
		Map<ContractMethods, Method> safeMethods = new HashMap<ContractMethods, Method>();
		Method eval = findMethod(methods, ContractMethods.EVALUATE.name);
		if (eval == null || !eval.getGenericReturnType().getTypeName().equals("boolean")) {
			throw new DispatchException("[failure] evaluate method tampered");
		}
		safeMethods.put(ContractMethods.EVALUATE, eval);
		
		Method onTrue = findMethod(methods, ContractMethods.ONTRUE.name);
		if (onTrue == null || !onTrue.getGenericReturnType().getTypeName().equals(Dispatch.EVAL_TYPE_STANDARD)) {
			throw new DispatchException("[failure] onTrue method tampered");
		}
		safeMethods.put(ContractMethods.ONTRUE, onTrue);
		
		Method onFalse = findMethod(methods, ContractMethods.ONFALSE.name);
		if (onFalse == null || !onFalse.getGenericReturnType().getTypeName().equals(Dispatch.EVAL_TYPE_STANDARD)) {
			throw new DispatchException("[failure] onFalse method tampered");
		}
		safeMethods.put(ContractMethods.ONFALSE, onTrue);
		
		return safeMethods;
	}
	
	/*
	 * Kills the contract if it can't be verified
	 */
	public void skipNext() {
		this.theContracts.remove(0);
	}
	
	/*
	 * Finds the method by name
	 * @param Method[] : methods to search through
	 * @param name : name of the method
	 * @return null if not there else method
	 */
	private static Method findMethod(Method[] methods, String name) {
		for (Method m : methods) {
			if (m.getName().equals(name)) {
				return m;
			}
		}
		
		return null;
	}

}
