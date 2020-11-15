package cz.incad.kramerius.services.logging;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class OnlyMessageFormatter extends Formatter {

    @Override
    public String format(LogRecord record) {
        return record.getMessage()+"\n";
    }
}