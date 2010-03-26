package cz.incad.kramerius.processes.mock;

import java.util.Arrays;

public class MockLPProcess {

	public static final java.util.logging.Logger LOGGER = java.util.logging.Logger
			.getLogger(MockLPProcess.class.getName());

	
	public static void main(String[] args) {
		LOGGER.info("args:"+Arrays.asList(args));
		// 1TB  space
		long tb = 1l << 40;
		// 1GB  space
		long gb = 1l << 30;
		long start =System.currentTimeMillis();
		for (long i = 0; i < tb; i++) {
			if ((i%100000000) == 0) {
				//LOGGER.info("iterating "+i);
				LOGGER.info("  diff = "+(System.currentTimeMillis()-start)+"ms and i ="+i);
			}
		}

		LOGGER.info(" stop with "+(System.currentTimeMillis()-start)+"ms");
		
	}
}
