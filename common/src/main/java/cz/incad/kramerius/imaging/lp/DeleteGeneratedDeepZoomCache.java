package cz.incad.kramerius.imaging.lp;

import static cz.incad.kramerius.FedoraNamespaces.RDF_NAMESPACE_URI;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.util.Arrays;

import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.name.Names;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.FedoraRelationship;
import cz.incad.kramerius.KrameriusModels;
import cz.incad.kramerius.RelsExtHandler;
import cz.incad.kramerius.imaging.DeepZoomCacheService;
import cz.incad.kramerius.imaging.DeepZoomTileSupport;
import cz.incad.kramerius.imaging.DiscStrucutreForStore;
import cz.incad.kramerius.imaging.lp.guice.Fedora3Module;
import cz.incad.kramerius.imaging.lp.guice.GenerateDeepZoomCacheModule;
import cz.incad.kramerius.imaging.lp.guice.PlainModule;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.imgs.KrameriusImageSupport;
import cz.incad.kramerius.utils.pid.LexerException;
import cz.incad.kramerius.utils.pid.PIDParser;

public class DeleteGeneratedDeepZoomCache {


    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(GenerateThumbnail.class.getName());

    public static void main(String[] args) throws IOException {
        System.out.println("Delete deepZoomCache :" + Arrays.asList(args));
        if (args.length == 1) {
            Injector injector = Guice.createInjector(new GenerateDeepZoomCacheModule(), new Fedora3Module());
            FedoraAccess fa = injector.getInstance(Key.get(FedoraAccess.class, Names.named("securedFedoraAccess")));
            DiscStrucutreForStore discStruct = injector.getInstance(DiscStrucutreForStore.class);
            deleteCacheForUUID(args[0], fa, discStruct);
        }
    }

    public static void deleteCacheForUUID(String uuid, final FedoraAccess fedoraAccess, final DiscStrucutreForStore discStruct) throws IOException {
        KrameriusModels krameriusModel = fedoraAccess.getKrameriusModel(uuid);
        if (krameriusModel.equals(KrameriusModels.PAGE)) {
            try {
                deleteFolder(uuid, discStruct);
            } catch (XPathExpressionException e) {
                LOGGER.severe(e.getMessage());
            }
        } else {
            fedoraAccess.processRelsExt(uuid, new RelsExtHandler() {

                private int pageIndex = 1;

                @Override
                public void handle(Element elm, FedoraRelationship relation, int level) {
                    if (relation.equals(FedoraRelationship.hasPage)) {
                        try {
                            String pid = elm.getAttributeNS(RDF_NAMESPACE_URI, "resource");
                            PIDParser pidParse = new PIDParser(pid);
                            pidParse.disseminationURI();
                            String uuid = pidParse.getObjectId();
                            LOGGER.info("Deleting " + (pageIndex++) +" uuid = "+uuid);
                            deleteFolder(uuid, discStruct);
                        } catch (DOMException e) {
                            LOGGER.severe(e.getMessage());
                        } catch (LexerException e) {
                            LOGGER.severe(e.getMessage());
                        } catch (XPathExpressionException e) {
                            LOGGER.severe(e.getMessage());
                        } catch (IOException e) {
                            LOGGER.severe(e.getMessage());
                        }
                    }
                }

                @Override
                public boolean breakProcess() {
                    return false;
                }

                @Override
                public boolean accept(FedoraRelationship relation) {
                    return relation.name().startsWith("has");
                }
            });
        }

    }

    private static void deleteFolder(String uuid,DiscStrucutreForStore discStruct) throws IOException, XPathExpressionException {
        File uuidFolder = discStruct.getUUIDFile(uuid, KConfiguration.getInstance().getDeepZoomCacheDir());
        FileUtils.deleteDirectory(uuidFolder);
    }

}
