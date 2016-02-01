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
import com.umeng.analytics.MobclickAgent;
import com.umeng.message.PushAgent;

public class UsercenterActivity extends Activity implements OnUploadProcessListener {

	private long exitTime = 0;  //�����ؼ����˳�ʱ��
	private static final String TAG = "zhihu.ui.UsercenterActivity";   //����ǣ��ڿ���̨���Կ�������ҳ�����������

	private final int TO_UPLOAD_FILE = 1; // ȥ�ϴ��ļ�
	private final int UPLOAD_FILE_DONE = 2; // �ϴ��ļ���Ӧ
	private final int UPLOAD_INIT_PROCESS = 3; // �ϴ���ʼ��
	private final int UPLOAD_IN_PROCESS = 4; // �ϴ���
	private final int DOWNLOAD_IMAGE_SUCCESS = 5; // ����ͼƬ�ɹ�
	private final int DOWNLOAD_IMAGE_FAILURE = 6; // ����ͼƬʧ��
	private final int USERINFO_SUCCESS = 7; // ��ѯ�û���Ϣ�ɹ�
	private final int USERINFO_FAILURE = 8; // ��ѯ�û���Ϣʧ��

	private final int UPLOAD_FILE_FAILURE = 9; // �ϴ��ļ�ʧ��
	
	private final int UPDATE_USER_SUCCESS = 10; // �����û���Ϣ�ɹ�(����ͷ��)
	private final int UPDATE_USER_FAILURE = 11; // �����û���Ϣʧ��(����ͷ��)

	private String requestImgUrl = Constants.NODE_URL + "/updateuser"; // ���������·��

	private ImageButton title_move_btn;    //�������е��л���ఴť
	private RelativeLayout user_itemmenu;  //�û���ť������ͼ����ȡ����ؼ�Ŀ��������͸����
	private practiceschool.circlepic.CircularImage user_portrait; // �û�ͷ��
	private Button user_write_icon; // ��ʼд����ť
	private Button user_publish_icon; // �ҵ�д����ť
	private Button user_boy_btn; // �к���ť
	private Button user_girl_btn; // Ů����ť
	private EditText username_edit; // �ǳƱ༭��
	private Button publish_btn; // ������ť
	private TextView userrank_number; // �û��ȼ����ؼ���
	private TextView userscore_number; // �û����֣��ؼ���
	private String sexChecked = "boy"; // �û�ѡ���Ա�
	private String userpic; // ͷ���ַ
	private String username; // �û��ǳ�
	private ProgressDialog progressDialog; // ����loading�Ի���
	private String userid = Constants.app_userid; // �û�ID��  ���ӣ�"556c58006df508f80f887460"

	/* ͷ������ */
	private final String IMAGE_FILE_NAME = "xueyuan_" + new Date().getTime() + ".jpg";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.v(TAG, "OnCreate");
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE); // ����ʹ���Զ������
		setContentView(R.layout.usercenter_main);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.usercenter_title); // �Զ������������

		// ��ʼ�����ֿؼ�
		this.initComponent();

		// ��ʼ����Ⱦ����
		this.initData();

		// �л����˵���ť
		title_move_btn = (ImageButton) findViewById(R.id.title_move_btn);
		title_move_btn.setOnClickListener(new OnClickListener() {

			public void onClick(View view) {

				LeftMenu.menu.toggle(true);

			}
		});

		// ��ʼ�����˵�
		LeftMenu.initSlidingMenu(UsercenterActivity.this, "user");

		MobclickAgent.updateOnlineConfig(this); // ����ͳ��
		PushAgent.getInstance(this).onAppStart();  //��������
	}

	/**
	 * ��ʼ������ؼ�
	 */
	private void initComponent() {

		user_itemmenu = (RelativeLayout) findViewById(R.id.user_itemmenu);  //�û���ť������ͼ
		user_portrait = (practiceschool.circlepic.CircularImage) findViewById(R.id.user_portrait);  //�û�ͷ��
		user_write_icon = (Button) findViewById(R.id.user_write_icon);      //���ҵ��ղء���ť
		user_publish_icon = (Button) findViewById(R.id.user_publish_icon);  //���ҵķ�������ť
		user_boy_btn = (Button) findViewById(R.id.user_boy_btn);            //ѡ���к�����ť
		user_girl_btn = (Button) findViewById(R.id.user_girl_btn);          //ѡ��Ů������ť
		username_edit = (EditText) findViewById(R.id.username_edit);        //���û��ǳơ��༭��
		publish_btn = (Button) findViewById(R.id.publish_btn);              //���༭�����·�������ť
		userrank_number = (TextView) findViewById(R.id.userrank_number);    //���û��ȼ����ı�
		userscore_number = (TextView) findViewById(R.id.userscore_number);  //���û����֡��ı�
		progressDialog = new ProgressDialog(this);                          //����loading�Ի��� 

		// ���ñ���͸����
		user_itemmenu.getBackground().setAlpha(90); // 0~255͸����ֵ

		/**
		 * ��ת�����ҵ��ղء�
		 */
		user_write_icon.setOnClickListener(new OnClickListener() {

			public void onClick(View view) {

				Intent intent = new Intent();
				intent.setClass(UsercenterActivity.this, CollectActivity.class);
				UsercenterActivity.this.startActivity(intent);

			}
		});

		/**
		 * ��ת�����ҵķ�����
		 */
		user_publish_icon.setOnClickListener(new OnClickListener() {

			public void onClick(View view) {

				Intent intent = new Intent();
				intent.setClass(UsercenterActivity.this, PublishActivity.class);
				UsercenterActivity.this.startActivity(intent);

			}
		});
 
		/**
		 * ѡ���к�
		 */
		user_boy_btn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				user_boy_btn.setBackgroundResource(R.drawable.boy_selected);
				user_girl_btn.setBackgroundResource(R.drawable.girl_unselected);
				sexChecked = "boy";
			}
		});

		/**
		 * ѡ��Ů��
		 */
		user_girl_btn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				user_girl_btn.setBackgroundResource(R.drawable.girl_selected);
				user_boy_btn.setBackgroundResource(R.drawable.boy_unselected);
				sexChecked = "girl";
			}
		});

		/**
		 * �ϴ�ͷ��
		 */
		user_portrait.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				showImagePickDialog();
			}
		});

		/**
		 * �����û���Ϣ
		 */
		publish_btn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				
				//��ȡ�û��ǳ�ֵ
				username = username_edit.getText().toString().trim();
				
				//���ﱣ���û���Ϣ���ϴ�ͼƬ�Ͳ��ϴ�ͼƬ�������ӿڣ������Ż�һ��
				if (ImageUtils.picPath != null) {			
					handler.sendEmptyMessage(TO_UPLOAD_FILE);
				} else {
					try {
						String ajaxUrl = Constants.NODE_URL + "/userEditInfo";
						String ajaxData = "userid=" + userid + "&username=" + URLEncoder.encode(username, "utf-8") + "&sex=" + sexChecked;						 
						progressDialog.setMessage("���ڸ����У����Ժ�...");  
						progressDialog.show();   //��ʾ���ضԻ���
						new Thread(new UpdateThread(ajaxUrl, ajaxData)).start();    //����һ�����߳�
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

			}
		});

	}
	
	
	/**
	 * �����û���Ϣ�߳�(����ͷ��)
	 */
	private class UpdateThread implements Runnable {

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
				Toast.makeText(UsercenterActivity.this, "�����쳣��������������", Toast.LENGTH_SHORT).show();
			}
			Looper.loop();
		}
	}

	/**
	 * ��ʼ����Ⱦ����
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
		 *imageLoader�������Ǽ����б�ͼƬ
		 * ����1��ͼƬurl 
		 * ����2����ʾͼƬ�Ŀؼ� 
		 * ����3����ʾͼƬ������ 
		 * ����4��������
		 */
		userpic = Constants.app_userpic;
		Constants.imageLoader.cancelDisplayTask(user_portrait);
		Constants.imageLoader.displayImage(userpic, user_portrait, Constants.options, Constants.animateFirstListener);

		// �����û��ȼ��ͻ���
		String ajaxUrl = Constants.NODE_URL + "/userinfo";
		String ajaxData = "userid=" + userid;
		if (Constants.isNetworkConnected(UsercenterActivity.this)) { // �ж��Ƿ�������
			try {
				new Thread(new UserinfoThread(ajaxUrl, ajaxData)).start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			Toast.makeText(UsercenterActivity.this, "�����쳣��������������", Toast.LENGTH_SHORT).show();
		}

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
					// String result = "��Ӧ�룺" + msg.arg1 + "\n��Ӧ��Ϣ��" + msg.obj +
					// "\n��ʱ��" + UploadUtil.getRequestTime() + "��";
					try {
						saveUserInfo((JSONObject) msg.obj);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					Toast.makeText(UsercenterActivity.this, "�û���Ϣ���³ɹ�", 0).show();
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
	 * �û���Ϣ���³ɹ�(����ͷ��)
	 */
	private void udpateUser(JSONObject data) throws JSONException {
		int resultCode = (Integer) data.getInt("resultCode");
		if (resultCode == 1000) {
			progressDialog.hide();
			Toast.makeText(UsercenterActivity.this, "�û���Ϣ���³ɹ�", Toast.LENGTH_SHORT).show();
			Constants.app_usersex = sexChecked;
			Constants.app_username = username;
		} else if (resultCode == 0) {
			Toast.makeText(UsercenterActivity.this, "����ʧ��", Toast.LENGTH_SHORT).show();
		}
	}


	/**
	 * ��ȡ�û���Ϣ�߳�
	 */
	private class UserinfoThread implements Runnable {

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
				Toast.makeText(UsercenterActivity.this, "�����쳣��������������", Toast.LENGTH_SHORT).show();
			}
			Looper.loop();
		}
	}

	//����ɹ�����ȡ�û���Ϣ
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
			Toast.makeText(UsercenterActivity.this, "��ȡ���û���Ϣʧ��", Toast.LENGTH_SHORT).show();
		}
	}

	private void saveUserInfo(JSONObject data) throws JSONException {
		int resultCode = (Integer) data.getInt("resultCode");
		if (resultCode == 1000) {
			JSONObject infoData = (JSONObject) data.get("userinfo");
			Constants.app_usersex = (String) infoData.getString("sex");
			// �û����ı����ʽ��Ҫת��utf-8
			try {
				Constants.app_username = (String) new String(infoData.getString("username").getBytes("iso-8859-1"), "utf-8");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Constants.app_userpic = (String) infoData.getString("userpic");
			ImageUtils.picPath = null;   //�����û�ͷ��ɹ���ԭ��ͼƬ·�����
		}
	}

	/**
	 * ��ȡ�û�ͷ��
	 */
	public void showImagePickDialog() {

		String title = "��ȡͼƬ��ʽ";
		String[] choices = new String[] { "����", "���ֻ���ѡ��" };
		
		//��������Ի���
		new AlertDialog.Builder(this).setTitle(title).setItems(choices, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				switch (which) {
				case 0:
					ImageUtils.openCameraImage(UsercenterActivity.this);   //����
					break;
				case 1:
					ImageUtils.openLocalImage(UsercenterActivity.this);    //ѡȡ����ͼƬ
					break;
				}
			}
		}).setNegativeButton("����", null).show();    //��ʾ�Ի����еķ��ذ�ť
	}

	/**
	 * ���ա����ֻ���ѡ��ͼƬ�������봦��startActivityForResult - onActivityResult���������¼����Ƕ�Ӧ��ϵ��
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
			// �Զ���Uri(ImageUtils.imageUriFromCamera),���ڱ������պ�ͼƬ��ַ
			if (ImageUtils.imageUriFromCamera != null) {
				// ����ֱ����ʾͼƬ,���߽�����������(��ѹ����ü���)
				// iv.setImageURI(ImageUtils.imageUriFromCamera);
				// ��ͼƬ���вü�
				ImageUtils.cropImage(this, ImageUtils.imageUriFromCamera, true);
			}
			break;
		// �ֻ�����ȡͼƬ
		case ImageUtils.GET_IMAGE_FROM_PHONE:
			if (data != null && data.getData() != null) {
				// ����ֱ����ʾͼƬ,���߽�����������(��ѹ����ü���)
				// iv.setImageURI(data.getData());
				// ��ͼƬ���вü�
				ImageUtils.cropImage(this, data.getData(), true);
			}
			break;
		// �ü�ͼƬ����
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
	 * �û�ͷ����ʾͼƬ
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

	/**
	 * ѹ��ͼƬ
	 */
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
	 * �ύ�û���Ϣ��������
	 */
	private void toUploadFile() {
		progressDialog.setMessage("���ڱ��棬���Ժ�...");
		progressDialog.show();
		String fileKey = "upload";
		UploadUtil uploadUtil = UploadUtil.getInstance();
		uploadUtil.setOnUploadProcessListener(this); // ���ü����������ϴ�״̬
		Map<String, String> params = new HashMap<String, String>();
		params.put("userid", userid);
		params.put("username", username);
		params.put("sex", sexChecked);

		// ��ȡ�ϴ�ͷ�����·��
		File file = new File(Environment.getExternalStorageDirectory(), IMAGE_FILE_NAME);
		String _filePath = file.getAbsolutePath();

		// ��һ�����̣߳���������
		// _filePath���ϴ���ͼƬ·��
		// fileKey��ǰ���Լ����keyֵ
		// requestImgUrl������������ַ
		// params������
		
		uploadUtil.uploadFile(_filePath, fileKey, requestImgUrl, params);
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
			Toast.makeText(UsercenterActivity.this, "�����쳣��������������", Toast.LENGTH_SHORT).show();
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
		Log.v(TAG, "onStart");
		super.onStart();
	}

	@Override
	public void onResume() {
		Log.v(TAG, "OnResume");
		super.onResume();
		MobclickAgent.onResume(this); // ����ͳ��
	}

	@Override
	public void onPause() {
		super.onPause();
		MobclickAgent.onPause(this); // ����ͳ��
	}

	public void onStop() {
		Log.v(TAG, "OnStop");
		super.onStop();
	}

	public void onDestroy() {
		Log.v(TAG, "Destroy");
		super.onDestroy();
	}
	
	/**
	 * �ٰ�һ�η��ؼ��˳�����
	 */
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