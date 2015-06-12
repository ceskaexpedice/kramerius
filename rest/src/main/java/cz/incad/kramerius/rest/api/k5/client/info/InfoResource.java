package cz.incad.kramerius.rest.api.k5.client.info;

import com.google.inject.Inject;
import com.google.inject.Provider;
import cz.incad.kramerius.rest.api.exceptions.GenericApplicationException;
import cz.incad.kramerius.service.ResourceBundleService;
import cz.incad.kramerius.service.TextsService;
import cz.incad.kramerius.utils.conf.KConfiguration;
import net.sf.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Created by Martin Rumanek on 27.5.15.
 */

@Path("/v5.0/info")
public class InfoResource {

    private static final String DEFAULT_INTRO_CONSTANT = "default_intro";

    private static final String INTRO_CONSTANT = "intro";

    private static final String RIGHT_MSG = "rightMsg";

    public static Logger LOGGER = Logger.getLogger(InfoResource.class.getName());

    @Inject
    private KConfiguration configuration;

    @Inject
    private TextsService textService;

    @Inject
    private Provider<Locale> provider;

    @Inject
    private ResourceBundleService resourceBundleService;

    InputStream revisions = this.getClass().getClassLoader().getResourceAsStream("build.properties");

    @GET
    @Produces({MediaType.APPLICATION_JSON + ";charset=utf-8"})
    public Response getInfo() {

        Properties buildProperties = new Properties();
        String version = "";
        String hash = "";
        try {
            buildProperties.load(revisions);
            version = buildProperties.getProperty("version");
            hash = buildProperties.getProperty("hash");
        } catch (IOException e) {
            e.printStackTrace();
        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("version", version);
        jsonObject.put("hash", hash);

        String adminEmail = configuration.getProperty("administrator.email");
        if (adminEmail != null && !adminEmail.isEmpty()) {
            jsonObject.put("email", adminEmail);
        }

        String intro = null;
        String rightsMsg = null;
        try {
            if (textService.isAvailable(INTRO_CONSTANT, provider.get())) {
                intro = textService.getText(INTRO_CONSTANT, provider.get());
            } else {
                intro = textService.getText(DEFAULT_INTRO_CONSTANT, provider.get());
            }
            if (intro != null && !intro.isEmpty()) {
                jsonObject.put("intro", intro);
            }

            if (textService.isAvailable(RIGHT_MSG, provider.get())) {
                rightsMsg = textService.getText(RIGHT_MSG, provider.get());
            } else {
                rightsMsg = resourceBundleService.getResourceBundle("labels", provider.get()).getString(RIGHT_MSG);
            }

            if (intro != null && !intro.isEmpty()) {
                jsonObject.put("intro", intro);
            }

            if (rightsMsg != null && !rightsMsg.isEmpty()) {
                jsonObject.put("rightMsg", rightsMsg);
            }

        } catch (IOException e) {
            throw new GenericApplicationException(e.getMessage());
        }

        String maxPage = KConfiguration.getInstance().getProperty(
                "generatePdfMaxRange");
        boolean turnOff = KConfiguration.getInstance().getConfiguration().getBoolean("turnOffPdfCheck");

        if (turnOff) {
            jsonObject.put("pdfMaxRange", "unlimited");
        } else {
            jsonObject.put("pdfMaxRange", maxPage);
        }


        return Response.ok().entity(jsonObject.toString()).build();
    }
}
