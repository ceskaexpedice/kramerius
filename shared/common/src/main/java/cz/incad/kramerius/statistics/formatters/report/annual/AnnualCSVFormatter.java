package cz.incad.kramerius.statistics.formatters.report.annual;

import cz.incad.kramerius.statistics.formatters.report.StatisticsReportFormatter;
import cz.incad.kramerius.statistics.impl.AnnualStatisticsReport;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class AnnualCSVFormatter implements StatisticsReportFormatter {

    private OutputStream os;

    private Map<String, Integer> cumulativeMap = new HashMap<>();

    public void printHeader(StringBuilder builder) throws UnsupportedEncodingException, IOException {
        builder.append("count").append(',');
        builder.append("model");
        builder.append("\n");
    }

    @Override
    public void processReportRecord(Map<String, Object> record) {
        Integer count = (Integer) record.get("count");
        String model = (String)record.get("model");

        if (!cumulativeMap.containsKey(model)) {
            cumulativeMap.put(model, new Integer(0));
        }
        cumulativeMap.put(model, new Integer(count + cumulativeMap.get(model)));

    }

    @Override
    public String getFormat() {
        return CSV_FORMAT;
    }

//    @Override
//    public void beforeProcess(HttpServletResponse response) throws IOException {
//        this.cumulativeMap = new HashMap<>();
//    }
//
//    @Override
//    public void afterProcess(HttpServletResponse response) throws IOException {
//        StringBuilder builder = new StringBuilder();
//        this.printHeader(builder);
//        String[] models = cumulativeMap.keySet().toArray(new String[cumulativeMap.keySet().size()]);
//        Arrays.sort(models);
//        for (String model: models) {
//            builder.append(cumulativeMap.get(model)).append(',');
//            builder.append(model).append("\n");
//        }
//        os.write(builder.toString().getBytes(DEFAULT_ENCODING));
//        this.os = null;
//    }

    @Override
    public void beforeProcess(OutputStream os) throws IOException {
        this.cumulativeMap = new HashMap<>();
    }

    @Override
    public void afterProcess(OutputStream os) throws IOException {
        StringBuilder builder = new StringBuilder();
        this.printHeader(builder);
        String[] models = cumulativeMap.keySet().toArray(new String[cumulativeMap.keySet().size()]);
        Arrays.sort(models);
        for (String model: models) {
            builder.append(cumulativeMap.get(model)).append(',');
            builder.append(model).append("\n");
        }
        os.write(builder.toString().getBytes(DEFAULT_ENCODING));
        this.os = null;
    }

    @Override
    public String getReportId() {
        return AnnualStatisticsReport.REPORT_ID;
    }

    @Override
    public String getMimeType() {
        return CSV_MIME_TYPE;
    }

//    @Override
//    public void addInfo(HttpServletResponse response, String info) throws IOException {
//        this.os = response.getOutputStream();
//        String text = "Roční výkaz";
//        String comment = "# " + text + ", " + info + "\n";
//        this.os.write(comment.getBytes(DEFAULT_ENCODING));
//    }

    @Override
    public void addInfo(OutputStream of, String info) throws IOException {
        this.os = of;
        String text = "Roční výkaz";
        String comment = "# " + text + ", " + info + "\n";
        this.os.write(comment.getBytes(DEFAULT_ENCODING));

    }


}

