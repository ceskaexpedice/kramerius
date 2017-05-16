package cz.incad.kramerius.service.replication;

import java.io.ByteArrayOutputStream;

import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;

import cz.incad.kramerius.utils.XMLUtils;

public abstract class AbstractReplicationFormat implements ReplicationFormat {

	protected byte[] serializeToBytes(Document document) throws TransformerException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		XMLUtils.print(document, bos);
		return bos.toByteArray();
	}

}
