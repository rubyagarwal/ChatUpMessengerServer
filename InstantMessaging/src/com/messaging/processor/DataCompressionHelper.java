package com.messaging.processor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import org.apache.log4j.Logger;

import com.messaging.core.Config;

public class DataCompressionHelper {
	
	private static final Logger logger = Logger.getLogger(DataCompressionHelper.class);

	public byte[] decompressData(byte[] comBs) {
		logger.info("Before decompression " + comBs.length);
		byte[] decomBs = null;
		try {
			Inflater inflater = new Inflater();
			inflater.setInput(comBs);
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream(
					comBs.length);
			byte[] buffer = new byte[Config.BUFFER_SIZE];
			while (!inflater.finished()) {
				int count = inflater.inflate(buffer);
				outputStream.write(buffer, 0, count);
			}
			outputStream.close();
			decomBs = outputStream.toByteArray();
		} catch (IOException ioe) {
			System.err.println("IOE");
			ioe.printStackTrace();
		} catch (DataFormatException dfe) {
			System.err.println("DFE");
			dfe.printStackTrace();
		}
		logger.info("After decompression " + decomBs.length);
		return decomBs;
	}

	public byte[] compressData(byte[] decomBs) {
		logger.info("Before compression " + decomBs.length);
		byte[] comBs = null;

		try {
			Deflater deflater = new Deflater();
			deflater.setInput(decomBs);
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream(
					decomBs.length);
			deflater.finish();
			byte[] buffer = new byte[Config.BUFFER_SIZE];
			while (!deflater.finished()) {
				int count = deflater.deflate(buffer); 
				outputStream.write(buffer, 0, count);
			}
			outputStream.close();
			comBs = outputStream.toByteArray();
		} catch (IOException ioe) {
			System.err.println("IOE");
			ioe.printStackTrace();
		}
		logger.info("After compression " + comBs.length);
		return comBs;
	}

}
