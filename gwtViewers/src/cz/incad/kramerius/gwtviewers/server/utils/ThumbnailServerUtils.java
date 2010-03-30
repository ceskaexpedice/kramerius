package cz.incad.kramerius.gwtviewers.server.utils;

import java.awt.Image;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.logging.Level;

import javax.imageio.ImageIO;


import cz.incad.kramerius.gwtviewers.client.SimpleImageTO;

public class ThumbnailServerUtils {

	public static final java.util.logging.Logger LOGGER = java.util.logging.Logger
			.getLogger(ThumbnailServerUtils.class.getName());

	public static Properties disectSizesOldStyle(String uuid, List<SimpleImageTO> sits ) {
		int mwidth = 0;
		Properties collected = new Properties();
		for (SimpleImageTO sit : sits) {
			try {
				Image readthumbs = ThumbnailServerUtils.readThumbnail(sit.getUrl());
				int width = readthumbs.getWidth(null);
				if (mwidth < width) mwidth = width;
				collected.setProperty(sit.getIdentification(), ""+width);
			} catch (Exception e) {
				e.printStackTrace();
				collected.setProperty(sit.getIdentification(), ""+mwidth);
			}
		}
		collected.setProperty(MetadataStore.WIDTH_KEY, ""+mwidth);
		return collected;
	}
	
	// parallelize
	public static Properties disectSizes(String uuid, List<SimpleImageTO> sits ) throws FileNotFoundException, IOException {
		Properties collected;
		try {
			int maxThread = 3;
			int iterations = sits.size() / maxThread;
			if ((sits.size() % maxThread) != 0) {
				iterations +=1;
			}
			collected = new Properties();
			for (int i = 0; i < iterations; i++) {
				// i=0, from = 0*10, to = 1*10
				// i=2, from = 1*10, to = 2*10
				// i=2, from = 1*10, to = 2*10
				int from = i * maxThread;
				int tto = (i+1) * maxThread;
				int to = Math.min(tto, sits.size()); 
				long iStart = System.currentTimeMillis();
				int parties = (to - from) +1;
				//System.out.println("Pocet threadu (maxthread + 1) = "+parties);
				CyclicBarrier barrier = new CyclicBarrier(parties);
				for (int j = from; j < to; j++) {
					new Thread(new Worker(j, sits.get(j), collected, barrier)).start();
				}
				barrier.await();
				long iStop = System.currentTimeMillis();
				collected.put("iterace("+i+")", (iStop-iStart));
			}
			return collected;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	//TODO: Zjisteni velikosti... nic vic.
	public static Image readThumbnail(String thumbnailURL) {
		try {
			URL url = new URL(thumbnailURL);
			URLConnection connection = url.openConnection();
			return ImageIO.read(connection.getInputStream());
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} 
	}

	static class Worker implements Runnable {
		
		private SimpleImageTO sit;
		private Properties properties;
		private CyclicBarrier barrier;
		private int index;
		
		public Worker(int index, SimpleImageTO sit, Properties properties,
				CyclicBarrier barrier) {
			super();
			this.index = index;
			this.sit = sit;
			this.properties = properties;
			this.barrier = barrier;
		}


		@Override
		public void run() {
			try {
				Image readthumbs = ThumbnailServerUtils.readThumbnail(sit.getUrl());
				int width = readthumbs.getWidth(null);
				this.properties.setProperty(sit.getIdentification(), ""+width);
				if (barrier != null) this.barrier.await();
			} catch (InterruptedException e) {
				LOGGER.log(Level.SEVERE, e.getMessage(), e);
			} catch (BrokenBarrierException e) {
				LOGGER.log(Level.SEVERE, e.getMessage(), e);
			}
		}
	}
}
