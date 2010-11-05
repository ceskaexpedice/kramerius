package cz.incad.kramerius.imaging.lp;

import static cz.incad.kramerius.FedoraNamespaces.RDF_NAMESPACE_URI;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import javax.xml.xpath.XPathExpressionException;

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
import cz.incad.kramerius.impl.fedora.FedoraDatabaseUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.imgs.KrameriusImageSupport;
import cz.incad.kramerius.utils.pid.LexerException;
import cz.incad.kramerius.utils.pid.PIDParser;

public class GenerateThumbnail {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(GenerateThumbnail.class.getName());

    public static void main(String[] args) throws IOException {
        System.out.println("Generate thumbnails :" + Arrays.asList(args));
        if (args.length == 1) {
            Injector injector = Guice.createInjector(new GenerateDeepZoomCacheModule(), new Fedora3Module());
            FedoraAccess fa = injector.getInstance(Key.get(FedoraAccess.class, Names.named("securedFedoraAccess")));
            Provider<Connection> conProvider = injector.getProvider(Key.get(Connection.class, Names.named("fedora3")));
            DeepZoomTileSupport tileSupport = injector.getInstance(DeepZoomTileSupport.class);
            DiscStrucutreForStore discStruct = injector.getInstance(DiscStrucutreForStore.class);
            prepareCacheForUUID(args[0], fa, discStruct, tileSupport);
        }
    }

    public static void prepareCacheForUUID(String uuid, final FedoraAccess fedoraAccess, final DiscStrucutreForStore discStruct, final DeepZoomTileSupport tileSupport) throws IOException {
        KrameriusModels krameriusModel = fedoraAccess.getKrameriusModel(uuid);
        if (krameriusModel.equals(KrameriusModels.PAGE)) {
            try {
                prepareThumbnail(uuid, fedoraAccess, discStruct, tileSupport);
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
                            LOGGER.info("caching page " + (pageIndex++));
                            prepareThumbnail(uuid, fedoraAccess, discStruct, tileSupport);
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

    public static void prepareThumbnail(String uuid, FedoraAccess fedoraAccess, DiscStrucutreForStore discStruct, DeepZoomTileSupport tileSupport) throws IOException, XPathExpressionException {
        BufferedImage scaled = scaleToFullThumb(uuid, fedoraAccess, tileSupport);

        String rootPath = KConfiguration.getInstance().getConfiguration().getString("fullThumbnail.cacheDirectory", "${sys:user.home}/.kramerius4/fullThumb");
        File cachedFile = discStruct.getUUIDFile(uuid, rootPath);

        if (!cachedFile.exists()) {
            boolean file = cachedFile.createNewFile();
            if (!file) {
                throw new IOException("cannot creeate file " + cachedFile.getAbsolutePath());
            }
        }
        FileOutputStream fos = new FileOutputStream(cachedFile);
        try {
            KrameriusImageSupport.writeImageToStream(scaled, "jpeg", fos);
        } finally {
            fos.close();
        }
    }

    public static BufferedImage scaleToFullThumb(String uuid, FedoraAccess fedoraAccess, DeepZoomTileSupport tileSupport) throws XPathExpressionException, IOException {
        BufferedImage img = KrameriusImageSupport.readImage(uuid, "IMG_FULL", fedoraAccess, 0);
        int width = img.getWidth();
        int height = img.getHeight();
        
        Dimension dim = new Dimension(img.getWidth(), img.getHeight());
        double scale = tileSupport.getClosestScale(dim,tileSupport.getTileSize());
        
        int targetWidth = (int) (width / scale);
        int targetHeight = (int) (height / scale);

        BufferedImage scaled = KrameriusImageSupport.scale(img, targetWidth, targetHeight);
        return scaled;
    }
}
