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

	private final int USERINFO_SUCCESS = 1; // ��ѯ��ʦ��Ϣ�ɹ�
	private final int USERINFO_FAILURE = 2; // ��ѯ��ʦ��Ϣʧ��

	private ImageButton title_move_btn;
	private ImageButton title_menu;
	private LinearLayout tutor_itemmenu;
	private practiceschool.circlepic.CircularImage tutor_portrait; // ��ʦͷ��
	private TextView tutor_name;
	private TextView tutor_description;
	private TextView tutor_feature;
	private TextView tutor_rank;
	private TextView tutor_weixin;
	private RelativeLayout tutor_banner_bg;
	private String tutor_url; // ��ʦͷ��url
	private String tutorid; // ��ʦID��
	private LinearLayout myView;
	
	//��ָ���һ���ʱ����С�ٶ�
	private static final int XSPEED_MIN = 200;
	
	//��ָ���һ���ʱ����С����
	private static final int XDISTANCE_MIN = 150;
	
	//��¼��ָ����ʱ�ĺ����ꡣ
	private float xDown;
	
	//��¼��ָ�ƶ�ʱ�ĺ����ꡣ
	private float xMove;
	
	//���ڼ�����ָ�������ٶȡ�
	private VelocityTracker mVelocityTracker;
	
	private TitlePopup titlePopup;
	private IWXAPI api;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.v(TAG, "OnCreate");
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE); // ����ʹ���Զ������
		setContentView(R.layout.tutor_main);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.tutor_title); // �Զ�����������ָ�ֵ

		// ��ʼ�����ֿؼ�
		this.initComponent();

		// ��ʼ�����������������ݡ������������ݡ�������������
		Bundle bundle = this.getIntent().getExtras();
		if (bundle != null) {
			tutorid = bundle.getString("tutorid");
			initData(tutorid); // ����������������
		}


		/**
		 * ��ʼ���л����˵���ť
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
		 * ��ʼ�����Ͻǲ˵�
		 */  
		titlePopup = new TitlePopup(this, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		titlePopup.addAction(new ActionItem(this, "���������", R.drawable.mm_title_btn_share_normal));
		titlePopup.addAction(new ActionItem(this, "��������Ȧ", R.drawable.mm_title_btn_weixin_normal));	

		titlePopup.setItemOnClickListener(new OnItemOnClickListener(){
 
			@Override
			public void onItemClick(ActionItem item, int position) {
				api.registerApp(Constants.APP_ID);
				WXWebpageObject webpage = new WXWebpageObject();
				webpage.webpageUrl = Constants.NODE_URL + "/tutorshare?key=" + tutorid;
				
				//��WXMediaMessage�����ʼ��һ��WXMediaMessage����
				WXMediaMessage msg = new WXMediaMessage(webpage);
				msg.title = "ְ������ѧԺ";
				msg.description = "��Ҫ����������ĵ�ʦѧϰ������Ҫ���Լ���ø�ǿ���Ǿ͸Ͻ���ְ������ѧԺ�ɣ�";
				Bitmap thumb = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
				msg.thumbData = Constants.bmpToByteArray(thumb, true);
				
				//����һ��Req
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
		
		MobclickAgent.updateOnlineConfig(this);  //����ͳ��
	
	}

	/**
	 * ��ʼ������ؼ�
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

		// ���ñ���͸����
		tutor_itemmenu.getBackground().setAlpha(90); // 0~255͸����ֵ

	}

	/**
	 * ��ʼ����Ⱦ����
	 */
	private void initData(String tutorid) {

		// ��ȡ��ʦ��Ϣ
		String ajaxUrl = Constants.NODE_URL + "/getTutor";
		String ajaxData = "id=" + tutorid;
		if (Constants.isNetworkConnected(TutorActivity.this)) { // �ж��Ƿ�������
			try {
				new Thread(new TutorinfoThread(ajaxUrl, ajaxData)).start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			Toast.makeText(TutorActivity.this, "�����쳣��������������", Toast.LENGTH_SHORT).show();
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
	 * ��ȡ�û���Ϣ�߳�
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
				Toast.makeText(TutorActivity.this, "�����쳣��������������", Toast.LENGTH_SHORT).show();
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
			Toast.makeText(TutorActivity.this, "��ȡ�õ�ʦ��Ϣʧ��", Toast.LENGTH_SHORT).show();
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
			//��ľ���
			int distanceX = (int) (xMove - xDown);
			//��ȡ˳ʱ�ٶ�
			int xSpeed = getScrollVelocity();
			//�������ľ�����������趨����С�����һ�����˲���ٶȴ��������趨���ٶ�ʱ�����ص���һ��activity
			if(distanceX > XDISTANCE_MIN && xSpeed > XSPEED_MIN) {
				finish();
				//�����л����������ұ߽��룬����˳�
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
	 * ����VelocityTracker���󣬲�������content����Ļ����¼����뵽VelocityTracker���С�
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
	 * ����VelocityTracker����
	 */
	private void recycleVelocityTracker() {
		mVelocityTracker.recycle();
		mVelocityTracker = null;
	}
	
	/**
	 * ��ȡ��ָ��content���滬�����ٶȡ�
	 * 
	 * @return �����ٶȣ���ÿ�����ƶ��˶�������ֵΪ��λ��
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
		MobclickAgent.onResume(this);  //����ͳ��
	}

	@Override
	public void onPause() {
		Log.v(TAG, "OnPause");
		super.onPause();
		MobclickAgent.onPause(this);  //����ͳ��
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