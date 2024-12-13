package cz.incad.kramerius.fedora.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FedoraAccessProxyUtils {

    private FedoraAccessProxyUtils() {}

    public static  List<String> window(List<String> list, String pid, int size) {
        int index = list.indexOf(pid);
        if (index >= 0)  {
            int minIndex = Math.max(index-size, 0);
            size += size-(index-minIndex);

            int maxIndex = Math.min(index+size, list.size());
            return list.subList(minIndex, maxIndex);
        } else {
            List<String> retList = new ArrayList<>(list.subList(0,Math.min(list.size(), size)));
            retList.add(pid);
            return retList;
        }
    }

}
