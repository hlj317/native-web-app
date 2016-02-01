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

	private long exitTime = 0;  //�����ؼ����˳�ʱ��
	private final int TO_UPLOAD_FILE = 1; // ȥ�ϴ��ļ�
	private final int UPLOAD_FILE_DONE = 2; // �ϴ��ļ���Ӧ
	private final int TO_SELECT_PHOTO = 3; // ѡ���ļ�
	private final int UPLOAD_INIT_PROCESS = 4; // �ϴ���ʼ��
	private final int UPLOAD_IN_PROCESS = 5; // �ϴ���
	private final int UPLOAD_FILE_FAILURE = 6; // �ϴ�ʧ��
	private String KEY_PHOTO_PATH = "photo_path"; // ��Intent��ȡͼƬ·����KEY
	private String requestImgUrl = Constants.NODE_URL + "/register"; // ���������·��
	private Button boyBtn; // �к�ѡ�а�ť�ؼ�
	private Button girlBtn; // Ů��ѡ�а�ť�ؼ�
	private String sexChecked = "boy"; // �û�ѡ���Ա�
	private Button registerBtn; // ���ע�ᰴť�ؼ�
	private EditText username; // �û��ǳƿؼ�
	private EditText account; // �û��˺ſؼ�
	private EditText password; // �û�����ؼ�
	private EditText repassword; // �û��ٴ���������ؼ�
	private String usernameText; // �û��˺�ֵ
	private String accountText; // �û��˺�ֵ
	private String passwordText; // �û�����ֵ
	private String repasswordText; // �û��ٴ���������ֵ
	private TextView gotoLogin_btn; // ��ת����¼ҳ��
	private practiceschool.circlepic.CircularImage portrait; // �û�ͷ��ؼ�
	private ProgressDialog progressDialog; // ����loading�Ի���
	private String targetPath;
	private String avatorpath = Environment.getExternalStorageDirectory() + "/usercover/";
	private Bitmap bitmap;
	/* ͷ������ */
	private final String IMAGE_FILE_NAME = "xueyuan_" + new Date().getTime() + ".jpg";

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE); // ����û�б���
		setContentView(R.layout.register_main);
		this.initComponent();

		MobclickAgent.updateOnlineConfig(this); // ����ͳ��
	}

	/**
	 * ��ʼ�������еĿؼ�
	 */
	private void initComponent() {

		portrait = (practiceschool.circlepic.CircularImage) findViewById(R.id.portrait);
		boyBtn = (Button) findViewById(R.id.boy);
		girlBtn = (Button) findViewById(R.id.girl);
		registerBtn = (Button) findViewById(R.id.finish_register_btn);
		gotoLogin_btn = (TextView) findViewById(R.id.gotoLogin_btn);
		gotoLogin_btn.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG); // �����»���
		progressDialog = new ProgressDialog(this);
		account = (EditText) findViewById(R.id.account_edit);
		username = (EditText) findViewById(R.id.username_edit);
		password = (EditText) findViewById(R.id.password_edit);
		repassword = (EditText) findViewById(R.id.repassword_edit);

		// ѡ���к�
		boyBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				boyBtn.setBackgroundResource(R.drawable.reg_boy_selected);
				girlBtn.setBackgroundResource(R.drawable.reg_girl_unselected);
				sexChecked = "boy";
			}
		});

		// ѡ��Ů��
		girlBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				girlBtn.setBackgroundResource(R.drawable.reg_girl_selected);
				boyBtn.setBackgroundResource(R.drawable.reg_boy_unselected);
				sexChecked = "girl";
			}
		});

		// �ϴ�ͷ��
		portrait.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				showImagePickDialog();
			}
		});

		// ע���˺�
		registerBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {

				if (ImageUtils.picPath != null) {
					usernameText = username.getText().toString().trim();
					accountText = account.getText().toString().trim();
					passwordText = password.getText().toString().trim();
					repasswordText = repassword.getText().toString().trim();
					if (usernameText.equals("")) {
						Toast.makeText(RegisterActivity.this, "�ǳƲ���Ϊ��", Toast.LENGTH_SHORT).show();
					} else if (usernameText.length()>15) {
						Toast.makeText(RegisterActivity.this, "�ǳƲ��ܳ���15λ", Toast.LENGTH_SHORT).show();
					}else if (accountText.equals("")) {
						Toast.makeText(RegisterActivity.this, "�˺Ų���Ϊ��", Toast.LENGTH_SHORT).show();
					} else if (passwordText.equals("")) {
						Toast.makeText(RegisterActivity.this, "���벻��Ϊ��", Toast.LENGTH_SHORT).show();
					} else if (!(passwordText.equals(repasswordText))) {
						Toast.makeText(RegisterActivity.this, "������������벻һ��", Toast.LENGTH_SHORT).show();
					} else if (!nameContentCheck(accountText)) {
						Toast.makeText(RegisterActivity.this, "�˺���ֻ���Գ�����ĸ�����֡��»���", Toast.LENGTH_SHORT).show();
					} else if (!nameLengthCheck(accountText)) {
						Toast.makeText(RegisterActivity.this, "�˺ų��Ȳ����Ϲ���������3��10λ", Toast.LENGTH_SHORT).show();
					} else if (!passwordContentCheck(passwordText)) {
						Toast.makeText(RegisterActivity.this, "������ֻ���Գ�����ĸ�����֡��»���", Toast.LENGTH_SHORT).show();
					} else if (!passwordLenghtCheck(passwordText)) {
						Toast.makeText(RegisterActivity.this, "���볤�Ȳ����Ϲ���������6��15λ", Toast.LENGTH_SHORT).show();
					} else {
						handler.sendEmptyMessage(TO_UPLOAD_FILE);
					}
				} else {
					Toast.makeText(RegisterActivity.this, "���ϴ�����ͷ��", Toast.LENGTH_SHORT).show();
				}

			}
		});

		// ��ת����¼ҳ��
		gotoLogin_btn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
				RegisterActivity.this.startActivity(intent);
				RegisterActivity.this.finish();
			}
		});
	}

	// �ʺ�������֤
	public Boolean nameContentCheck(String names) {
		char tempName[] = names.toCharArray();

		// ��֤����
		for (int i = 0; i < tempName.length; i++) {
			if ((tempName[i] > 47 && tempName[i] < 58) || (tempName[i] > 64 && tempName[i] < 91) || (tempName[i] > 96 && tempName[i] < 123) || (tempName[i] == 95)) {

			} else {
				return false;
			}
		}
		return true;
	}

	// �˺ų�����֤
	public Boolean nameLengthCheck(String names) {
		char tempName[] = names.toCharArray();

		if (tempName.length < 3 || tempName.length > 10) {

			return false;

		} else {

		}

		return true;

	}

	// ����������֤
	public Boolean passwordContentCheck(String passwords) {
		char tempPass[] = passwords.toCharArray();

		// ��֤����
		for (int i = 0; i < tempPass.length; i++) {
			if ((tempPass[i] > 47 && tempPass[i] < 58) || (tempPass[i] > 64 && tempPass[i] < 91) || (tempPass[i] > 96 && tempPass[i] < 123) || (tempPass[i] == 95)) {

			} else {

				return false;
			}
		}
		return true;
	}

	// ���볤����֤
	public Boolean passwordLenghtCheck(String passwords) {
		char tempPass[] = passwords.toCharArray();

		if (tempPass.length < 6 || tempPass.length > 15) {
			return false;

		} else {

		}
		return true;

	}

	/**
	 * ��ȡ�û�ͷ��
	 */
	public void showImagePickDialog() {

		String title = "��ȡͼƬ��ʽ";
		String[] choices = new String[] { "����", "���ֻ���ѡ��" };
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
		}).setNegativeButton("����", null).show();
	}

	/**
	 * ���ա����ֻ���ѡ��ͼƬ�������봦��
	 */
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		// �����������ȡ�����򷵻�
		if (resultCode == RESULT_CANCELED) {
			return;
		}

		switch (requestCode) {
		// ���ջ�ȡͼƬ
		case ImageUtils.GET_IMAGE_BY_CAMERA:
			// uri�������Ӱ��ͼƬ��ȡ��ʽ,���¶�ѡһ
			// ��ʽһ,�Զ���Uri(ImageUtils.imageUriFromCamera),���ڱ������պ�ͼƬ��ַ
			if (ImageUtils.imageUriFromCamera != null) {
				// ����ֱ����ʾͼƬ,���߽�����������(��ѹ����ü���)
				// iv.setImageURI(ImageUtils.imageUriFromCamera);
				// ��ͼƬ���вü�
				ImageUtils.cropImage(this, ImageUtils.imageUriFromCamera, false);
			}
			break;
		// �ֻ�����ȡͼƬ
		case ImageUtils.GET_IMAGE_FROM_PHONE:
			if (data != null && data.getData() != null) {
				// ����ֱ����ʾͼƬ,���߽�����������(��ѹ����ü���)
				// iv.setImageURI(data.getData());
				// ��ͼƬ���вü�
				ImageUtils.cropImage(this, data.getData(), false);
			}
			break;
		// �ü�ͼƬ����
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
	 * ����ü�֮���ͼƬ����
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
	 * �ύ�û���Ϣ��������
	 */
	private void toUploadFile() {
		// uploadImageResult.setText("�����ϴ���...");
		progressDialog.setMessage("����ע�ᣬ���Ժ�...");
		progressDialog.show();
		String fileKey = "upload";
		UploadUtil uploadUtil = UploadUtil.getInstance();
		uploadUtil.setOnUploadProcessListener(this); // ���ü����������ϴ�״̬
		Map<String, String> params = new HashMap<String, String>();
		params.put("username", usernameText);
		params.put("sex", sexChecked);
		params.put("account", accountText);
		params.put("password", passwordText);
		// ��һ�����̣߳���������
		
		//��ȡ�ϴ�ͷ�����·��
		File file = new File(Environment.getExternalStorageDirectory(),IMAGE_FILE_NAME);  
        String _filePath=file.getAbsolutePath();  
	
		uploadUtil.uploadFile(_filePath, fileKey, requestImgUrl, params);
	}  

	/**
	 * �첽��Ϣ�ص�������
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
			// �û����ı����ʽ��Ҫת��utf-8
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
			Toast.makeText(RegisterActivity.this, "ע��ɹ�����ʼ����֮�ðɣ�", 0).show();
			Intent intent = new Intent(RegisterActivity.this, DiscoveryActivity.class);
			RegisterActivity.this.startActivity(intent);
			RegisterActivity.this.finish();
		} else if (resultCode == 1001) {
			Toast.makeText(RegisterActivity.this, "�˺��ظ�����������д", Toast.LENGTH_SHORT).show();
		} else if (resultCode == 1002) {
			Toast.makeText(RegisterActivity.this, "�ǳ��ظ�����������д", Toast.LENGTH_SHORT).show();
		} else if (resultCode == 1003) {
			Toast.makeText(RegisterActivity.this, "ͬһ��IP��һСʱ֮��ֻ��ע��һ��", Toast.LENGTH_SHORT).show();
		}
	}
	
	/**
	 * ʹ��SharedPreferences�����û���¼��Ϣ
	 * 
	 * @param context
	 * @param username
	 * @param password
	 */
	public static void saveLoginInfo(Context context, String username, String password) {
		// ��ȡSharedPreferences����
		SharedPreferences sharedPre = context.getSharedPreferences("config", context.MODE_PRIVATE);
		// ��ȡEditor����
		Editor editor = sharedPre.edit();
		// ���ò���
		editor.putString("username", username);
		editor.putString("password", password);
		// �ύ
		editor.commit();
	}

	/**
	 * �ϴ���Ӧ��ʵ��OnUploadProcessListener�ӿ�
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
			Toast.makeText(RegisterActivity.this, "�����쳣��������������", Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * �ϴ��У�ʵ��OnUploadProcessListener�ӿ�
	 */
	public void onUploadProcess(int uploadSize) {
		Message msg = Message.obtain();
		msg.what = UPLOAD_IN_PROCESS;
		msg.arg1 = uploadSize;
		handler.sendMessage(msg);
	}

	/**
	 * ׼���ϴ���ʵ��OnUploadProcessListener�ӿ�
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
		MobclickAgent.onResume(this); // ����ͳ��
	}

	@Override
	public void onPause() {
		super.onPause();
		MobclickAgent.onPause(this); // ����ͳ��
	}

	public void onStop() {
		super.onStop();
	}

	public void onDestroy() {
		super.onDestroy();
	}
	
	//�ٰ�һ�η��ؼ��˳�����
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){   
	        if((System.currentTimeMillis()-exitTime) > 2000){  
	            Toast.makeText(getApplicationContext(), "�ٰ�һ���˳�����", Toast.LENGTH_SHORT).show();                                
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
