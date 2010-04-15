package cz.incad.kramerius.lp;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class DecoratedOutputStream extends FileOutputStream {

	private long actualSize;
	
	public DecoratedOutputStream(File file, boolean append)
			throws FileNotFoundException {
		super(file, append);
	}

	public DecoratedOutputStream(File file) throws FileNotFoundException {
		super(file);
	}

	public DecoratedOutputStream(FileDescriptor fdObj) {
		super(fdObj);
	}

	public DecoratedOutputStream(String name, boolean append)
			throws FileNotFoundException {
		super(name, append);
	}

	public DecoratedOutputStream(String name) throws FileNotFoundException {
		super(name);
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		super.write(b, off, len);
		this.actualSize += len;
	}

	@Override
	public void write(byte[] b) throws IOException {
		super.write(b);
		this.actualSize += b.length;
	}

	@Override
	public void write(int b) throws IOException {
		super.write(b);
		this.actualSize +=1; 
	}

	public long getActualSize() {
		return actualSize;
	}
	
}
