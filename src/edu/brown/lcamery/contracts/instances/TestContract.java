package edu.brown.lcamery.contracts.instances;

import edu.brown.lcamery.contracts.Contract;

public class TestContract extends Contract {

	@Override
	protected boolean evaluate() {
		return true;
	}

	@Override
	protected void onTrue() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onFalse() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected boolean firstResponse(String question) {
		if (question.equals("101")) {
			return true;
		}
		return false;
	}

	@Override
	protected boolean secondResponse(String question) {
		if (question.equals("222")) {
			return true;
		}
		return false;
	}

}
