package practiceschool.utils;

import java.io.ByteArrayOutputStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;




import com.example.practiceschool.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.View;
import android.widget.ImageView;

public class Constants {
	/** 
	 ******************************************* 参数设置信息开始 ******************************************
	 */
	// Node服务器路径
	public static final String NODE_URL = "http://120.55.99.230";
	 
	//微信SDK的APPID
	public static final String APP_ID = "wxf8c2bf5f7edb595f";

	// 线程个数
	public static int POOL_SIZE = 100;

	public static int THUMBNAILS_SIZE = 4;

	// 获取当前系统的CPU 数目
	public static int CPU_NUMS = Runtime.getRuntime().availableProcessors();

	private Constants() {
	}

	// 配置
	public static class Config {
		public static final boolean DEVELOPER_MODE = false;
	}

	// 额外类
	public static class Extra {
		public static final String IMAGES = "com.nostra13.example.universalimageloader.IMAGES";
		public static final String IMAGE_POSITION = "com.nostra13.example.universalimageloader.IMAGE_POSITION";
	}

	//判断网络是否可用
	public static boolean isNetworkConnected(Context context) {
		if (context != null) {
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
			if (mNetworkInfo != null) {
				return mNetworkInfo.isAvailable();
			}
		}
		return false;
	}
	
	// 设置图片下载期间显示的图片
	public static DisplayImageOptions options = new DisplayImageOptions.Builder().showStubImage(R.drawable.ic_stub) 
			.showImageForEmptyUri(R.drawable.ic_empty) // 设置图片Uri为空或是错误的时候显示的图片
			.showImageOnFail(R.drawable.ic_error)      // 设置图片加载或解码过程中发生错误显示的图片
			.bitmapConfig(Bitmap.Config.RGB_565)       // 设置图片的质量
			.imageScaleType(ImageScaleType.IN_SAMPLE_INT)  // 设置图片的缩放类型，该方法可以有效减少内存的占用
			.cacheInMemory(true)                           // 设置下载的图片是否缓存在内存中
			.cacheOnDisc(true)   // 设置下载的图片是否缓存在SD卡中
			.build();            // 创建配置过的DisplayImageOption对象; // DisplayImageOptions是用于设置图片显示的类
	
	// 设置图片下载期间显示的图片(大图)
	public static DisplayImageOptions options_big = new DisplayImageOptions.Builder().showStubImage(R.drawable.ic_stub_big) 
			.showImageForEmptyUri(R.drawable.ic_empty) // 设置图片Uri为空或是错误的时候显示的图片
			.showImageOnFail(R.drawable.ic_error)      // 设置图片加载或解码过程中发生错误显示的图片
			.bitmapConfig(Bitmap.Config.RGB_565)       // 设置图片的质量
			.imageScaleType(ImageScaleType.IN_SAMPLE_INT)  // 设置图片的缩放类型，该方法可以有效减少内存的占用
			.cacheInMemory(true)                           // 设置下载的图片是否缓存在内存中
			.cacheOnDisc(true)   // 设置下载的图片是否缓存在SD卡中
			.build();            // 创建配置过得DisplayImageOption对象; // DisplayImageOptions是用于设置图片显示的类
	
	public static ImageLoader imageLoader = ImageLoader.getInstance(); // 图片异步下载类，包括图片的软应用缓存以及将图片存放到SDCard或者文件中
	public static ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener(); // 图片加载第一次显示监听器
	
	//转换用户头衔
	public static String SwitchRankText(int rank){
		String rankText = "实习生(1)";
		if(rank>=2 && rank<5){
			rankText = "正式员工"+"("+String.valueOf(rank)+")";
		}else if(rank>=5 && rank<9){
			rankText = "主管"+"("+String.valueOf(rank)+")";
		}else if(rank>=10 && rank<15){
			rankText = "经理"+"("+String.valueOf(rank)+")";
		}else if(rank>=16 && rank<20){
			rankText = "区域经理"+"("+String.valueOf(rank)+")";
		}else if(rank>=21 && rank<25){
			rankText = "高级经理"+"("+String.valueOf(rank)+")";
		}else if(rank>=26 && rank<32){
			rankText = "总监"+"("+String.valueOf(rank)+")";
		}else if(rank>=33 && rank<39){
			rankText = "高级总监"+"("+String.valueOf(rank)+")";
		}else if(rank>=40 && rank<47){
			rankText = "副总"+"("+String.valueOf(rank)+")";
		}else if(rank>=48 && rank<55){
			rankText = "总经理"+"("+String.valueOf(rank)+")";
		}else if(rank>=56 && rank<63){
			rankText = "CEO"+"("+String.valueOf(rank)+")";
		}else if(rank>=64){
			rankText = "董事长"+"("+String.valueOf(rank)+")";
		}
		return rankText;
	}
	
	//随机获取导师封面
	public static int setRandomCover(){
		int n=1+(int)(Math.random()*9);
		int cover = 1;
		switch (n) {
		case 1:
			cover = R.drawable.tutor_banner_bg1;
			break;
		case 2:
			cover = R.drawable.tutor_banner_bg2;
			break;
		case 3:
			cover = R.drawable.tutor_banner_bg3;
			break;
		case 4:
			cover = R.drawable.tutor_banner_bg4;
			break;
		case 5:
			cover = R.drawable.tutor_banner_bg5;
			break;
		case 6:
			cover = R.drawable.tutor_banner_bg6;
			break;
		case 7:
			cover = R.drawable.tutor_banner_bg7;
			break;
		case 8:
			cover = R.drawable.tutor_banner_bg8;
			break;
		case 9:
			cover = R.drawable.tutor_banner_bg9;
			break;
		default:
			break;
		}
		return cover;
	}
	
	//转换导师推荐值等级
	public static String SwitchRecommandText(int rank){
		String rankText = "☆";
		if(rank==2){
			rankText = "★";
		}else if(rank==3){
			rankText = "★☆";
		}else if(rank==4){
			rankText = "★★";
		}else if(rank==5){
			rankText = "★★☆";
		}else if(rank==6){
			rankText = "★★★";
		}else if(rank==7){
			rankText = "★★★☆";
		}else if(rank==8){
			rankText = "★★★★";
		}else if(rank==9){
			rankText = "★★★★☆";
		}else if(rank==10){
			rankText = "★★★★★";
		}
		return rankText;
	}
	
	//获取文章类型
	public static String getType(String type){
		String title = null;
		if(type.equals("exercise")){
			title = "健身无敌";
		}else if(type.equals("girls")){
			title = "热血泡妞";
		}else if(type.equals("ask")){
			title = "有什么要问";
		}else if(type.equals("learning")){
			title = "职场之道";
		}else if(type.equals("collect")){
			title = "我的收藏";
		}
		return title;
	}
	
	//当前用户信息	
	public static String app_userid = "";
	public static String app_username ="";
	public static String app_usersex = "";
	public static String app_userpic = "";
	public static int app_userrank = 1;
	public static String app_userrankText = "";
	public static String app_userscore = "";


	/**
	 * 图片加载第一次显示监听器
	 */
	private static class AnimateFirstDisplayListener extends SimpleImageLoadingListener {

		static final List<String> displayedImages = Collections.synchronizedList(new LinkedList<String>());

		@Override
		public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
			if (loadedImage != null) {
				ImageView imageView = (ImageView) view;
				// 是否第一次显示
				boolean firstDisplay = !displayedImages.contains(imageUri);
				if (firstDisplay) {
					// 图片淡入效果
					FadeInBitmapDisplayer.animate(imageView, 500);
					displayedImages.add(imageUri);
				}
			}
		}

		@Override
		public void onLoadingStarted(String imageUri, View view) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
			// TODO Auto-generated method stub
			switch (failReason.getType()) {
			
				case IO_ERROR:
					// handler.sendEmptyMessage();
					break;
				case DECODING_ERROR:
					break;
				case NETWORK_DENIED:
					break;
				case OUT_OF_MEMORY:
					break;
				case UNKNOWN:
					break;
				default:
					break;
			}
		}

		@Override
		public void onLoadingCancelled(String imageUri, View view) {
			// TODO Auto-generated method stub

		}
	}

	public static byte[] bmpToByteArray(final Bitmap bmp, final boolean needRecycle) {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		bmp.compress(CompressFormat.PNG, 100, output);
		if (needRecycle) {
			bmp.recycle();
		}
		
		byte[] result = output.toByteArray();
		try {
			output.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return result;
	}
	
	public static String buildTransaction(final String type) {
		return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
	}
	
}
