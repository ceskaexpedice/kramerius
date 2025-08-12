package cz.incad.kramerius.statistics.formatters.report.nkp;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.statistics.formatters.report.StatisticsReportFormatter;
import cz.incad.kramerius.statistics.impl.NKPLogReport;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NKPJSONFormatter implements StatisticsReportFormatter {

    public static final Logger LOGGER = Logger.getLogger(NKPJSONFormatter.class.getName());

    private OutputStream os;



    @Override
    public String getMimeType() {
        return JSON_MIME_TYPE;
    }

    @Override
    public String getFormat() {
        return JSON_FORMAT;
    }

    @Override
    public String getReportId() {
        return  NKPLogReport.REPORT_ID;
    }

    @Override
    public void processReportRecord(Map<String, Object> record) {
        try {
            JSONObject object = new JSONObject(record);
            this.os.write(object.toString().getBytes(DEFAULT_ENCODING));
            this.os.write("\n".getBytes(DEFAULT_ENCODING));
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(),e);
        }
    }

    @Override
    public void beforeProcess(OutputStream os) throws IOException {
        this.os = os;
    }

    @Override
    public void afterProcess(OutputStream os) throws IOException {
        this.os = null;
    }

    @Override
    public void addInfo(OutputStream os, String info) throws IOException {
        this.os = os;
    }
}
