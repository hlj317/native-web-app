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

	// POST请求
	public static String loginByPost(String ajaxUrl, String ajaxData) {
		// 提交数据到服务器
		try {
			URL url = new URL(ajaxUrl);

			// 建立HTTP请求对象
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();

			// 超时时间
			conn.setReadTimeout(30000);          //设置从主机读取数据超时（单位：毫秒）
			conn.setConnectTimeout(30000);		 //设置连接主机超时（单位：毫秒）			

			// 请求方式
			conn.setRequestMethod("POST");

			// 准备数据
			String data = ajaxData;

			// 设置HTTP请求头
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			conn.setRequestProperty("Charset", "UTF-8");
			conn.setRequestProperty("Content-Length", data.length() + "");

			// post的方式实际上是浏览器把数据写给了服务器
			conn.setDoOutput(true);
			OutputStream os = conn.getOutputStream();
			os.write(data.getBytes());
			os.flush();
			os.close();

			// 获取请求返回码
			int code = conn.getResponseCode();
			if (code == 200) {
				// 请求成功，把文件流转成一个字符串返回
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

	// 上传图片
	public static String uploadFile(String ajaxUrl, String ajaxData) {
		String end = "\r\n";
		String twoHyphens = "--";
		String boundary = "*****";
		String newName ="test1.jpg";
		String uploadFile =Environment.getExternalStorageDirectory()+"/test1.jpg";
		try {
			URL url = new URL(ajaxUrl);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			/* 允许Input、Output，不使用Cache */
			con.setDoInput(true);
			con.setDoOutput(true);
			con.setUseCaches(false);
			/* 设置传送的method=POST */
			con.setRequestMethod("POST");
			/* setRequestProperty */
			con.setRequestProperty("Connection", "Keep-Alive");
			con.setRequestProperty("Charset", "UTF-8");
			con.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
			/* 设置DataOutputStream */
			DataOutputStream ds = new DataOutputStream(con.getOutputStream());
			ds.writeBytes(twoHyphens + boundary + end);
			ds.writeBytes("Content-Disposition: form-data; " + "name=\"file1\";imgData=\"" + newName + "\"" + end);
			ds.writeBytes(end);
			/* 取得文件的FileInputStream */
			FileInputStream fStream = new FileInputStream(uploadFile);
			/* 设置每次写入1024bytes */
			int bufferSize = 1024;
			byte[] buffer = new byte[bufferSize];
			int length = -1;
			/* 从文件读取数据至缓冲区 */
			while ((length = fStream.read(buffer)) != -1) {
				/* 将资料写入DataOutputStream中 */
				ds.write(buffer, 0, length);
			}
			ds.writeBytes(end);
			ds.writeBytes(twoHyphens + boundary + twoHyphens + end);
			/* close streams */
			fStream.close();
			ds.flush();
			/* 取得Response内容 */
			InputStream is = con.getInputStream();
			int ch;
			StringBuffer b = new StringBuffer();
			while ((ch = is.read()) != -1) {
				b.append((char) ch);
			}
			/* 将Response显示于Dialog */
			//showDialog("上传成功" + b.toString().trim());
			/* 关闭DataOutputStream */
			ds.close();
		} catch (Exception e) {
			//showDialog("上传失败" + e);
		}
		return null;
	}

	// GET请求
	public static String loginByGet(String ajaxUrl, String ajaxData) {
		try {

			String path = ajaxUrl + "?" + ajaxData;
			URL url = new URL(path);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setReadTimeout(30000);          //设置从主机读取数据超时（单位：毫秒）
			conn.setConnectTimeout(30000);		 //设置连接主机超时（单位：毫秒）
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

	// 读取文件流
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
			return "获取失败";
		}
	}

}
