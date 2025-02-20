package cz.incad.kramerius.indexer.fa;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.language.DefaultTemplateLexer;
import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import cz.incad.kramerius.utils.FedoraUtils;
import cz.incad.kramerius.utils.XMLUtils;

public class FedoraAccessBridge {

    public static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    
    private FedoraAccess fedoraAccess;
    
    @Inject
    public FedoraAccessBridge(@Named("rawFedoraAccess") FedoraAccess fedoraAccess) {
        super();
        this.fedoraAccess = fedoraAccess;
    }

    public byte[] getFoxml(String pid) throws IOException, ParserConfigurationException, SAXException, TransformerException {
        InputStream stream = FedoraAccessBridge.class.getResourceAsStream("res/foxml.stg");
        String string = IOUtils.toString(stream, Charset.forName("UTF-8"));
        StringTemplateGroup tmplGroup = new StringTemplateGroup(new StringReader(string), DefaultTemplateLexer.class);
        StringTemplate foxml = tmplGroup.getInstanceOf("FOXML");

        if (this.fedoraAccess.isObjectAvailable(pid)) {
            String relsExtString = IOUtils.toString(this.fedoraAccess.getDataStream(pid, FedoraUtils.RELS_EXT_STREAM), Charset.forName("UTF-8"));
            relsExtString = removeXmlInstruction(relsExtString);
            foxml.setAttribute("relsext", relsExtString);

            String biblioModsString = IOUtils.toString(this.fedoraAccess.getDataStream(pid, FedoraUtils.BIBLIO_MODS_STREAM), Charset.forName("UTF-8"));
            biblioModsString = removeXmlInstruction(biblioModsString);
            foxml.setAttribute("mods",biblioModsString);

            String dcString = IOUtils.toString(this.fedoraAccess.getDataStream(pid, FedoraUtils.DC_STREAM), Charset.forName("UTF-8"));
            dcString = removeXmlInstruction(dcString);
            foxml.setAttribute("dc",dcString);

            foxml.setAttribute("pid", pid);
            Date date = this.fedoraAccess.getStreamLastmodifiedFlag(pid, FedoraUtils.RELS_EXT_STREAM);
            foxml.setAttribute("date", SIMPLE_DATE_FORMAT.format(date));
            List<String> streamNames = new ArrayList<String>();
            List<Map<String,String>> streamsOfObject = this.fedoraAccess.getStreamsOfObject(pid);
            for (Map<String, String> map : streamsOfObject) {
                streamNames.add(map.get("dsid"));
            }
            if (streamNames.contains(FedoraUtils.IMG_FULL_STREAM)) {
                foxml.setAttribute("mimetype", this.fedoraAccess.getMimeTypeForStream(pid, FedoraUtils.IMG_FULL_STREAM));
            }
            return foxml.toString().getBytes("UTF-8");

        } else {
            throw new IOException("cannot read object '"+pid+"'");
        }
    }

    private String removeXmlInstruction(String readAsString) {
        if (readAsString.trim().startsWith("<?")) {
            int endIndex = readAsString.indexOf("?>");
            return readAsString.substring(endIndex+2);
        } else return readAsString;
    }

    public InputStream getStreamContent(String pid, String string) throws IOException {
        return this.fedoraAccess.getDataStream(pid, string);
    }
    public Document getStreamContentAsDocument(String pid, String string) throws ParserConfigurationException, SAXException, IOException {
        InputStream is = this.fedoraAccess.getDataStream(pid, string);
        return XMLUtils.parseDocument(is, true);
    }

    public String getStreamMimeType(String pid, String dsId) throws IOException {
        return this.fedoraAccess.getMimeTypeForStream(pid, dsId);
    }

    public boolean existPid(String pid) throws IOException {
        return this.fedoraAccess.isContentAccessible(pid);
    }

    public String getKrameriusModelName(String s) throws IOException {
        return this.fedoraAccess.getKrameriusModelName(s);
    }

    public byte[] getStreamContentAsArray(String pid, String dsId) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        IOUtils.copy(this.getStreamContent(pid, dsId), bos);
        return bos.toByteArray();
    }
}
