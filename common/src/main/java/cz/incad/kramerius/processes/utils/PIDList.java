package cz.incad.kramerius.processes.utils;

import cz.incad.kramerius.processes.os.impl.unix.UnixPIDList;
import cz.incad.kramerius.processes.os.impl.windows.WindowsPIDList;
import org.apache.commons.lang3.SystemUtils;

import java.io.IOException;
import java.util.List;

/**
 * This helper class for getting list of pids
 * @author pavels
 */
public abstract class PIDList {
	
	public abstract List<String> getProcessesPIDS() throws IOException, InterruptedException;
	
	/**
	 * Returns list of all processes pids
	 * @return
	 */
	public static PIDList createPIDList() {
		if (SystemUtils.IS_OS_LINUX || SystemUtils.IS_OS_UNIX) {
			return new UnixPIDList();
		} else if (SystemUtils.IS_OS_WINDOWS) {
			return new WindowsPIDList();
		} else throw new UnsupportedOperationException("unsupported OS");
	}
}
