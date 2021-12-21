package cz.incad.kramerius.rest.apiNew.client.v60;

import com.google.inject.Inject;
import cz.incad.kramerius.rest.apiNew.exceptions.InternalErrorException;
import cz.incad.kramerius.service.ResourceBundleService;
import cz.incad.kramerius.service.TextsService;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.kramerius.searchIndex.indexerProcess.Indexer;
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
 * @see cz.incad.kramerius.rest.api.k5.client.info.InfoResource
 */
@Path("/client/v6.0/info")
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
            //TODO: tohle asi vyhodit, uz resime v ConfigResource
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

    @Deprecated //TODO: replace with database (table CONFIG with columns KEY and VALUE)
    private String getRightMsg(Locale locale) throws IOException {
        String key = "rightMsg";
        if (textService.isAvailable(key, locale)) {
            return textService.getText(key, locale);
        } else {
            return resourceBundleService.getResourceBundle("labels", locale).getString(key);
        }
    }
}
