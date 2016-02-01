package practiceschool.ui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import practiceschool.network.HttpMethod;
import practiceschool.network.UploadUtil;
import practiceschool.network.UploadUtil.OnUploadProcessListener;
import practiceschool.utils.Constants;
import practiceschool.utils.ImageUtils;
import practiceschool.utils.LeftMenu;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.practiceschool.R;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.umeng.analytics.MobclickAgent;

public class UsercenterActivity extends Activity implements OnUploadProcessListener {

	private long exitTime = 0;  //按返回键，退出时间
	private static final String TAG = "zhihu.ui.UsercenterActivity";

	private final int TO_UPLOAD_FILE = 1; // 去上传文件
	private final int UPLOAD_FILE_DONE = 2; // 上传文件响应
	private final int UPLOAD_INIT_PROCESS = 3; // 上传初始化
	private final int UPLOAD_IN_PROCESS = 4; // 上传中
	private final int DOWNLOAD_IMAGE_SUCCESS = 5; // 下载图片成功
	private final int DOWNLOAD_IMAGE_FAILURE = 6; // 下载图片失败
	private final int USERINFO_SUCCESS = 7; // 查询用户信息成功
	private final int USERINFO_FAILURE = 8; // 查询用户信息失败

	private final int UPLOAD_FILE_FAILURE = 9; // 上传文件失败
	
	private final int UPDATE_USER_SUCCESS = 10; // 更新用户信息成功(不带头像)
	private final int UPDATE_USER_FAILURE = 11; // 更新用户信息失败(不带头像)

	private String requestImgUrl = Constants.NODE_URL + "/updateuser"; // 请求服务器路径

	private ImageButton title_move_btn;
	private SlidingMenu menu;
	private RelativeLayout user_itemmenu;
	private practiceschool.circlepic.CircularImage user_portrait; // 用户头像
	private Button user_write_icon; // 开始写作按钮
	private Button user_publish_icon; // 我的写作按钮
	private Button user_boy_btn; // 男孩按钮
	private Button user_girl_btn; // 女孩按钮
	private EditText username_edit; // 昵称编辑框
	private Button publish_btn; // 发布按钮
	private TextView userrank_number; // 用户等级（控件）
	private TextView userscore_number; // 用户积分（控件）
	private String sexChecked = "boy"; // 用户选中性别
	private String userpic; // 头像地址
	private String username; // 用户昵称
	private String userrank; // 用户等级
	private String userscore; // 用户积分

	private String targetPath;
	private String avatorpath = Environment.getExternalStorageDirectory() + "/usercover/";
	private Bitmap bitmap;
	private ProgressDialog progressDialog; // 加载loading对话框

	private String userid = Constants.app_userid; // 用户ID号  例子："556c58006df508f80f887460"

	/* 头像名称 */
	private final String IMAGE_FILE_NAME = "xueyuan_" + new Date().getTime() + ".jpg";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.v(TAG, "OnCreate");
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE); // 声明使用自定义标题
		setContentView(R.layout.usercenter_main);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.usercenter_title); // 自定义标题栏布局赋值

		// 初始化布局控件
		this.initComponent();

		// 初始化渲染数据
		this.initData();

		// 切换左侧菜单按钮
		title_move_btn = (ImageButton) findViewById(R.id.title_move_btn);
		title_move_btn.setOnClickListener(new OnClickListener() {

			public void onClick(View view) {

				LeftMenu.menu.toggle(true);

			}
		});

		// 初始化左侧菜单
		LeftMenu.initSlidingMenu(UsercenterActivity.this, "user");

		MobclickAgent.updateOnlineConfig(this); // 友盟统计
	}

	/**
	 * 初始化界面控件
	 */
	private void initComponent() {

		user_itemmenu = (RelativeLayout) findViewById(R.id.user_itemmenu);
		user_portrait = (practiceschool.circlepic.CircularImage) findViewById(R.id.user_portrait);
		user_write_icon = (Button) findViewById(R.id.user_write_icon);
		user_publish_icon = (Button) findViewById(R.id.user_publish_icon);
		user_boy_btn = (Button) findViewById(R.id.user_boy_btn);
		user_girl_btn = (Button) findViewById(R.id.user_girl_btn);
		username_edit = (EditText) findViewById(R.id.username_edit);
		publish_btn = (Button) findViewById(R.id.publish_btn);
		userrank_number = (TextView) findViewById(R.id.userrank_number);
		userscore_number = (TextView) findViewById(R.id.userscore_number);
		progressDialog = new ProgressDialog(this);

		// 设置背景透明度
		user_itemmenu.getBackground().setAlpha(90); // 0~255透明度值

		/**
		 * 跳转到“我的收藏”
		 */
		user_write_icon.setOnClickListener(new OnClickListener() {

			public void onClick(View view) {

				Intent intent = new Intent();
				intent.setClass(UsercenterActivity.this, CollectActivity.class);
				UsercenterActivity.this.startActivity(intent);

			}
		});

		/**
		 * 跳转到“我的发布”
		 */
		user_publish_icon.setOnClickListener(new OnClickListener() {

			public void onClick(View view) {

				Intent intent = new Intent();
				intent.setClass(UsercenterActivity.this, PublishActivity.class);
				UsercenterActivity.this.startActivity(intent);
				UsercenterActivity.this.finish();

			}
		});
 
		/**
		 * 选中男孩
		 */
		user_boy_btn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				user_boy_btn.setBackgroundResource(R.drawable.boy_selected);
				user_girl_btn.setBackgroundResource(R.drawable.girl_unselected);
				sexChecked = "boy";
			}
		});

		/**
		 * 选中女孩
		 */
		user_girl_btn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				user_girl_btn.setBackgroundResource(R.drawable.girl_selected);
				user_boy_btn.setBackgroundResource(R.drawable.boy_unselected);
				sexChecked = "girl";
			}
		});

		/**
		 * 上传头像
		 */
		user_portrait.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				showImagePickDialog();
			}
		});

		/**
		 * 保存用户信息
		 */
		publish_btn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {

				username = username_edit.getText().toString().trim();
				if (ImageUtils.picPath != null) {			
					handler.sendEmptyMessage(TO_UPLOAD_FILE);
				} else {
					try {
						String ajaxUrl = Constants.NODE_URL + "/userEditInfo";
						String ajaxData = "userid=" + userid + "&username=" + URLEncoder.encode(username, "utf-8") + "&sex=" + sexChecked;
						progressDialog.setMessage("正在更新中，请稍后...");
						progressDialog.show();
						new Thread(new UpdateThread(ajaxUrl, ajaxData)).start();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

			}
		});

	}
	
	
	/**
	 * 更新用户信息线程(不带头像)
	 */
	class UpdateThread implements Runnable {

		private String ajaxUrl;
		private String ajaxData;

		public UpdateThread(String ajaxUrl, String ajaxData) {
			super();
			this.ajaxUrl = ajaxUrl;
			this.ajaxData = ajaxData;
		}

		public void run() {
			Looper.prepare();
			final String result = HttpMethod.loginByPost(ajaxUrl, ajaxData);
			Message msg = Message.obtain();
			if (result != null) {
				try {
					JSONObject data = new JSONObject(result);
					msg.what = UPDATE_USER_SUCCESS;
					msg.obj = data;
					handler.sendMessage(msg);
				} catch (JSONException e) {
					msg.what = UPDATE_USER_FAILURE;
					handler.sendMessage(msg);
				}
			} else {
				Toast.makeText(UsercenterActivity.this, "网络异常，请检查您的网络", Toast.LENGTH_SHORT).show();
			}
			Looper.loop();
		}
	}

	/**
	 * 初始化渲染数据
	 */
	private void initData() {

		username_edit.setHint(Constants.app_username);
		userrank_number.setText(Constants.app_userrankText);
		userscore_number.setText(Constants.app_userscore);

		if (sexChecked.equals(Constants.app_usersex)) {
			user_boy_btn.setBackgroundResource(R.drawable.boy_selected);
			user_girl_btn.setBackgroundResource(R.drawable.girl_unselected);
		} else {
			user_boy_btn.setBackgroundResource(R.drawable.boy_unselected);
			user_girl_btn.setBackgroundResource(R.drawable.girl_selected);
		}

		/**
		 * 显示图片 参数1：图片url 参数2：显示图片的控件 参数3：显示图片的设置 参数4：监听器
		 */
		userpic = Constants.app_userpic;
		Constants.imageLoader.cancelDisplayTask(user_portrait);
		Constants.imageLoader.displayImage(userpic, user_portrait, Constants.options, Constants.animateFirstListener);

		// 更新用户等级和积分
		String ajaxUrl = Constants.NODE_URL + "/userinfo";
		String ajaxData = "userid=" + userid;
		if (Constants.isNetworkConnected(UsercenterActivity.this)) { // 判断是否有网络
			try {
				new Thread(new UserinfoThread(ajaxUrl, ajaxData)).start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			Toast.makeText(UsercenterActivity.this, "网络异常，请检查您的网络", Toast.LENGTH_SHORT).show();
		}

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
					try {
						saveUserInfo((JSONObject) msg.obj);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					Toast.makeText(UsercenterActivity.this, "用户信息更新成功", 0).show();
					break;
				case DOWNLOAD_IMAGE_SUCCESS:
					user_portrait.setImageBitmap((Bitmap) msg.obj);
					break;
				case DOWNLOAD_IMAGE_FAILURE:
					break;
				case USERINFO_SUCCESS:
					try {
						updateUserInfo((JSONObject) msg.obj);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					break;
				case USERINFO_FAILURE:
					break;
				case UPLOAD_FILE_FAILURE:
					break;
				case UPDATE_USER_SUCCESS:
					try {
						udpateUser((JSONObject) msg.obj);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				case UPDATE_USER_FAILURE:
					break;
				default:
					break;
				}
			}
			super.handleMessage(msg);
		}

	};
	
	/**
	 * 用户信息更新成功(不带头像)
	 */
	private void udpateUser(JSONObject data) throws JSONException {
		int resultCode = (Integer) data.getInt("resultCode");
		if (resultCode == 1000) {
			progressDialog.hide();
			Toast.makeText(UsercenterActivity.this, "用户信息更新成功", Toast.LENGTH_SHORT).show();
			Constants.app_usersex = sexChecked;
			Constants.app_username = username;
		} else if (resultCode == 0) {
			Toast.makeText(UsercenterActivity.this, "更新失败", Toast.LENGTH_SHORT).show();
		}
	}


	/**
	 * 获取用户信息线程
	 */
	class UserinfoThread implements Runnable {

		private String ajaxUrl;
		private String ajaxData;

		public UserinfoThread(String ajaxUrl, String ajaxData) {
			super();
			this.ajaxUrl = ajaxUrl;
			this.ajaxData = ajaxData;
		}

		public void run() {
			Looper.prepare();
			final String result = HttpMethod.loginByPost(ajaxUrl, ajaxData);
			Message msg = Message.obtain();
			if (result != null) {
				try {
					JSONObject data = new JSONObject(result);
					msg.what = USERINFO_SUCCESS;
					msg.obj = data;
					handler.sendMessage(msg);
				} catch (JSONException e) {
					msg.what = USERINFO_FAILURE;
					handler.sendMessage(msg);
				}
			} else {
				Toast.makeText(UsercenterActivity.this, "网络异常，请检查您的网络", Toast.LENGTH_SHORT).show();
			}
			Looper.loop();
		}
	}

	private void updateUserInfo(JSONObject data) throws JSONException {
		int resultCode = (Integer) data.getInt("resultCode");
		if (resultCode == 1000) {
			JSONObject infoData = (JSONObject) data.get("userinfo");
			Constants.app_userrank = (Integer) infoData.getInt("rank");
			Constants.app_userrankText = Constants.SwitchRankText((Integer) infoData.getInt("rank"));
			Constants.app_userscore = String.valueOf((Integer) infoData.getInt("score"));
			userrank_number.setText(Constants.app_userrankText);
			userscore_number.setText(Constants.app_userscore);
		} else if (resultCode == 0) {
			Toast.makeText(UsercenterActivity.this, "获取该用户信息失败", Toast.LENGTH_SHORT).show();
		}
	}

	private void saveUserInfo(JSONObject data) throws JSONException {
		int resultCode = (Integer) data.getInt("resultCode");
		if (resultCode == 1000) {
			JSONObject infoData = (JSONObject) data.get("userinfo");
			Constants.app_usersex = (String) infoData.getString("sex");
			// 用户名的编码格式需要转成utf-8
			try {
				Constants.app_username = (String) new String(infoData.getString("username").getBytes("iso-8859-1"), "utf-8");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Constants.app_userpic = (String) infoData.getString("userpic");
			ImageUtils.picPath = null;
		}
	}

	/**
	 * 获取用户头像
	 */
	public void showImagePickDialog() {

		String title = "获取图片方式";
		String[] choices = new String[] { "拍照", "从手机中选择" };
		new AlertDialog.Builder(this).setTitle(title).setItems(choices, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				switch (which) {
				case 0:
					ImageUtils.openCameraImage(UsercenterActivity.this);
					break;
				case 1:
					ImageUtils.openLocalImage(UsercenterActivity.this);
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
				// 可以直接显示图片,或者进行其他处理(如压缩或裁剪等)
				// iv.setImageURI(ImageUtils.imageUriFromCamera);
				// 对图片进行裁剪
				ImageUtils.cropImage(this, ImageUtils.imageUriFromCamera, true);
			}
			break;
		// 手机相册获取图片
		case ImageUtils.GET_IMAGE_FROM_PHONE:
			if (data != null && data.getData() != null) {
				// 可以直接显示图片,或者进行其他处理(如压缩或裁剪等)
				// iv.setImageURI(data.getData());
				// 对图片进行裁剪
				ImageUtils.cropImage(this, data.getData(), true);
			}
			break;
		// 裁剪图片后结果
		case ImageUtils.CROP_IMAGE:

			if (data != null) {
				setImageToView(data, user_portrait);
			}
			break;
		default:
			break;
		}
	}

	/**
	 * 保存裁剪之后的图片数据
	 * 
	 * @param picdata
	 */
	private void setImageToView(Intent data, practiceschool.circlepic.CircularImage imageView) {
		Bundle extras = data.getExtras();
		if (extras != null) {
			Bitmap photo = extras.getParcelable("data");
			imageView.setImageBitmap(photo);
			saveBitmap(photo);
		}
	}

	private void saveBitmap(Bitmap mBitmap) {
		File f = new File(Environment.getExternalStorageDirectory(), IMAGE_FILE_NAME);
		try {
			f.createNewFile();
			FileOutputStream fOut = null;
			fOut = new FileOutputStream(f);
			mBitmap.compress(Bitmap.CompressFormat.JPEG, 80, fOut);
			fOut.flush();
			fOut.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 提交用户信息到服务器
	 */
	private void toUploadFile() {
		// uploadImageResult.setText("正在上传中...");
		progressDialog.setMessage("正在保存，请稍后...");
		progressDialog.show();
		String fileKey = "upload";
		UploadUtil uploadUtil = UploadUtil.getInstance();
		uploadUtil.setOnUploadProcessListener(this); // 设置监听器监听上传状态
		Map<String, String> params = new HashMap<String, String>();
		params.put("userid", userid);
		params.put("username", username);
		params.put("sex", sexChecked);

		// 获取上传头像绝对路径
		File file = new File(Environment.getExternalStorageDirectory(), IMAGE_FILE_NAME);
		String _filePath = file.getAbsolutePath();

		// 开一个子线程，做请求处理
		uploadUtil.uploadFile(_filePath, fileKey, requestImgUrl, params);
	}

	/**
	 * 上传响应，实现OnUploadProcessListener接口
	 */
	public void onUploadDone(int responseCode, String message) {

		progressDialog.dismiss();
		Message msg = Message.obtain();
		if (message != null) {
			try {
				JSONObject data = new JSONObject(message);
				msg.what = UPLOAD_FILE_DONE;
				msg.arg1 = responseCode;
				msg.obj = data;
				handler.sendMessage(msg);
			} catch (JSONException e) {
				msg.what = UPLOAD_FILE_FAILURE;
				handler.sendMessage(msg);
			}
		} else {
			Toast.makeText(UsercenterActivity.this, "网络异常，请检查您的网络", Toast.LENGTH_SHORT).show();
		}
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

	@Override
	public void onStart() {
		Log.v(TAG, "onStart");
		super.onStart();
	}

	@Override
	public void onResume() {
		Log.v(TAG, "OnResume");
		super.onResume();
		MobclickAgent.onResume(this); // 友盟统计
	}

	@Override
	public void onPause() {
		super.onPause();
		MobclickAgent.onPause(this); // 友盟统计
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