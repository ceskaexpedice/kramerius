package cz.incad.Kramerius;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cz.incad.Kramerius.thumbdisk.ThumbnailDiskStorage;
import cz.incad.Kramerius.thumbfedora.ThumbnailFedoraStorage;
import cz.incad.kramerius.utils.conf.KConfiguration;

public interface ThumbnailStorage {
	
	boolean checkExists(String uuid);

	void redirectToServlet(String uuid, HttpServletResponse response);
	
	void uploadThumbnail(String uuid, HttpServletRequest request);
	
	
	public enum Type {
		//thumbnails in fedora
		FEDORA {
			@Override
			public ThumbnailStorage createStorage() {
				return new ThumbnailFedoraStorage();
			}
		}, 
		//thumbnails in disk
		DISK {

			@Override
			public ThumbnailStorage createStorage() {
				return new ThumbnailDiskStorage();
			}
			
		};

		abstract ThumbnailStorage createStorage();
		
	}

}
