package practiceschool.ui;

import java.io.File;
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
import com.example.practiceschool.R;
import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class PublisheditActivity extends Activity implements OnUploadProcessListener{

	private static final String TAG = "zhihu.ui.PublisheditActivity";

	private final int TO_UPLOAD_FILE = 1; // ȥ�ϴ��ļ�
	private final int UPLOAD_FILE_DONE = 2; // �ϴ��ļ���Ӧ
	private final int TO_SELECT_PHOTO = 3; // ѡ���ļ�
	private final int UPLOAD_INIT_PROCESS = 4; // �ϴ���ʼ��
	private final int UPLOAD_IN_PROCESS = 5; // �ϴ���
	private final int LOAD_ARTICLE_SUCCESS = 6; // ������������ɹ�
	private final int LOAD_ARTICLE_FAILURE = 7; // ������������ʧ��
	private final int UPDATE_ARTICLE_SUCCESS = 8; // �������³ɹ�
	private final int UPDATE_ARTICLE_FAILURE = 9; // ��������ʧ��
	private String requestImgUrl = Constants.NODE_URL + "/userEditArticle"; // ���������·��
	private Button publishBtn; // �༭�귢����ť�ؼ�
	private EditText title; // ���±��⣨�ؼ���
	private EditText description; // �����������ؼ���
	private EditText content; // �������ݣ��ؼ���
	private TextView checkstatus; // ���״̬���ؼ���
	private TextView failcause; // ʧ��ԭ�򣨿ؼ���
	private TextView mychannel; // ������𣨿ؼ���

	private String titleText; // ���±���
	private String descriptionText; // ��������
	private String contentText; // ��������
	private String failcauseText; // ����δͨ�����ԭ��
	private int mychannelText; // �������
	private String coverUrl; // ���·���·��

	private ImageView cover; // ���·���
	private ProgressDialog progressDialog; // ����loading�Ի���
	private String articleid; // ����ID

	private String targetPath;
	private String avatorpath = Environment.getExternalStorageDirectory() + "/articlecover/";
	private Bitmap bitmap;
	public ImageButton title_move_btn;
	/* �������� */
	private final String IMAGE_FILE_NAME = "xueyuan_" + new Date().getTime() + ".jpg";

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE); // ����ʹ���Զ������
		setContentView(R.layout.publishedit_main);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.publishedit_title); // �Զ�����������ָ�ֵ

		// ��ʼ�����ֿؼ�
		this.initComponent();

		// ��ʼ�����������������ݡ������������ݡ�������������
		Bundle bundle = this.getIntent().getExtras();
		if (bundle != null) {
			articleid = bundle.getString("articleid");
			loadArticleInfo(articleid); // ����������������
		}

		/**
		 * ��ʼ���л����˵���ť
		 */
		title_move_btn = (ImageButton) findViewById(R.id.title_move_btn);
		title_move_btn.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				Intent intent = new Intent();
				intent.setClass(PublisheditActivity.this, PublishActivity.class);
				PublisheditActivity.this.startActivity(intent);
				PublisheditActivity.this.finish();
			}
		});
		
		MobclickAgent.updateOnlineConfig(this);  //����ͳ��
	}

	/**
	 * ������������
	 */
	private void loadArticleInfo(String articleid) {
		String ajaxUrl = Constants.NODE_URL + "/articleinfo";
		String ajaxData = "articleid=" + articleid;

		// �ж��Ƿ�������
		if (Constants.isNetworkConnected(PublisheditActivity.this)) {
			try {
				new Thread(new articleinfoThread(ajaxUrl, ajaxData)).start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * ��ʼ�������еĿؼ�
	 */
	private void initComponent() {

		cover = (ImageView) findViewById(R.id.cover);
		publishBtn = (Button) findViewById(R.id.publish_btn);
		progressDialog = new ProgressDialog(this);
		title = (EditText) findViewById(R.id.title);
		description = (EditText) findViewById(R.id.description);
		content = (EditText) findViewById(R.id.content);
		checkstatus = (TextView) findViewById(R.id.checkstatus);
		failcause = (TextView) findViewById(R.id.failcause);
		mychannel = (TextView) findViewById(R.id.mychannel);

		// �ϴ�����
		cover.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				showImagePickDialog();
			}
		});

		// ��������
		publishBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {

				titleText = title.getText().toString().trim();
				descriptionText = description.getText().toString().trim();
				contentText = content.getText().toString().trim();
				
				if (titleText.equals("")) {
					Toast.makeText(PublisheditActivity.this, "���ⲻ��Ϊ��", Toast.LENGTH_SHORT).show();
				}else if (titleText.length()>25) {
					Toast.makeText(PublisheditActivity.this, "���ⲻ�ܳ���25����", Toast.LENGTH_SHORT).show();
				}else if (descriptionText.equals("")) {
					Toast.makeText(PublisheditActivity.this, "��������Ϊ��", Toast.LENGTH_SHORT).show();
				}else if (contentText.equals("")) {
					Toast.makeText(PublisheditActivity.this, "���ݲ���Ϊ��", Toast.LENGTH_SHORT).show();
				}else{				
					// �ж��Ƿ�������
					if (Constants.isNetworkConnected(PublisheditActivity.this)) {
						if ((ImageUtils.picPath != null) || (targetPath != null)) {
							handler.sendEmptyMessage(TO_UPLOAD_FILE);
						} else {
							try {
								String ajaxUrl = Constants.NODE_URL + "/userEditMessage";
								String ajaxData = "articleid=" + articleid + "&title=" + URLEncoder.encode(titleText, "utf-8") + "&description=" + URLEncoder.encode(descriptionText, "utf-8")
										+ "&content=" + URLEncoder.encode(contentText, "utf-8");
								progressDialog.setMessage("���ڷ����У����Ժ�...");
								progressDialog.show();
								new Thread(new UpdateThread(ajaxUrl, ajaxData)).start();
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}				
					
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
					ImageUtils.openCameraImage(PublisheditActivity.this);
					break;
				case 1:
					ImageUtils.openLocalImage(PublisheditActivity.this);
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
		ImageUtils.picPath = ImageUtils.getRealPathFromURI(imageUriFromCamera, PublisheditActivity.this);
		ImageUtils.compressBitmap(ImageUtils.picPath, targetPath, 640); // ѹ��
		bitmap = ImageUtils.decodeBitmap(targetPath, 100); // �ֽ�
		cover.setImageBitmap(bitmap); // ��ʾ
	}

	/**
	 * �ύ�û���Ϣ��������
	 */
	private void toUploadFile() {
		// uploadImageResult.setText("�����ϴ���...");
		progressDialog.setMessage("���ڷ����У����Ժ�...");
		progressDialog.show();
		String fileKey = "upload";
		UploadUtil uploadUtil = UploadUtil.getInstance();
		uploadUtil.setOnUploadProcessListener(this); // ���ü����������ϴ�״̬
		Map<String, String> params = new HashMap<String, String>();
		params.put("articleid", articleid);
		params.put("title", titleText);
		params.put("content", contentText);
		params.put("description", descriptionText);

		// ��һ�����̣߳���������
		uploadUtil.uploadFile(targetPath, fileKey, requestImgUrl, params);
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
					udpateSuccss();
					break;
				case LOAD_ARTICLE_SUCCESS:
					try {
						renderInfo((JSONObject) msg.obj);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				case LOAD_ARTICLE_FAILURE:
					break;
				case UPDATE_ARTICLE_SUCCESS:
					try {
						udpateArticle((JSONObject) msg.obj);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				case UPDATE_ARTICLE_FAILURE:
					break;
				default:
					break;
				}
			}
			super.handleMessage(msg);
		}

	};

	/**
	 * ���¸��³ɹ���Ķ���
	 */
	private void udpateSuccss() {
		Toast.makeText(PublisheditActivity.this, "���·����ɹ�����ȴ����...", Toast.LENGTH_SHORT).show();
		Intent intent = new Intent();
		intent.setClass(PublisheditActivity.this, PublishActivity.class);
		PublisheditActivity.this.startActivity(intent);
		PublisheditActivity.this.finish();
	}

	/**
	 * ���¸��³ɹ�(��������)
	 */
	private void udpateArticle(JSONObject data) throws JSONException {
		int resultCode = (Integer) data.getInt("resultCode");
		if (resultCode == 1000) {
			udpateSuccss();
		} else if (resultCode == 0) {
			Toast.makeText(PublisheditActivity.this, "����ʧ��", Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * ���ݷ�������ȡ�ɹ�����Ⱦ��������
	 */
	private void renderInfo(JSONObject data) throws JSONException {

		int resultCode = (Integer) data.getInt("resultCode");
		if (resultCode == 1000) {

			JSONObject infoData = (JSONObject) data.get("articleinfo");
			titleText = (String) infoData.getString("title");
			descriptionText = (String) infoData.getString("description");
			contentText = (String) infoData.getString("content");
			title.setText(titleText);
			description.setText(descriptionText);
			content.setText(contentText);

			checkstatus.setText((String) infoData.getString("checkstatus"));
			failcauseText = (String) infoData.getString("failcause");
			mychannelText = (Integer) infoData.getInt("channel");
			if (failcauseText.equals("")) {
				failcause.setVisibility(View.GONE);
			} else {
				failcause.setVisibility(View.VISIBLE);
				failcause.setText("δͨ��ԭ��" + failcauseText);
			}

			if (mychannelText == 1) {
				mychannel.setText("��𣺽����޵�");
			} else if (mychannelText == 2) {
				mychannel.setText("�����Ѫ���");
			} else if (mychannelText == 3) {
				mychannel.setText("�����ʲôҪ��");
			} else if (mychannelText == 4) {
				mychannel.setText("��𣺺�ڴ�");
			}

			/**
			 * ��ʾͼƬ ����1��ͼƬurl ����2����ʾͼƬ�Ŀؼ� ����3����ʾͼƬ������ ����4��������
			 */
			coverUrl = (String) infoData.getString("cover");
			Constants.imageLoader.displayImage(coverUrl, cover, Constants.options_big, Constants.animateFirstListener);

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
	 * �������������߳�
	 */
	class articleinfoThread implements Runnable {

		private String ajaxUrl;
		private String ajaxData;

		public articleinfoThread(String ajaxUrl, String ajaxData) {
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
					msg.what = LOAD_ARTICLE_SUCCESS;
					msg.obj = data;
					handler.sendMessage(msg);
				} catch (JSONException e) {
					msg.what = LOAD_ARTICLE_FAILURE;
					handler.sendMessage(msg);
				}
			} else {
				Toast.makeText(PublisheditActivity.this, "�����쳣��������������", Toast.LENGTH_SHORT).show();
			}
			Looper.loop();
		}
	}

	/**
	 * ���������߳�(��������)
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
					msg.what = UPDATE_ARTICLE_SUCCESS;
					msg.obj = data;
					handler.sendMessage(msg);
				} catch (JSONException e) {
					msg.what = UPDATE_ARTICLE_FAILURE;
					handler.sendMessage(msg);
				}
			} else {
				Toast.makeText(PublisheditActivity.this, "�����쳣��������������", Toast.LENGTH_SHORT).show();
			}
			Looper.loop();
		}
	}
	

	@Override
	public void onStart() {
		super.onStart();
	}
	
	@Override
	public void onResume() {
		Log.v(TAG, "OnResume");
		super.onResume();
		MobclickAgent.onResume(this);  //����ͳ��
	}

	@Override
	public void onPause() {
		Log.v(TAG, "OnPause unregister progress receiver");
		super.onPause();
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

}
