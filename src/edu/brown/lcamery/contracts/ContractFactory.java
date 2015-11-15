package edu.brown.lcamery.contracts;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

public final class ContractFactory {
	private final String FIRST;
	private final String SECOND;
	private String failure;
	private String success;
	private int timeout;
	private Contract c;
	
	public ContractFactory(String contractName, String firstAsk, String secondAsk) {
		final String CONTRACT = String.copyValueOf(contractName.toCharArray());
		this.FIRST = String.copyValueOf(firstAsk.toCharArray());
		this.SECOND = String.copyValueOf(secondAsk.toCharArray());
		timeout = 3;
		try {
			c = (Contract) Class.forName("edu.lcamery.brown.constracts.instances." + CONTRACT).newInstance();
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private boolean doContract() {
		Socket first = null;
		Socket second = null;
		try {
			first = new Socket(FIRST.split(":")[0], Integer.parseInt(FIRST.split(":")[1]));
			first.setSoTimeout(100000);
			second = new Socket(SECOND.split(":")[0], Integer.parseInt(SECOND.split(":")[1]));
			second.setSoTimeout(100000);
			
			int t = timeout;
			BufferedReader one = new BufferedReader(new InputStreamReader(first.getInputStream()));
			while (t-- != 0 && c.firstResponse(one.readLine())) {
				c.firstResponse(one.readLine());
			}
			
			t = timeout;
			BufferedReader two = new BufferedReader(new InputStreamReader(second.getInputStream()));
			while (t-- != 0 && c.secondResponse(two.readLine())) {
				c.firstResponse(two.readLine());
			}
			
			if(c.evaluate()) {
				this.success = "onTrue";
				c.onTrue();
			} else {
				this.success = "onFalse";
				c.onFalse();
			}
			
			//Cleanup:
			if (first != null) {
				try {
					first.close();
				} catch (IOException e) {
					throw new ContractException();
				}
			}
			if (second != null) {
				try {
					second.close();
				} catch (IOException e) {
					throw new ContractException();
				}
			}
		} catch(ContractException e) {
			this.failure = e.getMessage();
			return false;
		} catch (NumberFormatException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		return true;
	}
	
	public String getFailure() {
		return String.copyValueOf(this.failure.toCharArray());
	}
	
	public String getSuccess() {
		return String.copyValueOf(this.success.toCharArray());
	}
	
	public static void main(String[] args) {
		if (args.length != 3) {
			System.out.println("[Error] Invalid argument failure.");
			return;
		}
		ContractFactory eval = new ContractFactory(args[0], args[1], args[2]);
		if(eval.doContract()) {
			System.out.println("[Success] " + eval.getSuccess());
		} else {
			System.out.println("[Failure]" + eval.getFailure());
		}
	}
	
	public void setTimeout(int time) {
		this.timeout = time;
	}

}
