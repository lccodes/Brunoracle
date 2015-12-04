package edu.brown.lcamery.server.support;

import java.util.LinkedList;
import java.util.List;

public enum FieldTypes {
	KEY1,KEY2,DEP1,DEP2;
	
	public static FieldTypes getOther(FieldTypes type) {
		if (type.equals(FieldTypes.KEY1)) {
			return FieldTypes.DEP1;
		} else if (type.equals(FieldTypes.DEP1)) {
			return FieldTypes.KEY1;
		} else if (type.equals(FieldTypes.DEP2)) {
			return FieldTypes.KEY2;
		} else {
			return FieldTypes.DEP2;
		}
	}
	
	public static List<FieldTypes> getDeposits() {
		List<FieldTypes> list = new LinkedList<FieldTypes>();
		list.add(DEP1);
		list.add(DEP2);
		return list;
	}
	
	public static List<FieldTypes> getKeys() {
		List<FieldTypes> list = new LinkedList<FieldTypes>();
		list.add(KEY1);
		list.add(KEY2);
		return list;
	}
}
