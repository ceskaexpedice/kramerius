package cz.incad.kramerius.processes.os.impl.windows;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cz.incad.kramerius.processes.PIDList;

public class WindowsPIDList extends PIDList {

	@Override
	public List<String> getProcessesPIDS() throws IOException, InterruptedException {
		return new ArrayList<String>();
	}
}
