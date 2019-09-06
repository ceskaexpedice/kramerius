package cz.incad.kramerius.pdf.impl;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.antlr.stringtemplate.StringTemplate;
import org.xml.sax.SAXException;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfWriter;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.document.DocumentService;
import cz.incad.kramerius.document.model.AbstractPage;
import cz.incad.kramerius.document.model.ImagePage;
import cz.incad.kramerius.document.model.PreparedDocument;
import cz.incad.kramerius.document.model.TextPage;
import cz.incad.kramerius.imaging.ImageStreams;
import cz.incad.kramerius.pdf.SimplePDFService;
import cz.incad.kramerius.pdf.commands.ITextCommand;
import cz.incad.kramerius.pdf.commands.ITextCommands;
import cz.incad.kramerius.pdf.commands.Image;
import cz.incad.kramerius.pdf.commands.render.RenderPDF;
import cz.incad.kramerius.pdf.utils.pdf.DocumentUtils;
import cz.incad.kramerius.pdf.utils.pdf.FontMap;
import cz.incad.kramerius.security.SecurityException;
import cz.incad.kramerius.service.ResourceBundleService;
import cz.incad.kramerius.service.TextsService;
import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.imgs.KrameriusImageSupport;

public class SimplePDFServiceImpl implements SimplePDFService {

    public static final Logger LOGGER  = Logger.getLogger(SimplePDFService.class.getName());
    
    private FedoraAccess fedoraAccess;
    private Provider<Locale> localeProvider;
    private TextsService textsService;
    private ResourceBundleService resourceBundleService;
    private SolrAccess solrAccess;
    private DocumentService documentService;

    @Inject
    public SimplePDFServiceImpl(
            @Named("securedFedoraAccess") FedoraAccess fedoraAccess,
            SolrAccess solrAccess, KConfiguration configuration,
            Provider<Locale> localeProvider, TextsService textsService,
            ResourceBundleService resourceBundleService) {
        super();
        this.fedoraAccess = fedoraAccess;
        this.localeProvider = localeProvider;
        this.textsService = textsService;
        this.resourceBundleService = resourceBundleService;
        this.solrAccess = solrAccess;

    }

    

    @Override
    public void pdf(PreparedDocument rdoc, OutputStream os, FontMap fontMap) throws IOException, DocumentException {
        
        ITextCommands cmnds = null;
        try {
            String template = template(rdoc, this.fedoraAccess, this.textsService, this.localeProvider.get());

            Document doc = DocumentUtils.createDocument(rdoc);
            PdfWriter pdfWriter = PdfWriter.getInstance(doc, os);
            doc.open();

            cmnds = new ITextCommands();
            cmnds.load(XMLUtils.parseDocument(new StringReader(template)).getDocumentElement(), cmnds);

            RenderPDF render = new RenderPDF(fontMap, this.fedoraAccess);
            render.render(doc, pdfWriter, cmnds);

            doc.close();
            os.flush();

        } catch (InstantiationException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        } catch (IllegalAccessException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        } catch (ParserConfigurationException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        } catch (SAXException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        } finally {
            if (cmnds != null) {
                deleteFiles(cmnds);
            }
        }
    }


    public static void deleteFiles(ITextCommands cmds) {
        List<ITextCommand> commands = cmds.getCommands();
        for (ITextCommand iTextCommand : commands) {
            if (iTextCommand instanceof Image) {
                Image cmdImage = (Image) iTextCommand;
                String file = cmdImage.getFile();
                if (file != null) {
                    File toDelete = new File(file);
                    toDelete.delete();
                }
            }
        }
    }

    public static String template(PreparedDocument rdoc, FedoraAccess fa, TextsService textsService, Locale locale) throws IOException,
            FileNotFoundException {
        StringWriter strWriter = new StringWriter();

        String pdfHeader = textsService.getText("pdf_header",locale);
        String pdfFooter = textsService.getText("pdf_footer",locale);
        strWriter.write("<commands");
        if (pdfHeader != null) {
            strWriter.write(" page-header='"+pdfHeader+"'");
        }
        if (pdfFooter != null) {
        strWriter.write(" page-footer='"+pdfFooter+"'");
        }
        strWriter.write(">\n");

        List<AbstractPage> pages = new ArrayList<AbstractPage>(rdoc.getPages());
        for (int i = 0,ll=pages.size(); i < ll; i++) {
            AbstractPage apage = pages.get(i);
            if (apage instanceof ImagePage) {
                String pid = apage.getUuid();
                if (i > 0) {
                    strWriter.write("<pagebreak></pagebreak>");
                }
                try {

                    // image 
                    BufferedImage javaImg = KrameriusImageSupport.readImage(pid,ImageStreams.IMG_FULL.getStreamName(), fa,0);
                    String imgPath = writeImage(javaImg);
                    StringTemplate template = new StringTemplate(IOUtils.readAsString(SimplePDFServiceImpl.class.getResourceAsStream("templates/_image_page.st"), Charset.forName("UTF-8"), true));
                    template.setAttribute("imgpath", imgPath);
                    template.setAttribute("pid", pid);
                    strWriter.write(template.toString());

                } catch (XPathExpressionException e) {
                    LOGGER.log(Level.SEVERE,e.getMessage(),e);
                } catch (SecurityException e) {
                    LOGGER.log(Level.INFO,e.getMessage());
                    String text = textsService.getText("security_fail",locale);
                    text = text != null ? text : "security_fail";

                    StringTemplate template = new StringTemplate(IOUtils.readAsString(SimplePDFServiceImpl.class.getResourceAsStream("templates/_security_fail.st"), Charset.forName("UTF-8"), true));
                    template.setAttribute("securityfail", text);

                    strWriter.write(template.toString());
                }
            }
        }

        strWriter.write("\n</commands>");
        return strWriter.toString();
    }


    private static String writeImage(BufferedImage javaImg) throws IOException,
            FileNotFoundException {
        
        File tmpFile = File.createTempFile("img", "jpg");
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(tmpFile);
            ImageIO.write(javaImg, "jpg", fos);
            return tmpFile.getAbsolutePath();
        } finally {
            IOUtils.tryClose(fos);
        }
    }
    
    public static void main(String[] args) throws IOException {
        StringTemplate template = new StringTemplate(IOUtils.readAsString(SimplePDFServiceImpl.class.getResourceAsStream("templates/_image_page.st"), Charset.forName("UTF-8"), true));
        template.setAttribute("imgpath", new File("Test").getAbsolutePath());
        
        System.out.println(template.toString());
    }

    
}
