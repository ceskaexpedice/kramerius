package indexer;

import org.dom4j.Document;

public class XmlTest {

    private final String name;
    private final String description;
    private final String testType;
    private final String model;
    private final Document inDoc;
    private final Document outDoc;

    public XmlTest(String name, String description, String testType, String model, Document inDoc, Document outDoc) {
        this.name = name;
        this.description = description;
        this.testType = testType;
        this.model = model;
        this.inDoc = inDoc;
        this.outDoc = outDoc;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getTestType() {
        return testType;
    }

    public String getDocType() {
        return model;
    }

    public Document getInDoc() {
        return inDoc;
    }

    public Document getOutDoc() {
        return outDoc;
    }

}
