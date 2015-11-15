package edu.brown.lcamery.contracts;

public abstract class Contract {
	protected abstract boolean evaluate();
	protected abstract void onTrue();
	protected abstract void onFalse();
	protected abstract boolean firstResponse(String question);
	protected abstract boolean secondResponse(String question);
}
