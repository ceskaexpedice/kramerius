/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.incad.kramerius.resourceindex;

/**
 *
 * @author Alberto
 */
import cz.incad.kramerius.utils.conf.KConfiguration;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;

import javax.sql.DataSource;

import java.util.logging.Logger;
import org.nsdl.mptstore.core.TableManager;
import org.nsdl.mptstore.util.NTriplesUtil;

import java.util.Properties;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.dbcp.BasicDataSourceFactory;
import org.nsdl.mptstore.core.BasicTableManager;
import org.nsdl.mptstore.core.DDLGenerator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MPTStoreService implements IResourceIndex {

    private static final Logger logger = Logger.getLogger(MPTStoreService.class.getName());
    static final String PROP_DDL =
            "driver.fedora.mpt.db.ddlGenerator";
    static final String PROP_URL = "driver.fedora.mpt.jdbc.url";
    static final String PROP_USERNAME =
            "driver.fedora.mpt.jdbc.user";
    static final String PROP_PASSWD =
            "driver.fedora.mpt.jdbc.password";
    static final String PROP_PREDICATE_MAP =
            "driver.fedora.mpt.db.map";
    static final String PROP_MAP_PREFIX =
            "driver.fedora.mpt.db.prefix";
    static final String PROP_DB_DRIVER =
            "driver.fedora.mpt.db.driverClassName";
    static final String PROP_BACKSLASH_ESCAPE =
            "driver.fedora.mpt.db.backslashIsEscape";
    private TableManager adaptor;
    private DataSource dataSource;
    KConfiguration config;

    public MPTStoreService() {
        config = KConfiguration.getInstance();
        this.adaptor = getTableManager();
    }

    private TableManager getTableManager() {

        /* Initialize the DDL generator */
        DDLGenerator generator;
        try {
            String ddlGen = config.getProperty(PROP_DDL);
            generator = (DDLGenerator) Class.forName(ddlGen).newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Could not initialize DDL generator", e);
        }

        /* Initialize database connection */
        BasicDataSource source;
        Properties dbParams = new Properties();

        dbParams.setProperty("url", config.getProperty(PROP_URL));
        dbParams.setProperty("username", config.getProperty(PROP_USERNAME));
        dbParams.setProperty("password", config.getProperty(PROP_PASSWD));
        dbParams.setProperty("driverClassName", config.getProperty(PROP_DB_DRIVER));
        try {
            logger.fine("USING DRIVER "
                    + config.getProperty(PROP_DB_DRIVER));
            Class.forName(config.getProperty(PROP_DB_DRIVER));
            source =
                    (BasicDataSource) BasicDataSourceFactory.createDataSource(dbParams);
        } catch (Exception e) {
            throw new RuntimeException("Could not establish database connection",
                    e);
        }
        this.dataSource = source;

        /* Finally, create the table manager */
        BasicTableManager manager;
        String mapTable =
                config.getProperty(PROP_PREDICATE_MAP);
        String prefix = config.getProperty(PROP_MAP_PREFIX);
        try {
            manager =
                    new BasicTableManager(source, generator, mapTable, prefix);
        } catch (SQLException e) {
            throw new RuntimeException("Could not initialize table mapper", e);
        }
        return manager;
    }

    public String latestRecordDate() {

        String mods;
        try {
            mods =
                    adaptor.getTableFor(NTriplesUtil.parsePredicate(PRED_lastModifiedDate));
        } catch (ParseException e) {
            /* Should never get here :) */
            throw new RuntimeException("Could not parse predicate ", e);
        }
        logger.fine("getting latest record date");
        String date;
        Connection c;
        try {
            c = dataSource.getConnection();
            PreparedStatement s =
                    c.prepareStatement("SELECT max(o) FROM " + mods,
                    ResultSet.FETCH_FORWARD,
                    ResultSet.CONCUR_READ_ONLY);
            ResultSet r = s.executeQuery();
            r.next();
            date = r.getString(1);
            c.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return date;
    }
    static final String PRED_lastModifiedDate = "<info:fedora/fedora-system:def/view#lastModifiedDate>";
    static final String PRED_dcTitle = "<http://purl.org/dc/elements/1.1/title>";
    static final String PRED_model = "<info:fedora/fedora-system:def/model#hasModel>";
    static final String SPARQL_NS = "http://www.w3.org/2001/sw/DataAccess/rf1/result";
    static final String OUTPUT_DATE_FORMAT = "dd-MM-yyyy HH:mm:ss";
    static final String INPUT_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    @Override
    public Document getFedoraObjectsFromModelExt(String model, int limit, int offset, String orderby, String orderDir) throws Exception {
        /*
         * iTQL query
         * select $object $title $date from <#ri>
        where  $object <fedora-model:hasModel> <info:fedora/model:<c:out value="${selModel}" />>
        and  $object <dc:title> $title
        and  $object <fedora-view:lastModifiedDate> $date
        order by $<c:out value="${order}" /> <c:out value="${order_dir}" />
        limit <c:out value="${rows}" />
        offset <c:out value="${param.offset}" />
         */
        String t1, t2, t3, torder;
        try {
            t1 = adaptor.getTableFor(NTriplesUtil.parsePredicate(PRED_dcTitle));
            t2 = adaptor.getTableFor(NTriplesUtil.parsePredicate(PRED_lastModifiedDate));
            t3 = adaptor.getTableFor(NTriplesUtil.parsePredicate(PRED_model));
            torder = t2 + ".o";
        } catch (ParseException e) {
            /* Should never get here :) */
            throw new RuntimeException("Could not parse predicate ", e);
        }
        logger.fine("getting latest record date");
        Document xmldoc;
        Connection c;
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            xmldoc = builder.newDocument();
            Element root = xmldoc.createElementNS(SPARQL_NS, "sparql");
            Element results = xmldoc.createElementNS(SPARQL_NS, "results");
            xmldoc.appendChild(root);
            root.appendChild(results);
            c = dataSource.getConnection();
            String sql = "select " + t1 + ".s, " + t1 + ".o, " + t2 + ".o from " + t1 + "," + t2 + "," + t3
                    + " where " + t3 + ".o='<info:fedora/model:" + model + ">' and " + t1 + ".s=" + t2 + ".s and " + t1 + ".s=" + t3 + ".s "
                    + " order by " + torder + " " + orderDir
                    + " limit " + limit + " offset " + offset;

            PreparedStatement s =
                    c.prepareStatement(sql,
                    ResultSet.FETCH_FORWARD,
                    ResultSet.CONCUR_READ_ONLY);
            ResultSet r = s.executeQuery();
            String uuid;

            /*
             *
             * <sparql xmlns="http://www.w3.org/2001/sw/DataAccess/rf1/result">
            <head>
            <variable name="object"/>
            <variable name="title"/>
            <variable name="date"/>
            </head>
            <results>
            <result>
            <object uri="info:fedora/uuid:9c1ad6d4-e645-11de-a504-001143e3f55c"/>

            <title>Spisy Masarykovy Akademie Prace 1921</title>
            <date datatype="http://www.w3.org/2001/XMLSchema#dateTime">2010-05-03T08:24:40.776Z</date>
            </result>

             */
            Element e, e2;
            Node n;
            while (r.next()) {
                e = xmldoc.createElementNS(SPARQL_NS, "result");
                uuid = r.getString(1).split("uuid:")[1];
                uuid = uuid.substring(0, uuid.length() - 1);

                e2 = xmldoc.createElementNS(SPARQL_NS, "object");
                e2.setAttribute("uri", uuid);
                //n = xmldoc.createTextNode(uuid);
                //e.appendChild(n);
                e.appendChild(e2);

                e2 = xmldoc.createElementNS(SPARQL_NS, "title");
                n = xmldoc.createTextNode(org.nsdl.mptstore.util.NTriplesUtil.unescapeLiteralValue(r.getString(2)));
                e2.appendChild(n);
                e.appendChild(e2);

                e2 = xmldoc.createElementNS(SPARQL_NS, "date");
                String date = r.getString(3);
                date = date.split("\"")[1];
                DateFormat formatter = new SimpleDateFormat(INPUT_DATE_FORMAT);
                Date dateValue = formatter.parse(date);
                formatter = new SimpleDateFormat(OUTPUT_DATE_FORMAT);
                n = xmldoc.createTextNode(formatter.format(dateValue));
                e2.appendChild(n);
                e.appendChild(e2);

                results.appendChild(e);


            }
            r.close();
            c.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        //return result.toString();
        return xmldoc;
    }

    @Override
    public ArrayList<String> getFedoraPidsFromModel(String model, int limit, int offset) throws Exception {
        /*
         * iTQL query
         * select $object $title $date from <#ri>
        where  $object <fedora-model:hasModel> <info:fedora/model:<c:out value="${selModel}" />>
        and  $object <dc:title> $title
        and  $object <fedora-view:lastModifiedDate> $date
        order by $<c:out value="${order}" /> <c:out value="${order_dir}" />
        limit <c:out value="${rows}" />
        offset <c:out value="${param.offset}" />
         */
        String t1;
        try {
            t1 = adaptor.getTableFor(NTriplesUtil.parsePredicate(PRED_model));
        } catch (ParseException e) {
            /* Should never get here :) */
            throw new RuntimeException("Could not parse predicate ", e);
        }
        logger.fine("getting latest record date");
        Document xmldoc;
        Connection c;
        ArrayList<String> resList = new ArrayList<String>();
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            xmldoc = builder.newDocument();
            Element root = xmldoc.createElementNS(SPARQL_NS, "sparql");
            Element results = xmldoc.createElementNS(SPARQL_NS, "results");
            xmldoc.appendChild(root);
            root.appendChild(results);
            c = dataSource.getConnection();
            String sql = "select " + t1 + ".s"
                    + " where " + t1 + ".o='<info:fedora/model:" + model + ">' "
                    + " order by " + t1 + ".s"
                    + " limit " + limit + " offset " + offset;

            PreparedStatement s =
                    c.prepareStatement(sql,
                    ResultSet.FETCH_FORWARD,
                    ResultSet.CONCUR_READ_ONLY);
            ResultSet r = s.executeQuery();
            String uuid;

            while (r.next()) {
                uuid = r.getString(1).split("uuid:")[1];
                uuid = uuid.substring(0, uuid.length() - 1);
                resList.add(uuid);


            }
            r.close();
            c.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        //return result.toString();
        return resList;
    }
}
