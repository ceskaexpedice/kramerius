package cz.incad.kramerius.imaging.lp;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.util.Arrays;
import java.util.logging.Level;

import javax.xml.xpath.XPathExpressionException;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.name.Names;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.ProcessSubtreeException;
import cz.incad.kramerius.imaging.DeepZoomTileSupport;
import cz.incad.kramerius.imaging.DiscStrucutreForStore;
import cz.incad.kramerius.imaging.lp.guice.Fedora3Module;
import cz.incad.kramerius.imaging.lp.guice.GenerateDeepZoomCacheModule;
import cz.incad.kramerius.impl.AbstractTreeNodeProcessorAdapter;
import cz.incad.kramerius.pdf.impl.OutputStreams;
import cz.incad.kramerius.utils.FedoraUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.imgs.KrameriusImageSupport;
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
        if (fedoraAccess.isImageFULLAvailable(uuid)) {
            try {
                prepareThumbnail(uuid, fedoraAccess, discStruct, tileSupport);
            } catch (XPathExpressionException e) {
                LOGGER.severe(e.getMessage());
            }
        } else {
            try {
                fedoraAccess.processSubtree(PIDParser.UUID_PREFIX+uuid, new AbstractTreeNodeProcessorAdapter() {
                    
                    @Override
                    public void processUuid(String pageUuid, int level) throws ProcessSubtreeException {
                        try {
                            if (fedoraAccess.isImageFULLAvailable(pageUuid)) {
                                prepareThumbnail(pageUuid, fedoraAccess, discStruct, tileSupport);
                            }
                        } catch (XPathExpressionException e) {
                            LOGGER.log(Level.SEVERE, e.getMessage(),e);
                        } catch (IOException e) {
                            LOGGER.log(Level.SEVERE, e.getMessage(),e);
                        }
                    }
                });
            } catch (ProcessSubtreeException e1) {
                LOGGER.log(Level.SEVERE,e1.getMessage(),e1);
            }

            /*
            fedoraAccess.processRelsExt(uuid, new RelsExtHandler() {
                private int pageIndex = 1;
                @Override
                public void handle(Element elm, FedoraRelationship relation, String relationshipName, int level) {
                    if (relation.name().startsWith("has")) {
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
                public boolean accept(FedoraRelationship relation, String relationShipName) {
                    return relation.name().startsWith("has");
                }
            });
            */
        }
    }

    public static void prepareThumbnail(String uuid, FedoraAccess fedoraAccess, DiscStrucutreForStore discStruct, DeepZoomTileSupport tileSupport) throws IOException, XPathExpressionException {
        if (!fedoraAccess.isFullthumbnailAvailable(uuid)) {
            BufferedImage scaled = scaleToFullThumb(uuid, fedoraAccess, tileSupport);

            File tmpFile = File.createTempFile("img_preview", ""+System.currentTimeMillis());
            FileOutputStream fos = new FileOutputStream(tmpFile);
            try {
                KrameriusImageSupport.writeImageToStream(scaled, "jpeg", fos);
                // vytvori novy ds
                fedoraAccess.getAPIM().addDatastream("uuid:"+uuid, FedoraUtils.IMG_PREVIEW_STREAM, null, null, false, "image/jpeg", null, 
                        tmpFile.toURI().toString(), "M", "A", "MD5", null, "noLog");
                
            } finally {
                fos.close();
            }

        } else {
            LOGGER.info(" for '"+uuid+"' is not necessary generate full thumbnail");
        }
    }

    public static BufferedImage scaleToFullThumb(String uuid, FedoraAccess fedoraAccess, DeepZoomTileSupport tileSupport) throws XPathExpressionException, IOException {
        BufferedImage img = KrameriusImageSupport.readImage(uuid, FedoraUtils.IMG_FULL_STREAM, fedoraAccess, 0);
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
