package cz.incad.kramerius.processes.impl.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;

public class FollowStreamThread extends Thread {
	
	public static final java.util.logging.Logger LOGGER = java.util.logging.Logger
			.getLogger(FollowStreamThread.class.getName());
	
	private InputStream followStream;
	private OutputStream os;
	
	public FollowStreamThread(InputStream followStream, OutputStream os) {
		super();
		this.followStream = followStream;
		this.os = os;
	}


	@Override
	public void run() {
		LOGGER.info("start following stream ");
		try {
			while(followStream.available() != 0) {
				byte[] buffer = new byte[1<<12];
				int bread = -1;
				while((bread = followStream.read(buffer)) > 0) {
					System.out.println("read  "+bread);
					this.os.write(buffer, 0, bread);
					if (os instanceof ByteArrayOutputStream) {
						ByteArrayOutputStream bos = (ByteArrayOutputStream) os;
						System.out.println(bos.toByteArray().length);
					}
				}
			}
			LOGGER.info(" end ");
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		} finally {
			if (os != null) {
				try { os.close(); } catch(IOException e) { LOGGER.log(Level.SEVERE, e.getMessage(), e);}
			}
		}
	}
}
