package cz.incad.kramerius.rest.apiNew.admin.v10;

import com.google.inject.Inject;
import cz.incad.kramerius.rest.apiNew.exceptions.ForbiddenException;
import cz.incad.kramerius.rest.apiNew.exceptions.InternalErrorException;
import cz.incad.kramerius.service.ResourceBundleService;
import cz.incad.kramerius.service.TextsService;
import cz.incad.kramerius.utils.conf.KConfiguration;
import org.json.JSONObject;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * @see cz.incad.kramerius.rest.api.k5.client.info.InfoResource
 */
@Path("/admin/v1.0/config")
public class ConfigResource extends AdminApiResource {

    public static Logger LOGGER = Logger.getLogger(ConfigResource.class.getName());

    private static HashMap<String, Locale> LOCALES = new HashMap<String, Locale>() {{
        put("en", Locale.ENGLISH);
        put("cs", new Locale("cs", "cz"));
    }};

    //TODO: prejmenovat role podle spravy uctu
    private static final String ROLE_READ_INFO = "kramerius_admin";
    private static final String ROLE_WRITE_CONFIG = "kramerius_admin";

    @Inject
    private TextsService textService;

    @Inject
    private ResourceBundleService resourceBundleService;

    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response getItems(@QueryParam("language") String langCode) {
        try {
            boolean disableAuth = true; //TODO: reenable for production
            //authentication
            if (!disableAuth) {
                AuthenticatedUser user = getAuthenticatedUser();
                String role = ROLE_READ_INFO;
                if (!user.getRoles().contains(role)) {
                    throw new ForbiddenException("user '%s' is not allowed to do this (missing role '%s')", user.getName(), role); //403
                }
            }
            JSONObject json = new JSONObject();
            json.put("pdfMaxRange", getPdfMaxRange());
            json.put("version", getVersion());
            if (langCode != null && LOCALES.containsKey(langCode)) {
                json.put("rightMsg", getRightMsg(LOCALES.get(langCode)));
            } else {
                JSONObject rightMsg = new JSONObject();
                for (String code : LOCALES.keySet()) {
                    rightMsg.put(code, getRightMsg(LOCALES.get(code)));
                }
                json.put("rightMsg", rightMsg);
            }
            return Response.ok(json).build();
        } catch (IOException e) {
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
        buildProperties.load(revisions);
        return buildProperties.getProperty("version");
    }

    private String getRightMsg(Locale locale) throws IOException {
        //TODO: replace with database (table CONFIG with columns KEY and VALUE)
        String key = "rightMsg";
        if (textService.isAvailable(key, locale)) {
            return textService.getText(key, locale);
        } else {
            return resourceBundleService.getResourceBundle("labels", locale).getString(key);
        }
    }

    //TODO: metoda pro nastavení pdfMaxRange

    //TODO: metoda pro nastaveni rightMsg (podle jazyka)
}
