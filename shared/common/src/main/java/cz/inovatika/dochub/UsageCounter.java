package cz.inovatika.dochub;

import cz.incad.kramerius.security.licenses.limits.LimitInterval;

public interface UsageCounter {

    void logUsage(String pid, String user);

    long getUsageCount(String user,  String pid, LimitInterval interval, int value);
}