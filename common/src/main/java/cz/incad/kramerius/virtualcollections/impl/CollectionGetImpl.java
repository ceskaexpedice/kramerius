package cz.incad.kramerius.virtualcollections.impl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;

import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.RESTHelper;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.virtualcollections.CollectionGet;

public class CollectionGetImpl implements CollectionGet {
    
    public static Logger LOGGER = Logger.getLogger(CollectionGetImpl.class.getName());

    
    @Override
    public JSONArray collections() {
        try {
            String apipoint = KConfiguration.getInstance().getConfiguration().getString("api.point");
            String loc = apipoint+ (apipoint.endsWith("/") ? "" : "/") +"vc";
            InputStream inputStream = RESTHelper.inputStream(loc, "", "");
            String string = IOUtils.readAsString(inputStream, Charset.forName("UTF-8"), true);
            JSONArray jsonArray = new JSONArray(string);
            return jsonArray;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
        return null;
    }

}
