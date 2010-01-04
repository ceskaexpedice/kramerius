package com.qbizm.kramerius.imptool.poc.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Priority;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.varia.NullAppender;

public class LogSummaryAppender extends NullAppender {

  private static final Map<String, Map<Level, List<String>>> stats = new HashMap<String, Map<Level, List<String>>>();

  /**
   * Uklada jenom warningy a errory
   */
  public synchronized void doAppend(LoggingEvent event) {
    if (event.getLevel().isGreaterOrEqual(Priority.WARN)) {
      String className = event.getClass().getName();
      if (!stats.containsKey(className)) {
        stats.put(className, new HashMap<Level, List<String>>());
      }

      Map<Level, List<String>> classStats = stats.get(className);
      if (!classStats.containsKey(event.getLevel())) {
        classStats.put(event.getLevel(), new ArrayList<String>());
      }

      List<String> messageStats = classStats.get(event.getLevel());
      String message = event.getMessage().toString();
      messageStats.add(message.substring(0, message.indexOf(':')));
    }
  }

  public static Map<String, Map<Level, List<String>>> getStats() {
    return stats;
  }

}
