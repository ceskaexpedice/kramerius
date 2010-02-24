package cz.incad.Kramerius.backend.pdf.impl;

import static cz.incad.kramerius.FedoraNamespaces.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.inject.Inject;
import com.qbizm.kramerius.ext.ontheflypdf.edi.Cell;
import com.qbizm.kramerius.ext.ontheflypdf.edi.Document;
import com.qbizm.kramerius.ext.ontheflypdf.edi.EdiException;
import com.qbizm.kramerius.ext.ontheflypdf.edi.Image;
import com.qbizm.kramerius.ext.ontheflypdf.edi.Line;
import com.qbizm.kramerius.ext.ontheflypdf.edi.Page;
import com.qbizm.kramerius.ext.ontheflypdf.edi.PageLayer;
import com.qbizm.kramerius.ext.ontheflypdf.edi.Table;
import com.qbizm.kramerius.ext.ontheflypdf.edi.TextBlock;
import com.qbizm.kramerius.ext.ontheflypdf.edi.font.Font;
import com.qbizm.kramerius.ext.ontheflypdf.edi.generating.PdfDocumentGenerator;

import cz.incad.Kramerius.backend.pdf.GeneratePDFService;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.FedoraNamespaces;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.pid.LexerException;
import cz.incad.kramerius.utils.pid.PIDParser;

public class GeneratePDFServiceImpl implements GeneratePDFService {

	public static final java.util.logging.Logger LOGGER = java.util.logging.Logger
			.getLogger(GeneratePDFServiceImpl.class.getName());
	
	@Inject
	FedoraAccess fedoraAccess;
	@Inject
	KConfiguration configuration;
	
	
	@Override
	public void generatePDF(String parentUUID, List<String> uuids, OutputStream os)
			throws IOException {
		try {
			Document doc = new Document();
			if (!uuids.isEmpty()) {
				doc.addPage(createFirstPage(doc, parentUUID));
			}
			for (String uuid : uuids) {
				String url = createDJVUUrl(uuid);
				Image imgElem = new Image(url);
				imgElem.setPosition(2, 2);
				doc.addPage(new Page(imgElem));
			}
			if (doc.getPagesCount() > 0) {
				PdfDocumentGenerator pdfDocumentGenerator = new PdfDocumentGenerator(doc);
				pdfDocumentGenerator.setDisector(new KrameriusImageFormatDisector());
				pdfDocumentGenerator.generateDocument(os);
			}
		} catch (EdiException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}
		
	}

	private Font getDocumentFont() {
        String fontFile = "resources"
            + "/"
            + "ext_ontheflypdf_ArialCE.ttf";
        
        String fontFilePath = getClass().getClassLoader()
            .getResource(fontFile).getPath();
        return new Font(fontFilePath);
	}

	private Page createFirstPage(Document doc, String uuid) throws IOException {
		org.w3c.dom.Document biblioMods = this.fedoraAccess.getDC(uuid);
		Element root = biblioMods.getDocumentElement();
		Map stModel = prepareDCModel(root);
		StringTemplateGroup grp = new StringTemplateGroup("pdf-metadata");
		StringTemplate stMetadata = grp.getInstanceOf("cz/incad/Kramerius/backend/pdf/impl/metadata");
		stMetadata.setAttribute("dc", stModel);
		String metadata = stMetadata.toString();

		StringTemplate stDescription = grp.getInstanceOf("cz/incad/Kramerius/backend/pdf/impl/description");
		String description = stDescription.toString();

        PageLayer layer = new PageLayer();

        //line
        int lineX = 5;
        int lineY = 140;
        Line line = new Line(lineX, doc.getHeight() - lineY,
                doc.getWidth() - lineX, doc.getHeight() - lineY);
        layer.addElement(line);
        
//        //logo image
//        String logoFile = "resources"
//                + "/" 
//                + getProperties().getProperty("firstpage.logo.image");
//        int logoX = Integer.parseInt(getProperties().getProperty("firstpage.logo.x"));
//        int logoY = Integer.parseInt(getProperties().getProperty("firstpage.logo.y"));
//        
//        if(logoFile != null) {
//            String logoFilePath = getClass().getClassLoader()
//                .getResource(logoFile).getPath();
//            
//            if(new File(logoFilePath).exists()) {
//                elem = new Image(logoFilePath);
//                elem.setPosition(logoX, logoY);
//                layer.addElement(elem);
//            }
//        }
        
        
        //licence condreitions text

        TextBlock txtBlock = new TextBlock(description, getDocumentFont());
        Table table = new Table(1);
        Cell cell = new Cell(txtBlock);
        int paddingX = 5;
        int paddingY = 25;
        table.setWidth(doc.getWidth() - (2*lineX) - paddingX);
        table.setBorderWidth(0f);
        table.setPosition(lineX + paddingX, doc.getHeight() - lineY - paddingY);
        table.addCell(cell);
        layer.addElement(table);
        
        //put metadata to the right position
        Table tab = new Table(1);
        cell = new Cell(new TextBlock(metadata,getDocumentFont()));
        tab.addCell(cell);
        tab.setWidth(200);
        tab.setBorderWidth(0f);
        layer.addElement(tab);
        
        //return complete first page for the periodical item
        return new Page(layer);
	}


	private Map prepareDCModel(Element root) {
		Map stModel = new HashMap();
		NodeList nlist = root.getChildNodes();
		for (int i = 0,ll=nlist.getLength(); i < ll; i++) {
			Node item = nlist.item(i);
			if (item.getNodeType() == Node.ELEMENT_NODE) {
				if (item.getNamespaceURI().equals(FedoraNamespaces.DC_NAMESPACE_URI)) {
					String name = item.getLocalName();
					String value = item.getTextContent();
					if (stModel.containsKey(name)) {
						Object obj = stModel.get(name);
						List<String> vals = null;
						if (obj instanceof String) {
							vals = new ArrayList<String>();
							vals.add(obj.toString());
							stModel.put(name, vals);
						} else {
							vals = (List<String>) obj;
						}
						vals.add(value);
					} else {
						stModel.put(name, value);
					}
				}
			}
		}
		return stModel;
	}


	private String createThumbUrl(String objectId) {
		String imgUrl = this.configuration.getThumbServletUrl() +"?uuid="+objectId+"&scale=1.0&rawdata=true";
		return imgUrl;
	}

	private String createDJVUUrl(String objectId) {
		String imgUrl = this.configuration.getDJVUServletUrl() +"?uuid="+objectId+"";
		return imgUrl;
	}


	public FedoraAccess getFedoraAccess() {
		return fedoraAccess;
	}

	public void setFedoraAccess(FedoraAccess fedoraAccess) {
		this.fedoraAccess = fedoraAccess;
	}

	public KConfiguration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(KConfiguration configuration) {
		this.configuration = configuration;
	}

	
}
