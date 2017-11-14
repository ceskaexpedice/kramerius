package cz.incad.kramerius.fedora.om.impl;

import cz.incad.kramerius.fedora.om.RepositoryException;
import cz.incad.kramerius.FedoraNamespaceContext;
import cz.incad.kramerius.FedoraNamespaces;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.pid.PIDParser;
import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.language.DefaultTemplateLexer;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by pstastny on 10/11/2017.
 */
public class RELSEXTSPARQLBuilderImpl implements RELSEXTSPARQLBuilder {

    private FedoraNamespaceContext namespaceContext = new FedoraNamespaceContext();

    void prefix(StringBuilder builder) {
        namespaceContext.getPrefixes().stream().filter(s-> !namespaceContext.getNamespaceURI(s).equals(FedoraNamespaces.FEDORA_ACCESS_NAMESPACE_URI)).forEach(s-> {builder.append("PREFIX ").append(s).append(':').append(" <").append(namespaceContext.getNamespaceURI(s)).append(">").append('\n');});
    }

    @Override
    public String sparqlProps(String relsExt, RELSEXTSPARQLBuilderListener listener) throws IOException, SAXException, ParserConfigurationException, RepositoryException {
        StringTemplateGroup strGroup = SPARQL_TEMPLATES();

        Map<SPARQLBuilderRelation,SPARQLBuilderObject> map = new HashMap<>();
        List<SPARQLBuilderRelation> ordering = new ArrayList<>();

        Document document = XMLUtils.parseDocument(new StringReader(relsExt), true);
        Element description = XMLUtils.findElement(document.getDocumentElement(), "Description", FedoraNamespaces.RDF_NAMESPACE_URI);
        NodeList childNodes = description.getChildNodes();
        for (int i = 0,ll=childNodes.getLength(); i < ll; i++) {
            Node n = childNodes.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                Element elm = (Element) n;
                String localName = elm.getLocalName();
                String namespaceURI = elm.getNamespaceURI();
                Attr resource = elm.getAttributeNodeNS(FedoraNamespaces.RDF_NAMESPACE_URI, "resource");
                if (resource != null) {
                    String value = resource.getValue();
                    if (value.startsWith(PIDParser.INFO_FEDORA_PREFIX)) {
                        value = value.substring(PIDParser.INFO_FEDORA_PREFIX.length());
                        if (listener != null) {
                            value = listener.inform(value, localName);
                        }
                    }

                    SPARQLBuilderRelation relation = new SPARQLBuilderRelation(namespaceURI, localName, value);
                    map.put(relation, new SPARQLBuilderObject(value, TYPE.refrence));
                    ordering.add(relation);
                } else {
                    SPARQLBuilderRelation relation = new SPARQLBuilderRelation(namespaceURI,localName, elm.getTextContent());
                    map.put( relation , new SPARQLBuilderObject(elm.getTextContent(), TYPE.literal));
                    ordering.add(relation);
                }
            }
        }

        StringTemplate sparql = strGroup.getInstanceOf("relsext_sparql");
        sparql.setAttribute("relations",map);
        sparql.setAttribute("ordering",ordering);
        return sparql.toString();
    }

    public static StringTemplateGroup SPARQL_TEMPLATES() throws IOException {
        InputStream stream = RELSEXTSPARQLBuilderImpl.class.getResourceAsStream("res/relsextsparql.stg");
        String string = org.apache.commons.io.IOUtils.toString(stream, Charset.forName("UTF-8"));
        return new StringTemplateGroup(new StringReader(string), DefaultTemplateLexer.class);
    }


    public static class SPARQLBuilderRelation {

        private String namespace;
        private String property;
        private String object;

        public SPARQLBuilderRelation(String namespace, String property, String object) {
            this.namespace = namespace;
            this.property = property;
            this.object = object;
        }

        public String getNamespace() {
            return namespace;
        }

        public String getProperty() {
            return property;
        }

        public String getBuilderValue() {
            return "<"+this.namespace+(this.namespace.endsWith("#")? "" :"#")+this.property+">";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            SPARQLBuilderRelation that = (SPARQLBuilderRelation) o;

            if (getNamespace() != null ? !getNamespace().equals(that.getNamespace()) : that.getNamespace() != null)
                return false;
            if (getProperty() != null ? !getProperty().equals(that.getProperty()) : that.getProperty() != null)
                return false;
            return object != null ? object.equals(that.object) : that.object == null;
        }

        @Override
        public int hashCode() {
            int result = getNamespace() != null ? getNamespace().hashCode() : 0;
            result = 31 * result + (getProperty() != null ? getProperty().hashCode() : 0);
            result = 31 * result + (object != null ? object.hashCode() : 0);
            return result;
        }
    }


    public  static enum TYPE {
        literal,
        refrence
    };

    public static  class SPARQLBuilderObject {
        private String value;
        private TYPE type;

        public SPARQLBuilderObject(String value, TYPE type) {
            this.value = value;
            this.type = type;
        }

        public String getValue() {
            return value;
        }

        public String getBuilderValue() {
            switch (this.type) {
                case literal:
                    return '"' + this.value + '"';
                case refrence:
                    return '<' + this.value + '>';
                default:
                    throw new IllegalStateException("bad type");
            }
        }

        public TYPE getType() {
            return type;
        }
    }

}
