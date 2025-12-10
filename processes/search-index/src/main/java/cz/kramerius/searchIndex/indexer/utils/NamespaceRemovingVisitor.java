package cz.kramerius.searchIndex.indexer.utils;

import org.dom4j.*;

public class NamespaceRemovingVisitor extends VisitorSupport {
    private final boolean removeNsFromElements;
    private final boolean removeNsFromAttributes;
    private Namespace from;
    private Namespace to;

    public NamespaceRemovingVisitor(boolean removeNsFromElements, boolean removeNsFromAttributes) {
        this.removeNsFromElements = removeNsFromElements;
        this.removeNsFromAttributes = removeNsFromAttributes;
    }

    public void visit(Element element) {
        if (removeNsFromElements) {
            if (!Namespace.NO_NAMESPACE.equals(element.getNamespace())) {
                QName newQName = new QName(element.getName(), Namespace.NO_NAMESPACE);
                element.setQName(newQName);
            }
        }
    }

    public void visit(Attribute attribute) {
        if (removeNsFromAttributes) {
            Element parent = attribute.getParent();
            String value = attribute.getValue();
            parent.remove(attribute);
            parent.addAttribute(new QName(attribute.getName(), Namespace.NO_NAMESPACE), value);
        }
    }
}
