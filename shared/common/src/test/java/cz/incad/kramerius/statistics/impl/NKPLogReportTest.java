package cz.incad.kramerius.statistics.impl;

import cz.incad.kramerius.statistics.ReportedAction;
import cz.incad.kramerius.statistics.accesslogs.database.DatabaseStatisticsAccessLogImpl;
import junit.framework.Assert;
import org.antlr.stringtemplate.StringTemplate;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Ignore;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

public class NKPLogReportTest {

//    @Test
//    public void testTemplate() {
//        StringTemplate statRecord = DatabaseStatisticsAccessLogImpl.stGroup.getInstanceOf("nkpLogsReport");
//        statRecord.setAttribute("action", "READ");
//        statRecord.setAttribute("action", ReportedAction.READ.name());
//        statRecord.setAttribute("fromDefined", true);
//        statRecord.setAttribute("toDefined", true);
//        Assert.assertNotNull(statRecord.toString());
//        Assert.assertTrue(statRecord.toString().contains("date"));
//        Assert.assertTrue(statRecord.toString().contains(">"));
//        Assert.assertTrue(statRecord.toString().contains("?"));
//    }
//
//    @Test
//    @Ignore
//    public void testProcessDatabase() throws SQLException {
//        String url = "jdbc:postgresql://localhost/kramerius4-edu";
//        Properties props = new Properties();
//        props.setProperty("user","fedoraAdmin");
//        props.setProperty("password","fedoraAdmin");
//        //props.setProperty("ssl","true");
//        Connection conn = DriverManager.getConnection(url, props);
//
//        String sql = "select \n" +
//                "\tsl.record_id as slrecord_id, \n" +
//                "\tsl.pid as slpid, \n" +
//                "\tsl.date as sldate,\n" +
//                "\tsl.remote_ip_address as slremote_ip_address, \n" +
//                "\tsl.\"USER\" as sluser, \n" +
//                "\tsl.stat_action as slstat_action,\n" +
//                "\tsl.dnnt as sldnnt,\n" +
//                "\tsl.providedbydnnt as slprovidedbydnnt,\n" +
//                "\tsl.evaluatemap as slevaluatemap,\n" +
//                "\tsl.usersessionattributes as slusersessionattributes,\n" +
//                "\t\n" +
//                "\tsd.detail_id as sddetail_id,\n" +
//                "\tsd.pid as sdpid,\n" +
//                "\tsd.model as sdmodel,\n" +
//                "\tsd.issued_date as sdissued_date,\n" +
//                "\tsd.solr_date as sdsolr_date,\n" +
//                "\tsd.rights as sdrights,\n" +
//                "\tsd.lang as sdlang,\n" +
//                "\tsd.title as sdtitle,\n" +
//                "\tsd.branch_id as sdbranch_id,\n" +
//                "\t\n" +
//                "\tsa.author_id as saauthor_id,\n" +
//                "\tsa.author_name as saauthor_name,\n" +
//                "\n" +
//                "\tsp.publisher_id as sppublisher_id,\n" +
//                "\tsp.publisher_name as sppublisher_name\n" +
//                "\t\n" +
//                "\tfrom statistics_access_log sl\n" +
//                "join statistic_access_log_detail sd using(record_id)\n" +
//                "left join statistic_access_log_detail_authors sa using(detail_id)\n" +
//                "left join statistic_access_log_detail_publishers sp using(detail_id)\n" +
//                "where date>'2021-01-02' AND date <'2021-01-03'\n" +
//                "order by sl.record_id, sd.detail_id, sd.branch_id, sa.author_id\n";
//
//        long start = System.currentTimeMillis();
//        final AtomicInteger counter = new AtomicInteger(0);
////
////        System.out.println(sql);
////        NKPLogReport.statisticsIterate(new ArrayList(),sql, conn, (record)->{
////            if (record.get("branches").size() > 1) {
////                System.out.println(record);
////            }
////            counter.incrementAndGet();
////        });
//
//        new NKPLogReport.StastisticsIteration(sql, new ArrayList(),conn, record-> {
//            //logReport(collectedRecord, sup);
//            System.out.println(record);
//        }).iterate();
//
//        long stop = System.currentTimeMillis();
//        System.out.println("It took "+(stop - start)+" ms ");
//    }

//    @Test
//    public void testNull() {
//
//
//
//        long start = System.currentTimeMillis();
//        for (int i = 0; i < 200000; i++) {
//
//            LocalDateTime localDateTime = new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
//            localDateTime = localDateTime.plusDays(1);
//
//            hashVal("xxxsadlfjsdlakjflkxcxlkjlkjkljdfdf");
//            if (i % 10000 == 0) {
//                System.out.println("Test "+i);
//            }
//        }
//        long stop = System.currentTimeMillis();
//        System.out.println((stop - start)+" ms ");
//    }
//
    public static String hashVal(String value)  {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            Base64.Encoder base64Encoder = Base64.getEncoder();
            md5.update(value.getBytes("UTF-8"));
            return base64Encoder.encodeToString(md5.digest());
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            //LOGGER.log(Level.SEVERE, e.getMessage(),e);
            return value;
        }
    }

}
