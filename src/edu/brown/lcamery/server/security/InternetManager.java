package edu.brown.lcamery.server.security;

import java.io.FileDescriptor;
import java.security.Permission;

public class InternetManager extends ContractManager {
	
	public InternetManager(int pass) {
		super(pass);
	}
	
	@Override
	public void checkPermission(Permission p) {
	}
	
	@Override
	public void checkAccept(String host, int port) {
	}
	
	@Override
	public void checkAccess(Thread t) {
	}
	
	@Override
	public void checkAccess(ThreadGroup t) {
	}
	
	@Override
	public void checkConnect(String host, int port) {
	}
	
	@Override
	public void checkConnect(String host, int port, Object o) {
	}
	
	@Override
	public void checkCreateClassLoader() {
	}
	
	@Override
	public void checkDelete(String f) {
	}
	
	@Override
	public void checkExec(String cmd) {
	}
	
	@Override
	public void checkExit(int status) {
	}
	
	@Override
	public void checkRead(FileDescriptor r) {
	}
	
	@Override
	public void checkRead(String r) {
	}
	
	@Override
	public void checkRead(String r, Object o) {
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
