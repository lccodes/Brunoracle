package edu.brown.lcamery.server.support;

public enum ContractMethods {
	EVALUATE("evaluate"),
	ONTRUE("onTrue"),
	ONFALSE("onFalse"),
	FIRSTRESPONSE("firstResponse"),
	SECONDRESPONSE("secondResponse"),
	FIRSTDEPOSIT("firstdeposit"),
	SECONDDEPOSIT("seconddeposit");
	
	public String name;
	private ContractMethods(String name) {
		this.name = name;
	}
}
