/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.incad.kramerius.resourceindex;

import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import java.util.ArrayList;
import org.w3c.dom.Document;
import org.apache.commons.configuration.Configuration;

/**
 *
 * @author Alberto
 */
public class MulgaraImpl implements IResourceIndex {
    
    @Override
    public Document getVirtualCollections() throws Exception {
            Configuration config = KConfiguration.getInstance().getConfiguration();
            String query = "select $object $title $canLeave from <#ri> " +
                    " where $object <fedora-model:hasModel> <info:fedora/model:collection>  " + 
                    " and  $object <dc:title> $title " +
                    " and  $object <dc:type> $canLeave " +
                    " order by  $title ";
            String urlStr = config.getString("FedoraResourceIndex") + "?type=tuples&flush=true&lang=itql&format=Sparql&distinct=off&stream=off" +
                    "&query=" + java.net.URLEncoder.encode(query, "UTF-8");
            java.net.URL url = new java.net.URL(urlStr);
            //TODO: Zrusit 
            return XMLUtils.parseDocument(url.openStream(), true);
    }

    @Override
    public Document getFedoraObjectsFromModelExt(String model, int limit, int offset, String orderby, String orderDir) throws Exception {
        
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
    }
    
    @Override
    public Document getFedoraModels() throws Exception{
        
            Configuration config = KConfiguration.getInstance().getConfiguration();
            String query = "select $object $title from <#ri> " +
                            "where $object <fedora-model:hasModel> <info:fedora/fedora-system:ContentModel-3.0>  " +
                            "and  $object <dc:title> $title" ;
            String urlStr = config.getString("FedoraResourceIndex") + "?type=tuples&flush=true&lang=itql&format=Sparql&distinct=off&stream=off" +
                    "&query=" + java.net.URLEncoder.encode(query, "UTF-8");
            java.net.URL url = new java.net.URL(urlStr);
            //TODO: Zrusit 
            return XMLUtils.parseDocument(url.openStream(), true);
        
    }

    @Override
    public ArrayList<String> getFedoraPidsFromModel(String model, int limit, int offset) throws Exception {
        
            Configuration config = KConfiguration.getInstance().getConfiguration();
            String query = "select $object from <#ri> " +
                    "where $object <fedora-model:hasModel> <info:fedora/model:" + model + ">  " + 
                    //" order by $object" +
                    " limit  " + limit +
                    " offset  " + offset;
            ArrayList<String> resList = new ArrayList<String>();
            String urlStr = config.getString("FedoraResourceIndex") + "?type=tuples&flush=true&lang=itql&format=CSV&distinct=off&stream=off" +
                    "&query=" + java.net.URLEncoder.encode(query, "UTF-8");
            java.net.URL url = new java.net.URL(urlStr);

            java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(url.openStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                if(inputLine.indexOf("/")>0){
                    resList.add(inputLine.split("/")[1]);
                }
            }
            in.close();
            return resList;
    }

    @Override
    public ArrayList<String> getParentsPids(String pid) throws Exception{
        Configuration config = KConfiguration.getInstance().getConfiguration();
            String query = "$object * <info:fedora/" + pid + ">  ";
            ArrayList<String> resList = new ArrayList<String>();
            String urlStr = config.getString("FedoraResourceIndex") + "?type=triples&flush=true&lang=spo&format=N-Triples&limit=&distinct=off&stream=off" +
                    "&query=" + java.net.URLEncoder.encode(query, "UTF-8");
            java.net.URL url = new java.net.URL(urlStr);

            java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(url.openStream()));
            String inputLine;
            int end;
            while ((inputLine = in.readLine()) != null) {
//<info:fedora/uuid:5fe0b160-62d5-11dd-bdc7-000d606f5dc6> <http://www.nsdl.org/ontologies/relationships#hasPage> <info:fedora/uuid:75fca1f0-64b2-11dd-9fd4-000d606f5dc6> .
//<info:fedora/uuid:f0da6570-8f3b-11dd-b796-000d606f5dc6> <http://www.nsdl.org/ontologies/relationships#isOnPage> <info:fedora/uuid:75fca1f0-64b2-11dd-9fd4-000d606f5dc6> .
                end = inputLine.indexOf(">");
//13 je velikost   <info:fedora/
                inputLine = inputLine.substring(13,end);
                resList.add(inputLine);
            }
            in.close();
            return resList;
    }

    @Override
    public ArrayList<String> getPidPaths(String pid) throws Exception{

            ArrayList<String> resList = new ArrayList<String>();
            ArrayList<String> parents  = this.getParentsPids(pid);

            for(int i=0; i<parents.size(); i++){
                ArrayList<String> grands  = this.getPidPaths(parents.get(i));
                for(int j=0; j<grands.size(); j++){
                    resList.add(grands.get(j) + "/" + parents.get(i));
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
            String urlStr = config.getString("FedoraResourceIndex") + "?type=triples&flush=true&lang=spo&format=N-Triples&limit=&distinct=off&stream=off" +
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
