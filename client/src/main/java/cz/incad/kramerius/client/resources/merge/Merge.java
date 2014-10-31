package cz.incad.kramerius.client.resources.merge;

import cz.incad.kramerius.client.resources.merge.validators.ValidateInput;

public interface Merge<T>  {

    public String merge(String fromWar, String fromConf);

    public void setValidateInput(ValidateInput<T> v);

    public ValidateInput<T> getValidateInput();
}
