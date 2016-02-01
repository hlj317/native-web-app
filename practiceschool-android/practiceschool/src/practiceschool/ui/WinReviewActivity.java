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

	private final int REVIEW_SUCCESS = 1; // 发表评论成功
	private final int REVIEW_FAILURE = 2; // 发表评论失败

	private EditText message_content; // 对话框的评论内容
	private Button review_btn; // 对话框的评论按钮
	private String articleid; // 文章ID
	private String userid; // 用户ID
	private String username; // 用户姓名
	private String usersex; // 用户性别
	private String userpic; // 用户头像
	private String reviewcontent; // 评论内容

	private ImageView loadingView; // 正在加载的图标
	private RotateAnimation refreshingAnimation; // 均匀旋转动画

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.win_review_main);
		// setFinishOnTouchOutside(false);
		// 初始化布局控件
		this.initComponent();

		// 初始化加载文章详情数据、点亮评论数据、所有评论数据
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
	 * 初始化界面控件
	 */
	private void initComponent() {
		message_content = (EditText) findViewById(R.id.message_content);
		review_btn = (Button) findViewById(R.id.review_btn);
		loadingView = (ImageView) findViewById(R.id.loading_icon);
		refreshingAnimation = (RotateAnimation) AnimationUtils.loadAnimation(this, R.anim.rotating);

		// 写评论
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
				// 判断是否有网络
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
	 * 数据服务器读取成功后，评论发布成功
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

			// 广播通知用户积分变化
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
	 * 发表评论线程
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
				Toast.makeText(WinReviewActivity.this, "网络异常，请检查您的网络", Toast.LENGTH_SHORT).show();
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
