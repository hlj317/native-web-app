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
	
	/** 获取到的图片路径 */
	public static String picPath;
	
	private static final String TAG = BitmapUtils.class.getSimpleName();

	public static void openCameraImage(final Activity activity) {
		
		//创建拍照的图片地址
		ImageUtils.imageUriFromCamera = ImageUtils.createImagePathUri(activity);
		 
		//创建一个拍照的intent
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		// MediaStore.EXTRA_OUTPUT参数不设置时,系统会自动生成一个uri,但是只会返回一个缩略图
		// 返回图片在onActivityResult中通过以下代码获取
		// Bitmap bitmap = (Bitmap) data.getExtras().get("data");
		
		//传入拍照后，图片存放的位置
		intent.putExtra(MediaStore.EXTRA_OUTPUT, ImageUtils.imageUriFromCamera);
		
		//启动拍照，传入返回码（startActivityForResult - onActivityResult，这两个事件流是对应关系）
		activity.startActivityForResult(intent, ImageUtils.GET_IMAGE_BY_CAMERA);
		
		
	}

	
	public static void openLocalImage(final Activity activity) {
		//创建拍照的图片地址
		
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		intent.setType("image/*");
		activity.startActivityForResult(intent, ImageUtils.GET_IMAGE_FROM_PHONE);

	}



	//图片路径URi转string格式
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

	/**   
	 * 对图片进行裁剪
	 */  
	public static void cropImage(Activity activity, Uri srcUri ,Boolean cover) {

		if (srcUri == null) {
			Log.i("tag", "The uri is not exist.");
			return;
		}

		Intent intent = new Intent("com.android.camera.action.CROP");
		picPath = getRealPathFromURI(srcUri,activity);
		intent.setDataAndType(Uri.fromFile(new File(picPath)), "image/*");

		// 设置裁剪
		intent.putExtra("crop", "true");
		// aspectX aspectY 是宽高的比例
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		// outputX outputY 是裁剪图片宽高
		intent.putExtra("outputX", 200);
		intent.putExtra("outputY", 200);
		intent.putExtra("return-data", true);
		activity.startActivityForResult(intent, CROP_IMAGE);
		
	}

	/**
	 * 创建一条图片地址uri,用于保存拍照后的照片
	 * 
	 * @param context
	 * @return 图片的uri
	 */
	private static Uri createImagePathUri(Context context) {
		Uri imageFilePath = null;

		// 判断SD卡状态
		String status = Environment.getExternalStorageState();

		// 创建一条时间
		SimpleDateFormat timeFormatter = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA);
		long time = System.currentTimeMillis();

		// 为图片命名
		String imageName = timeFormatter.format(new Date(time));

		// ContentValues是我们希望这条记录被创建时包含的数据信息
		// ContentValues是一种存储的机制，只能存储基本类型的数据，HashTable类可以存储对象
		ContentValues values = new ContentValues(3);

		// 向手机DCIM目录插入图片名称、时间、类型
		values.put(MediaStore.Images.Media.DISPLAY_NAME, imageName);
		values.put(MediaStore.Images.Media.DATE_TAKEN, time);
		values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");

		if (status.equals(Environment.MEDIA_MOUNTED)) {// 判断是否有SD卡,优先使用SD卡存储,当没有SD卡时使用手机存储图片
			imageFilePath = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
		} else {
			imageFilePath = context.getContentResolver().insert(MediaStore.Images.Media.INTERNAL_CONTENT_URI, values);
		}

		Log.i("", "生成的照片输出路径：" + imageFilePath.toString());
		return imageFilePath;
	}
	
	
	/*压缩图片*/
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
     * 根据URI获取图片物理路径
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
     * 生成8位16进制的缓存因子：规则的8位哈希码，不足前面补零
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
