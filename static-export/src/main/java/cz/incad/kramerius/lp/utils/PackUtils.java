package cz.incad.kramerius.lp.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import cz.incad.kramerius.utils.IOUtils;

public class PackUtils {

	public static void unpack(File folder) throws IOException {
		InputStream resource = null;
		try {
			resource = PackUtils.class.getClassLoader()
					.getResourceAsStream("cz/incad/kramerius/lp/res/pack.zip");
			ZipInputStream zipInputStream = new ZipInputStream(resource);
			ZipEntry entry = null;
			while ((entry = zipInputStream.getNextEntry()) != null) {
				String name = entry.getName();
				if (entry.isDirectory()) {
					File unpackedfolder = new File(folder, name);
					boolean createdDirs = unpackedfolder.mkdirs();
					if (!createdDirs) throw new IOException("cannot create new folder '"+unpackedfolder.getAbsolutePath()+"'");
				} else {
					File unpackedfile = new File(folder, name);
					boolean createNewFile = unpackedfile.createNewFile();
					if (!createNewFile) throw new IOException("cannot create new file '"+unpackedfile.getAbsolutePath()+"'");
					FileOutputStream fos = null;
					try {
						fos = new FileOutputStream(unpackedfile);
						IOUtils.copyStreams(zipInputStream, fos);
					} finally {
						if (fos != null)
							fos.close();
						if (entry != null)
							zipInputStream.closeEntry();
					}
				}

			}
		} finally {
			if (resource != null) {
				resource.close();
			}
		}

	}
}
