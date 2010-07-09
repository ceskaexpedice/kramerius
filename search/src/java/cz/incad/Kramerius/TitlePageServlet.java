package cz.incad.Kramerius;

import static cz.incad.kramerius.FedoraNamespaces.RDF_NAMESPACE_URI;
import static cz.incad.utils.IKeys.UUID_PARAMETER;

import java.io.IOException;
import java.util.logging.Level;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import cz.incad.Kramerius.backend.guice.GuiceServlet;
import cz.incad.Kramerius.views.ApplicationURL;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.FedoraNamespaces;
import cz.incad.kramerius.FedoraRelationship;
import cz.incad.kramerius.KrameriusModels;
import cz.incad.kramerius.RelsExtHandler;
import cz.incad.kramerius.intconfig.InternalConfiguration;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.pid.LexerException;
import cz.incad.kramerius.utils.pid.PIDParser;

public class TitlePageServlet extends GuiceServlet {


	public static final java.util.logging.Logger LOGGER = java.util.logging.Logger
			.getLogger(TitlePageServlet.class.getName());
	
	@Inject
	@Named("securedFedoraAccess")
	private FedoraAccess fedoraAccess;
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		String uuid = req.getParameter(UUID_PARAMETER);
		Document relsExt = fedoraAccess.getRelsExt(uuid);
		KrameriusModels krameriusModel = fedoraAccess.getKrameriusModel(relsExt);
		if (krameriusModel.equals(KrameriusModels.PAGE)) {
			redirectToPage(req, resp,uuid);
		} else {
			FindTitlePage ftp = new FindTitlePage();
			fedoraAccess.processRelsExt(relsExt,  ftp);
			String titlePageUUID = ftp.getTitlePageUUID();
			redirectToPage(req, resp, titlePageUUID);
		}
	}

	private void redirectToPage(HttpServletRequest req, HttpServletResponse resp, String uuid) throws IOException {
		String thumbServlet = ThumbnailImageServlet.thumbImageServlet(req);
		resp.sendRedirect(thumbServlet+"?uuid="+uuid+"&outputFormat=RAW");
	}
	
	
	class FindTitlePage implements RelsExtHandler {

		String titlePage = null;
		boolean breakProcess = false;
		private int previousLevel = -1;
		
		@Override
		public void handle(Element elm, FedoraRelationship relation, int level) {
			try {
				if (relation == FedoraRelationship.hasPage) {
					String pid = elm.getAttributeNS(RDF_NAMESPACE_URI, "resource");
					PIDParser pidParse = new PIDParser(pid);
					pidParse.disseminationURI();
					String objectId = pidParse.getObjectId();
					Document biblioMods = fedoraAccess.getBiblioMods(objectId);

					Element part = XMLUtils.findElement(biblioMods.getDocumentElement(), "part", FedoraNamespaces.BIBILO_MODS_URI);
					String attribute = part.getAttribute("type");
					LOGGER.info("type '"+attribute+"'");
					if ("TitlePage".equals(attribute)) {
						breakProcess = true;
						titlePage = objectId;
					} else {
						if (titlePage  ==  null) {
							titlePage = objectId;
						}
					}
					if ((previousLevel != -1) && (previousLevel > level)) {
						breakProcess = true;
					}
					previousLevel = level;
			
				}
			} catch (DOMException e) {
				LOGGER.log(Level.SEVERE, e.getMessage(), e);
			} catch (LexerException e) {
				LOGGER.log(Level.SEVERE, e.getMessage(), e);
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, e.getMessage(), e);
			}
		}
		
		@Override
		public boolean accept(FedoraRelationship relation) {
			return relation.name().startsWith("has");
		}

		public String getTitlePageUUID() {
			return titlePage;
		}

		@Override
		public boolean breakProcess() {
			return breakProcess;
		}

		
	}
}
