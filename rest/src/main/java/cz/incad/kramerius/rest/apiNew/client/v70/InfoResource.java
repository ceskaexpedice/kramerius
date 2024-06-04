package cz.incad.kramerius.rest.apiNew.client.v70;

import com.google.inject.Inject;
import cz.incad.kramerius.rest.apiNew.exceptions.InternalErrorException;
import cz.incad.kramerius.service.ResourceBundleService;
import cz.incad.kramerius.service.TextsService;
import cz.incad.kramerius.utils.StringUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.kramerius.searchIndex.indexer.execution.Indexer;
import org.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Resource providing basic info:
 * <ul>
 *   <li>version: Kramerius version</li>
 *   <li>hash: latest commit in this version (hash)</li>
 *   <li>indexerVersion: the version of the indexer</li>
 *   <li>instance.acronym: Instance acronym</li>
 *   <li>instance.registr: Link to the Register application</li>
 *   <li>instance.client: link to the Client application</li>
 * </ul>
 *
 */
@Path("/client/v7.0/info")
public class InfoResource extends ClientApiResource {

    public static Logger LOGGER = Logger.getLogger(InfoResource.class.getName());
    
    private static HashMap<String, Locale> LOCALES = new HashMap<String, Locale>() {{
        put("en", Locale.ENGLISH);
        put("cs", new Locale("cs", "cz"));
    }};

    @Inject
    private TextsService textService;

    @Inject
    private ResourceBundleService resourceBundleService;

    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response getItems(@QueryParam("language") String langCode) {
        try {
            JSONObject json = new JSONObject();
            json.put("pdfMaxRange", getPdfMaxRange());
            json.put("version", getVersion());
            json.put("hash", getHash());
            if (langCode != null && LOCALES.containsKey(langCode)) {
                json.put("rightMsg", getRightMsg(LOCALES.get(langCode)));
            } else {
                JSONObject rightMsg = new JSONObject();
                for (String code : LOCALES.keySet()) {
                    rightMsg.put(code, getRightMsg(LOCALES.get(code)));
                }
                json.put("rightMsg", rightMsg);
            }
            json.put("indexerVersion", Indexer.INDEXER_VERSION);
            
            // Acronym & and info about instance 
            String acronym = KConfiguration.getInstance().getConfiguration().getString("acronym","");
            if (StringUtils.isAnyString(acronym)) {
                JSONObject instance =  new JSONObject();

                instance.put("acronym", acronym);
                instance.put("registr", String.format("https://registr.digitalniknihovna.cz/library/%s", acronym));
                
                // client url 
                String clientUrl = KConfiguration.getInstance().getConfiguration().getString("client");
                if (clientUrl != null) instance.put("client", clientUrl);

                // contact
                String contact = KConfiguration.getInstance().getConfiguration().getString("contact");
                if (contact != null) instance.put("contact", contact);

                json.put("instance", instance);
            }
            
            return Response.ok(json).build();
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

    private String getPdfMaxRange() {
        String maxRange = KConfiguration.getInstance().getProperty("generatePdfMaxRange");
        boolean disabled = KConfiguration.getInstance().getConfiguration().getBoolean("turnOffPdfCheck");
        return disabled ? "unlimited" : maxRange;
    }

    private String getVersion() throws IOException {
        Properties buildProperties = new Properties();
        InputStream revisions = this.getClass().getClassLoader().getResourceAsStream("build.properties");
        if (revisions != null) {
            buildProperties.load(revisions);
            return buildProperties.getProperty("version");
        } else {
            LOGGER.warning("build.properties is not present");
            return "";
        }
    }

    private String getHash() throws IOException {
        Properties buildProperties = new Properties();
        InputStream revisions = this.getClass().getClassLoader().getResourceAsStream("build.properties");
        if (revisions != null) {
            buildProperties.load(revisions);
            return buildProperties.getProperty("hash");
        } else {
            LOGGER.warning("build.properties is not present");
            return "";
        }
    }

    @Deprecated
    private String getRightMsg(Locale locale) throws IOException {
        String key = "rightMsg";
        if (textService.isAvailable(key, locale)) {
            return textService.getText(key, locale);
        } else {
            return resourceBundleService.getResourceBundle("labels", locale).getString(key);
        }
    }
}
