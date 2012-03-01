package org.kramerius;

import java.io.FileNotFoundException;

import javax.xml.bind.JAXBException;

import org.xml.sax.SAXException;

import com.qbizm.kramerius.imptool.poc.Main;
import com.qbizm.kramerius.imptool.poc.valueobj.ServiceException;

import cz.incad.kramerius.utils.conf.KConfiguration;

public class Convert {

    /**
     * @param args[0] visibility (true, false)
     */
    public static void main(String[] args) throws InterruptedException, JAXBException, FileNotFoundException, SAXException, ServiceException {
        boolean visible = Boolean.parseBoolean(KConfiguration.getInstance().getProperty("convert.defaultRights","false"));
        if (args.length>0){
            visible = Boolean.parseBoolean(args[0]);
        }
        String uuid = Main.convert(KConfiguration.getInstance().getProperty("convert.directory"), KConfiguration.getInstance().getProperty("convert.target.directory"), false, visible, null);
        Import.ingest(KConfiguration.getInstance().getProperty("ingest.url"), KConfiguration.getInstance().getProperty("ingest.user"), KConfiguration.getInstance().getProperty("ingest.password"), KConfiguration.getInstance().getProperty("convert.target.directory"));
        /*if (!KConfiguration.getInstance().getConfiguration().getBoolean("ingest.skip",false)){
            Download.startIndexing("converted", uuid);
        }*/
    }

}
