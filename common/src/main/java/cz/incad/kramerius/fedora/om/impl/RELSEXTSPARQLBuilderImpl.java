package cz.incad.kramerius.fedora.om.impl;

import cz.incad.kramerius.fedora.om.RepositoryException;
import cz.incad.kramerius.FedoraNamespaceContext;
import cz.incad.kramerius.FedoraNamespaces;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.pid.PIDParser;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
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
        StringBuilder builder = new StringBuilder();

        prefix(builder);

        Map<SPARQLBuilderRelation,SPARQLBuilderObject> map = new HashMap<>();

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
                        value = listener.inform(value, localName);
                    }
                    map.put( new SPARQLBuilderRelation(namespaceURI,localName), new SPARQLBuilderObject(value, TYPE.refrence));
                } else {
                    map.put( new SPARQLBuilderRelation(namespaceURI,localName), new SPARQLBuilderObject(elm.getTextContent(), TYPE.literal));
                }
            }
        }
        delete(builder);
        insert(builder,map);
        where(builder);
        return builder.toString();
    }

    void delete(StringBuilder builder) {
        builder.append("DELETE {}").append('\n');
    }

    void where(StringBuilder builder) {
        builder.append("WHERE {}");
    }


    void insert(StringBuilder sbuilder, Map<SPARQLBuilderRelation, SPARQLBuilderObject> values) {
        sbuilder.append("INSERT { <> ");

        List<SPARQLBuilderRelation> keysList =  new ArrayList<>(values.keySet());
        for (int i = 0,ll=keysList.size(); i < ll; i++) {
            SPARQLBuilderRelation s = keysList.get(i);
            sbuilder.append("  ");
            SPARQLBuilderObject builder = values.get(s);
            sbuilder.append(s.getBuilderValue(this.namespaceContext)).append(" ").append(values.get(s).getBuilderValue());
            if (i<ll-1) { sbuilder.append(" ;"); }
            else { sbuilder.append(" ."); }
            sbuilder.append('\n').append("       ");
        }
        sbuilder.append("}\n");
    }

    public static class SPARQLBuilderRelation {

        private String namespace;
        private String property;

        public SPARQLBuilderRelation(String namespace, String property) {
            this.namespace = namespace;
            this.property = property;
        }

        public String getNamespace() {
            return namespace;
        }

        public String getProperty() {
            return property;
        }

        public String getBuilderValue(NamespaceContext namespaceContext) {
            return "<"+this.namespace+(this.namespace.endsWith("#")? "" :"#")+this.property+">";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SPARQLBuilderRelation that = (SPARQLBuilderRelation) o;
            if (getNamespace() != null ? !getNamespace().equals(that.getNamespace()) : that.getNamespace() != null)
                return false;
            return getProperty() != null ? getProperty().equals(that.getProperty()) : that.getProperty() == null;
        }

        @Override
        public int hashCode() {
            int result = getNamespace() != null ? getNamespace().hashCode() : 0;
            result = 31 * result + (getProperty() != null ? getProperty().hashCode() : 0);
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
