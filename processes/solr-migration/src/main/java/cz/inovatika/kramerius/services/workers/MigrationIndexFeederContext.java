package cz.inovatika.kramerius.services.workers;

import cz.inovatika.kramerius.services.iterators.IterationItem;

import java.util.List;

public abstract class MigrationIndexFeederContext {

    protected List<IterationItem> allItems;

    public MigrationIndexFeederContext(List<IterationItem> allItems) {
        this.allItems = allItems;
    }

    public List<IterationItem> getAllItems() {
        return allItems;
    }
}
