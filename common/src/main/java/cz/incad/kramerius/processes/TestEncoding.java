package cz.incad.kramerius.processes;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.Set;
import java.util.SortedMap;

import cz.incad.kramerius.processes.impl.ProcessStarter;

public class TestEncoding {
	
	public static void main(String[] args) throws IOException {
//		SortedMap<String,Charset> availableCharsets = Charset.availableCharsets();
//		Set<String> keySet = availableCharsets.keySet();
//		for (String string : keySet) {
//			System.out.println(string);
//		}
//		ProcessStarter.updateName("Drobnůstky");

		String txt  = "Generov%C3%A1n%C3%AD+%27Drobn%C5%AFstky%27+na+CD";
		System.out.println(URLDecoder.decode(txt, "UTF-8"));
	}
}
