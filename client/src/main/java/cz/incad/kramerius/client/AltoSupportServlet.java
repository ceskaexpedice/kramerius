package cz.incad.kramerius.client;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.sf.json.JSONObject;

import org.apache.commons.configuration.ConfigurationException;
import org.json.JSONException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;

import cz.incad.kramerius.rest.api.k5.client.utils.SOLRUtils;
import cz.incad.kramerius.utils.ALTOUtils.AltoDisected;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.XMLUtils.ElementsFilter;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.jersey.BasicAuthenticationFilter;


/**
 * Alto support 
 * @author pavels
 */
public class AltoSupportServlet extends HttpServlet {

    public static final Logger LOGGER = Logger.getLogger(AltoSupportServlet.class.getName());
    
    public static String get(String url,String userName, String pswd) throws JSONException {
        Client c = Client.create();
        WebResource r = c.resource(url);
        if (userName != null && pswd != null) {
            r.addFilter(new BasicAuthenticationFilter(userName,pswd));
        }
        Builder builder = r.accept(MediaType.APPLICATION_XML);
        return builder.get(String.class);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String q = req.getParameter("q");
        String pid = req.getParameter("pid");
        try {


            String filterQuery = "PID:"+URLEncoder.encode(pid.replace(":", "\\:"),"UTF-8");
            String query = URLEncoder.encode(q,"UTF-8");
            String fieldList = URLEncoder.encode("text text_ocr text_ocr_lemmatized text_ocr_lemmatized_ascii", "UTF-8");

            String searchUrl = KConfiguration.getInstance().getConfiguration().getString("api.point")+"/search?"+"fq="+filterQuery+"&q="+query+"&defType=edismax&qf="+fieldList+"&hl=true";
            String xml = get(searchUrl, null, null);
            Document parsed = XMLUtils.parseDocument(new StringReader(xml));
            Set<String> hterms = findHighlightTerm(parsed.getDocumentElement(), pid);
            JSONObject jsonObject = new JSONObject();
            for (String sterm : hterms) {
                String altoUrl = KConfiguration.getInstance().getConfiguration().getString("api.point")+"/item/"+pid+"/streams/ALTO";

                String alto = get(altoUrl, null, null);
                byte[] bytes = alto.getBytes(Charset.forName("UTF-8"));
                
                Document parsedAlto = XMLUtils.parseDocument(new ByteArrayInputStream(bytes));
                AltoDisected disected2 = cz.incad.kramerius.utils.ALTOUtils.disectAlto(sterm, parsedAlto);
                jsonObject.put(sterm, disected2.toJSON());
                
            }
            resp.setContentType("application/json");
            resp.getWriter().write(jsonObject.toString());
        } catch (JSONException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        } catch (ParserConfigurationException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        } catch (SAXException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        }
    }
    
    public static Set<String> findHighlightTerm(final Element elm, final String pid) {
        List<Element> elmRecursive = XMLUtils.getElementsRecursive(elm, new ElementsFilter() {
            @Override
            public boolean acceptElement(Element itm) {
                if (itm.hasAttribute("name")) {
                    String name = itm.getAttribute("name");
                    if (name.equals("highlighting")) return true;
                }
                return false;
            }
        });
        
        if (!elmRecursive.isEmpty()) {
            List<Element> found = new ArrayList<Element>();
            for (Element resElm : elmRecursive) {
                List<Element> nfound = XMLUtils.getElementsRecursive(resElm, new ElementsFilter() {
                    @Override
                    public boolean acceptElement(Element itm) {
                        if (itm.hasAttribute("name")) {
                            String name = itm.getAttribute("name");
                            if (name.equals(pid)) return true;
                        }
                        return false;
                    }
                });
                found.addAll(nfound);
            }

            List<String> fieldList = Arrays.asList("text","text_ocr", "text_ocr_lemmatized", "text_ocr_lemmatized_ascii");
            Set<String> terms = new HashSet<>();
            for (Element docEl : found) {

                for (String fieldName :
                        fieldList) {

                    List<String> textArray = SOLRUtils.array(docEl, fieldName, String.class);
                    for (String text : textArray) {
                        if (text != null) {
                            String textContent = textContent(text);
                            if (textContent != null) {
                                terms.add(textContent);
                            }
                        }
                    }
                    if (textArray.isEmpty()) {
                        String value = SOLRUtils.value(docEl, fieldName, String.class);
                        if (value != null) {
                            String textContent = textContent(value);
                            if (textContent != null) {
                                terms.add(textContent);
                            }
                        }
                    }

                }

            }
            
            return terms;
        } else return new HashSet<>();
        
    }

    private static String textContent(String text) {
        String ntext = "<div>"+text+"</div>";
        try {
            Document parsed = XMLUtils.parseDocument(new StringReader(ntext));
            Element foundElm = XMLUtils.findElement(parsed.getDocumentElement(), "em");
            return foundElm.getTextContent();
        } catch (ParserConfigurationException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        } catch (SAXException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        }
        return null;
    }
}
