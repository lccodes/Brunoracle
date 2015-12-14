package edu.brown.lcamery.server.security;


public class ContractManager extends SecurityManager {
	protected final int pass;
	protected boolean on;
	
	public ContractManager(int pass) {
		this.pass = pass;
		this.on = true;
	}
	
	public void toggle(int pass) {
		if (pass == this.pass) {
			this.on = !this.on;
		}
	}
	
	public void mandate(int pass, boolean signal) {
		if (pass == this.pass) {
			this.on = signal;
		}
	}
}
