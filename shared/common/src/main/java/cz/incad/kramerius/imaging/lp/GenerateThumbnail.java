package cz.incad.kramerius.imaging.lp;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.concurrent.locks.Lock;
import java.util.logging.Level;

import javax.xml.xpath.XPathExpressionException;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;

import com.google.inject.name.Names;
import cz.incad.kramerius.fedora.RepoModule;
import cz.incad.kramerius.imaging.DeepZoomTileSupport;
import cz.incad.kramerius.imaging.DiscStrucutreForStore;
import cz.incad.kramerius.imaging.lp.guice.Fedora3Module;
import cz.incad.kramerius.imaging.lp.guice.GenerateDeepZoomCacheModule;
import cz.incad.kramerius.security.SecuredAkubraRepository;
import cz.incad.kramerius.statistics.NullStatisticsModule;
import cz.incad.kramerius.utils.FedoraUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.imgs.KrameriusImageSupport;
import cz.incad.kramerius.utils.imgs.KrameriusImageSupport.ScalingMethod;
import cz.incad.kramerius.utils.pid.LexerException;
import cz.incad.kramerius.utils.pid.PIDParser;
import org.ceskaexpedice.akubra.AkubraRepository;
import org.ceskaexpedice.akubra.KnownDatastreams;
import org.ceskaexpedice.akubra.RepositoryException;
import org.ceskaexpedice.akubra.relsext.TreeNodeProcessor;

public class GenerateThumbnail {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(GenerateThumbnail.class.getName());

    public static void main(String[] args) throws IOException {
        System.out.println("Generate thumbnails :" + Arrays.asList(args));
        if (args.length == 1) {
            Injector injector = Guice.createInjector(new GenerateDeepZoomCacheModule(), new RepoModule(), new Fedora3Module(), new NullStatisticsModule());
            AkubraRepository akubraRepository = injector.getInstance(Key.get(SecuredAkubraRepository.class));
            DeepZoomTileSupport tileSupport = injector.getInstance(DeepZoomTileSupport.class);
            DiscStrucutreForStore discStruct = injector.getInstance(DiscStrucutreForStore.class);
            prepareCacheForUUID(args[0], akubraRepository, discStruct, tileSupport);
        }
    }

    public static void prepareCacheForUUID(String pid, final AkubraRepository akubraRepository, final DiscStrucutreForStore discStruct, final DeepZoomTileSupport tileSupport) throws IOException {
        if (akubraRepository.datastreamExists(pid, KnownDatastreams.IMG_FULL)) {
            try {
                prepareThumbnail(pid, akubraRepository, discStruct, tileSupport);
            } catch (XPathExpressionException e) {
                LOGGER.severe(e.getMessage());
            } catch (LexerException e) {
                LOGGER.severe(e.getMessage());
            } catch (RepositoryException e) {
                LOGGER.severe(e.getMessage());
            }
        } else {
            try {
                akubraRepository.re().processInTree(pid, new TreeNodeProcessor() {
                    
                    @Override
                    public void process(String pid, int level) {
                        try {
                            if (akubraRepository.datastreamExists(pid, KnownDatastreams.IMG_FULL)) {
                                prepareThumbnail(pid, akubraRepository, discStruct, tileSupport);
                            }
                        } catch (XPathExpressionException e) {
                            LOGGER.log(Level.SEVERE, e.getMessage(),e);
                        } catch (IOException e) {
                            LOGGER.log(Level.SEVERE, e.getMessage(),e);
                        } catch (LexerException e) {
                            LOGGER.log(Level.SEVERE, e.getMessage(),e);
                        } catch (RepositoryException e) {
                            LOGGER.log(Level.SEVERE, e.getMessage(),e);
                        }
                    }
                    
                    @Override
                    public boolean skipBranch(String pid, int level) {
                        return false;
                    }


                    @Override
                    public boolean breakProcessing(String pid, int level) {
                        // TODO Auto-generated method stub
                        return false;
                    }
                });
            } catch (RepositoryException e1) {
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

    public static void prepareThumbnail(String pid, AkubraRepository akubraRepository, DiscStrucutreForStore discStruct, DeepZoomTileSupport tileSupport) throws IOException, XPathExpressionException, LexerException, RepositoryException {
        PIDParser pidParser = new PIDParser(pid);
        pidParser.objectPid();
        String uuid = pidParser.getObjectId();

        BufferedImage scaled = scaleToFullThumb(pid, akubraRepository, tileSupport);
        if (scaled != null) {
            File tmpFile = File.createTempFile("img_preview", ""+System.currentTimeMillis());
            tmpFile.deleteOnExit();
            FileOutputStream fos = new FileOutputStream(tmpFile);
            try {
                KrameriusImageSupport.writeImageToStream(scaled, "jpeg", fos);
                akubraRepository.doWithWriteLock(pid, () -> {
                    if (akubraRepository.datastreamExists(pid, KnownDatastreams.IMG_PREVIEW)) {
                        LOGGER.info("Purge previous IMG_PREVIEW datastream ... for pid " + pid);
                        akubraRepository.deleteDatastream(pid, KnownDatastreams.IMG_PREVIEW);
                    }
                    LOGGER.info("Adding new IMG_PREVIEW datastream ... for pid " + pid);
                    try {
                        akubraRepository.createManagedDatastream(pid, KnownDatastreams.IMG_PREVIEW, "image/jpeg", new FileInputStream(tmpFile));
                    } catch (FileNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                    return null;
                });
            } finally {
                fos.close();
                tmpFile.delete();
            }
        }

    }

    public static BufferedImage scaleToFullThumb(String pid, AkubraRepository akubraRepository, DeepZoomTileSupport tileSupport) throws XPathExpressionException, IOException {
        BufferedImage img = KrameriusImageSupport.readImage(pid, FedoraUtils.IMG_FULL_STREAM, akubraRepository, 0);
        if (img != null) {
            Dimension dim = new Dimension(img.getWidth(), img.getHeight());
            return scaleByHeight(img, new Rectangle(dim), KConfiguration.getInstance().getConfiguration().getInt("preview.height",700), null);
        } else {
            LOGGER.severe("skipping image "+pid);
            return null;
        }
    }
    
    public static BufferedImage scaleByHeight(BufferedImage img, Rectangle pageBounds, int height, ScalingMethod scalingMethod) {
        if (scalingMethod == null) scalingMethod = ScalingMethod.BILINEAR;
        int nHeight = height;
        double div = (double)pageBounds.getHeight() / (double)nHeight;
        double nWidth = (double)pageBounds.getWidth() / div;
        BufferedImage scaledImage = KrameriusImageSupport.scale(img, (int)nWidth, nHeight, scalingMethod, false);
        return scaledImage;
    }

}
