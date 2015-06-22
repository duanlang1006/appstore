package com.kapps.market.util;

import java.io.File;

/**
 * �ļ����������
 * 
 * @author admin
 * 
 */
public class FileHelper {
	private boolean isMusic(File file) {
		return file.getPath().endsWith("mp3");
	}

	private boolean isDocument(File file) {
		return file.getPath().endsWith("txt");
	}
}
