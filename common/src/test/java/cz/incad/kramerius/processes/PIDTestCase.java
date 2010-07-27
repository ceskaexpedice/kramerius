package cz.incad.kramerius.processes;

import java.io.IOException;
import java.util.List;

import junit.framework.TestCase;

public class PIDTestCase extends TestCase {

	public void testPIDList() throws IOException, InterruptedException {
		PIDList pidlist = PIDList.createPIDList();
		List<String> processesPIDS = pidlist.getProcessesPIDS();
		assertTrue(!processesPIDS.isEmpty());
	}
}
