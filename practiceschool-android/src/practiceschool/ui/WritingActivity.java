package practiceschool.ui;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import practiceschool.network.UploadUtil;
import practiceschool.network.UploadUtil.OnUploadProcessListener;
import practiceschool.utils.Constants;
import practiceschool.utils.ImageUtils;
import practiceschool.utils.LeftMenu;


import com.example.practiceschool.R;
import com.umeng.analytics.MobclickAgent;
import com.umeng.message.PushAgent;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

public class WritingActivity extends Activity implements OnUploadProcessListener {

	private long exitTime = 0;  //按返回键，退出时间
	private static final String TAG = "zhihu.ui.WritingActivity";

	private final int TO_UPLOAD_FILE = 1; // 去上传文件
	private final int UPLOAD_FILE_DONE = 2; // 上传文件响应
	private final int TO_SELECT_PHOTO = 3; // 选择文件
	private final int UPLOAD_INIT_PROCESS = 4; // 上传初始化
	private final int UPLOAD_IN_PROCESS = 5; // 上传中
	private final int DOWNLOAD_IMAGE_SUCCESS = 6; // 下载图片成功
	private final int DOWNLOAD_IMAGE_FAILURE = 7; // 下载图片失败
	private String KEY_PHOTO_PATH = "photo_path"; // 从Intent获取图片路径的KEY
	private String requestImgUrl = Constants.NODE_URL + "/addArticle"; // 请求服务器路径
	private Button publishBtn; // 完成注册按钮控件
	private EditText title; // 文章标题（控件）
	private EditText content; // 文章内容（控件）
	private String titleText; // 文章标题
	private String contentText; // 文章内容
	private ImageView cover; // 文章封面
	private ProgressDialog progressDialog; // 加载loading对话框
	private Button channel_exercise; // 类型：健身无敌（控件）
	private Button channel_girls; // 类型：热血泡妞（控件）
	private Button channel_ask; // 类型：有什么要问（控件）
	private Button channel_learning; // 类型：晋升之道（控件）
	private String channelCheck = "4"; // 默认选中职场之道

	private String targetPath;
	private String avatorpath = Environment.getExternalStorageDirectory() + "/articlecover/";
	private Bitmap bitmap;
	public ImageButton title_move_btn;
	/* 封面名称 */
	private final String IMAGE_FILE_NAME = "xueyuan_" + new Date().getTime() + ".jpg";

	/**
	 * 当前用户信息
	 */
	private String userid = Constants.app_userid; // 用户ID号
	private String username = Constants.app_username; // 用户昵称
	private String usersex = Constants.app_usersex; // 用户性别
	private String userpic = Constants.app_userpic; // 用户头像

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE); // 声明使用自定义标题
		setContentView(R.layout.writing_main);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.writing_title); // 自定义标题栏布局赋值
		this.initComponent();

		LeftMenu.initSlidingMenu(WritingActivity.this, "write"); // 初始化左侧菜单
		/**
		 * 初始化切换左侧菜单按钮
		 */
		title_move_btn = (ImageButton) findViewById(R.id.title_move_btn);
		title_move_btn.setOnClickListener(new OnClickListener() {

			public void onClick(View view) {

				LeftMenu.menu.toggle(true);

			}
		});
		
		MobclickAgent.updateOnlineConfig(this);  //友盟统计
		PushAgent.getInstance(this).onAppStart();  //友盟推送
	}

	/**
	 * 初始化布局中的控件
	 */
	private void initComponent() {

		cover = (ImageView) findViewById(R.id.cover);
		publishBtn = (Button) findViewById(R.id.publish_btn);
		progressDialog = new ProgressDialog(this);
		title = (EditText) findViewById(R.id.title);
		content = (EditText) findViewById(R.id.content);

		final Button channel_exercise = (Button) findViewById(R.id.channel_exercise);
		final Button channel_girls = (Button) findViewById(R.id.channel_girls);
		final Button channel_ask = (Button) findViewById(R.id.channel_ask);
		final Button channel_learning = (Button) findViewById(R.id.channel_learning);

		/**
		 * 选中"健身无敌"
		 */
		channel_exercise.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				channel_exercise.setBackgroundResource(R.drawable.channel_focus);
				channel_girls.setBackgroundResource(R.drawable.channel_default);
				channel_ask.setBackgroundResource(R.drawable.channel_default);
				channel_learning.setBackgroundResource(R.drawable.channel_default);
				channelCheck = "1";
			}
		});

		/**
		 * 选中"热血泡妞"
		 */
		channel_girls.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				channel_exercise.setBackgroundResource(R.drawable.channel_default);
				channel_girls.setBackgroundResource(R.drawable.channel_focus);
				channel_ask.setBackgroundResource(R.drawable.channel_default);
				channel_learning.setBackgroundResource(R.drawable.channel_default);
				channelCheck = "2";
			}
		});

		/**
		 * 选中"有什么要问"
		 */
		channel_ask.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				channel_exercise.setBackgroundResource(R.drawable.channel_default);
				channel_girls.setBackgroundResource(R.drawable.channel_default);
				channel_ask.setBackgroundResource(R.drawable.channel_focus);
				channel_learning.setBackgroundResource(R.drawable.channel_default);
				channelCheck = "3";
			}
		});

		/**
		 * 选中"职场之道"
		 */
		channel_learning.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				channel_exercise.setBackgroundResource(R.drawable.channel_default);
				channel_girls.setBackgroundResource(R.drawable.channel_default);
				channel_ask.setBackgroundResource(R.drawable.channel_default);
				channel_learning.setBackgroundResource(R.drawable.channel_focus);
				channelCheck = "4";
			}
		});

		// 测试代码，下载图片
		// String _url =
		// "http://120.55.99.230/public/upload/covers/upload_6999b517a8808cae142e755da1cd4c81.jpg";
		// new Thread(new downloadRunnable(_url,cover)).start();

		// 上传封面
		cover.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				showImagePickDialog();
			}
		});

		// 发布文章
		publishBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {

				if ((ImageUtils.picPath != null) || (targetPath != null)) {
					titleText = title.getText().toString().trim();
					contentText = content.getText().toString().trim();
					if (titleText.equals("")) {
						Toast.makeText(WritingActivity.this, "标题不能为空", Toast.LENGTH_SHORT).show();
					}else if (titleText.length()>25) {
						Toast.makeText(WritingActivity.this, "标题不能超过25个字", Toast.LENGTH_SHORT).show();
					}else if (contentText.equals("")) {
						Toast.makeText(WritingActivity.this, "内容不能为空", Toast.LENGTH_SHORT).show();
					}else{
						handler.sendEmptyMessage(TO_UPLOAD_FILE);				
					}
				} else {
					Toast.makeText(WritingActivity.this, "请上传文章封面", Toast.LENGTH_SHORT).show();
				}

			}
		});
	}

	/**
	 * 获取文章封面
	 */
	public void showImagePickDialog() {

		String title = "获取图片方式";
		String[] choices = new String[] { "拍照", "从手机中选择" };
		new AlertDialog.Builder(this).setTitle(title).setItems(choices, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				switch (which) {
				case 0:
					ImageUtils.openCameraImage(WritingActivity.this);
					break;
				case 1:
					ImageUtils.openLocalImage(WritingActivity.this);
					break;
				}
			}
		}).setNegativeButton("返回", null).show();
	}

	/**
	 * 拍照、从手机中选择图片，返回码处理
	 */
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		// 如果返回码是取消，则返回
		if (resultCode == RESULT_CANCELED) {
			return;
		}

		switch (requestCode) {
		// 拍照获取图片
		case ImageUtils.GET_IMAGE_BY_CAMERA:
			// uri传入与否影响图片获取方式,以下二选一
			// 方式一,自定义Uri(ImageUtils.imageUriFromCamera),用于保存拍照后图片地址
			if (ImageUtils.imageUriFromCamera != null) {
				showImage(ImageUtils.imageUriFromCamera);
			}

			break;
		// 手机相册获取图片
		case ImageUtils.GET_IMAGE_FROM_PHONE:
			if (data != null && data.getData() != null) {
				showImage(data.getData());
			}
			break;
		// 裁剪图片后结果
		case ImageUtils.CROP_IMAGE:
			if (ImageUtils.cropImageUri != null) {

				File fileDir = new File(avatorpath);
				if (!fileDir.exists()) {
					fileDir.mkdirs();// 创建文件夹
				}
				targetPath = avatorpath + IMAGE_FILE_NAME;
				ImageUtils.compressBitmap(ImageUtils.picPath, targetPath, 640); // 压缩
				bitmap = ImageUtils.decodeBitmap(targetPath, 100); // 分解
				cover.setImageBitmap(bitmap); // 显示

			}
			break;
		default:
			break;
		}
	}

	/**
	 * 显示拍照/图库中的图片
	 */
	private void showImage(Uri imageUriFromCamera) {
		File fileDir = new File(avatorpath);
		if (!fileDir.exists()) {
			fileDir.mkdirs();// 创建文件夹
		}
		targetPath = avatorpath + IMAGE_FILE_NAME;
		ImageUtils.picPath = ImageUtils.getRealPathFromURI(imageUriFromCamera, WritingActivity.this);
		ImageUtils.compressBitmap(ImageUtils.picPath, targetPath, 640); // 压缩
		bitmap = ImageUtils.decodeBitmap(targetPath, 100); // 分解
		cover.setImageBitmap(bitmap); // 显示
	

	}

	/**
	 * 提交用户信息到服务器
	 */
	private void toUploadFile() {
		// uploadImageResult.setText("正在上传中...");
		progressDialog.setMessage("正在发布，请稍后...");
		progressDialog.show();
		String fileKey = "upload";
		UploadUtil uploadUtil = UploadUtil.getInstance();
		uploadUtil.setOnUploadProcessListener(this); // 设置监听器监听上传状态
		Map<String, String> params = new HashMap<String, String>();
		System.out.println("****************"+channelCheck);
		params.put("channel", channelCheck);
		params.put("title", titleText);
		params.put("content", contentText);
		params.put("description", getDescription(contentText));
		params.put("writeid", userid);
		params.put("writename", username);
		params.put("writesex", usersex);
		params.put("writepic", userpic);
  
		// 开一个子线程，做请求处理
		uploadUtil.uploadFile(targetPath, fileKey, requestImgUrl, params);
	}

	/**
	 * 获取description内容
	 */
	private String getDescription(String str) {
		String newStr = str.replaceAll("\n", " ");
		if (newStr.length() > 50) {
			newStr = newStr.substring(0, 50) + "...";
		}
		return newStr;
	}

	/**
	 * 异步消息回调处理器
	 */
	static class MyHandler extends Handler {
		WeakReference<Activity> mActivityReference;

		MyHandler(Activity activity) {
			mActivityReference = new WeakReference<Activity>(activity);
		}
	}

	private Handler handler = new MyHandler(this) {
		@Override
		public void handleMessage(Message msg) {
			final Activity activity = mActivityReference.get();
			if (activity != null) {
				switch (msg.what) {
				case TO_UPLOAD_FILE:
					toUploadFile();
					break;
				case UPLOAD_INIT_PROCESS:
					// progressBar.setMax(msg.arg1);
					break;
				case UPLOAD_IN_PROCESS:
					// progressBar.setProgress(msg.arg1);
					break;
				case UPLOAD_FILE_DONE:
					// String result = "响应码：" + msg.arg1 + "\n响应信息：" + msg.obj +
					// "\n耗时：" + UploadUtil.getRequestTime() + "秒";
					ImageUtils.picPath = null;
					Toast.makeText(WritingActivity.this, "文章发布成功，请等待审核...", Toast.LENGTH_SHORT).show();
					//文章发布成功，数据清空
					title.setText("");
					content.setText("");
					Intent intent = new Intent();
					intent.setClass(WritingActivity.this, PublishActivity.class);
					WritingActivity.this.startActivity(intent);
					WritingActivity.this.finish();
					break;
				case DOWNLOAD_IMAGE_SUCCESS:
					cover.setImageBitmap((Bitmap) msg.obj);
				case DOWNLOAD_IMAGE_FAILURE:
				default:
					break;
				}
			}
			super.handleMessage(msg);
		}

	};

	/**
	 * 上传响应，实现OnUploadProcessListener接口
	 */
	public void onUploadDone(int responseCode, String message) {
		progressDialog.dismiss();
		Message msg = Message.obtain();
		msg.what = UPLOAD_FILE_DONE;
		msg.arg1 = responseCode;
		msg.obj = message;
		handler.sendMessage(msg);
	}

	/**
	 * 上传中，实现OnUploadProcessListener接口
	 */
	public void onUploadProcess(int uploadSize) {
		Message msg = Message.obtain();
		msg.what = UPLOAD_IN_PROCESS;
		msg.arg1 = uploadSize;
		handler.sendMessage(msg);
	}

	/**
	 * 准备上传，实现OnUploadProcessListener接口
	 */
	public void initUpload(int fileSize) {
		Message msg = Message.obtain();
		msg.what = UPLOAD_INIT_PROCESS;
		msg.arg1 = fileSize;
		handler.sendMessage(msg);
	}

	/**
	 * 下载服务器图片线程
	 */
	class downloadRunnable implements Runnable {
		private String ajaxUrl;
		private ImageView cover;

		public downloadRunnable(String ajaxUrl, ImageView cover) {
			super();
			this.ajaxUrl = ajaxUrl;
			this.cover = cover;
		}

		@Override
		public void run() {
			Looper.prepare();
			HttpClient hc = new DefaultHttpClient();
			HttpGet hg = new HttpGet(ajaxUrl);
			final Bitmap bm;
			Message msg = Message.obtain();
			try {
				HttpResponse hr = hc.execute(hg);
				bm = BitmapFactory.decodeStream(hr.getEntity().getContent());
				msg.what = DOWNLOAD_IMAGE_SUCCESS;
				msg.obj = bm;
				handler.sendMessage(msg);
			} catch (Exception e) {
				msg.what = DOWNLOAD_IMAGE_FAILURE;
				handler.sendMessage(msg);
			}
			Looper.loop();
		}
	};

	/**
	 * 使用SharedPreferences保存用户写作信息
	 * 
	 * @param context
	 * @param title
	 * @param content
	 */
	public static void saveCrashInfo(Context context, String title, String content) {
		// 获取SharedPreferences对象
		SharedPreferences sharedPre = context.getSharedPreferences("config", context.MODE_PRIVATE);
		// 获取Editor对象
		Editor editor = sharedPre.edit();
		// 设置参数
		editor.putString("title", title);
		editor.putString("content", content);
		// 提交
		editor.commit();
	}
	
	@Override
	public void onStart() {
		super.onStart();
	}
	
	@Override
	public void onResume() {
		Log.v(TAG, "OnResume");
		super.onResume();
		// 默认显示已保存的文章标题和内容
		SharedPreferences sharedPre = getSharedPreferences("config", MODE_PRIVATE);
		String _title = sharedPre.getString("title", "");
		String _content = sharedPre.getString("content", "");
		title.setText(_title);
		content.setText(_content);
		MobclickAgent.onResume(this);  //友盟统计
	}

	@Override
	public void onPause() {
		Log.v(TAG, "OnPause unregister progress receiver");
		super.onPause();
		titleText = title.getText().toString().trim();
		contentText = content.getText().toString().trim();
		saveCrashInfo(WritingActivity.this, titleText, contentText);
		MobclickAgent.onPause(this);  //友盟统计
	}

	public void onStop() {
		Log.v(TAG, "OnStop");
		super.onStop();
	}

	public void onDestroy() {
		Log.v(TAG, "Destroy");
		super.onDestroy();
	}
	
	//再按一次返回键退出程序
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){   
	        if((System.currentTimeMillis()-exitTime) > 2000){  
	            Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();                                
	            exitTime = System.currentTimeMillis();   
	        } else {
	            finish();
	            System.exit(0);
	        }
	        return true;   
	    }
	    return super.onKeyDown(keyCode, event);
	}
	

}
