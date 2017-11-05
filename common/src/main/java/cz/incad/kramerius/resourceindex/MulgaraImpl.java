/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.incad.kramerius.resourceindex;

import cz.incad.kramerius.FedoraNamespaceContext;
import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.apache.commons.configuration.Configuration;

/**
 *
 * @author Alberto
 *
 */
@Deprecated
public class MulgaraImpl implements IResourceIndex {

    @Override
    public List<Map<String, String>> getSubjects(String pid) throws ResourceIndexException {
        return null;
    }

    @Override
    public Document getVirtualCollections() throws ResourceIndexException {
        try {
            Configuration config = KConfiguration.getInstance().getConfiguration();
            String query = "select $object $title $canLeave from <#ri> "
                    + " where $object <fedora-model:hasModel> <info:fedora/model:collection>  "
                    + " and  $object <dc:title> $title " + " and  $object <dc:type> $canLeave " + " order by  $title ";
            String urlStr = config.getString("FedoraResourceIndex")
                    + "?type=tuples&flush=true&lang=itql&format=Sparql&distinct=off&stream=off" + "&query="
                    + java.net.URLEncoder.encode(query, "UTF-8");
            java.net.URL url = new java.net.URL(urlStr);
            // TODO: Zrusit
            return XMLUtils.parseDocument(url.openStream(), true);
        } catch (UnsupportedEncodingException e) {
            throw new UnsupportedOperationException(e);
        } catch (MalformedURLException e) {
            throw new UnsupportedOperationException(e);
        } catch (ParserConfigurationException e) {
            throw new UnsupportedOperationException(e);
        } catch (SAXException e) {
            throw new UnsupportedOperationException(e);
        } catch (IOException e) {
            throw new UnsupportedOperationException(e);
        }
    }

    //@Override
    public Document getFedoraObjectsFromModelExt(String model, int limit, int offset, String orderby, String orderDir)
            throws ResourceIndexException {
        try {
            Configuration config = KConfiguration.getInstance().getConfiguration();
            String query = "select $object $title $date from <#ri> " +
                    " where $object <fedora-model:hasModel> <info:fedora/model:" + model + ">  " + 
                    " and  $object <dc:title> $title " +
                    " and  $object <fedora-view:lastModifiedDate> $date ";
            if(orderby != null){
                query += " order by  $" + orderby + " " + orderDir;
            }
            query += " limit  " + limit + " offset  " + offset;
            String urlStr = config.getString("FedoraResourceIndex") + "?type=tuples&flush=true&lang=itql&format=Sparql&distinct=off&stream=off" +
                    "&query=" + java.net.URLEncoder.encode(query, "UTF-8");
            java.net.URL url = new java.net.URL(urlStr);
            //TODO: Zrusit 
            return XMLUtils.parseDocument(url.openStream(), true);
        } catch (UnsupportedEncodingException e) {
            throw new ResourceIndexException(e);
        } catch (MalformedURLException e) {
            throw new ResourceIndexException(e);
        } catch (ParserConfigurationException e) {
            throw new ResourceIndexException(e);
        } catch (SAXException e) {
            throw new ResourceIndexException(e);
        } catch (IOException e) {
            throw new ResourceIndexException(e);
        }
    }

    @Override
    public List<String> getObjectsByModel(String model, int limit, int offset, String orderby, String orderDir)
            throws ResourceIndexException {
        try {
            Configuration config = KConfiguration.getInstance().getConfiguration();
            String query = "select $object $title $date from <#ri> "
                    + " where $object <fedora-model:hasModel> <info:fedora/model:" + model + ">  "
                    + " and  $object <dc:title> $title " + " and  $object <fedora-view:lastModifiedDate> $date ";
            if (orderby != null) {
                query += " order by  $" + orderby + " " + orderDir;
            }
            query += " limit  " + limit + " offset  " + offset;
            String urlStr = config.getString("FedoraResourceIndex")
                    + "?type=tuples&flush=true&lang=itql&format=Sparql&distinct=off&stream=off" + "&query="
                    + java.net.URLEncoder.encode(query, "UTF-8");
            java.net.URL url = new java.net.URL(urlStr);
            return SPARQLUtils.sparqlResults(XMLUtils.parseDocument(url.openStream(), true));
        } catch (UnsupportedEncodingException e) {
            throw new ResourceIndexException(e);
        } catch (MalformedURLException e) {
            throw new ResourceIndexException(e);
        } catch (XPathExpressionException e) {
            throw new ResourceIndexException(e);
        } catch (ParserConfigurationException e) {
            throw new ResourceIndexException(e);
        } catch (SAXException e) {
            throw new ResourceIndexException(e);
        } catch (IOException e) {
            throw new ResourceIndexException(e);
        }
    }

    @Override
    public Document getFedoraModels() throws ResourceIndexException {

//         Configuration config =
//         KConfiguration.getInstance().getConfiguration();
//         String query = "select $object $title from <#ri> " +
//         "where $object <fedora-model:hasModel>
//         <info:fedora/fedora-system:ContentModel-3.0> " +
//         "and $object <dc:title> $title" ;
//         String urlStr = config.getString("FedoraResourceIndex") +
//         "?type=tuples&flush=true&lang=itql&format=Sparql&distinct=off&stream=off"
//         +
//         "&query=" + java.net.URLEncoder.encode(query, "UTF-8");
//         java.net.URL url = new java.net.URL(urlStr);
//         //TODO: Zrusit
//         return XMLUtils.parseDocument(url.openStream(), true);
         
         try {
            Configuration config = KConfiguration.getInstance().getConfiguration();
             String query = "select $object $title from <#ri> " +
                             "where $object <fedora-model:hasModel> <info:fedora/fedora-system:ContentModel-3.0>  " +
                             "and  $object <dc:title> $title" ;
             String urlStr = config.getString("FedoraResourceIndex") + "?type=tuples&flush=true&lang=itql&format=Sparql&distinct=off&stream=off" +
                     "&query=" + java.net.URLEncoder.encode(query, "UTF-8");
             java.net.URL url = new java.net.URL(urlStr);
             //TODO: Zrusit 
             return XMLUtils.parseDocument(url.openStream(), true);
        } catch (UnsupportedEncodingException e) {
            throw new ResourceIndexException(e);
        } catch (MalformedURLException e) {
            throw new ResourceIndexException(e);
        } catch (ParserConfigurationException e) {
            throw new ResourceIndexException(e);
        } catch (SAXException e) {
            throw new ResourceIndexException(e);
        } catch (IOException e) {
            throw new ResourceIndexException(e);
        }
    }

    @Override
    public List<String> getFedoraPidsFromModel(String model, int limit, int offset) throws ResourceIndexException {
        BufferedReader in = null;
        try {
            Configuration config = KConfiguration.getInstance().getConfiguration();
            String query = "select $object from <#ri> " + "where $object <fedora-model:hasModel> <info:fedora/model:"
                    + model + ">  " +
                    // " order by $object" +
                    " limit  " + limit + " offset  " + offset;
            ArrayList<String> resList = new ArrayList<String>();
            String urlStr = config.getString("FedoraResourceIndex")
                    + "?type=tuples&flush=true&lang=itql&format=CSV&distinct=off&stream=off" + "&query="
                    + java.net.URLEncoder.encode(query, "UTF-8");
            java.net.URL url = new java.net.URL(urlStr);

            in = new BufferedReader(new java.io.InputStreamReader(url.openStream()));
            String inputLine = null;
            while ((inputLine = in.readLine()) != null) {
                if (inputLine.indexOf("/") > 0) {
                    resList.add(inputLine.split("/")[1]);
                }
            }
            return resList;
        } catch (UnsupportedEncodingException e) {
            throw new ResourceIndexException(e);
        } catch (MalformedURLException e) {
            throw new ResourceIndexException(e);
        } catch (IOException e) {
            throw new ResourceIndexException(e);
        } finally {
            IOUtils.tryClose(in);
        }
    }

    @Override
    public List<String> getParentsPids(String pid) throws ResourceIndexException {
        java.io.BufferedReader in = null;

        try {
            Configuration config = KConfiguration.getInstance().getConfiguration();
            String query = "$object * <info:fedora/" + pid + ">  ";
            ArrayList<String> resList = new ArrayList<String>();
            String urlStr = config.getString("FedoraResourceIndex")
                    + "?type=triples&flush=true&lang=spo&format=N-Triples&limit=&distinct=off&stream=off" + "&query="
                    + java.net.URLEncoder.encode(query, "UTF-8");
            java.net.URL url = new java.net.URL(urlStr);

            in = new java.io.BufferedReader(new java.io.InputStreamReader(url.openStream()));
            String inputLine;
            int end;
            while ((inputLine = in.readLine()) != null) {
                // <info:fedora/uuid:5fe0b160-62d5-11dd-bdc7-000d606f5dc6>
                // <http://www.nsdl.org/ontologies/relationships#hasPage>
                // <info:fedora/uuid:75fca1f0-64b2-11dd-9fd4-000d606f5dc6> .
                // <info:fedora/uuid:f0da6570-8f3b-11dd-b796-000d606f5dc6>
                // <http://www.nsdl.org/ontologies/relationships#isOnPage>
                // <info:fedora/uuid:75fca1f0-64b2-11dd-9fd4-000d606f5dc6> .
                end = inputLine.indexOf(">");
                // 13 je velikost <info:fedora/
                inputLine = inputLine.substring(13, end);
                resList.add(inputLine);
            }
            return resList;

        } catch (UnsupportedEncodingException e) {
            throw new ResourceIndexException(e);
        } catch (IOException e) {
            throw new ResourceIndexException(e);
        } finally {
            IOUtils.tryClose(in);
        }

    }

    @Override
    public List<String> getPidPaths(String pid) throws ResourceIndexException {
        return ResourceIndexUtils.getPidPaths(pid, this);
    }

    @Override
    public boolean existsPid(String pid) throws ResourceIndexException {
        BufferedReader in = null;
        try {
            Configuration config = KConfiguration.getInstance().getConfiguration();
            String query = "<info:fedora/" + pid + "> <info:fedora/fedora-system:def/model#hasModel>  * ";
            String urlStr = config.getString("FedoraResourceIndex")
                    + "?type=triples&flush=true&lang=spo&format=N-Triples&limit=&distinct=off&stream=off" + "&query="
                    + java.net.URLEncoder.encode(query, "UTF-8");
            java.net.URL url = new java.net.URL(urlStr);

            in = new BufferedReader(new java.io.InputStreamReader(url.openStream()));
            return in.readLine() != null;

        } catch (UnsupportedEncodingException e) {
            throw new ResourceIndexException(e);
        } catch (MalformedURLException e) {
            throw new ResourceIndexException(e);
        } catch (IOException e) {
            throw new ResourceIndexException(e);
        } finally {
            IOUtils.tryClose(in);
        }
    }

    @Override
    public List<String> getObjectsInCollection(String collection, int limit, int offset) throws ResourceIndexException {
        BufferedReader in = null;
        try {
            Configuration config = KConfiguration.getInstance().getConfiguration();
            String query = "* <rdf:isMemberOfCollection>  <info:fedora/" + collection + ">  ";

            ArrayList<String> resList = new ArrayList<String>();
            String urlStr = config.getString("FedoraResourceIndex")
                    + "?type=triples&flush=true&lang=spo&format=N-Triples&limit=&distinct=off&stream=off" + "&query="
                    + java.net.URLEncoder.encode(query, "UTF-8");
            java.net.URL url = new java.net.URL(urlStr);

            in = new java.io.BufferedReader(new java.io.InputStreamReader(url.openStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                resList.add(inputLine.substring(1, inputLine.indexOf("> <")));
            }
            return resList;

        } catch (IOException e) {
            throw new ResourceIndexException(e);
        } finally {
            IOUtils.tryClose(in);
        }
    }

}
