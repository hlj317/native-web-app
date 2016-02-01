package practiceschool.ui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import practiceschool.network.UploadUtil;
import practiceschool.network.UploadUtil.OnUploadProcessListener;
import practiceschool.utils.Constants;
import practiceschool.utils.ImageUtils;


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
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class RegisterActivity extends Activity implements OnUploadProcessListener {

	private long exitTime = 0;  //按返回键，退出时间
	private final int TO_UPLOAD_FILE = 1; // 去上传文件
	private final int UPLOAD_FILE_DONE = 2; // 上传文件响应
	private final int TO_SELECT_PHOTO = 3; // 选择文件
	private final int UPLOAD_INIT_PROCESS = 4; // 上传初始化
	private final int UPLOAD_IN_PROCESS = 5; // 上传中
	private final int UPLOAD_FILE_FAILURE = 6; // 上传失败
	private String KEY_PHOTO_PATH = "photo_path"; // 从Intent获取图片路径的KEY
	private String requestImgUrl = Constants.NODE_URL + "/register"; // 请求服务器路径
	private Button boyBtn; // 男孩选中按钮控件
	private Button girlBtn; // 女孩选中按钮控件
	private String sexChecked = "boy"; // 用户选中性别
	private Button registerBtn; // 完成注册按钮控件
	private EditText username; // 用户昵称控件
	private EditText account; // 用户账号控件
	private EditText password; // 用户密码控件
	private EditText repassword; // 用户再次输入密码控件
	private String usernameText; // 用户账号值
	private String accountText; // 用户账号值
	private String passwordText; // 用户密码值
	private String repasswordText; // 用户再次输入密码值
	private TextView gotoLogin_btn; // 跳转到登录页面
	private practiceschool.circlepic.CircularImage portrait; // 用户头像控件
	private ProgressDialog progressDialog; // 加载loading对话框
	private String targetPath;
	private String avatorpath = Environment.getExternalStorageDirectory() + "/usercover/";
	private Bitmap bitmap;
	/* 头像名称 */
	private final String IMAGE_FILE_NAME = "xueyuan_" + new Date().getTime() + ".jpg";

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE); // 声明没有标题
		setContentView(R.layout.register_main);
		this.initComponent();

		MobclickAgent.updateOnlineConfig(this); // 友盟统计
		PushAgent.getInstance(this).onAppStart();  //友盟推送
	}

	/**
	 * 初始化布局中的控件
	 */
	private void initComponent() {

		portrait = (practiceschool.circlepic.CircularImage) findViewById(R.id.portrait);
		boyBtn = (Button) findViewById(R.id.boy);
		girlBtn = (Button) findViewById(R.id.girl);
		registerBtn = (Button) findViewById(R.id.finish_register_btn);
		gotoLogin_btn = (TextView) findViewById(R.id.gotoLogin_btn);
		gotoLogin_btn.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG); // 设置下划线
		progressDialog = new ProgressDialog(this);
		account = (EditText) findViewById(R.id.account_edit);
		username = (EditText) findViewById(R.id.username_edit);
		password = (EditText) findViewById(R.id.password_edit);
		repassword = (EditText) findViewById(R.id.repassword_edit);

		// 选中男孩
		boyBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				boyBtn.setBackgroundResource(R.drawable.reg_boy_selected);
				girlBtn.setBackgroundResource(R.drawable.reg_girl_unselected);
				sexChecked = "boy";
			}
		});

		// 选中女孩
		girlBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				girlBtn.setBackgroundResource(R.drawable.reg_girl_selected);
				boyBtn.setBackgroundResource(R.drawable.reg_boy_unselected);
				sexChecked = "girl";
			}
		});

		// 上传头像
		portrait.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				showImagePickDialog();
			}
		});

		// 注册账号
		registerBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {

				if (ImageUtils.picPath != null) {
					usernameText = username.getText().toString().trim();
					accountText = account.getText().toString().trim();
					passwordText = password.getText().toString().trim();
					repasswordText = repassword.getText().toString().trim();
					if (usernameText.equals("")) {
						Toast.makeText(RegisterActivity.this, "昵称不能为空", Toast.LENGTH_SHORT).show();
					} else if (usernameText.length()>15) {
						Toast.makeText(RegisterActivity.this, "昵称不能超过15位", Toast.LENGTH_SHORT).show();
					}else if (accountText.equals("")) {
						Toast.makeText(RegisterActivity.this, "账号不能为空", Toast.LENGTH_SHORT).show();
					} else if (passwordText.equals("")) {
						Toast.makeText(RegisterActivity.this, "密码不能为空", Toast.LENGTH_SHORT).show();
					} else if (!(passwordText.equals(repasswordText))) {
						Toast.makeText(RegisterActivity.this, "两次输入的密码不一致", Toast.LENGTH_SHORT).show();
					} else if (!nameContentCheck(accountText)) {
						Toast.makeText(RegisterActivity.this, "账号中只可以出现字母、数字、下划线", Toast.LENGTH_SHORT).show();
					} else if (!nameLengthCheck(accountText)) {
						Toast.makeText(RegisterActivity.this, "账号长度不符合规则，请输入3至10位", Toast.LENGTH_SHORT).show();
					} else if (!passwordContentCheck(passwordText)) {
						Toast.makeText(RegisterActivity.this, "密码中只可以出现字母、数字、下划线", Toast.LENGTH_SHORT).show();
					} else if (!passwordLenghtCheck(passwordText)) {
						Toast.makeText(RegisterActivity.this, "密码长度不符合规则，请输入6至15位", Toast.LENGTH_SHORT).show();
					} else {
						handler.sendEmptyMessage(TO_UPLOAD_FILE);
					}
				} else {
					Toast.makeText(RegisterActivity.this, "请上传您的头像", Toast.LENGTH_SHORT).show();
				}

			}
		});

		// 跳转到登录页面
		gotoLogin_btn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
				RegisterActivity.this.startActivity(intent);
				RegisterActivity.this.finish();
			}
		});
	}

	// 帐号内容验证
	public Boolean nameContentCheck(String names) {
		char tempName[] = names.toCharArray();

		// 验证内容
		for (int i = 0; i < tempName.length; i++) {
			if ((tempName[i] > 47 && tempName[i] < 58) || (tempName[i] > 64 && tempName[i] < 91) || (tempName[i] > 96 && tempName[i] < 123) || (tempName[i] == 95)) {

			} else {
				return false;
			}
		}
		return true;
	}

	// 账号长度验证
	public Boolean nameLengthCheck(String names) {
		char tempName[] = names.toCharArray();

		if (tempName.length < 3 || tempName.length > 10) {

			return false;

		} else {

		}

		return true;

	}

	// 密码内容验证
	public Boolean passwordContentCheck(String passwords) {
		char tempPass[] = passwords.toCharArray();

		// 验证内容
		for (int i = 0; i < tempPass.length; i++) {
			if ((tempPass[i] > 47 && tempPass[i] < 58) || (tempPass[i] > 64 && tempPass[i] < 91) || (tempPass[i] > 96 && tempPass[i] < 123) || (tempPass[i] == 95)) {

			} else {

				return false;
			}
		}
		return true;
	}

	// 密码长度验证
	public Boolean passwordLenghtCheck(String passwords) {
		char tempPass[] = passwords.toCharArray();

		if (tempPass.length < 6 || tempPass.length > 15) {
			return false;

		} else {

		}
		return true;

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
					ImageUtils.openCameraImage(RegisterActivity.this);
					break;
				case 1:
					ImageUtils.openLocalImage(RegisterActivity.this);
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
				ImageUtils.cropImage(this, ImageUtils.imageUriFromCamera, false);
			}
			break;
		// 手机相册获取图片
		case ImageUtils.GET_IMAGE_FROM_PHONE:
			if (data != null && data.getData() != null) {
				// 可以直接显示图片,或者进行其他处理(如压缩或裁剪等)
				// iv.setImageURI(data.getData());
				// 对图片进行裁剪
				ImageUtils.cropImage(this, data.getData(), false);
			}
			break;
		// 裁剪图片后结果
		case ImageUtils.CROP_IMAGE:
			
			if (data != null) {
				setImageToView(data, portrait);
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
		File f = new File(Environment.getExternalStorageDirectory(),IMAGE_FILE_NAME);
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
		progressDialog.setMessage("正在注册，请稍后...");
		progressDialog.show();
		String fileKey = "upload";
		UploadUtil uploadUtil = UploadUtil.getInstance();
		uploadUtil.setOnUploadProcessListener(this); // 设置监听器监听上传状态
		Map<String, String> params = new HashMap<String, String>();
		params.put("username", usernameText);
		params.put("sex", sexChecked);
		params.put("account", accountText);
		params.put("password", passwordText);
		// 开一个子线程，做请求处理
		
		//获取上传头像绝对路径
		File file = new File(Environment.getExternalStorageDirectory(),IMAGE_FILE_NAME);  
        String _filePath=file.getAbsolutePath();  
	
		uploadUtil.uploadFile(_filePath, fileKey, requestImgUrl, params);
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
					try {
						saveUserInfo((JSONObject) msg.obj);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					break;
				case UPLOAD_FILE_FAILURE:
					break;
				default:
					break;
				}
			}
			super.handleMessage(msg);
		}

	};
 
	private void saveUserInfo(JSONObject data) throws JSONException {
		int resultCode = (Integer) data.getInt("resultCode");
		if (resultCode == 1000) {
			ImageUtils.picPath = null;
			saveLoginInfo(RegisterActivity.this, accountText, passwordText);
			JSONObject infoData = (JSONObject) data.get("userinfo");
			Constants.app_userid = (String) infoData.getString("_id");
			Constants.app_usersex = (String) infoData.getString("sex");
			// 用户名的编码格式需要转成utf-8
			try {
				Constants.app_username = (String) new String(infoData.getString("username").getBytes("iso-8859-1"), "utf-8");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Constants.app_userpic = (String) infoData.getString("userpic");
			Constants.app_userrank = (Integer) infoData.getInt("rank");
			Constants.app_userrankText = Constants.SwitchRankText((Integer) infoData.getInt("rank"));
			Constants.app_userscore = String.valueOf((Integer) infoData.getInt("score"));
			Toast.makeText(RegisterActivity.this, "注册成功！开始修炼之旅吧！", 0).show();
			Intent intent = new Intent(RegisterActivity.this, DiscoveryActivity.class);
			RegisterActivity.this.startActivity(intent);
			RegisterActivity.this.finish();
		} else if (resultCode == 1001) {
			Toast.makeText(RegisterActivity.this, "账号重复，请重新填写", Toast.LENGTH_SHORT).show();
		} else if (resultCode == 1002) {
			Toast.makeText(RegisterActivity.this, "昵称重复，请重新填写", Toast.LENGTH_SHORT).show();
		} else if (resultCode == 1003) {
			Toast.makeText(RegisterActivity.this, "同一个IP，一小时之内只能注册一次", Toast.LENGTH_SHORT).show();
		}
	}
	
	/**
	 * 使用SharedPreferences保存用户登录信息
	 * 
	 * @param context
	 * @param username
	 * @param password
	 */
	public static void saveLoginInfo(Context context, String username, String password) {
		// 获取SharedPreferences对象
		SharedPreferences sharedPre = context.getSharedPreferences("config", context.MODE_PRIVATE);
		// 获取Editor对象
		Editor editor = sharedPre.edit();
		// 设置参数
		editor.putString("username", username);
		editor.putString("password", password);
		// 提交
		editor.commit();
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
			Toast.makeText(RegisterActivity.this, "网络异常，请检查您的网络", Toast.LENGTH_SHORT).show();
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
		super.onStart();
	}

	@Override
	public void onResume() {
		super.onResume();
		MobclickAgent.onResume(this); // 友盟统计
	}

	@Override
	public void onPause() {
		super.onPause();
		MobclickAgent.onPause(this); // 友盟统计
	}

	public void onStop() {
		super.onStop();
	}

	public void onDestroy() {
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
