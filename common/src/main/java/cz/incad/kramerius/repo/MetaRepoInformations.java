package cz.incad.kramerius.repo;

import java.util.List;

public interface MetaRepoInformations {

    public List<String> getFedoraObjectsFromModel(String model, int limit, int offset, String orderby, String orderDir) throws MetaRepoInformationsException;

}
