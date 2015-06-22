package com.kapps.market.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * ������
 * 
 * @author admin
 * 
 */
public class SecurityUtil {

	private static String base64_random = "httpstd";

	/**
	 * ���ַ����Ϊmd5��ʽ
	 * 
	 * @param value
	 * @return
	 */
	public static String md5Encode(String value) {
		String tmp = null;
		try {
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			md5.update(value.getBytes("utf8"));
			byte[] md = md5.digest();
			tmp = binToHex(md);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return tmp;
	}

	public static String binToHex(byte[] md) {
		StringBuffer sb = new StringBuffer("");
		int read = 0;
		for (int i = 0; i < md.length; i++) {
			read = md[i];
			if (read < 0)
				read += 256;
			if (read < 16)
				sb.append("0");
			sb.append(Integer.toHexString(read));
		}
		return sb.toString();
	}

	/**
	 * base64����
	 * 
	 * @param value
	 *            �ַ�
	 * @return
	 */
	public static String encodeBase64(String value) {
		return base64_random + Base64.encode(value);
	}

	/**
	 * base64����
	 * 
	 * @param value
	 *            �ַ�
	 * @param random
	 *            ������
	 * @return
	 */
	public static String decodeBase64(String value) {
		if (value == null || value.length() <= base64_random.length()) {
			return value;

		} else {
			int count = value.length();
			int last = base64_random.length();
			return Base64.decode(value.substring(last, count), "utf-8");
		}
	}

}
