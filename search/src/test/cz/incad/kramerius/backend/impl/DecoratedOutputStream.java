package cz.incad.kramerius.backend.impl;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class DecoratedOutputStream extends FileOutputStream {

	private long size;
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
		System.out.println("Actual size "+this.actualSize);
	}

	@Override
	public void write(byte[] b) throws IOException {
		super.write(b);
		this.actualSize += b.length;
		System.out.println("Actual size "+this.actualSize);
	}

	@Override
	public void write(int b) throws IOException {
		super.write(b);
		this.actualSize +=1; 
		System.out.println("Actual size "+this.actualSize);
	}
	
}
