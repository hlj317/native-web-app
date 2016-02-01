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
	 ******************************************* ����������Ϣ��ʼ ******************************************
	 */
	// Node������·��
	public static final String NODE_URL = "http://120.55.99.230";
	 
	//΢��SDK��APPID
	public static final String APP_ID = "wxf8c2bf5f7edb595f";

	// �̸߳���
	public static int POOL_SIZE = 100;

	public static int THUMBNAILS_SIZE = 4;

	// ��ȡ��ǰϵͳ��CPU ��Ŀ
	public static int CPU_NUMS = Runtime.getRuntime().availableProcessors();

	private Constants() {
	}

	// ����
	public static class Config {
		public static final boolean DEVELOPER_MODE = false;
	}

	// ������
	public static class Extra {
		public static final String IMAGES = "com.nostra13.example.universalimageloader.IMAGES";
		public static final String IMAGE_POSITION = "com.nostra13.example.universalimageloader.IMAGE_POSITION";
	}

	//�ж������Ƿ����
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
	
	// ����ͼƬ�����ڼ���ʾ��ͼƬ
	public static DisplayImageOptions options = new DisplayImageOptions.Builder().showStubImage(R.drawable.ic_stub) 
			.showImageForEmptyUri(R.drawable.ic_empty) // ����ͼƬUriΪ�ջ��Ǵ����ʱ����ʾ��ͼƬ
			.showImageOnFail(R.drawable.ic_error)      // ����ͼƬ���ػ��������з���������ʾ��ͼƬ
			.bitmapConfig(Bitmap.Config.RGB_565)       // ����ͼƬ������
			.imageScaleType(ImageScaleType.IN_SAMPLE_INT)  // ����ͼƬ���������ͣ��÷���������Ч�����ڴ��ռ��
			.cacheInMemory(true)                           // �������ص�ͼƬ�Ƿ񻺴����ڴ���
			.cacheOnDisc(true)   // �������ص�ͼƬ�Ƿ񻺴���SD����
			.build();            // �������ù���DisplayImageOption����; // DisplayImageOptions����������ͼƬ��ʾ����
	
	// ����ͼƬ�����ڼ���ʾ��ͼƬ(��ͼ)
	public static DisplayImageOptions options_big = new DisplayImageOptions.Builder().showStubImage(R.drawable.ic_stub_big) 
			.showImageForEmptyUri(R.drawable.ic_empty) // ����ͼƬUriΪ�ջ��Ǵ����ʱ����ʾ��ͼƬ
			.showImageOnFail(R.drawable.ic_error)      // ����ͼƬ���ػ��������з���������ʾ��ͼƬ
			.bitmapConfig(Bitmap.Config.RGB_565)       // ����ͼƬ������
			.imageScaleType(ImageScaleType.IN_SAMPLE_INT)  // ����ͼƬ���������ͣ��÷���������Ч�����ڴ��ռ��
			.cacheInMemory(true)                           // �������ص�ͼƬ�Ƿ񻺴����ڴ���
			.cacheOnDisc(true)   // �������ص�ͼƬ�Ƿ񻺴���SD����
			.build();            // �������ù���DisplayImageOption����; // DisplayImageOptions����������ͼƬ��ʾ����
	
	public static ImageLoader imageLoader = ImageLoader.getInstance(); // ͼƬ�첽�����࣬����ͼƬ����Ӧ�û����Լ���ͼƬ��ŵ�SDCard�����ļ���
	public static ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener(); // ͼƬ���ص�һ����ʾ������
	
	//ת���û�ͷ��
	public static String SwitchRankText(int rank){
		String rankText = "ʵϰ��(1)";
		if(rank>=2 && rank<5){
			rankText = "��ʽԱ��"+"("+String.valueOf(rank)+")";
		}else if(rank>=5 && rank<9){
			rankText = "����"+"("+String.valueOf(rank)+")";
		}else if(rank>=10 && rank<15){
			rankText = "����"+"("+String.valueOf(rank)+")";
		}else if(rank>=16 && rank<20){
			rankText = "������"+"("+String.valueOf(rank)+")";
		}else if(rank>=21 && rank<25){
			rankText = "�߼�����"+"("+String.valueOf(rank)+")";
		}else if(rank>=26 && rank<32){
			rankText = "�ܼ�"+"("+String.valueOf(rank)+")";
		}else if(rank>=33 && rank<39){
			rankText = "�߼��ܼ�"+"("+String.valueOf(rank)+")";
		}else if(rank>=40 && rank<47){
			rankText = "����"+"("+String.valueOf(rank)+")";
		}else if(rank>=48 && rank<55){
			rankText = "�ܾ���"+"("+String.valueOf(rank)+")";
		}else if(rank>=56 && rank<63){
			rankText = "CEO"+"("+String.valueOf(rank)+")";
		}else if(rank>=64){
			rankText = "���³�"+"("+String.valueOf(rank)+")";
		}
		return rankText;
	}
	
	//�����ȡ��ʦ����
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
	
	//ת����ʦ�Ƽ�ֵ�ȼ�
	public static String SwitchRecommandText(int rank){
		String rankText = "��";
		if(rank==2){
			rankText = "��";
		}else if(rank==3){
			rankText = "���";
		}else if(rank==4){
			rankText = "���";
		}else if(rank==5){
			rankText = "����";
		}else if(rank==6){
			rankText = "����";
		}else if(rank==7){
			rankText = "�����";
		}else if(rank==8){
			rankText = "�����";
		}else if(rank==9){
			rankText = "������";
		}else if(rank==10){
			rankText = "������";
		}
		return rankText;
	}
	
	//��ȡ��������
	public static String getType(String type){
		String title = null;
		if(type.equals("exercise")){
			title = "�����޵�";
		}else if(type.equals("girls")){
			title = "��Ѫ���";
		}else if(type.equals("ask")){
			title = "��ʲôҪ��";
		}else if(type.equals("learning")){
			title = "ְ��֮��";
		}else if(type.equals("collect")){
			title = "�ҵ��ղ�";
		}
		return title;
	}
	
	//��ǰ�û���Ϣ	
	public static String app_userid = "";
	public static String app_username ="";
	public static String app_usersex = "";
	public static String app_userpic = "";
	public static int app_userrank = 1;
	public static String app_userrankText = "";
	public static String app_userscore = "";


	/**
	 * ͼƬ���ص�һ����ʾ������
	 */
	private static class AnimateFirstDisplayListener extends SimpleImageLoadingListener {

		static final List<String> displayedImages = Collections.synchronizedList(new LinkedList<String>());

		@Override
		public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
			if (loadedImage != null) {
				ImageView imageView = (ImageView) view;
				// �Ƿ��һ����ʾ
				boolean firstDisplay = !displayedImages.contains(imageUri);
				if (firstDisplay) {
					// ͼƬ����Ч��
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
