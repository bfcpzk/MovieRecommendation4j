package util;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public class Test {
	

	public static void main(String[] args){
		Map<Double,Integer> test = new TreeMap<Double,Integer>().descendingMap();
		test.put(1.2, 1);
		test.put(0.2, 1);
		test.put(3.2, 1);
		test.put(8.2, 1);
		test.put(5.2, 1);
		test.put(6.2, 1);
		for(Entry entry : test.entrySet()){
			System.out.println(entry.getKey() + " " + entry.getValue());
		}
			
	}
}
