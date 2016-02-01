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

	private long exitTime = 0;  //�����ؼ����˳�ʱ��
	private static final String TAG = "zhihu.ui.WritingActivity";

	private final int TO_UPLOAD_FILE = 1; // ȥ�ϴ��ļ�
	private final int UPLOAD_FILE_DONE = 2; // �ϴ��ļ���Ӧ
	private final int TO_SELECT_PHOTO = 3; // ѡ���ļ�
	private final int UPLOAD_INIT_PROCESS = 4; // �ϴ���ʼ��
	private final int UPLOAD_IN_PROCESS = 5; // �ϴ���
	private final int DOWNLOAD_IMAGE_SUCCESS = 6; // ����ͼƬ�ɹ�
	private final int DOWNLOAD_IMAGE_FAILURE = 7; // ����ͼƬʧ��
	private String KEY_PHOTO_PATH = "photo_path"; // ��Intent��ȡͼƬ·����KEY
	private String requestImgUrl = Constants.NODE_URL + "/addArticle"; // ���������·��
	private Button publishBtn; // ���ע�ᰴť�ؼ�
	private EditText title; // ���±��⣨�ؼ���
	private EditText content; // �������ݣ��ؼ���
	private String titleText; // ���±���
	private String contentText; // ��������
	private ImageView cover; // ���·���
	private ProgressDialog progressDialog; // ����loading�Ի���
	private Button channel_exercise; // ���ͣ������޵У��ؼ���
	private Button channel_girls; // ���ͣ���Ѫ��椣��ؼ���
	private Button channel_ask; // ���ͣ���ʲôҪ�ʣ��ؼ���
	private Button channel_learning; // ���ͣ�����֮�����ؼ���
	private String channelCheck = "1"; // Ĭ��ѡ�н����޵�

	private String targetPath;
	private String avatorpath = Environment.getExternalStorageDirectory() + "/articlecover/";
	private Bitmap bitmap;
	public ImageButton title_move_btn;
	/* �������� */
	private final String IMAGE_FILE_NAME = "xueyuan_" + new Date().getTime() + ".jpg";

	/**
	 * ��ǰ�û���Ϣ
	 */
	private String userid = Constants.app_userid; // �û�ID��
	private String username = Constants.app_username; // �û��ǳ�
	private String usersex = Constants.app_usersex; // �û��Ա�
	private String userpic = Constants.app_userpic; // �û�ͷ��

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE); // ����ʹ���Զ������
		setContentView(R.layout.writing_main);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.writing_title); // �Զ�����������ָ�ֵ
		this.initComponent();

		LeftMenu.initSlidingMenu(WritingActivity.this, "write"); // ��ʼ�����˵�
		/**
		 * ��ʼ���л����˵���ť
		 */
		title_move_btn = (ImageButton) findViewById(R.id.title_move_btn);
		title_move_btn.setOnClickListener(new OnClickListener() {

			public void onClick(View view) {

				LeftMenu.menu.toggle(true);

			}
		});
		
		MobclickAgent.updateOnlineConfig(this);  //����ͳ��
	}

	/**
	 * ��ʼ�������еĿؼ�
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
		 * ѡ��"�����޵�"
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
		 * ѡ��"��Ѫ���"
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
		 * ѡ��"��ʲôҪ��"
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
		 * ѡ��"����֮��"
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

		// ���Դ��룬����ͼƬ
		// String _url =
		// "http://120.55.99.230/public/upload/covers/upload_6999b517a8808cae142e755da1cd4c81.jpg";
		// new Thread(new downloadRunnable(_url,cover)).start();

		// �ϴ�����
		cover.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				showImagePickDialog();
			}
		});

		// ��������
		publishBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {

				if ((ImageUtils.picPath != null) || (targetPath != null)) {
					titleText = title.getText().toString().trim();
					contentText = content.getText().toString().trim();
					if (titleText.equals("")) {
						Toast.makeText(WritingActivity.this, "���ⲻ��Ϊ��", Toast.LENGTH_SHORT).show();
					}else if (titleText.length()>25) {
						Toast.makeText(WritingActivity.this, "���ⲻ�ܳ���25����", Toast.LENGTH_SHORT).show();
					}else if (contentText.equals("")) {
						Toast.makeText(WritingActivity.this, "���ݲ���Ϊ��", Toast.LENGTH_SHORT).show();
					}else{
						handler.sendEmptyMessage(TO_UPLOAD_FILE);				
					}
				} else {
					Toast.makeText(WritingActivity.this, "���ϴ����·���", Toast.LENGTH_SHORT).show();
				}

			}
		});
	}

	/**
	 * ��ȡ���·���
	 */
	public void showImagePickDialog() {

		String title = "��ȡͼƬ��ʽ";
		String[] choices = new String[] { "����", "���ֻ���ѡ��" };
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
				showImage(ImageUtils.imageUriFromCamera);
			}

			break;
		// �ֻ�����ȡͼƬ
		case ImageUtils.GET_IMAGE_FROM_PHONE:
			if (data != null && data.getData() != null) {
				showImage(data.getData());
			}
			break;
		// �ü�ͼƬ����
		case ImageUtils.CROP_IMAGE:
			if (ImageUtils.cropImageUri != null) {

				File fileDir = new File(avatorpath);
				if (!fileDir.exists()) {
					fileDir.mkdirs();// �����ļ���
				}
				targetPath = avatorpath + IMAGE_FILE_NAME;
				ImageUtils.compressBitmap(ImageUtils.picPath, targetPath, 640); // ѹ��
				bitmap = ImageUtils.decodeBitmap(targetPath, 100); // �ֽ�
				cover.setImageBitmap(bitmap); // ��ʾ

			}
			break;
		default:
			break;
		}
	}

	/**
	 * ��ʾ����/ͼ���е�ͼƬ
	 */
	private void showImage(Uri imageUriFromCamera) {
		File fileDir = new File(avatorpath);
		if (!fileDir.exists()) {
			fileDir.mkdirs();// �����ļ���
		}
		targetPath = avatorpath + IMAGE_FILE_NAME;
		ImageUtils.picPath = ImageUtils.getRealPathFromURI(imageUriFromCamera, WritingActivity.this);
		ImageUtils.compressBitmap(ImageUtils.picPath, targetPath, 640); // ѹ��
		bitmap = ImageUtils.decodeBitmap(targetPath, 100); // �ֽ�
		cover.setImageBitmap(bitmap); // ��ʾ
	

	}

	/**
	 * �ύ�û���Ϣ��������
	 */
	private void toUploadFile() {
		// uploadImageResult.setText("�����ϴ���...");
		progressDialog.setMessage("���ڷ��������Ժ�...");
		progressDialog.show();
		String fileKey = "upload";
		UploadUtil uploadUtil = UploadUtil.getInstance();
		uploadUtil.setOnUploadProcessListener(this); // ���ü����������ϴ�״̬
		Map<String, String> params = new HashMap<String, String>();
		params.put("channel", channelCheck);
		params.put("title", titleText);
		params.put("content", contentText);
		params.put("description", getDescription(contentText));
		params.put("writeid", userid);
		params.put("writename", username);
		params.put("writesex", usersex);
		params.put("writepic", userpic);
 
		// ��һ�����̣߳���������
		uploadUtil.uploadFile(targetPath, fileKey, requestImgUrl, params);
	}

	/**
	 * ��ȡdescription����
	 */
	private String getDescription(String str) {
		String newStr = str.replaceAll("\n", " ");
		if (newStr.length() > 50) {
			newStr = newStr.substring(0, 50) + "...";
		}
		return newStr;
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
					ImageUtils.picPath = null;
					Toast.makeText(WritingActivity.this, "���·����ɹ�����ȴ����...", Toast.LENGTH_SHORT).show();
					//���·����ɹ����������
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
	 * �ϴ���Ӧ��ʵ��OnUploadProcessListener�ӿ�
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

	/**
	 * ���ط�����ͼƬ�߳�
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
	 * ʹ��SharedPreferences�����û�д����Ϣ
	 * 
	 * @param context
	 * @param title
	 * @param content
	 */
	public static void saveCrashInfo(Context context, String title, String content) {
		// ��ȡSharedPreferences����
		SharedPreferences sharedPre = context.getSharedPreferences("config", context.MODE_PRIVATE);
		// ��ȡEditor����
		Editor editor = sharedPre.edit();
		// ���ò���
		editor.putString("title", title);
		editor.putString("content", content);
		// �ύ
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
		// Ĭ����ʾ�ѱ�������±��������
		SharedPreferences sharedPre = getSharedPreferences("config", MODE_PRIVATE);
		String _title = sharedPre.getString("title", "");
		String _content = sharedPre.getString("content", "");
		title.setText(_title);
		content.setText(_content);
		MobclickAgent.onResume(this);  //����ͳ��
	}

	@Override
	public void onPause() {
		Log.v(TAG, "OnPause unregister progress receiver");
		super.onPause();
		titleText = title.getText().toString().trim();
		contentText = content.getText().toString().trim();
		saveCrashInfo(WritingActivity.this, titleText, contentText);
		MobclickAgent.onPause(this);  //����ͳ��
	}

	public void onStop() {
		Log.v(TAG, "OnStop");
		super.onStop();
	}

	public void onDestroy() {
		Log.v(TAG, "Destroy");
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
