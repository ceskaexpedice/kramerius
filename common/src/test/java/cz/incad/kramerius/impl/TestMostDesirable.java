package cz.incad.kramerius.impl;

import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

import cz.incad.kramerius.AbstractGuiceTestCase;
import cz.incad.kramerius.MostDesirable;
import cz.incad.kramerius.processes.DefinitionManager;
import cz.incad.kramerius.processes.DefinitionModule;
import cz.incad.kramerius.processes.LRProcess;
import cz.incad.kramerius.processes.LRProcessDefinition;
import cz.incad.kramerius.processes.LRProcessManager;
import cz.incad.kramerius.processes.States;
import junit.framework.TestCase;

public class TestMostDesirable extends AbstractGuiceTestCase {

	@Before
	public void doBefore() {
		dropTables();
	}
	
	@After
	public void doAfter() {
		dropTables();
	}

	
	@Test
	public void testDefinition() throws InterruptedException {
		Injector inj = injector();
		MostDesirable mostDesirable = inj.getInstance(MostDesirable.class);
		mostDesirable.saveAccess("uuid-e", new Date());
		mostDesirable.saveAccess("uuid-e", new Date());
		mostDesirable.saveAccess("uuid-e", new Date());
		mostDesirable.saveAccess("uuid-e", new Date());
		mostDesirable.saveAccess("uuid-e", new Date());
		mostDesirable.saveAccess("uuid-e", new Date());
		mostDesirable.saveAccess("uuid-e", new Date());
	
		mostDesirable.saveAccess("uuid-a", new Date());
		mostDesirable.saveAccess("uuid-a", new Date());
		mostDesirable.saveAccess("uuid-a", new Date());

		mostDesirable.saveAccess("uuid-b", new Date());
		
		List<String> one = mostDesirable.getMostDesirable(1);
		TestCase.assertTrue(one.size() == 1);
		TestCase.assertTrue(one.get(0).equals("uuid-e"));
		
		
		List<String> two = mostDesirable.getMostDesirable(2);
		TestCase.assertTrue(two.size() == 2);
		TestCase.assertTrue(two.get(0).equals("uuid-e"));
		TestCase.assertTrue(two.get(1).equals("uuid-a"));

		List<String> three = mostDesirable.getMostDesirable(3);
		TestCase.assertTrue(three.size() == 3);
		TestCase.assertTrue(three.get(0).equals("uuid-e"));
		TestCase.assertTrue(three.get(1).equals("uuid-a"));
		TestCase.assertTrue(three.get(2).equals("uuid-b"));
	}
	
	protected Injector injector() {
		Injector injector = Guice.createInjector(new DefinitionModule());
		return injector;
	}

	
}
