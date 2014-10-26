package cz.incad.Kramerius;

import java.awt.Desktop.Action;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.antlr.stringtemplate.StringTemplate;
import org.apache.commons.lang3.StringEscapeUtils;

import com.google.inject.Inject;
import com.google.inject.Provider;

import cz.incad.Kramerius.backend.guice.GuiceServlet;
import cz.incad.kramerius.intconfig.InternalConfiguration;
import cz.incad.kramerius.pdf.GeneratePDFService;
import cz.incad.kramerius.processes.DefinitionManager;
import cz.incad.kramerius.processes.LRProcessManager;
import cz.incad.kramerius.service.ResourceBundleService;
import cz.incad.kramerius.service.TextsService;
import cz.incad.kramerius.utils.ApplicationURL;

/**
 * This servlet produces bundles as properties or as xml via http protocol. <br>
 * This is useful for xslt transformations
 * 
 * @author pavels
 */
public class I18NServlet extends GuiceServlet {

    public static final java.util.logging.Logger LOGGER = java.util.logging.Logger
            .getLogger(I18NServlet.class.getName());

    @Inject
    TextsService textsService;
    @Inject
    ResourceBundleService resourceBundleService;

    @Inject
    GeneratePDFService generatePDFService;

    @Inject
    Provider<Locale> provider;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String action = req.getParameter("action");
        if (action == null)
            action = Actions.text.name();
        Actions selectedAction = Actions.valueOf(action);
        selectedAction.doAction(getServletContext(), req, resp,
                this.textsService, this.resourceBundleService, this.provider);
    }

    public static String i18nServlet(HttpServletRequest request) {
        return ApplicationURL.urlOfPath(request, InternalConfiguration.get()
                .getProperties().getProperty("servlets.mapping.i18n"));
    }

    static enum Formats {
        xml, json;

        public static Formats find(String val) {
            Formats[] vals = values();
            for (Formats f : vals) {
                if (f.name().equals(val))
                    return f;
            }
            return xml;
        }
    }

    static enum Actions {

        text {
            @Override
            public void doAction(ServletContext context,
                    HttpServletRequest req, HttpServletResponse resp,
                    TextsService tserv, ResourceBundleService rserv,
                    Provider<Locale> provider) {
                try {
                    String parameter = req.getParameter("name");
                    String format = req.getParameter("format");
                    Locale locale = locale(req, provider);
                    String text = tserv.getText(parameter, locale);

                    Formats foundFormat = Formats.find(format);
                    if (foundFormat == Formats.xml) {
                        StringBuffer formatBundle = formatTextToXML(text,
                                parameter);
                        resp.setContentType("application/xhtml+xml");
                        resp.setCharacterEncoding("UTF-8");
                        resp.getWriter().write(formatBundle.toString());
                    } else {
                        String json = formatTextToJSON(text, parameter);
                        resp.setCharacterEncoding("UTF-8");
                        resp.setContentType("application/json");
                        resp.getWriter().write(json);
                    }

                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                    try {
                        resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    } catch (IOException e1) {
                    }
                }
            }

        },
        bundle {

            @Override
            public void doAction(ServletContext context,
                    HttpServletRequest req, HttpServletResponse resp,
                    TextsService tserv, ResourceBundleService rserv,
                    Provider<Locale> provider) {
                try {
                    String parameter = req.getParameter("name");
                    String format = req.getParameter("format");

                    Locale locale = locale(req, provider);
                    ResourceBundle resourceBundle = rserv.getResourceBundle(
                            parameter, locale);
                    Formats foundFormat = Formats.find(format);
                    String renderedBundle = null;
                    if (foundFormat == Formats.xml) {
                        renderedBundle = formatBundleToXML(resourceBundle,
                                parameter).toString();
                        resp.setContentType("application/xhtml+xml");
                    } else {
                        resp.setContentType("application/json");
                        renderedBundle = formatBundleToJSON(resourceBundle,
                                parameter);
                    }
                    resp.setCharacterEncoding("UTF-8");
                    resp.getWriter().write(renderedBundle);
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                    try {
                        resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    } catch (IOException e1) {
                    }
                }

            }

        };

        abstract void doAction(ServletContext context, HttpServletRequest req,
                HttpServletResponse resp, TextsService tserv,
                ResourceBundleService rserv, Provider<Locale> provider);

        static Locale locale(HttpServletRequest req, Provider<Locale> provider) {
            String lang = req.getParameter("lang");
            String country = req.getParameter("country");
            if ((lang != null) && (country != null)) {
                Locale locale = new Locale(lang, country);
                return locale;
            } else {
                return provider.get();
            }
        }

        static String formatBundleToJSON(ResourceBundle bundle,
                String bundleName) {
            Map<String, String> map = new HashMap<String, String>();
            Set<String> keySet = bundle.keySet();
            for (String key : keySet) {
                String changedValue = bundle.getString(key);
                if (changedValue.contains("\"")) {
                    changedValue = changedValue.replace("\"", "\\\"");
                }
                changedValue = changedValue.replace("\n", "\\n");
                map.put(key, changedValue);
            }

            StringTemplate template = new StringTemplate(
                    "{\"bundle\":{\n"
                            + "   $bundle.keys:{k| \"$k$\":\"$bundle.(k)$\" };separator=\",\\n\"$"
                            + "\n}}");
            template.setAttribute("bundle", map);
            return template.toString();
        }

        static StringBuffer formatBundleToXML(ResourceBundle bundle,
                String bundleName) {
            StringBuffer buffer = new StringBuffer(
                    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            buffer.append("<bundle name='").append(bundleName).append("'>\n");
            Set<String> keySet = bundle.keySet();
            for (String key : keySet) {
                buffer.append("<value key='" + key + "'>")
                        .append(bundle.getString(key)).append("</value>");
            }
            buffer.append("\n</bundle>");
            return buffer;
        }

        static StringBuffer formatTextToXML(String text, String textName) {
            StringBuffer buffer = new StringBuffer(
                    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            buffer.append("<text name='").append(textName).append("'>\n");
            buffer.append(text);
            buffer.append("\n</text>");
            return buffer;
        }

        static String formatTextToJSON(String text, String textName) {
            Map<String, String> map = new HashMap<String, String>();
            map.put("name", textName);
            String escaped = StringEscapeUtils.escapeJson(text);
            map.put("value", escaped);
            StringTemplate template = new StringTemplate("{\"text\":{\n"
                    + "  \"name\":\"$data.name$\","
                    + "  \"value\":\"$data.value$\"" + "\n}}");
            template.setAttribute("data", map);
            return template.toString();

        }
    }
}
