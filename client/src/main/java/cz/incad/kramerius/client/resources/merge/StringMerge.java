package cz.incad.kramerius.client.resources.merge;

import cz.incad.kramerius.client.resources.merge.validators.ValidateInput;

public class StringMerge implements Merge<String>{

    @Override
    public String merge(String fromWar, String fromConf) {
        return fromWar + fromConf;
    }

    @Override
    public void setValidateInput(ValidateInput<String> v) {
        
    }

    @Override
    public ValidateInput<String> getValidateInput() {
        return null;
    }
}
