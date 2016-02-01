package practiceschool.ui;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.URLEncoder;

import org.json.JSONException;
import org.json.JSONObject;

import practiceschool.network.HttpMethod;
import practiceschool.utils.Constants;


import com.example.practiceschool.R;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class WinReviewActivity extends Activity {

	private final int REVIEW_SUCCESS = 1; // �������۳ɹ�
	private final int REVIEW_FAILURE = 2; // ��������ʧ��

	private EditText message_content; // �Ի������������
	private Button review_btn; // �Ի�������۰�ť
	private String articleid; // ����ID
	private String userid; // �û�ID
	private String username; // �û�����
	private String usersex; // �û��Ա�
	private String userpic; // �û�ͷ��
	private String reviewcontent; // ��������

	private ImageView loadingView; // ���ڼ��ص�ͼ��
	private RotateAnimation refreshingAnimation; // ������ת����

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.win_review_main);
		// setFinishOnTouchOutside(false);
		// ��ʼ�����ֿؼ�
		this.initComponent();

		// ��ʼ�����������������ݡ������������ݡ�������������
		Bundle bundle = this.getIntent().getExtras();
		if (bundle != null) {
			articleid = bundle.getString("articleid");
			userid = Constants.app_userid;
			username = Constants.app_username;
			usersex = Constants.app_usersex;
			userpic = Constants.app_userpic;
		}
	}

	/**
	 * ��ʼ������ؼ�
	 */
	private void initComponent() {
		message_content = (EditText) findViewById(R.id.message_content);
		review_btn = (Button) findViewById(R.id.review_btn);
		loadingView = (ImageView) findViewById(R.id.loading_icon);
		refreshingAnimation = (RotateAnimation) AnimationUtils.loadAnimation(this, R.anim.rotating);

		// д����
		review_btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				reviewcontent = message_content.getText().toString().trim();
				String ajaxUrl = Constants.NODE_URL + "/review";
				String ajaxData = null;
				try {
					ajaxData = "articleid=" + articleid + "&reviewcontent=" + URLEncoder.encode(reviewcontent, "utf-8") + "&userid=" + userid + "&username=" + URLEncoder.encode(username, "utf-8")
							+ "&usersex=" + usersex + "&userpic=" + userpic;
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				// �ж��Ƿ�������
				if (Constants.isNetworkConnected(WinReviewActivity.this)) {
					try {
						review_btn.setVisibility(View.INVISIBLE);
						loadingView.setVisibility(View.VISIBLE);
						loadingView.startAnimation(refreshingAnimation);
						new Thread(new reviewThread(ajaxUrl, ajaxData)).start();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

			}
		});

	}

	/**
	 * ���ݷ�������ȡ�ɹ������۷����ɹ�
	 */
	private void reviewSend(JSONObject data) throws JSONException {
		int resultCode = (Integer) data.getInt("resultCode");
		String description = (String) data.getString("description");
		String reviewtime = (String) data.getString("reviewtime");
		String reviewid = (String) data.getString("reviewid");
		if (resultCode == 1000) {
			// review_btn.setVisibility(View.VISIBLE);
			// loadingView.setVisibility(View.GONE);
			loadingView.clearAnimation();

			// �㲥֪ͨ�û����ֱ仯
			// Intent intent = new Intent();
			// intent.setAction("book.receiver.userReceiver");
			// intent.putExtra("score","2");
			// sendBroadcast(intent);

			Toast.makeText(getApplicationContext(), description, Toast.LENGTH_SHORT).show();
		} else if (resultCode == 1001) {
			Toast.makeText(getApplicationContext(), description, Toast.LENGTH_SHORT).show();
		}
		Intent intent = new Intent();
		intent.putExtra("reviewcontent", reviewcontent);
		intent.putExtra("reviewtime", reviewtime);
		intent.putExtra("reviewid", reviewid);
		setResult(1000, intent);
		WinReviewActivity.this.finish();
	};

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
				case REVIEW_SUCCESS:
					try {
						reviewSend((JSONObject) msg.obj);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					break;
				case REVIEW_FAILURE:
					break;
				default:
					break;
				}
			}
			super.handleMessage(msg);
		}

	};

	/**
	 * ���������߳�
	 */
	class reviewThread implements Runnable {

		private String ajaxUrl;
		private String ajaxData;

		public reviewThread(String ajaxUrl, String ajaxData) {
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
					msg.what = REVIEW_SUCCESS;
					msg.obj = data;
					handler.sendMessage(msg);
				} catch (JSONException e) {
					msg.what = REVIEW_FAILURE;
					handler.sendMessage(msg);
				}
			} else {
				Toast.makeText(WinReviewActivity.this, "�����쳣��������������", Toast.LENGTH_SHORT).show();
			}
			Looper.loop();
		}
	}

	public boolean onTouchEvent(MotionEvent event) {
		this.finish();
		return super.onTouchEvent(event);

	}

	@Override
	public void onBackPressed() {
		this.finish();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
