package cz.incad.kramerius.processes.impl;

import cz.incad.kramerius.processes.*;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class LRProcessDefinitionImpl implements LRProcessDefinition {

    public static final String DEFAULT_USER_DIR = "";

    private String libsDir;
    private String id;
    private String mainClass;
    private String description;
    private String standardStreamFolder;
    private String errStreamFolder;

    private List<String> parameters = new ArrayList<String>();
    private List<String> javaProcessParameters = new ArrayList<String>();


    private String inputTemplateClz = null;
    private List<String> outputTemplateClzs = new ArrayList<String>();

    private String securedAction;

    private List<LRDefinitionAction> actions = new ArrayList<LRDefinitionAction>();

    private boolean shouldCheckErrorStream = true;


    public LRProcessDefinitionImpl() {
        super();

    }

    @Override
    public String getLibsDir() {
        return this.libsDir;
    }

    @Override
    public List<String> getParameters() {
        return new ArrayList<String>(this.parameters);
    }


    public LRDefinitionAction getLogsAction() {
        return LRDefinitionAction.LOGS_ACTION;
    }

    public List<LRDefinitionAction> getActions() {
        return new ArrayList<LRDefinitionAction>(this.actions);
    }


    @Override
    public String getInputTemplateClass() {
        return this.inputTemplateClz;
    }

    public boolean isInputTemplateDefined() {
        return this.inputTemplateClz != null;
    }


    @Override
    public List<String> getOutputTemplateClasses() {
        return new ArrayList<String>(this.outputTemplateClzs);
    }

    @Override
    public boolean isOutputTemplatesDefined() {
        return !this.outputTemplateClzs.isEmpty();
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
                if (nodeName.equals("mainClass")) {
                    this.mainClass = item.getTextContent();
                }
                if (nodeName.equals("description")) {
                    this.description = item.getTextContent();
                }
                if (nodeName.equals("lib")) {
                    this.libsDir = item.getTextContent();
                }
                if (nodeName.equals("standardOs")) {
                    this.standardStreamFolder = item.getTextContent();
                }
                if (nodeName.equals("errOs")) {
                    this.errStreamFolder = item.getTextContent();
                }
                if (nodeName.equals("parameters")) {
                    oldStyleParameters(item);
                    if (this.parameters.isEmpty()) {
                        newStyleParameters(item);
                    }
                }
                if (nodeName.equals("securedaction")) {
                    this.securedAction = item.getTextContent();
                }
                if (nodeName.equals("javaProcessParameters")) {
                    javaProcessParameters(item);
                }
                if (nodeName.equals("actions")) {
                    actions(item);
                }
                if (nodeName.equals("checkErrorStream")) {
                    String textContent = item.getTextContent();
                    if (textContent != null)
                        this.shouldCheckErrorStream = Boolean.parseBoolean(textContent.trim());
                }

                if (nodeName.equals("templates")) {
                    NodeList templateItems = item.getChildNodes();
                    for (int j = 0, itl = templateItems.getLength(); j < itl; j++) {
                        Node templateItem = templateItems.item(j);
                        if (templateItem.getNodeType() == Node.ELEMENT_NODE) {
                            if (templateItem.getNodeName().equals("input")) {
                                Element inputElm = (Element) templateItem;
                                inputElm(inputElm);
                            } else if (templateItem.getNodeName().equals("output")) {
                                Element outputElm = (Element) templateItem;
                                outputElm(outputElm);
                            }
                        }
                    }
                }
            }
        }
    }

    private void outputElm(Element outputElm) {
        this.outputTemplateClzs.add(outputElm.getAttribute("class"));
    }

    private void inputElm(Element inputElm) {
        this.inputTemplateClz = inputElm.getAttribute("class");
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

    private void actions(Node item) {
        Element elm = (Element) item;
        NodeList nodes = elm.getChildNodes();
        for (int i = 0, ll = nodes.getLength(); i < ll; i++) {
            Node chItem = nodes.item(i);
            if (chItem.getNodeType() == Node.ELEMENT_NODE) {
                LRDefinitionAction action = new LRDefinitionAction();
                action.loadFromXml((Element) chItem);
                this.actions.add(action);
            }
        }
    }

    private void oldStyleParameters(Node item) {

        Element elm = (Element) item;
        NodeList nodes = elm.getChildNodes();
        for (int i = 0, ll = nodes.getLength(); i < ll; i++) {
            Node chItem = nodes.item(i);
            if (chItem.getNodeType() == Node.ELEMENT_NODE) {
                String chItemName = chItem.getNodeName();
                if (chItemName.equals("parameter")) {
                    String chItemVal = chItem.getTextContent();
                    this.parameters.add(chItemVal);
                }
            }
        }
    }

    private void newStyleParameters(Node item) {
        Element elm = (Element) item;
        String textContent = elm.getTextContent();
        StringTokenizer tokenizer = new StringTokenizer(textContent, " ");
        while (tokenizer.hasMoreTokens()) {
            String param = tokenizer.nextToken();
            if (!param.trim().equals("")) {
                this.parameters.add(param);
            }
        }

    }


    public String getId() {
        return id;
    }

    @Override
    public String getMainClass() {
        return this.mainClass;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    public String getStandardStreamFolder() {
        return standardStreamFolder;
    }

    public void setStandardStreamFolder(String standardStreamFolder) {
        this.standardStreamFolder = standardStreamFolder;
    }

    public String getErrStreamFolder() {
        return errStreamFolder;
    }

    public void setErrStreamFolder(String errStreamFolder) {
        this.errStreamFolder = errStreamFolder;
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

    @Override
    public boolean isCheckedErrorStream() {
        return this.shouldCheckErrorStream;
    }


    @Override
    public String toString() {
        return "LRProcessDefinitionImpl{" +
                "libsDir='" + libsDir + '\'' +
                ", id='" + id + '\'' +
                ", mainClass='" + mainClass + '\'' +
                ", description='" + description + '\'' +
                ", standardStreamFolder='" + standardStreamFolder + '\'' +
                ", errStreamFolder='" + errStreamFolder + '\'' +
                ", parameters=" + toString(parameters) +
                ", javaProcessParameters=" + toString(javaProcessParameters) +
                ", inputTemplateClz='" + inputTemplateClz + '\'' +
                ", outputTemplateClzs=" + outputTemplateClzs +
                ", securedAction='" + securedAction + '\'' +
                ", actions=" + actionsToString(actions) +
                ", shouldCheckErrorStream=" + shouldCheckErrorStream +
                '}';
    }

    private String actionsToString(List<LRDefinitionAction> actions) {
        StringBuilder builder = new StringBuilder();
        builder.append('[');
        for (LRDefinitionAction item : actions) {
            builder.append(item.getName()).append(", ");
        }
        builder.append(']');
        return builder.toString();
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
