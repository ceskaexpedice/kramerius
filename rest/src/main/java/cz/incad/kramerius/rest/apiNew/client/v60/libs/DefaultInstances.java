package cz.incad.kramerius.rest.apiNew.client.v60.libs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import cz.incad.kramerius.utils.conf.KConfiguration;

public class DefaultInstances implements Instances{
	
	
	private List<String> insts = new ArrayList<>();
	private Map<String, Boolean> statuses = new HashMap<>();
	
	
	
	public DefaultInstances() {
		super();
		List<Object> list = KConfiguration.getInstance().getConfiguration().getList("cdk.collections.sources._all", Arrays.asList("inovatika","knav","mzk","svkhk","svkul","uzei","svkul","kkp"));
		this.insts = list.stream().map(Object::toString).collect(Collectors.toList());
		this.insts.forEach(inst-> {
			this.statuses.put(inst, true);
		});
	}


	@Override
	public List<String> allInstances() {
		return insts;
	}

	@Override
	public List<String> enabledInstances() {
		List<String> enabled = new ArrayList<>();
		this.statuses.entrySet().forEach(entry-> {
			if (entry.getValue()) enabled.add(entry.getKey());
		});
		return enabled;
	}

	@Override
	public List<String> disabledInstances() {
		List<String> enabled = new ArrayList<>();
		this.statuses.entrySet().forEach(entry-> {
			if (!entry.getValue()) enabled.add(entry.getKey());
		});
		return enabled;
	}

	@Override
	public void setStatus(String inst, boolean status) {
		this.statuses.put(inst, status);
	}

	@Override
	public boolean getStatus(String inst) {
		if (this.statuses.containsKey(inst)) {
			return this.statuses.get(inst);
		} else return false;
	}

	@Override
	public boolean isAnyDisabled() {
		List<String> eInsts = this.enabledInstances();
		return this.insts.size() != eInsts.size();
	}
	
	
}
