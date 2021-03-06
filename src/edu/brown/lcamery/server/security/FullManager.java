package edu.brown.lcamery.server.security;

import java.io.FileDescriptor;
import java.security.Permission;

public class FullManager extends ContractManager {
	
	public FullManager(int pass) {
		super(pass);
	}

	@Override
	public void checkPermission(Permission p) {
		if (!on) {
			return;
		}
		throw new SecurityException("[security] contract attempted to check permission " + p.getName());
	}
	
	@Override
	public void checkAccept(String host, int port) {
		if (!on) {
			return;
		}
		throw new SecurityException("[security] contract attempted to open port");
	}
	
	@Override
	public void checkAccess(Thread t) {
		if (!on) {
			return;
		}
		throw new SecurityException("[security] contract attempted to access thread");
	}
	
	@Override
	public void checkAccess(ThreadGroup t) {
		if (!on) {
			return;
		}
		throw new SecurityException("[security] contract attempted to access threadgroup");
	}
	
	@Override
	public void checkConnect(String host, int port) {
		if (!on) {
			return;
		}
		throw new SecurityException("[security] contract attempted to connect to port");
	}
	
	@Override
	public void checkConnect(String host, int port, Object o) {
		if (!on) {
			return;
		}
		throw new SecurityException("[security] contract attempted to connect to port");
	}
	
	@Override
	public void checkCreateClassLoader() {
		if (!on) {
			return;
		}
		throw new SecurityException("[security] contract attempted to load class");
	}
	
	@Override
	public void checkDelete(String f) {
		if (!on) {
			return;
		}
		throw new SecurityException("[security] contract attempted to delete file " + f);
	}
	
	@Override
	public void checkExec(String cmd) {
		if (!on) {
			return;
		}
		throw new SecurityException("[security] contract attempted to exec");
	}
	
	@Override
	public void checkExit(int status) {
		if (!on) {
			return;
		}
		System.out.println("[security] contract attempted to exit");
		throw new SecurityException("[security] contract attempted to exit");
	}
	
	@Override
	public void checkRead(FileDescriptor r) {
		if (!on) {
			return;
		}
		throw new SecurityException("[security] contract attempted to read file");
	}
	
	@Override
	public void checkRead(String r) {
		if (!on) {
			return;
		}
		throw new SecurityException("[security] contract attempted to read file");
	}
	
	@Override
	public void checkRead(String r, Object o) {
		if (!on) {
			return;
		}
		throw new SecurityException("[security] contract attempted to read file");
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