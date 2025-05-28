package cz.incad.kramerius.rest.api.k5.client.utils;

import org.ceskaexpedice.akubra.relsext.TreeNodeProcessor;

import java.util.ArrayList;
import java.util.List;

public class ChildrenNodeProcessor implements TreeNodeProcessor {

    private List<String> children = new ArrayList<String>();

    @Override
    public void process(String pid, int level) {
        if (level >= 1) throw new UnsupportedOperationException("level "+level+" is unsupported");
    }


    @Override
    public boolean skipBranch(String pid, int level) {
        if (level == 1) {
            children.add(pid);
        }
        return level > 0;
    }

    @Override
    public boolean breakProcessing(String pid, int level) {
        return false;
    }

    public List<String> getChildren() {
        return children;
    }

}
