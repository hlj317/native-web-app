package practiceschool.network;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.os.Environment;

public class HttpMethod {

	// POST����
	public static String loginByPost(String ajaxUrl, String ajaxData) {
		// �ύ���ݵ�������
		try {
			URL url = new URL(ajaxUrl);

			// ����HTTP�������
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();

			// ��ʱʱ��
			conn.setReadTimeout(30000);          //���ô�������ȡ���ݳ�ʱ����λ�����룩
			conn.setConnectTimeout(30000);		 //��������������ʱ����λ�����룩			

			// ����ʽ
			conn.setRequestMethod("POST");

			// ׼������
			String data = ajaxData;

			// ����HTTP����ͷ
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			conn.setRequestProperty("Charset", "UTF-8");
			conn.setRequestProperty("Content-Length", data.length() + "");

			// post�ķ�ʽʵ�����������������д���˷�����
			conn.setDoOutput(true);
			OutputStream os = conn.getOutputStream();
			os.write(data.getBytes());
			os.flush();
			os.close();

			// ��ȡ���󷵻���
			int code = conn.getResponseCode();
			if (code == 200) {
				// ����ɹ������ļ���ת��һ���ַ�������
				InputStream is = conn.getInputStream();
				String text = readInputStream(is);
				return text;
			} else {
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;

	}

	// �ϴ�ͼƬ
	public static String uploadFile(String ajaxUrl, String ajaxData) {
		String end = "\r\n";
		String twoHyphens = "--";
		String boundary = "*****";
		String newName ="test1.jpg";
		String uploadFile =Environment.getExternalStorageDirectory()+"/test1.jpg";
		try {
			URL url = new URL(ajaxUrl);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			/* ����Input��Output����ʹ��Cache */
			con.setDoInput(true);
			con.setDoOutput(true);
			con.setUseCaches(false);
			/* ���ô��͵�method=POST */
			con.setRequestMethod("POST");
			/* setRequestProperty */
			con.setRequestProperty("Connection", "Keep-Alive");
			con.setRequestProperty("Charset", "UTF-8");
			con.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
			/* ����DataOutputStream */
			DataOutputStream ds = new DataOutputStream(con.getOutputStream());
			ds.writeBytes(twoHyphens + boundary + end);
			ds.writeBytes("Content-Disposition: form-data; " + "name=\"file1\";imgData=\"" + newName + "\"" + end);
			ds.writeBytes(end);
			/* ȡ���ļ���FileInputStream */
			FileInputStream fStream = new FileInputStream(uploadFile);
			/* ����ÿ��д��1024bytes */
			int bufferSize = 1024;
			byte[] buffer = new byte[bufferSize];
			int length = -1;
			/* ���ļ���ȡ������������ */
			while ((length = fStream.read(buffer)) != -1) {
				/* ������д��DataOutputStream�� */
				ds.write(buffer, 0, length);
			}
			ds.writeBytes(end);
			ds.writeBytes(twoHyphens + boundary + twoHyphens + end);
			/* close streams */
			fStream.close();
			ds.flush();
			/* ȡ��Response���� */
			InputStream is = con.getInputStream();
			int ch;
			StringBuffer b = new StringBuffer();
			while ((ch = is.read()) != -1) {
				b.append((char) ch);
			}
			/* ��Response��ʾ��Dialog */
			//showDialog("�ϴ��ɹ�" + b.toString().trim());
			/* �ر�DataOutputStream */
			ds.close();
		} catch (Exception e) {
			//showDialog("�ϴ�ʧ��" + e);
		}
		return null;
	}

	// GET����
	public static String loginByGet(String ajaxUrl, String ajaxData) {
		try {

			String path = ajaxUrl + "?" + ajaxData;
			URL url = new URL(path);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setReadTimeout(30000);          //���ô�������ȡ���ݳ�ʱ����λ�����룩
			conn.setConnectTimeout(30000);		 //��������������ʱ����λ�����룩
			conn.setRequestMethod("GET");

			int code = conn.getResponseCode();
			if (code == 200) {

				InputStream is = conn.getInputStream();
				String text = readInputStream(is);
				return text;
			} else {

				return null;

			}

		} catch (Exception e) {

			e.printStackTrace();
		}
		return null;

	}

	// ��ȡ�ļ���
	public static String readInputStream(InputStream is) {

		try {

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			int len = 0;
			byte[] buffer = new byte[1024];
			while ((len = is.read(buffer)) != -1) {

				baos.write(buffer, 0, len);
			}
			is.close();
			baos.close();
			byte[] result = baos.toByteArray();
			String temp = new String(result);
			return temp;

		} catch (Exception e) {

			e.printStackTrace();
			return "��ȡʧ��";
		}
	}

}
