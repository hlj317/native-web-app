package practiceschool.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import practiceschool.ui.MainActivity;


import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.util.Log;
import android.widget.ImageView;

@TargetApi(19)
public class ImageUtils {
	
	public static final int GET_IMAGE_BY_CAMERA = 5001;
	public static final int GET_IMAGE_FROM_PHONE = 5002;
	public static final int CROP_IMAGE = 5003;
	public static Uri imageUriFromCamera;
	public static Uri cropImageUri;
	public static Uri smallImageUri;
	
	public static Uri imageUri;
	
	/** ��ȡ����ͼƬ·�� */
	public static String picPath;
	
	private static final String TAG = BitmapUtils.class.getSimpleName();

	public static void openCameraImage(final Activity activity) {
		
		//�������յ�ͼƬ��ַ
		ImageUtils.imageUriFromCamera = ImageUtils.createImagePathUri(activity);
		 
		//����һ�����յ�intent
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		// MediaStore.EXTRA_OUTPUT����������ʱ,ϵͳ���Զ�����һ��uri,����ֻ�᷵��һ������ͼ
		// ����ͼƬ��onActivityResult��ͨ�����´����ȡ
		// Bitmap bitmap = (Bitmap) data.getExtras().get("data");
		
		//�������պ�ͼƬ��ŵ�λ��
		intent.putExtra(MediaStore.EXTRA_OUTPUT, ImageUtils.imageUriFromCamera);
		
		//�������գ����뷵����
		activity.startActivityForResult(intent, ImageUtils.GET_IMAGE_BY_CAMERA);
		
		
	}

	
	public static void openLocalImage(final Activity activity) {
		//�������յ�ͼƬ��ַ
		
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		intent.setType("image/*");
		activity.startActivityForResult(intent, ImageUtils.GET_IMAGE_FROM_PHONE);

	}



	//ͼƬ·��URiתstring��ʽ
	@TargetApi(19)
	public static String getRealPathFromURI(Uri imageUri,final Activity context) {  
	    if (context == null || imageUri == null)  
	        return null;  
	    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT && DocumentsContract.isDocumentUri(context, imageUri)) {  
	        if (isExternalStorageDocument(imageUri)) {  
	            String docId = DocumentsContract.getDocumentId(imageUri);  
	            String[] split = docId.split(":");  
	            String type = split[0];  
	            if ("primary".equalsIgnoreCase(type)) {  
	                return Environment.getExternalStorageDirectory() + "/" + split[1];  
	            }  
	        } else if (isDownloadsDocument(imageUri)) {  
	            String id = DocumentsContract.getDocumentId(imageUri);  
	            Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));  
	            return getDataColumn(context, contentUri, null, null);  
	        } else if (isMediaDocument(imageUri)) {  
	            String docId = DocumentsContract.getDocumentId(imageUri);  
	            String[] split = docId.split(":");  
	            String type = split[0];  
	            Uri contentUri = null;  
	            if ("image".equals(type)) {  
	                contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;  
	            } else if ("video".equals(type)) {  
	                contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;  
	            } else if ("audio".equals(type)) {  
	                contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;  
	            }  
	            String selection = MediaStore.Images.Media._ID + "=?";  
	            String[] selectionArgs = new String[] { split[1] };  
	            return getDataColumn(context, contentUri, selection, selectionArgs);  
	        }  
	    } // MediaStore (and general)  
	    else if ("content".equalsIgnoreCase(imageUri.getScheme())) {  
	        // Return the remote address  
	        if (isGooglePhotosUri(imageUri))  
	            return imageUri.getLastPathSegment();  
	        return getDataColumn(context, imageUri, null, null);  
	    }  
	    // File  
	    else if ("file".equalsIgnoreCase(imageUri.getScheme())) {  
	        return imageUri.getPath();  
	    }  
	    return null;  
	}  
	  
	public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {  
	    Cursor cursor = null;  
	    String column = MediaStore.Images.Media.DATA;  
	    String[] projection = { column };  
	    try {  
	        cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);  
	        if (cursor != null && cursor.moveToFirst()) {  
	            int index = cursor.getColumnIndexOrThrow(column);  
	            return cursor.getString(index);  
	        }  
	    } finally {  
	        if (cursor != null)  
	            cursor.close();  
	    }  
	    return null;  
	}  
	  
	/** 
	 * @param uri The Uri to check. 
	 * @return Whether the Uri authority is ExternalStorageProvider. 
	 */  
	public static boolean isExternalStorageDocument(Uri uri) {  
	    return "com.android.externalstorage.documents".equals(uri.getAuthority());  
	}  
	  
	/** 
	 * @param uri The Uri to check. 
	 * @return Whether the Uri authority is DownloadsProvider. 
	 */  
	public static boolean isDownloadsDocument(Uri uri) {  
	    return "com.android.providers.downloads.documents".equals(uri.getAuthority());  
	}  
	  
	/** 
	 * @param uri The Uri to check. 
	 * @return Whether the Uri authority is MediaProvider. 
	 */  
	public static boolean isMediaDocument(Uri uri) {  
	    return "com.android.providers.media.documents".equals(uri.getAuthority());  
	}  
	  
	/**   
	 * @param uri The Uri to check. 
	 * @return Whether the Uri authority is Google Photos. 
	 */  
	public static boolean isGooglePhotosUri(Uri uri) {  
	    return "com.google.android.apps.photos.content".equals(uri.getAuthority());  
	}	 
	
	public static void cropImage(Activity activity, Uri srcUri ,Boolean cover) {

		if (srcUri == null) {
			Log.i("tag", "The uri is not exist.");
			return;
		}

		Intent intent = new Intent("com.android.camera.action.CROP");
		picPath = getRealPathFromURI(srcUri,activity);
		intent.setDataAndType(Uri.fromFile(new File(picPath)), "image/*");

		// ���òü�
		intent.putExtra("crop", "true");
		// aspectX aspectY �ǿ�ߵı���
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		// outputX outputY �ǲü�ͼƬ���
		intent.putExtra("outputX", 200);
		intent.putExtra("outputY", 200);
		intent.putExtra("return-data", true);
		activity.startActivityForResult(intent, CROP_IMAGE);
		
	}

	/**
	 * ����һ��ͼƬ��ַuri,���ڱ������պ����Ƭ
	 * 
	 * @param context
	 * @return ͼƬ��uri
	 */
	private static Uri createImagePathUri(Context context) {
		Uri imageFilePath = null;

		// �ж�SD��״̬
		String status = Environment.getExternalStorageState();

		// ����һ��ʱ��
		SimpleDateFormat timeFormatter = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA);
		long time = System.currentTimeMillis();

		// ΪͼƬ����
		String imageName = timeFormatter.format(new Date(time));

		// ContentValues������ϣ��������¼������ʱ������������Ϣ
		// ContentValues��һ�ִ洢�Ļ��ƣ�ֻ�ܴ洢�������͵����ݣ�HashTable����Դ洢����
		ContentValues values = new ContentValues(3);

		// ���ֻ�DCIMĿ¼����ͼƬ���ơ�ʱ�䡢����
		values.put(MediaStore.Images.Media.DISPLAY_NAME, imageName);
		values.put(MediaStore.Images.Media.DATE_TAKEN, time);
		values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");

		if (status.equals(Environment.MEDIA_MOUNTED)) {// �ж��Ƿ���SD��,����ʹ��SD���洢,��û��SD��ʱʹ���ֻ��洢ͼƬ
			imageFilePath = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
		} else {
			imageFilePath = context.getContentResolver().insert(MediaStore.Images.Media.INTERNAL_CONTENT_URI, values);
		}

		Log.i("", "���ɵ���Ƭ���·����" + imageFilePath.toString());
		return imageFilePath;
	}
	
	
	/*ѹ��ͼƬ*/
    public static void compressBitmap(String sourcePath, String targetPath, float maxSize) {

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(sourcePath, options);

        final float originalWidth = options.outWidth;
        final float originalHeight = options.outHeight;

        float convertedWidth;
        float convertedHeight;

        if (originalWidth > originalHeight) {
            convertedWidth = maxSize;
            convertedHeight = maxSize / originalWidth * originalHeight;
        } else {
            convertedHeight = maxSize;
            convertedWidth = maxSize / originalHeight * originalWidth;
        }


        final float ratio = originalWidth / convertedWidth;

        options.inSampleSize = (int) ratio;
        options.inJustDecodeBounds = false;

        Bitmap convertedBitmap = BitmapFactory.decodeFile(sourcePath, options);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        convertedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
        FileOutputStream fileOutputStream;
        try {
            fileOutputStream = new FileOutputStream(new File(targetPath));
            fileOutputStream.write(byteArrayOutputStream.toByteArray());
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * ����URI��ȡͼƬ����·��
     */
    public static String getAbsoluteImagePath(Uri uri, Activity activity) {

        String[] proj = {
            MediaColumns.DATA
        };
        Cursor cursor = activity.managedQuery(uri, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaColumns.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    /**
     *
     * @param path
     * @param maxSize
     * @return
     */
    public static Bitmap decodeBitmap(String path, int maxSize) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(path, options);

        final int originalWidth = options.outWidth;
        final int originalHeight = options.outHeight;

        int convertedWidth;
        int convertedHeight;

        if (originalWidth > originalHeight) {
            convertedWidth = maxSize;
            convertedHeight = maxSize / originalWidth * originalHeight;
        } else {
            convertedHeight = maxSize;
            convertedWidth = maxSize / originalHeight * originalWidth;
        }

        options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;
        options.inSampleSize = computeSampleSize(options, maxSize, convertedWidth * convertedHeight);

        Bitmap convertedBitmap = BitmapFactory.decodeFile(path, options);

        if (convertedBitmap != null) {
            final int realWidth = convertedBitmap.getWidth();
            final int realHeight = convertedBitmap.getHeight();

        }

        return convertedBitmap;
    }

    /**
     *
     * @param path
     * @param maxWidth
     * @param maxHeight
     * @return
     */
    public static Bitmap decodeBitmap(String path, int maxWidth, int maxHeight) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(path, options);

        final int originalWidth = options.outWidth;
        final int originalHeight = options.outHeight;

        options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;
        options.inSampleSize = computeSampleSize(options, maxWidth, maxWidth * maxHeight);

        Bitmap convertedBitmap = BitmapFactory.decodeFile(path, options);

        if (convertedBitmap != null) {
            final int realWidth = convertedBitmap.getWidth();
            final int realHeight = convertedBitmap.getHeight();

        }

        return convertedBitmap;
    }

    private static int computeInitialSampleSize(BitmapFactory.Options options, int minSideLength, int maxNumOfPixels) {

        double w = options.outWidth;
        double h = options.outHeight;
        int lowerBound = (maxNumOfPixels == -1) ? 1 : (int) Math.ceil(Math.sqrt(w * h / maxNumOfPixels));
        int upperBound = (minSideLength == -1) ? minSideLength
                : (int) Math.min(Math.floor(w / minSideLength), Math.floor(h / minSideLength));
        if (upperBound < lowerBound) {
            return lowerBound;
        }
        if ((maxNumOfPixels == -1) && (minSideLength == -1)) {
            return 1;
        } else if (minSideLength == -1) {
            return lowerBound;
        } else {
            return upperBound;
        }
    }

    private static int computeSampleSize(BitmapFactory.Options options, int minSideLength, int maxNumOfPixels) {

        int initialSize = computeInitialSampleSize(options, minSideLength, maxNumOfPixels);
        int roundedSize;
        if (initialSize <= 8) {
            roundedSize = 1;
            while (roundedSize < initialSize) {
                roundedSize <<= 1;
            }
        } else {
            roundedSize = (initialSize + 7) / 8 * 8;
        }
        return roundedSize;
    }


    /**
     * ����8λ16���ƵĻ������ӣ������8λ��ϣ�룬����ǰ�油��
     * @param string
     * @return
     */
    public static String toRegularHashCode(String string) {
        final String hexHashCode = Integer.toHexString(string.hashCode());
        final StringBuilder stringBuilder = new StringBuilder(hexHashCode);
        while(stringBuilder.length() < 8){
            stringBuilder.insert(0, '0');
        }
        return stringBuilder.toString();
    }


}
