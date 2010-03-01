package cz.incad.kramerius.processes;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class AbstractGuiceTestCase {

	protected Injector injector() {
		Injector injector = Guice.createInjector(new TestModule());
		return injector;
	}
}
