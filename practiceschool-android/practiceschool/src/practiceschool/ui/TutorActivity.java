package practiceschool.ui;

import java.lang.ref.WeakReference;

import org.json.JSONException;
import org.json.JSONObject;

import practiceschool.menu.ActionItem;
import practiceschool.menu.TitlePopup;
import practiceschool.menu.TitlePopup.OnItemOnClickListener;
import practiceschool.network.HttpMethod;
import practiceschool.utils.Constants;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.practiceschool.R;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.SendMessageToWX;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.mm.sdk.openapi.WXMediaMessage;
import com.tencent.mm.sdk.openapi.WXWebpageObject;
import com.umeng.analytics.MobclickAgent;

public class TutorActivity extends Activity implements OnTouchListener{

	private static final String TAG = "zhihu.ui.TutorActivity";

	private final int USERINFO_SUCCESS = 1; // 查询导师信息成功
	private final int USERINFO_FAILURE = 2; // 查询导师信息失败

	private ImageButton title_move_btn;
	private ImageButton title_menu;
	private LinearLayout tutor_itemmenu;
	private practiceschool.circlepic.CircularImage tutor_portrait; // 导师头像
	private TextView tutor_name;
	private TextView tutor_description;
	private TextView tutor_feature;
	private TextView tutor_rank;
	private TextView tutor_weixin;
	private RelativeLayout tutor_banner_bg;
	private String tutor_url; // 导师头像url
	private String tutorid; // 导师ID号
	private LinearLayout myView;
	
	//手指向右滑动时的最小速度
	private static final int XSPEED_MIN = 200;
	
	//手指向右滑动时的最小距离
	private static final int XDISTANCE_MIN = 150;
	
	//记录手指按下时的横坐标。
	private float xDown;
	
	//记录手指移动时的横坐标。
	private float xMove;
	
	//用于计算手指滑动的速度。
	private VelocityTracker mVelocityTracker;
	
	private TitlePopup titlePopup;
	private IWXAPI api;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.v(TAG, "OnCreate");
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE); // 声明使用自定义标题
		setContentView(R.layout.tutor_main);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.tutor_title); // 自定义标题栏布局赋值

		// 初始化布局控件
		this.initComponent();

		// 初始化加载文章详情数据、点亮评论数据、所有评论数据
		Bundle bundle = this.getIntent().getExtras();
		if (bundle != null) {
			tutorid = bundle.getString("tutorid");
			initData(tutorid); // 加载文章详情数据
		}


		/**
		 * 初始化切换左侧菜单按钮
		 */
		title_move_btn = (ImageButton) findViewById(R.id.title_move_btn);
		title_move_btn.setOnClickListener(new OnClickListener() {

			public void onClick(View view) {

				finish();

			}
		});
		
		myView = (LinearLayout) findViewById(R.id.tutor_wrapper);
		myView.setOnTouchListener(this);
		
		
		/**
		 * 初始化右上角菜单
		 */  
		titlePopup = new TitlePopup(this, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		titlePopup.addAction(new ActionItem(this, "分享给好友", R.drawable.mm_title_btn_share_normal));
		titlePopup.addAction(new ActionItem(this, "分享到朋友圈", R.drawable.mm_title_btn_weixin_normal));	

		titlePopup.setItemOnClickListener(new OnItemOnClickListener(){
 
			@Override
			public void onItemClick(ActionItem item, int position) {
				api.registerApp(Constants.APP_ID);
				WXWebpageObject webpage = new WXWebpageObject();
				webpage.webpageUrl = Constants.NODE_URL + "/tutorshare?key=" + tutorid;
				
				//用WXMediaMessage对象初始化一个WXMediaMessage对象
				WXMediaMessage msg = new WXMediaMessage(webpage);
				msg.title = "职场修炼学院";
				msg.description = "想要向各种厉害的导师学习不？想要让自己变得更强吗？那就赶紧来职场修炼学院吧！";
				Bitmap thumb = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
				msg.thumbData = Constants.bmpToByteArray(thumb, true);
				
				//构造一个Req
				SendMessageToWX.Req req = new SendMessageToWX.Req();
				req.transaction = Constants.buildTransaction("webpage");
				req.message = msg;
				if(position == 0){
					req.scene = SendMessageToWX.Req.WXSceneSession;
				}else if(position == 1){
					req.scene = SendMessageToWX.Req.WXSceneTimeline;
				}
				api.sendReq(req);
				
			}
			
		});
		
		title_menu = (ImageButton) findViewById(R.id.title_menu);
		title_menu.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				titlePopup.show(v);
			}
		});
		
		MobclickAgent.updateOnlineConfig(this);  //友盟统计
	
	}

	/**
	 * 初始化界面控件
	 */
	private void initComponent() {

		tutor_itemmenu = (LinearLayout) findViewById(R.id.tutor_itemmenu);
		tutor_portrait = (practiceschool.circlepic.CircularImage) findViewById(R.id.tutor_portrait);
		tutor_name = (TextView) findViewById(R.id.name);
		tutor_description = (TextView) findViewById(R.id.description);
		tutor_feature = (TextView) findViewById(R.id.feature);
		tutor_rank = (TextView) findViewById(R.id.rank);
		tutor_weixin = (TextView) findViewById(R.id.weixin);
		tutor_banner_bg = (RelativeLayout) findViewById(R.id.tutor_banner_bg);
		tutor_banner_bg.setBackgroundResource(Constants.setRandomCover());
		api = WXAPIFactory.createWXAPI(this, Constants.APP_ID,false);

		// 设置背景透明度
		tutor_itemmenu.getBackground().setAlpha(90); // 0~255透明度值

	}

	/**
	 * 初始化渲染数据
	 */
	private void initData(String tutorid) {

		// 获取导师信息
		String ajaxUrl = Constants.NODE_URL + "/getTutor";
		String ajaxData = "id=" + tutorid;
		if (Constants.isNetworkConnected(TutorActivity.this)) { // 判断是否有网络
			try {
				new Thread(new TutorinfoThread(ajaxUrl, ajaxData)).start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			Toast.makeText(TutorActivity.this, "网络异常，请检查您的网络", Toast.LENGTH_SHORT).show();
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
				case USERINFO_SUCCESS:
					try {
						updateTutorInfo((JSONObject) msg.obj);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					break;
				case USERINFO_FAILURE:
					break;
				default:
					break;
				}
			}
			super.handleMessage(msg);
		}

	};

	/**
	 * 获取用户信息线程
	 */
	class TutorinfoThread implements Runnable {

		private String ajaxUrl;
		private String ajaxData;

		public TutorinfoThread(String ajaxUrl, String ajaxData) {
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
				Toast.makeText(TutorActivity.this, "网络异常，请检查您的网络", Toast.LENGTH_SHORT).show();
			}
			Looper.loop();
		}
	}

	private void updateTutorInfo(JSONObject data) throws JSONException {
		int resultCode = (Integer) data.getInt("resultCode");
		if (resultCode == 1000) {
			JSONObject infoData = (JSONObject) data.get("tutorinfo");
			tutor_name.setText((String) infoData.getString("name"));
			tutor_url = (String) infoData.getString("cover");
			tutor_description.setText((String) infoData.getString("description"));
			tutor_feature.setText((String) infoData.getString("feature"));
			tutor_rank.setText(Constants.SwitchRecommandText((Integer) infoData.getInt("rank")));
			tutor_weixin.setText((String) infoData.getString("weixin"));
			Constants.imageLoader.cancelDisplayTask(tutor_portrait);
			Constants.imageLoader.displayImage(tutor_url, tutor_portrait, Constants.options, Constants.animateFirstListener);
		} else if (resultCode == 0) {
			Toast.makeText(TutorActivity.this, "获取该导师信息失败", Toast.LENGTH_SHORT).show();
		}
	}
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		createVelocityTracker(event);
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			xDown = event.getRawX();
			break;
		case MotionEvent.ACTION_MOVE:
			xMove = event.getRawX();
			//活动的距离
			int distanceX = (int) (xMove - xDown);
			//获取顺时速度
			int xSpeed = getScrollVelocity();
			//当滑动的距离大于我们设定的最小距离且滑动的瞬间速度大于我们设定的速度时，返回到上一个activity
			if(distanceX > XDISTANCE_MIN && xSpeed > XSPEED_MIN) {
				finish();
				//设置切换动画，从右边进入，左边退出
				overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
			}
			break;
		case MotionEvent.ACTION_UP:
			recycleVelocityTracker();
			break;
		default:
			break;
		}
		return true;
	}
	
	/**
	 * 创建VelocityTracker对象，并将触摸content界面的滑动事件加入到VelocityTracker当中。
	 * 
	 * @param event
	 *        
	 */
	private void createVelocityTracker(MotionEvent event) {
		if (mVelocityTracker == null) {
			mVelocityTracker = VelocityTracker.obtain();
		}
		mVelocityTracker.addMovement(event);
	}
	
	/**
	 * 回收VelocityTracker对象。
	 */
	private void recycleVelocityTracker() {
		mVelocityTracker.recycle();
		mVelocityTracker = null;
	}
	
	/**
	 * 获取手指在content界面滑动的速度。
	 * 
	 * @return 滑动速度，以每秒钟移动了多少像素值为单位。
	 */
	private int getScrollVelocity() {
		mVelocityTracker.computeCurrentVelocity(1000);
		int velocity = (int) mVelocityTracker.getXVelocity();
		return Math.abs(velocity);
	}

	@Override
	public void onStart() {
		super.onStart();
	}
	
	@Override
	public void onResume() {
		Log.v(TAG, "OnResume");
		super.onResume();
		MobclickAgent.onResume(this);  //友盟统计
	}

	@Override
	public void onPause() {
		Log.v(TAG, "OnPause");
		super.onPause();
		MobclickAgent.onPause(this);  //友盟统计
	}

	public void onStop() {
		Log.v(TAG, "OnStop");
		super.onStop();
	}

	public void onDestroy() {
		Log.v(TAG, "OnDestroy");
		super.onDestroy();
	}

}