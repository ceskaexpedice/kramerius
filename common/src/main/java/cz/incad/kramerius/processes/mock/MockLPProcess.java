package cz.incad.kramerius.processes.mock;

import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Handler;
import java.util.logging.Logger;

import cz.incad.kramerius.processes.impl.ProcessStarter;

public class MockLPProcess {

    public static final boolean SA_FLAG = false;
    
	public static final java.util.logging.Logger LOGGER = java.util.logging.Logger
			.getLogger(MockLPProcess.class.getName());

	
	public static void main(String[] args) throws IOException {
		LOGGER.info("args:"+Arrays.asList(args));
		if (!SA_FLAG) {
	        ProcessStarter.updateName("Jmeno procesu mock..");
		}

		   
		//1MB space
		long mb = 1l << 20;
		// 1TB  space
		long tb = 1l << 40;
		// 1GB  space
		long gb = 1l << 30;
		long start =System.currentTimeMillis();
		for (long i = 0; i < gb; i++) {
			if ((i%10000) == 0) {
				LOGGER.info("  diff = "+(System.currentTimeMillis()-start)+"ms and i ="+i);
			}
		}
		System.err.println(" Pochyby, pochyby, pochyby...");
		LOGGER.info(" stop with "+(System.currentTimeMillis()-start)+"ms");
	}
}
