package org.latte.scripting.hostobjects;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.apache.commons.codec.binary.Base64;
import org.mozilla.javascript.JavaScriptException;

public class FileProxy {
	enum MODE { TEXT, BASE64 };
	private final File file;
	private final MODE mode;
	
	
	public FileProxy(File file, String type) {
		this.file = file;
		
		if("base64".equals(type)) mode = MODE.BASE64;
		else mode = MODE.TEXT;
	}
	
	public void write(String buf) throws IOException {
		PrintStream out = new PrintStream(new FileOutputStream(file));
		
		switch(mode) {
		case TEXT:
			out.print(buf);
			break;
		case BASE64:
			out.write(new Base64().decode(buf.getBytes()));
			break;
		}
		
		out.close();	
	}

	public String read() throws Exception {
		if(!file.exists()) throw new JavaScriptException(file.getAbsoluteFile() + " not found", "open", 0);	
		
		byte[] buffer = new byte[(int)file.length()];
		FileInputStream in = new java.io.FileInputStream(file);
		in.read(buffer);
		in.close();
		
		return new String(buffer);
	}
	
	public void remove() throws IOException {
		file.delete();
	}
}
