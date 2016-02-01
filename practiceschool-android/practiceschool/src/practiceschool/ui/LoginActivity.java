package practiceschool.ui;

import java.lang.ref.WeakReference;

import org.json.JSONException;
import org.json.JSONObject;

import practiceschool.network.HttpMethod;
import practiceschool.utils.Constants;


import com.example.practiceschool.R;
import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends Activity {

	private long exitTime = 0;  //�����ؼ����˳�ʱ��
	private final int LOGIN_SUCCESS = 1; // ��¼�ɹ�
	private final int LOGIN_FAILURE = 2; // ��¼ʧ��
	private ProgressDialog progressDialog; // ����loading�Ի���

	private EditText account_edit;
	private EditText password_edit;
	private Button login_btn;
	private TextView gotoReg_btn;
	private String accountText;
	private String accountPassword;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE); // ����û�б���
		setContentView(R.layout.login_main);
		this.initComponent();
		
		MobclickAgent.updateOnlineConfig(this);  //����ͳ��
	}

	/**
	 * ��ʼ�������еĿؼ�
	 */
	private void initComponent() {

		account_edit = (EditText) findViewById(R.id.account_edit);
		password_edit = (EditText) findViewById(R.id.password_edit);
		login_btn = (Button) findViewById(R.id.login_btn);
		gotoReg_btn = (TextView) findViewById(R.id.gotoReg_btn);
		gotoReg_btn.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG); // �����»���
		progressDialog = new ProgressDialog(this);

		// Ĭ����ʾ�ѱ�����˺ź�����
		SharedPreferences sharedPre = getSharedPreferences("config", MODE_PRIVATE);
		String _username = sharedPre.getString("username", "");
		String _password = sharedPre.getString("password", "");
		account_edit.setText(_username);
		password_edit.setText(_password);

		// ��¼�˺�
		login_btn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {

				accountText = account_edit.getText().toString().trim();
				accountPassword = password_edit.getText().toString().trim();
				String ajaxUrl = Constants.NODE_URL + "/login";
				String ajaxData = "account=" + accountText + "&password=" + accountPassword;

				// �ж��Ƿ�������
				if (Constants.isNetworkConnected(LoginActivity.this)) {
					try {
						progressDialog.setMessage("���ڵ�¼�У����Ժ�...");
						progressDialog.show();
						new Thread(new LoginThread(ajaxUrl, ajaxData)).start();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}else {
					Toast.makeText(LoginActivity.this, "�����쳣��������������", Toast.LENGTH_SHORT).show();
				}

			}
		});

		// ��ת��ע��ҳ��
		gotoReg_btn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
				LoginActivity.this.startActivity(intent);
				LoginActivity.this.finish();
			}
		});

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
				case LOGIN_SUCCESS:
					try {
						progressDialog.dismiss();
						saveUserInfo((JSONObject) msg.obj);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					break;
				case LOGIN_FAILURE:
					break;
				default:
					break;
				}
			}
			super.handleMessage(msg);
		}

	};

	/**
	 * ��¼�߳�
	 */
	class LoginThread implements Runnable {

		private String ajaxUrl;
		private String ajaxData;

		public LoginThread(String ajaxUrl, String ajaxData) {
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
					msg.what = LOGIN_SUCCESS;
					msg.obj = data;
					handler.sendMessage(msg);
				} catch (JSONException e) {
					msg.what = LOGIN_FAILURE;
					handler.sendMessage(msg);
				}
			} else {
				Toast.makeText(LoginActivity.this, "�����쳣��������������", Toast.LENGTH_SHORT).show();
			}
			Looper.loop();

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

	private void saveUserInfo(JSONObject data) throws JSONException {
		int resultCode = (Integer) data.getInt("resultCode");
		if (resultCode == 1000) {
			saveLoginInfo(LoginActivity.this, accountText, accountPassword);
			JSONObject infoData = (JSONObject) data.get("userinfo");
			Constants.app_userid = (String) infoData.getString("_id");
			Constants.app_usersex = (String) infoData.getString("sex");
			Constants.app_username = (String) infoData.getString("username");
			Constants.app_userpic = (String) infoData.getString("userpic");
			Constants.app_userrank = (Integer) infoData.getInt("rank");
			Constants.app_userrankText = Constants.SwitchRankText((Integer) infoData.getInt("rank"));
			Constants.app_userscore = String.valueOf((Integer) infoData.getInt("score"));
			Intent intent = new Intent(LoginActivity.this, DiscoveryActivity.class);
			LoginActivity.this.startActivity(intent);
			LoginActivity.this.finish();
		} else if (resultCode == 1001) {
			Toast.makeText(LoginActivity.this, "�û������������", Toast.LENGTH_SHORT).show();
		}
	}
	
	@Override
	public void onStart() {
		super.onStart();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);  //����ͳ��
	}

	@Override
	public void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);  //����ͳ��
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
