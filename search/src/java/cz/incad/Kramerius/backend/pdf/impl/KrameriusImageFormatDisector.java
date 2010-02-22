package cz.incad.Kramerius.backend.pdf.impl;

import com.qbizm.kramerius.ext.ontheflypdf.edi.generating.ImageFormat;
import com.qbizm.kramerius.ext.ontheflypdf.edi.generating.ImageFormatDisector;

public class KrameriusImageFormatDisector implements ImageFormatDisector{

	@Override
	public ImageFormat disecImageType(String arg0) {
		return ImageFormat.DJVU;
	}
}
