package cz.incad.kramerius.processes;

import org.junit.Test;

import com.google.inject.Injector;


public class DefinitionTestCase extends AbstractGuiceTestCase {

	@Test
	public void testDefinition() throws InterruptedException {
		Injector inj = injector();
		
		DefinitionManager defMgr = inj.getInstance(DefinitionManager.class);
		defMgr.load();
		
		LRProcessDefinition definition = defMgr.getLongRunningProcessDefinition("generovani_pdf");
		LRProcess process = definition.createNewProcess();
		process.startMe(false);
		System.out.println("Test");
		Thread.sleep(2000);
		
		LRProcessManager instmgr = inj.getInstance(LRProcessManager.class);
		LRProcess lrProcess = instmgr.getLongRunningProcess(process.getUUID());
		System.out.println("Kill process");
		lrProcess.stopMe();
	}
}
