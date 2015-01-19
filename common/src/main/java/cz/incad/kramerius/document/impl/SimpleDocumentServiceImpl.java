package cz.incad.kramerius.document.impl;

import java.io.IOException;

import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.ProcessSubtreeException;
import cz.incad.kramerius.document.DocumentService;
import cz.incad.kramerius.document.model.AbstractRenderedDocument;

public class SimpleDocumentServiceImpl implements DocumentService {

    @Override
    public AbstractRenderedDocument buildDocumentAsTree(ObjectPidsPath path,
            String pidFrom, int[] rect) throws IOException,
            ProcessSubtreeException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public AbstractRenderedDocument buildDocumentAsFlat(ObjectPidsPath path,
            String pidFrom, int howMany, int[] rect) throws IOException,
            ProcessSubtreeException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public AbstractRenderedDocument buildDocumentFromSelection(
            String[] selection, int[] rect) throws IOException,
            ProcessSubtreeException {
        // TODO Auto-generated method stub
        return null;
    }

}
