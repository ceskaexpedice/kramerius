package cz.incad.kramerius.processes.definition;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class ProcessDefinitionImpl implements ProcessDefinition {

    private String id;
    private String description;
    private List<String> javaProcessParameters = new ArrayList<String>();
    private String securedAction;

    public ProcessDefinitionImpl() {
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public List<String> getJavaProcessParameters() {
        return this.javaProcessParameters;
    }

    @Override
    public String getSecuredAction() {
        return this.securedAction;
    }

    public void setSecuredAction(String act) {
        this.securedAction = act;
    }

    public void loadFromXml(Element elm) {
        NodeList nodes = elm.getChildNodes();
        for (int i = 0, ll = nodes.getLength(); i < ll; i++) {
            Node item = nodes.item(i);
            if (item.getNodeType() == Node.ELEMENT_NODE) {
                String nodeName = item.getNodeName();
                if (nodeName.equals("id")) {
                    this.id = item.getTextContent();
                }
                if (nodeName.equals("description")) {
                    this.description = item.getTextContent();
                }
                if (nodeName.equals("securedaction")) {
                    this.securedAction = item.getTextContent();
                }
                if (nodeName.equals("javaProcessParameters")) {
                    javaProcessParameters(item);
                }
            }
        }
    }

    private void javaProcessParameters(Node item) {
        Element jpElem = (Element) item;
        String textContent = jpElem.getTextContent();
        StringTokenizer tokenizer = new StringTokenizer(textContent, " ");
        while (tokenizer.hasMoreTokens()) {
            String param = tokenizer.nextToken();
            if (!param.trim().equals("")) {
                this.javaProcessParameters.add(param);
            }
        }
    }

    @Override
    public String toString() {
        return "ProcessDefinitionImpl{" +
                "id='" + id + '\'' +
                ", description='" + description + '\'' +
                ", javaProcessParameters=" + toString(javaProcessParameters) +
                ", securedAction='" + securedAction + '\'' +
                '}';
    }

    private String toString(List<String> parameters) {
        StringBuilder builder = new StringBuilder();
        builder.append('[');
        for (String item : parameters) {
            builder.append(item).append(", ");
        }
        builder.append(']');
        return builder.toString();
    }
}
