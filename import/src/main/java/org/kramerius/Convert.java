package org.kramerius;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;

import javax.xml.bind.JAXBException;

import org.xml.sax.SAXException;

import com.qbizm.kramerius.imptool.poc.Main;
import com.qbizm.kramerius.imptool.poc.valueobj.ServiceException;

import cz.incad.kramerius.utils.conf.KConfiguration;

public class Convert {

    /**
     * @param args[0] visibility (true, false)
     * @throws UnsupportedEncodingException 
     */
    public static void main(String[] args) throws InterruptedException, JAXBException, FileNotFoundException, SAXException, ServiceException, UnsupportedEncodingException {
        String convertTargetDirectory = System.getProperties().containsKey("convert.target.directory") ? System.getProperty("convert.target.directory") : KConfiguration.getInstance().getProperty("convert.target.directory") ;
        String defaultRights = System.getProperties().containsKey("convert.defaultRights") ?  System.getProperty("convert.defaultRights") : KConfiguration.getInstance().getProperty("convert.defaultRights","false") ;
        String convertDirectory =  System.getProperties().containsKey("convert.directory") ? System.getProperty("convert.directory") : KConfiguration.getInstance().getProperty("convert.directory");
        
        boolean visible = Boolean.parseBoolean(defaultRights);
        if (args.length>0){
            visible = Boolean.parseBoolean(args[0]);
        }
        String uuid = Main.convert(convertDirectory, convertTargetDirectory, false, visible, null);
        Import.ingest(KConfiguration.getInstance().getProperty("ingest.url"), KConfiguration.getInstance().getProperty("ingest.user"), KConfiguration.getInstance().getProperty("ingest.password"), convertTargetDirectory);
        /*if (!KConfiguration.getInstance().getConfiguration().getBoolean("ingest.skip",false)){
            Download.startIndexing("converted", uuid);
        }*/
    }

}
