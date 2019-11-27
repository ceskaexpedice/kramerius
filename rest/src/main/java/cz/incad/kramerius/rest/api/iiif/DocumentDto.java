package cz.incad.kramerius.rest.api.iiif;

import cz.incad.kramerius.rest.api.k5.client.utils.SOLRUtils;
import org.w3c.dom.Element;

/**
 * DocumentDto
 *
 * @author Martin Rumanek
 */
public class DocumentDto {
    private Element indexDoc;
    private String pid;

    public DocumentDto(String pid, Element indexDoc) {
        this.pid = pid;
        this.indexDoc = indexDoc;
    }

    public String getTitle() {
        if ("page".equals(getModel())) {
            return SOLRUtils.value(indexDoc, "dc.title", String.class);
        } else {
            return SOLRUtils.value(indexDoc, "root_title", String.class);
        }
    }

    public String getModel() {
        return SOLRUtils.value(indexDoc, "fedora.model", String.class);
    }

    public String getPid() {
        return pid;
    }
}
