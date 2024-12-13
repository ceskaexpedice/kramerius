package cz.incad.kramerius.rest.apiNew.client.v70.libs;

import java.util.List;

public interface Instances {

    public List<OneInstance> allInstances();

    public List<OneInstance> enabledInstances();

    public List<OneInstance> disabledInstances();

    public OneInstance find(String acronym);

    public boolean isAnyDisabled();

    public void cronRefresh();

    public boolean isEnabledInstance(String acronym);
}
