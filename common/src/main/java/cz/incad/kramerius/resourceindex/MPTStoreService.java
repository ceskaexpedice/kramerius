/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.incad.kramerius.resourceindex;

/**
 *
 * @author Alberto
 */
import cz.incad.kramerius.utils.DatabaseUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.virtualcollections.VirtualCollection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;
import java.util.logging.Level;

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
import org.apache.commons.configuration.Configuration;

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
        loadTableNames();
    }


    String Table_lastModifiedDate;
    String Table_dcTitle;
    String Table_model;

    private void loadTableNames(){
        try {
            if(Table_model==null){
                Table_dcTitle = adaptor.getTableFor(NTriplesUtil.parsePredicate(PRED_dcTitle));
                Table_lastModifiedDate = adaptor.getTableFor(NTriplesUtil.parsePredicate(PRED_lastModifiedDate));
                Table_model = adaptor.getTableFor(NTriplesUtil.parsePredicate(PRED_model));
            }
        } catch (ParseException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
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
            logger.log(Level.FINE, "USING DRIVER {0}", config.getProperty(PROP_DB_DRIVER));
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
        String mapTable = config.getProperty(PROP_PREDICATE_MAP);
        String prefix = config.getProperty(PROP_MAP_PREFIX);
        try {
            manager = new BasicTableManager(source, generator, mapTable, prefix);
        } catch (SQLException e) {
            throw new RuntimeException("Could not initialize table mapper", e);
        }
        return manager;
    }

    public String latestRecordDate() {

        String mods;
        try {
            mods = adaptor.getTableFor(NTriplesUtil.parsePredicate(PRED_lastModifiedDate));
        } catch (ParseException e) {
            /* Should never get here :) */
            throw new RuntimeException("Could not parse predicate ", e);
        }
        logger.fine("getting latest record date");
        String date;
        Connection c;
        try {
            c = dataSource.getConnection();
            try {
                PreparedStatement s = c.prepareStatement(
                        "SELECT max(o) FROM " + mods,
                        ResultSet.FETCH_FORWARD,
                        ResultSet.CONCUR_READ_ONLY);
                try {
                    ResultSet r = s.executeQuery();
                    try {
                        r.next();
                        date = r.getString(1);
                    } finally {
                        DatabaseUtils.tryClose(r);
                    }
                } finally {
                    DatabaseUtils.tryClose(s);
                }
            } finally {
                DatabaseUtils.tryClose(c);
            }
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
        String torder = Table_lastModifiedDate + ".o";
        if(orderby.equals("title")){
            torder = Table_dcTitle + ".o";
        }
        
        logger.fine("getFedoraObjectsFromModelExt");
        Document xmldoc;
        Connection c = null;
        PreparedStatement s = null;
        ResultSet r = null;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            xmldoc = builder.newDocument();
            Element root = xmldoc.createElementNS(SPARQL_NS, "sparql");
            Element results = xmldoc.createElementNS(SPARQL_NS, "results");
            xmldoc.appendChild(root);
            root.appendChild(results);
            c = dataSource.getConnection();
            String sql = "select " + Table_dcTitle + ".s, " + Table_dcTitle + ".o, " + Table_lastModifiedDate + ".o from ";
            if(orderby.equals("title")){
                sql += Table_dcTitle + "," + Table_lastModifiedDate + "," + Table_model;
            }else{
                sql += Table_lastModifiedDate + "," + Table_dcTitle + "," + Table_model;
            }
             sql += " where " + Table_model + ".o='<info:fedora/model:" + model + ">' and " + Table_dcTitle + ".s=" + Table_lastModifiedDate + ".s and " + Table_dcTitle + ".s=" + Table_model + ".s "
                    + " order by " + torder + " " + orderDir
                    + " limit " + limit + " offset " + offset;

            s = c.prepareStatement(sql,
                    ResultSet.FETCH_FORWARD,
                    ResultSet.CONCUR_READ_ONLY);
            r = s.executeQuery();
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
                uuid = r.getString(1);
                //uuid = r.getString(1).split("info:fedora/")[1];
                uuid = uuid.substring(1, uuid.length() - 1);

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
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (r != null) {
                DatabaseUtils.tryClose(r);
            }
            if (s != null) {
                DatabaseUtils.tryClose(s);
            }
            if (c != null) {
                DatabaseUtils.tryClose(c);
            }
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
        
        logger.fine("getting latest record date");
        Document xmldoc;
        Connection c = null;
        PreparedStatement s = null;
        ResultSet r = null;
        ArrayList<String> resList = new ArrayList<String>();
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            xmldoc = builder.newDocument();
            Element root = xmldoc.createElementNS(SPARQL_NS, "sparql");
            Element results = xmldoc.createElementNS(SPARQL_NS, "results");
            xmldoc.appendChild(root);
            root.appendChild(results);
            c = dataSource.getConnection();
            String sql = "select " + Table_model + ".s"
                    + " where " + Table_model + ".o='<info:fedora/model:" + model + ">' "
                    + " order by " + Table_model + ".s"
                    + " limit " + limit + " offset " + offset;

            s = c.prepareStatement(sql,
                    ResultSet.FETCH_FORWARD,
                    ResultSet.CONCUR_READ_ONLY);
            r = s.executeQuery();
            String uuid;

            while (r.next()) {
                uuid = r.getString(1).split("info:fedora/")[1];
                uuid = uuid.substring(0, uuid.length() - 1);
                resList.add(uuid);


            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (r != null) {
                DatabaseUtils.tryClose(r);
            }
            if (s != null) {
                DatabaseUtils.tryClose(s);
            }
            if (c != null) {
                DatabaseUtils.tryClose(c);
            }
        }
        //return result.toString();
        return resList;
    }

    
    //@Override
    public ArrayList<String> getModelsPath(String uuid) throws Exception {
        ArrayList<String> modelPaths = new ArrayList<String>();
        ArrayList<String> pidpaths = getPidPaths(uuid);
        for(String pidpath : pidpaths){
            String modelPath = "";
            String[] pids = pidpath.split("/");
            for(int i = 0; i<pids.length; i++){
                modelPath += getModel(pids[i]);
                if(i<pids.length-1) modelPath += "/";
            }
            modelPaths.add(uuid);
        }
        return modelPaths;
    }

    private String getModel(String uuid) throws Exception{
        /*
         * iTQL query
            select $object $model from <#ri>
            where  $object <dc:identifier>  'uuid' 
            and  $object <fedora-model:hasModel> $model
         */
        
        String model = "";
        
        logger.fine("getting latest record date");
        Connection c;
        try {
            String sql = "select o from " + Table_model
                    + " where s='<info:fedora/uuid:43101770-b03b-11dd-8673-000d606f5dc6>" + uuid + ">' "
                    + " and o <> '<info:fedora/fedora-system:FedoraObject-3.0>' ";
            c = dataSource.getConnection();
            try {
                PreparedStatement s =
                        c.prepareStatement(sql,
                        ResultSet.FETCH_FORWARD,
                        ResultSet.CONCUR_READ_ONLY);
                try {
                    ResultSet r = s.executeQuery();
                    try {
                        if (r.next()) {
                            model = r.getString(1);
                            model = model.substring("<info:fedora/model:".length(), model.length()-1);
                        }
                    } finally {
                        DatabaseUtils.tryClose(r);
                    }
                } finally {
                    DatabaseUtils.tryClose(s);
                }
            } finally {
                DatabaseUtils.tryClose(c);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        //return result.toString();
        return model;
    }
    
    @Override
    public ArrayList<String> getParentsPids(String uuid) throws Exception {

        //Can use risearch with SPO language
        
        String query = "$object * <info:fedora/" + uuid + ">  ";
        ArrayList<String> resList = new ArrayList<String>();
        String urlStr = config.getConfiguration().getString("FedoraResourceIndex") + "?type=triples&flush=true&lang=spo&format=N-Triples&limit=&distinct=off&stream=off"
                + "&query=" + java.net.URLEncoder.encode(query, "UTF-8");
        java.net.URL url = new java.net.URL(urlStr);

        java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(url.openStream()));
        String inputLine;
        int end;
        while ((inputLine = in.readLine()) != null) {
//<info:fedora/uuid:5fe0b160-62d5-11dd-bdc7-000d606f5dc6> <http://www.nsdl.org/ontologies/relationships#hasPage> <info:fedora/uuid:75fca1f0-64b2-11dd-9fd4-000d606f5dc6> .
//<info:fedora/uuid:f0da6570-8f3b-11dd-b796-000d606f5dc6> <http://www.nsdl.org/ontologies/relationships#isOnPage> <info:fedora/uuid:75fca1f0-64b2-11dd-9fd4-000d606f5dc6> .
            end = inputLine.indexOf(">");
//13 je velikost   <info:fedora/
            inputLine = inputLine.substring(13, end);
            resList.add(inputLine);
        }
        in.close();
        return resList;
    }

    @Override
    public ArrayList<String> getPidPaths(String pid) throws Exception {
        logger.info(pid);
        ArrayList<String> resList = new ArrayList<String>();
        ArrayList<String> parents = this.getParentsPids(pid);
        logger.info(parents.toString());
        for (int i = 0; i < parents.size(); i++) {
            ArrayList<String> grands = this.getPidPaths(parents.get(i));
            logger.info(grands.toString());
            if (grands.isEmpty()) {
                resList.add(parents.get(i));
            } else {
                for (int j = 0; j < grands.size(); j++) {
                    resList.add(grands.get(j) + "/" + parents.get(i));
                }
            }
        }
        return resList;
    }
    
    @Override
    public boolean existsPid(String pid) throws Exception{
        Configuration config = KConfiguration.getInstance().getConfiguration();
            String query = "<info:fedora/" + pid + "> <info:fedora/fedora-system:def/model#hasModel>  * ";
            String urlStr = config.getString("FedoraResourceIndex") + "?type=triples&flush=true&lang=spo&format=N-Triples&limit=&distinct=off&stream=off" +
                    "&query=" + java.net.URLEncoder.encode(query, "UTF-8");
            java.net.URL url = new java.net.URL(urlStr);

            java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(url.openStream()));
            String inputLine;
            if ((inputLine = in.readLine()) != null) {
                in.close();
                return true;
            }else{
                in.close();
                return false;
            }
    }

    @Override
    public ArrayList<String> getObjectsInCollection(String collection, int limit, int offset) throws Exception {
        Configuration config = KConfiguration.getInstance().getConfiguration();
        String query = "* <rdf:isMemberOfCollection>  <info:fedora/" + collection + ">  ";
            
            ArrayList<String> resList = new ArrayList<String>();
            String urlStr = config.getString("FedoraResourceIndex") + "?type=triples&flush=true&lang=spo&format=N-Triples&limit="+limit+"&distinct=off&stream=off" +
                    "&query=" + java.net.URLEncoder.encode(query, "UTF-8");
            java.net.URL url = new java.net.URL(urlStr);

            java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(url.openStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                resList.add(inputLine.substring(1, inputLine.indexOf("> <")));
            }
            in.close();
            return resList;
    }
}
