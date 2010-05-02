package cz.incad;

import java.util.Map;

import com.google.inject.TypeLiteral;

public class Test {

	public static void main(String[] args) throws SecurityException, NoSuchMethodException {
		TypeLiteral<Map<Integer, String>> mapType = new TypeLiteral<Map<Integer, String>>() {};
		System.out.println(mapType.getType());
		TypeLiteral<?> keySetType = mapType.getReturnType(Map.class.getMethod("keySet"));
		System.out.println(keySetType); // prints "Set<Integer>"
	}
}
