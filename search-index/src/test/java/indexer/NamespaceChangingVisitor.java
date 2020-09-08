package indexer;

import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;
import org.dom4j.VisitorSupport;

import java.util.ListIterator;

public class NamespaceChangingVisitor extends VisitorSupport {
    private Namespace from;
    private Namespace to;

    public NamespaceChangingVisitor(Namespace from, Namespace to) {
        this.from = from;
        this.to = to;
    }

    public void visit(Element node) {
        Namespace ns = node.getNamespace();

        if (ns.getURI().equals(from.getURI())) {
            QName newQName = new QName(node.getName(), to);
            node.setQName(newQName);
        }

        ListIterator namespaces = node.additionalNamespaces().listIterator();
        while (namespaces.hasNext()) {
            Namespace additionalNamespace = (Namespace) namespaces.next();
            if (additionalNamespace.getURI().equals(from.getURI())) {
                namespaces.remove();
            }
        }
    }
}
