package cz.inovatika.kramerius.services.iterators.utils;

import cz.inovatika.kramerius.services.iterators.IterationItem;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

//TODO: Delete
public class IterationUtils {

    private IterationUtils() {}

    // TODO: Delete
    public static String repairPidIfNeeded(String pid) {
        if (pid.contains("@") && !pid.contains("/@")) {
            pid = pid.replaceAll("@","/@");
        }
        return pid;
    }


    public static List<IterationItem> pidsToIterationItem(File source, List<String> retVals) {
        List<String> repaired = retVals.stream().map(IterationUtils::repairPidIfNeeded).collect(Collectors.toList());
        return repaired.stream().map(p -> {
            return new IterationItem(p, source.getAbsolutePath());
        }).collect(Collectors.toList());
    }

    public static List<IterationItem> pidsToIterationItem(URL source, List<String> retVals) {
        List<String> repaired = retVals.stream().map(IterationUtils::repairPidIfNeeded).collect(Collectors.toList());
        return repaired.stream().map(p -> {
            return new IterationItem(p, source.toString());
        }).collect(Collectors.toList());
    }

    public static List<IterationItem> pidsToIterationItem(String source, List<String> retVals) {
        List<String> repaired = retVals.stream().map(IterationUtils::repairPidIfNeeded).collect(Collectors.toList());
        return repaired.stream().map(p -> {
            return new IterationItem(p, source);
        }).collect(Collectors.toList());
    }

}
