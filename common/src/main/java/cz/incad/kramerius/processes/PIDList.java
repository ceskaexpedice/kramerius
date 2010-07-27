package cz.incad.kramerius.processes;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang.SystemUtils;

import cz.incad.kramerius.processes.os.impl.unix.UnixPIDList;
import cz.incad.kramerius.processes.os.impl.windows.WindowsLRProcessImpl;
import cz.incad.kramerius.processes.os.impl.windows.WindowsPIDList;

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
