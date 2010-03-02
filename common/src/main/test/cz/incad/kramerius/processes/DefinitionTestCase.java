package cz.incad.kramerius.processes;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import com.google.inject.Injector;


public class DefinitionTestCase extends AbstractGuiceTestCase {

	@After
	public void doAfter() {
		dropTables();
	}

	
	@Test
	public void testDefinition() throws InterruptedException {
		Injector inj = injector();
		DefinitionManager defMgr = inj.getInstance(DefinitionManager.class);
		defMgr.load();
		
		LRProcessDefinition definition = defMgr.getLongRunningProcessDefinition("generovani_pdf");
		
		LRProcess process = definition.createNewProcess();
		process.startMe(false);

		Thread.sleep(2000);

		LRProcessManager instmgr = inj.getInstance(LRProcessManager.class);
		LRProcess lrProcess = instmgr.getLongRunningProcess(process.getUUID());
		Assert.assertTrue(instmgr.getLongRunningProcesses().size() == 1);
		Assert.assertNotNull(lrProcess);
		// kill process
		lrProcess.stopMe();
		
		// zjisti, jaky je stav po killovani
		LRProcess lrProcessAfterKill = instmgr.getLongRunningProcess(process.getUUID());
		Assert.assertTrue(instmgr.getLongRunningProcesses().size() == 1);
		Assert.assertNotNull(lrProcessAfterKill);
		Assert.assertTrue(lrProcessAfterKill.getProcessState().equals(States.KILLED));

	}
}
