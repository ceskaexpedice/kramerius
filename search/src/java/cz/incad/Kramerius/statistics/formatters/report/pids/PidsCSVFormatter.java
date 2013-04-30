package cz.incad.Kramerius.statistics.formatters.report.pids;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.logging.Level;

import javax.servlet.http.HttpServletResponse;

import cz.incad.Kramerius.statistics.formatters.report.StatisticsReportFormatter;
import cz.incad.Kramerius.statistics.formatters.report.lang.LangCSVFormatter;
import cz.incad.Kramerius.statistics.formatters.utils.StringUtils;
import cz.incad.kramerius.statistics.impl.PidsReport;

public class PidsCSVFormatter implements StatisticsReportFormatter{

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(LangCSVFormatter.class.getName());
    
    private OutputStream os;
    private boolean firstLine;

	@Override
	public String getMimeType() {
        return CSV_MIME_TYPE;
	}

	@Override
	public String getFormat() {
        return CSV_FORMAT;
	}

   public void printHeader() throws UnsupportedEncodingException, IOException {
        StringBuilder builder = new StringBuilder();
        builder.append("count").append(',');
        builder.append("pid").append(',');
        builder.append("model").append(',');
        builder.append("title");
        builder.append("\n");

        this.os.write(builder.toString().getBytes("UTF-8"));
    }

	@Override
	public void beforeProcess(HttpServletResponse response) throws IOException {
        this.os = response.getOutputStream();
        this.firstLine = true;
        this.printHeader();
	}

	@Override
	public void afterProcess(HttpServletResponse response) throws IOException {
        this.os = null;
        this.firstLine = false;
	}

	@Override
	public void processReportRecord(Map<String, Object> record) {
        try {
            if (!firstLine) os.write("\n".getBytes());
            StringBuilder builder = new StringBuilder();
            builder.append(record.get("count")).append(',');
            builder.append(StringUtils.nullify((String)record.get("pid"))).append(',');
            builder.append(StringUtils.nullify((String)record.get("model"))).append(',');
            builder.append('"').append(StringUtils.escapeNewLine(StringUtils.nullify((String)record.get("title")))).append('"');

            os.write(builder.toString().getBytes());
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        }
        firstLine = false;
	}

	@Override
	public String getReportId() {
		return PidsReport.REPORT_ID;
	}

	
}
