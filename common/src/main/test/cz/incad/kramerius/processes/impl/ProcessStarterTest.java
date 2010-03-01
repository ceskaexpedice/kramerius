package cz.incad.kramerius.processes.impl;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;

import org.junit.Test;

import cz.incad.kramerius.processes.mock.MockLPProcess;

public class ProcessStarterTest {

	@Test
	public void testProcessStarter() throws SecurityException, IllegalArgumentException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, SQLException {
		System.setProperty(ProcessStarter.MAIN_CLASS_KEY, MockLPProcess.class.getName());
		ProcessStarter.main(new String[] {});
	}
}
