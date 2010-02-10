package cz.incad.kramerius.utils.pid;

import java.io.IOException;
import java.io.StringReader;


/**
 * @author pavels
 *
 */
public class CharBuffer {

	private int[] buffer = null;	
	private int[] positions = null;
	private int depth = 0;
	private StringReader input = null;
	private int counter = 0;
	
	public CharBuffer(String inputString, int depth) throws LexerException {
		try {
			this.input = new StringReader(inputString);
			this.depth = depth;
			this.buffer = new int[this.depth];
			this.positions = new int[this.depth];
			
			for (int i = 0; i < this.buffer.length; i++) {
				this.buffer[i] = this.input.read();
				this.positions[i] = counter ++;
			}
		} catch (IOException e) {
			throw new LexerException(e.getMessage());
		}
	}
	
	public int la(int pos) throws LexerException {
		if ((pos >=1) && (pos <= this.depth)) {
			return this.buffer[pos - 1];
		} else throw new LexerException("cannot look ahead to '"+pos+"' position");
	}
	
	public int position(int pos) throws LexerException {
		if ((pos >=1) && (pos <= this.depth)) {
			return this.positions[pos - 1];
		} else throw new LexerException("cannot look ahead to '"+pos+"' position");
	}
	
	public void consume() throws LexerException {
		try {
			for (int i = 0; i < this.depth-1; i++) {
				this.buffer[i] = this.buffer[i+1];
				this.positions[i] = this.positions[i+1];
			}
			
			this.buffer[this.depth - 1] = this.input.read();
			this.positions[this.depth - 1] = this.counter ++;
		} catch (IOException e) {
			throw new LexerException(e.getMessage());
		}
	}

}