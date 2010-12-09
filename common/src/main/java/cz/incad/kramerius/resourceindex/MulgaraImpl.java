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
    public Document getFedoraObjectsFromModelExt(String model, int limit, int offset, String orderby, String orderDir) throws Exception {
        
            Configuration config = KConfiguration.getInstance().getConfiguration();
            String query = "select $object $title $date from <#ri> " +
                    "where $object <fedora-model:hasModel> <info:fedora/model:" + model + ">  " + 
                    " and  $object <dc:title> $title " +
                    " and  $object <fedora-view:lastModifiedDate> $date " +
                    " order by  " + orderby + " " + orderDir +
                    " limit  " + limit +
                    " offset  " + offset;
            String urlStr = config.getString("FedoraResourceIndex") + "?type=tuples&flush=true&lang=itql&format=Sparql&distinct=off&stream=off" +
                    "&query=" + java.net.URLEncoder.encode(query, "UTF-8");
            java.net.URL url = new java.net.URL(urlStr);
            return XMLUtils.parseDocument(url.openStream());
    }

    @Override
    public ArrayList<String> getFedoraPidsFromModel(String model, int limit, int offset) throws Exception {
        
            Configuration config = KConfiguration.getInstance().getConfiguration();
            String query = "select $object from <#ri> " +
                    "where $object <fedora-model:hasModel> <info:fedora/model:" + model + ">  " + 
                    " order by $object" +
                    " limit  " + limit +
                    " offset  " + offset;
            ArrayList<String> resList = new ArrayList<String>();
            String urlStr = config.getString("FedoraResourceIndex") + "?type=tuples&flush=true&lang=itql&format=Sparql&distinct=off&stream=off" +
                    "&query=" + java.net.URLEncoder.encode(query, "UTF-8");
            java.net.URL url = new java.net.URL(urlStr);

            java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(url.openStream()));
            String inputLine = in.readLine();
            while ((inputLine = in.readLine()) != null) {
                resList.add(inputLine.split("/")[1]);
            }
            in.close();
            return resList;
    }

}
