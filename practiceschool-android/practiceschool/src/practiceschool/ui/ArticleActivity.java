package practiceschool.ui;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
 
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import practiceschool.menu.ActionItem;
import practiceschool.menu.TitlePopup;
import practiceschool.menu.TitlePopup.OnItemOnClickListener;
import practiceschool.network.HttpMethod;
import practiceschool.utils.Constants;
import practiceschool.utils.LoaderListView;
import practiceschool.utils.MyScrollView;
import practiceschool.utils.MyScrollView.OnGetBottomListener;


import com.example.practiceschool.R;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.SendMessageToWX;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.mm.sdk.openapi.WXMediaMessage;
import com.tencent.mm.sdk.openapi.WXWebpageObject;
import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class ArticleActivity extends Activity implements OnGetBottomListener,OnTouchListener{

	private LoaderListView reviewList; // �����б�
	private List<Map<String, Object>> listData; // �����б�����
	private MyAdapter myAdapter; // ����������
	private int reviewsTotal; // ��������
	private int currentPage = 0; // ��ǰ����ҳ��
	private int pagesize = 5; // ÿҳ���ؼ�����������
	private View view_line; // ���۱����Ϸ��ĺ���
	private ProgressBar loading; // ����loading����

	private LoaderListView lightupList; // �����б�
	private List<Map<String, Object>> lightupData; // �����б�����
	private LightUpAdapter lightupAdapter; // ����������

	private View loadMoreView; // loading���ض���
	private ImageView loadingView; // ���ڼ��ص�ͼ��
	private TextView loadStateTextView; // ������ʾ
	private RotateAnimation refreshingAnimation; // ������ת����
	private boolean isLoading = false; // �Ƿ��Ѽ���
	private boolean isBottom = false; // �Ƿ��ѵ��ײ�

	private final int LOAD_ARTICLE_SUCCESS = 1; // �������³ɹ�
	private final int LOAD_ARTICLE_FAILURE = 2; // ��������ʧ��

	private final int LOAD_REVIEWS_SUCCESS = 3; // �������۳ɹ�
	private final int LOADMORE_REVIEWS_SUCCESS = 4; // ���ظ������۳ɹ�
	private final int LOAD_REVIEWS_FAILURE = 5; // ��������ʧ��
	private final int LOADED_BOTTOM = 6; // �������۵���
	private final int LOAD_LIGHTUPS_SUCCESS = 7; // ���ص������۳ɹ�

	private final int PRAIZE_SUCCESS = 8; // ���޳ɹ�
	private final int PRAIZE_FAILURE = 9; // ����ʧ��
	private final int COLLECT_SUCCESS = 10; // �ղسɹ�
	private final int COLLECT_FAILURE = 11; // �ղ�ʧ��
	private final int LIGHTUP_SUCCESS = 12; // �����ɹ�
	private final int LIGHTUP_FAILURE = 13; // ����ʧ��

	private TextView article_title; // ���±���
	private practiceschool.circlepic.CircularImage author_portrait; // ����ͷ��_�ؼ�
	private TextView author_name; // ��������_�ؼ�
	private ImageView author_sex; // �����Ա�_�ؼ�
	private TextView author_time; // ���߷���ʱ��
	private ImageView article_cover; // ���·���
	private TextView article_content; // ��������
	private String articleid; // ����ID
	private String writename; // ��������
	private String writeid; // ����ID
	private String writesex; // �����Ա�
	private String writepic; // ����ͷ��
	private String storeusers; // ��ƪ�����ղص��û�
	private String praizeusers; // ��ƪ���µ��޵��û�

	/**
	 * ��ǰ�û���Ϣ
	 */
	private String userid = Constants.app_userid; // �û�ID��
	private String username = Constants.app_username; // �û��ǳ�
	private String usersex = Constants.app_usersex; // �û��Ա�
	private String userpic = Constants.app_userpic; // �û�ͷ��

	private LinearLayout hot_title; // ���۵�������
	private LinearLayout all_review_title; // �������۱��ⱳ��
	private TextView all_review_text; // �������۱�������
	private ImageButton send_review_title_small; // д���۰�ť

	private ImageButton send_review_title_medium; // ����д���۰�ť
	private TextView art_praise_num; // �����������_�ؼ�
	private TextView art_collect_num; // �����ղ�����_�ؼ�
	private int art_praise_num_text; // �����������
	private int art_collect_num_text; // �����ղ�����
	private ImageButton art_praise_btn; // ������ް�ť
	private ImageButton art_collect_btn; // �����ղذ�ť
	private Boolean isPraising = false; // �Ƿ����ڵ���
	private Boolean isCollecting = false; // �Ƿ������ղ�
	private String type; // ��������

	private static final String TAG = "zhihu.ui.ArticleActivity";
	private ImageButton title_move_btn;
	private ImageButton title_menu;
	private TextView discovery_title_text;
	public SlidingMenu menu;
	
	private MyScrollView myView;

	// ��ָ���һ���ʱ����С�ٶ�
	private static final int XSPEED_MIN = 200;

	// ��ָ���һ���ʱ����С����
	private static final int XDISTANCE_MIN = 200;

	// ��¼��ָ����ʱ�ĺ����ꡣ
	private float xDown;

	// ��¼��ָ�ƶ�ʱ�ĺ����ꡣ
	private float xMove;

	// ���ڼ�����ָ�������ٶȡ�
	private VelocityTracker mVelocityTracker;
	
	private TitlePopup titlePopup;
	private IWXAPI api;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE); // ����ʹ���Զ������
		setContentView(R.layout.article_main);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.article_title); // �Զ�����������ָ�ֵ

		reviewList = (LoaderListView) findViewById(R.id.reviewList);
		lightupList = (LoaderListView) findViewById(R.id.lightupList);

		myAdapter = new MyAdapter(this);
		lightupAdapter = new LightUpAdapter(this);

		// �����б�ײ���ͼ
		loadMoreView = getLayoutInflater().inflate(R.layout.review_load, null);
		reviewList.addFooterView(loadMoreView);

		loadingView = (ImageView) loadMoreView.findViewById(R.id.loading_icon2);
		loadStateTextView = (TextView) loadMoreView.findViewById(R.id.loadstate_tv);
		refreshingAnimation = (RotateAnimation) AnimationUtils.loadAnimation(this, R.anim.rotating);
		// �������ת������
		LinearInterpolator lir = new LinearInterpolator();
		refreshingAnimation.setInterpolator(lir);

		// �����ListView�߶Ⱥ󣬱�����ScrollView�����ö�
		reviewList.setFocusable(false);

		myView = (MyScrollView) findViewById(R.id.article_wrapper);
		myView.setBottomListener(this);
		myView.setOnTouchListener(this);
		myView.smoothScrollTo(0,20);

		// ��ʼ�����ֿؼ�
		this.initComponent();

		// ��ʼ�����������������ݡ������������ݡ�������������
		Bundle bundle = this.getIntent().getExtras();
		if (bundle != null) {
			articleid = bundle.getString("articleid");
			type = bundle.getString("type");
			discovery_title_text.setText(Constants.getType(type));
			loading.setVisibility(View.VISIBLE);
			loadArticleInfo(articleid); // ����������������
			loadReviews(articleid, 0, 3, "lightup"); // ���ص�����������
			loadReviews(articleid, 0, pagesize, "load"); // ����������������
		}

		api = WXAPIFactory.createWXAPI(this, Constants.APP_ID,false);
		
		MobclickAgent.updateOnlineConfig(this);  //����ͳ��

	}

	/**
	 * ��ʼ������ؼ�
	 */
	private void initComponent() {
		article_title = (TextView) findViewById(R.id.article_title);
		author_portrait = (practiceschool.circlepic.CircularImage) findViewById(R.id.author_portrait);
		author_name = (TextView) findViewById(R.id.author_name);
		author_sex = (ImageView) findViewById(R.id.author_sex);
		author_time = (TextView) findViewById(R.id.author_time);
		article_cover = (ImageView) findViewById(R.id.article_cover);
		article_content = (TextView) findViewById(R.id.article_content);

		hot_title = (LinearLayout) findViewById(R.id.hot_title);
		all_review_title = (LinearLayout) findViewById(R.id.all_review_title);
		all_review_text = (TextView) findViewById(R.id.all_review_text);
		view_line = (View) findViewById(R.id.view_line);
		send_review_title_small = (ImageButton) findViewById(R.id.send_review_title_small);
		send_review_title_medium = (ImageButton) findViewById(R.id.send_review_title_medium);
		art_praise_num = (TextView) findViewById(R.id.art_praise_num);
		art_collect_num = (TextView) findViewById(R.id.art_collect_num);
		art_praise_btn = (ImageButton) findViewById(R.id.art_praise_btn);
		art_collect_btn = (ImageButton) findViewById(R.id.art_collect_btn);

		// д����
		send_review_title_small.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				openWinReview();
			}
		});

		// �����У�д����
		send_review_title_medium.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				openWinReview();
			}
		});

		// �����У�����
		art_praise_btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String ajaxUrl = Constants.NODE_URL + "/praise";
				String ajaxData = null;
				try {
					ajaxData = "articleid=" + articleid + "&userid=" + userid;
				} catch (Exception e) {
					e.printStackTrace();
				}
				// �ж��Ƿ�������
				if (Constants.isNetworkConnected(ArticleActivity.this) && (!isPraising)) {
					try {
						isPraising = true;
						new Thread(new praizeThread(ajaxUrl, ajaxData)).start();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});

		// �����У��ղ�
		art_collect_btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String ajaxUrl = Constants.NODE_URL + "/store";
				String ajaxData = null;
				try {
					ajaxData = "articleid=" + articleid + "&userid=" + userid;
				} catch (Exception e) {
					e.printStackTrace();
				}
				// �ж��Ƿ�������
				if (Constants.isNetworkConnected(ArticleActivity.this) && (!isCollecting)) {
					try {
						isCollecting = true;
						new Thread(new collectThread(ajaxUrl, ajaxData)).start();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});

		// LeftMenu.initSlidingMenu(ArticleActivity.this, "time"); // ��ʼ�����˵�

		/**
		 * ��ʼ���л����˵���ť
		 */
		discovery_title_text = (TextView) findViewById(R.id.discovery_title_text);
		title_move_btn = (ImageButton) findViewById(R.id.title_move_btn);
		
		title_move_btn.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_UP:
					finish();
					break;
				default:
					break;
				}
				return false;
			}
		});


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
				webpage.webpageUrl = "http://120.55.99.230/articleshare?key=" + articleid;
				
				//��WXMediaMessage�����ʼ��һ��WXMediaMessage����
				WXMediaMessage msg = new WXMediaMessage(webpage);
				msg.title = "ְ������ѧԺ";
				msg.description = "��Ҫ�ݵ����ǵ�MM������Ҫ���õ���Ĳ�����Ҫ����������ĵ�ʦѧϰ�����Ǿ͸Ͻ���ְ������ѧԺ�ɣ�";
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

		loading = (ProgressBar) findViewById(R.id.article_loading);

	}
	


	/**
	 * �����۶Ի���
	 */
	private void openWinReview() {
		Intent intent = new Intent();
		intent.setClass(ArticleActivity.this, WinReviewActivity.class);
		Bundle bundle = new Bundle();
		bundle.putString("articleid", articleid);
		bundle.putString("writename", writename);
		bundle.putString("writesex", writesex);
		bundle.putString("writepic", writepic);
		intent.putExtras(bundle);
		ArticleActivity.this.startActivityForResult(intent, 1000);
	}

	/**
	 * ��дonActivityResult���� �����ύ��ִ��
	 */
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		// ���Ը��ݶ���������������Ӧ�Ĳ���
		if (1000 == resultCode) {
			String _reviewcontent = data.getExtras().getString("reviewcontent");
			String _reviewtime = data.getExtras().getString("reviewtime");
			String _reviewid = data.getExtras().getString("reviewid");
			all_review_text.setText("��������(" + (++reviewsTotal) + ")");
			Map<String, Object> map = new HashMap<String, Object>();
			map = new HashMap<String, Object>();
			map.put("user_pic_url", userpic);
			map.put("user_name", username);
			map.put("user_sex", usersex);
			map.put("reviewid", _reviewid);
			map.put("user_time", _reviewtime);
			map.put("praise_num", "0");
			map.put("user_message", _reviewcontent);
			currentPage++;
			listData.add(0, map);
			myAdapter.notifyDataSetChanged();
		}

	}

	/**
	 * ������������
	 */
	private void loadArticleInfo(String articleid) {
		String ajaxUrl = Constants.NODE_URL + "/articleinfo";
		String ajaxData = "articleid=" + articleid;

		// �ж��Ƿ�������
		if (Constants.isNetworkConnected(ArticleActivity.this)) {
			try {
				new Thread(new articleinfoThread(ajaxUrl, ajaxData)).start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * ���������б�
	 */
	private void loadReviews(String articleid, int pagestart, int pagesize, String action) {
		String ajaxUrl;
		String ajaxData = "articleid=" + articleid + "&pagestart=" + pagestart + "&pagesize=" + pagesize;
		if (action == "lightup") {
			ajaxUrl = Constants.NODE_URL + "/lightuplist";
		} else {
			ajaxUrl = Constants.NODE_URL + "/artreviewlist";
		}
		// �ж��Ƿ�������
		if (Constants.isNetworkConnected(ArticleActivity.this)) {
			try {
				new Thread(new reviewlistThread(ajaxUrl, ajaxData, action)).start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * �������۴���
	 */
	public void onBottom() {

		// ������ڼ��ؾ�����
		if (isLoading || isBottom)
			return;
		isLoading = true;
		loadingView.setVisibility(View.VISIBLE);
		loadingView.startAnimation(refreshingAnimation);
		loadStateTextView.setVisibility(View.VISIBLE);
		loadStateTextView.setText(R.string.load_review);
		loadReviews(articleid, currentPage, pagesize, "loadmore");

	}

	/**
	 * ������������״̬���ص�����
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
				case LOAD_ARTICLE_SUCCESS:
					try {
						renderInfo((JSONObject) msg.obj);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					break;
				case LOAD_ARTICLE_FAILURE:
					break;
				case LOAD_REVIEWS_SUCCESS:
					try {
						renderReviews((JSONObject) msg.obj);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					break;
				case LOADMORE_REVIEWS_SUCCESS:
					try {
						AddRenderList((JSONObject) msg.obj);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					break;
				case LOAD_LIGHTUPS_SUCCESS:
					try {
						renderLightups((JSONObject) msg.obj);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					break;
				case LOADED_BOTTOM:
					loadBottom();
					break;
				case LOAD_REVIEWS_FAILURE:
					break;
				case PRAIZE_SUCCESS:
					try {
						doPraize((JSONObject) msg.obj);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					break;
				case PRAIZE_FAILURE:
					break;
				case COLLECT_SUCCESS:
					try {
						doCollect((JSONObject) msg.obj);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					break;
				case COLLECT_FAILURE:
					break;
				case LIGHTUP_SUCCESS:
					try {
						doLightup((JSONObject) msg.obj);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					break;
				case LIGHTUP_FAILURE:
					break;
				default:
					break;
				}
			}
			super.handleMessage(msg);
		}

	};

	/**
	 * �����Ѽ���ȫ��
	 */
	private void loadBottom() {
		loadingView.setVisibility(View.GONE);
		loadStateTextView.setVisibility(View.VISIBLE);
		loadingView.clearAnimation();
		loadStateTextView.setText(R.string.load_bottom);
		isLoading = false;
		isBottom = true;
	}

	// ViewHolder��̬��
	static class ViewHolder {

		public practiceschool.circlepic.CircularImage user_pic; // �û�ͷ��
		public TextView user_name; // �û�����
		public ImageView user_sex; // �û��Ա�
		public TextView user_time; // �û�����ʱ��
		public TextView lightup_num; // ������
		public TextView lightup_btn; // ������ť
		public TextView user_message; // ��������

	}

	/**
	 * �Զ�������������
	 */
	public class MyAdapter extends BaseAdapter {
		private LayoutInflater mInflater = null;

		private MyAdapter(Context context) {
			// ����context�����ļ��ز��֣��������MainActivity������this
			this.mInflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			// �ڴ�������������������ݼ��е���Ŀ��
			return listData.size();
		}

		@Override
		public Object getItem(int position) {
			// ��ȡ���ݼ�����ָ��������Ӧ��������
			return position;
		}

		@Override
		public long getItemId(int position) {
			// ��ȡ���б�����ָ��������Ӧ����id
			return position;
		}

		// ��ȡһ�������ݼ���ָ����������ͼ����ʾ����
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			// �������convertViewΪ�գ�����Ҫ����View
			if (convertView == null) {
				holder = new ViewHolder();
				// �����Զ����Item���ּ��ز���
				convertView = mInflater.inflate(R.layout.review_list, null);
				holder.user_pic = (practiceschool.circlepic.CircularImage) convertView.findViewById(R.id.user_pic);
				holder.user_name = (TextView) convertView.findViewById(R.id.user_name);
				holder.user_sex = (ImageView) convertView.findViewById(R.id.user_sex);
				holder.user_time = (TextView) convertView.findViewById(R.id.user_time);
				holder.lightup_num = (TextView) convertView.findViewById(R.id.praise_num);
				holder.lightup_btn = (TextView) convertView.findViewById(R.id.praise_pic);
				holder.user_message = (TextView) convertView.findViewById(R.id.user_message);
				// �����úõĲ��ֱ��浽�����У�������������Tag��Ա���淽��ȡ��Tag
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			holder.user_name.setText((String) listData.get(position).get("user_name"));
			String _sex = (String) listData.get(position).get("user_sex");
			if (_sex.equals("boy")) {
				holder.user_sex.setImageResource(R.drawable.boy_icon);
			} else {
				holder.user_sex.setImageResource(R.drawable.girl_icon);
			}
			holder.user_time.setText((String) listData.get(position).get("user_time"));
			holder.lightup_num.setText("(" + (String) listData.get(position).get("praise_num") + ")");
			holder.user_message.setText((String) listData.get(position).get("user_message"));

			// �������������Ϊ�˽��convertView�����õ�ʱ��ͼƬԤ�������
			holder.user_pic.setImageResource(R.drawable.default_portrait);

			final String user_pic_url = (String) listData.get(position).get("user_pic_url");
			holder.user_pic.setTag(user_pic_url);

			// ��Button��ӵ����¼� ���Button֮��ListView��ʧȥ���㣬��Ҫֱ�Ӱ�Button�Ľ���ȥ��
			final String _reviewid = (String) listData.get(position).get("reviewid");
			final TextView _lightup_btn = holder.lightup_btn;
			final int _position = position;
			holder.lightup_btn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					gotoLightup(_reviewid, _lightup_btn, _position, "review");
				}
			});

			/**
			 * ��ʾͼƬ ����1��ͼƬurl ����2����ʾͼƬ�Ŀؼ� ����3����ʾͼƬ������ ����4��������
			 */
			Constants.imageLoader.cancelDisplayTask(holder.user_pic);
			Constants.imageLoader.displayImage(user_pic_url, holder.user_pic, Constants.options, Constants.animateFirstListener);

			return convertView;
		}

	}

	/**
	 * �Զ����������������
	 */
	public class LightUpAdapter extends BaseAdapter {
		private LayoutInflater mInflater = null;

		private LightUpAdapter(Context context) {
			// ����context�����ļ��ز��֣��������MainActivity������this
			this.mInflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			// �ڴ�������������������ݼ��е���Ŀ��
			return lightupData.size();
		}

		@Override
		public Object getItem(int position) {
			// ��ȡ���ݼ�����ָ��������Ӧ��������
			return position;
		}

		@Override
		public long getItemId(int position) {
			// ��ȡ���б�����ָ��������Ӧ����id
			return position;
		}

		// ��ȡһ�������ݼ���ָ����������ͼ����ʾ����
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			// �������convertViewΪ�գ�����Ҫ����View
			if (convertView == null) {
				holder = new ViewHolder();
				// �����Զ����Item���ּ��ز���
				convertView = mInflater.inflate(R.layout.review_list, null);
				holder.user_pic = (practiceschool.circlepic.CircularImage) convertView.findViewById(R.id.user_pic);
				holder.user_name = (TextView) convertView.findViewById(R.id.user_name);
				holder.user_sex = (ImageView) convertView.findViewById(R.id.user_sex);
				holder.user_time = (TextView) convertView.findViewById(R.id.user_time);
				holder.lightup_num = (TextView) convertView.findViewById(R.id.praise_num);
				holder.lightup_btn = (TextView) convertView.findViewById(R.id.praise_pic);
				holder.user_message = (TextView) convertView.findViewById(R.id.user_message);
				// �����úõĲ��ֱ��浽�����У�������������Tag��Ա���淽��ȡ��Tag
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			holder.user_name.setText((String) lightupData.get(position).get("user_name"));
			String _sex = (String) lightupData.get(position).get("user_sex");
			if (_sex.equals("boy")) {
				holder.user_sex.setImageResource(R.drawable.boy_icon);
			} else {
				holder.user_sex.setImageResource(R.drawable.girl_icon);
			}
			holder.user_time.setText((String) lightupData.get(position).get("user_time"));
			holder.lightup_num.setText("(" + (String) lightupData.get(position).get("praise_num") + ")");
			holder.user_message.setText((String) lightupData.get(position).get("user_message"));

			// �������������Ϊ�˽��convertView�����õ�ʱ��ͼƬԤ�������
			holder.user_pic.setImageResource(R.drawable.default_portrait);

			final String user_pic_url = (String) lightupData.get(position).get("user_pic_url");
			holder.user_pic.setTag(user_pic_url);

			final String _reviewid = (String) lightupData.get(position).get("reviewid");
			final TextView _lightup_btn = holder.lightup_btn;
			final int _position = position;
			holder.lightup_btn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					gotoLightup(_reviewid, _lightup_btn, _position, "lightup");
				}
			});

			/**
			 * ��ʾͼƬ ����1��ͼƬurl ����2����ʾͼƬ�Ŀؼ� ����3����ʾͼƬ������ ����4��������
			 */
			Constants.imageLoader.cancelDisplayTask(holder.user_pic);
			Constants.imageLoader.displayImage(user_pic_url, holder.user_pic, Constants.options, Constants.animateFirstListener);

			return convertView;
		}

	}
	
	@Override
	public void onStart() {
		Log.v(TAG, "OnStart");
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

	/**
	 * �����Ƿ�ɹ�
	 */
	private void doPraize(JSONObject data) throws JSONException {
		int resultCode = (Integer) data.getInt("resultCode");
		String description = (String) data.getString("description");
		if (resultCode == 1000) {
			art_praise_num.setText(String.valueOf(++art_praise_num_text));
			art_praise_btn.setImageResource(R.drawable.praise_btn_red);
			Toast.makeText(getApplicationContext(), description, Toast.LENGTH_SHORT).show();
		} else if (resultCode == 1001) {
			art_praise_btn.setImageResource(R.drawable.praise_btn_red);
			Toast.makeText(getApplicationContext(), description, Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(getApplicationContext(), description, Toast.LENGTH_SHORT).show();
		}
		isPraising = false;
	};

	/**
	 * �ղ��Ƿ�ɹ�
	 */
	private void doCollect(JSONObject data) throws JSONException {
		int resultCode = (Integer) data.getInt("resultCode");
		String description = (String) data.getString("description");
		if (resultCode == 1000) {
			art_collect_num.setText(String.valueOf(++art_collect_num_text));
			art_collect_btn.setImageResource(R.drawable.collect_btn_red);
			// art_collect_btn.setBackgroundResource(R.drawable.collect_btn_red);
			Toast.makeText(getApplicationContext(), description, Toast.LENGTH_SHORT).show();
		} else if (resultCode == 1001) {
			art_collect_btn.setImageResource(R.drawable.collect_btn_red);
			Toast.makeText(getApplicationContext(), description, Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(getApplicationContext(), description, Toast.LENGTH_SHORT).show();
		}
		isCollecting = false;
	};

	/**
	 * �����Ƿ�����ɹ�
	 */
	private void doLightup(JSONObject data) throws JSONException {
		int resultCode = (Integer) data.getInt("resultCode");
		int position = (Integer) data.getInt("position");
		String type = (String) data.getString("type");
		TextView lightup_btn = (TextView) data.get("lightup_btn");
		String description = (String) data.getString("description");
		if (resultCode == 1000) {

			if (type.equals("review")) {
				String lightup_number = String.valueOf(Integer.parseInt((String) listData.get(position).get("praise_num")) + 1);
				Map<String, Object> map = listData.get(position);
				map.put("praise_num", lightup_number); // ���µ��ļ�����ӵ�Map���滻���ļ���
				listData.set(position, map); // �滻listItems��ԭ����map
				myAdapter.notifyDataSetChanged(); // ֪ͨAdapter���ݸı�
			} else if (type.equals("lightup")) {
				String lightup_number = String.valueOf(Integer.parseInt((String) lightupData.get(position).get("praise_num")) + 1);
				Map<String, Object> map = lightupData.get(position);
				map.put("praise_num", lightup_number); // ���µ��ļ�����ӵ�Map���滻���ļ���
				lightupData.set(position, map); // �滻listItems��ԭ����map
				lightupAdapter.notifyDataSetChanged(); // ֪ͨAdapter���ݸı�
			}
			Toast.makeText(getApplicationContext(), description, Toast.LENGTH_SHORT).show();
		} else if (resultCode == 1001) {
			Toast.makeText(getApplicationContext(), description, Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(getApplicationContext(), description, Toast.LENGTH_SHORT).show();
		}
	};

	/**
	 * �û�����
	 */
	private void gotoLightup(String reviewid, TextView lightup_btn, int position, String type) {
		String ajaxUrl = Constants.NODE_URL + "/lightup";
		String ajaxData = null;
		try {
			ajaxData = "reviewid=" + reviewid + "&userid=" + userid;
		} catch (Exception e) {
			e.printStackTrace();
		}
		// �ж��Ƿ�������
		if (Constants.isNetworkConnected(ArticleActivity.this)) {
			try {
				new Thread(new lightupThread(ajaxUrl, ajaxData, lightup_btn, position, type)).start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * ���ݷ�������ȡ�ɹ�����Ⱦ��������
	 */
	private void renderInfo(JSONObject data) throws JSONException {

		int resultCode = (Integer) data.getInt("resultCode");
		if (resultCode == 1000) {
			loading.setVisibility(View.INVISIBLE);
			JSONObject infoData = (JSONObject) data.get("articleinfo");
			article_title.setText(infoData.getString("title"));
			writeid = (String) infoData.getString("writeid");
			writename = (String) infoData.getString("writename");
			author_name.setText(writename);
			writesex = (String) infoData.getString("writesex");
			if (writesex.equals("boy")) {
				author_sex.setImageResource(R.drawable.boy_icon);
			} else {
				author_sex.setImageResource(R.drawable.girl_icon);
			}
			author_time.setText(infoData.getString("articletime"));
			art_praise_num_text = infoData.getInt("praizecount");
			art_praise_num.setText(String.valueOf(art_praise_num_text));
			art_collect_num_text = infoData.getInt("storecount");
			art_collect_num.setText(String.valueOf(art_collect_num_text));
			article_content.setText(infoData.getString("content"));

			praizeusers = (String) infoData.getString("praizeusers");
			if (praizeusers.indexOf(userid) > -1) {
				art_praise_btn.setImageResource(R.drawable.praise_btn_red);
			}

			storeusers = (String) infoData.getString("storeusers");
			if (storeusers.indexOf(userid) > -1) {
				art_collect_btn.setImageResource(R.drawable.collect_btn_red);
			}

			/**
			 * ��ʾͼƬ ����1��ͼƬurl ����2����ʾͼƬ�Ŀؼ� ����3����ʾͼƬ������ ����4��������
			 */
			writepic = (String) infoData.getString("writepic");
			Constants.imageLoader.displayImage(infoData.getString("cover"), article_cover, Constants.options_big, Constants.animateFirstListener);
			Constants.imageLoader.displayImage(writepic, author_portrait, Constants.options, Constants.animateFirstListener);

		}
	};

	/**
	 * ���ݷ�������ȡ�ɹ�����Ⱦ���������б�
	 */
	private void renderLightups(JSONObject data) throws JSONException {

		int resultCode = (Integer) data.getInt("resultCode");
		lightupData = new ArrayList<Map<String, Object>>();
		if (resultCode == 1000) {
			// �����б�
			JSONArray lightuplist = new JSONArray(data.getString("docs"));
			hot_title.setVisibility(View.VISIBLE);
			for (int i = 0; i < lightuplist.length(); i++) {
				JSONObject temp = (JSONObject) lightuplist.get(i);
				Map<String, Object> map = new HashMap<String, Object>();
				map = new HashMap<String, Object>();
				map.put("reviewid", temp.getString("_id"));
				map.put("user_pic_url", temp.getString("userpic"));
				map.put("user_name", temp.getString("username"));
				map.put("user_sex", temp.getString("usersex"));
				map.put("user_time", temp.getString("reviewtime"));
				map.put("praise_num", temp.getString("lightcount"));
				map.put("user_message", temp.getString("reviewcontent"));
				lightupData.add(map);
			}
			lightupList.setAdapter(lightupAdapter);
		} else if (resultCode == 1001) {
			hot_title.setVisibility(View.INVISIBLE);
		}
	};

	/**
	 * ���ݷ�������ȡ�ɹ�����Ⱦ�����б�
	 */
	private void renderReviews(JSONObject data) throws JSONException {

		int resultCode = (Integer) data.getInt("resultCode");
		listData = new ArrayList<Map<String, Object>>();
		all_review_title.setVisibility(View.VISIBLE);
		view_line.setVisibility(View.VISIBLE);
		if (resultCode == 1000) {
			// �����б�
			currentPage++;
			JSONArray reviewlist = new JSONArray(data.getString("docs"));
			reviewsTotal = data.getInt("total");
			all_review_text.setText("��������(" + reviewsTotal + ")");
			for (int i = 0; i < reviewlist.length(); i++) {
				JSONObject temp = (JSONObject) reviewlist.get(i);
				Map<String, Object> map = new HashMap<String, Object>();
				map = new HashMap<String, Object>();
				map.put("reviewid", temp.getString("_id"));
				map.put("user_pic_url", temp.getString("userpic"));
				map.put("user_name", temp.getString("username"));
				map.put("user_sex", temp.getString("usersex"));
				map.put("user_time", temp.getString("reviewtime"));
				map.put("praise_num", temp.getString("lightcount"));
				map.put("user_message", temp.getString("reviewcontent"));
				listData.add(map);
			}
		} else if (resultCode == 1002) {
			reviewsTotal = 0;
			all_review_text.setText("��������(" + reviewsTotal + ")");
		}
		reviewList.setAdapter(myAdapter);
	};

	/**
	 * ���ݷ��������ظ������ݣ���Ⱦ�����б�
	 */
	private void AddRenderList(JSONObject data) throws JSONException {

		int resultCode = (Integer) data.getInt("resultCode");
		if (resultCode == 1000) {
			currentPage++;
			JSONArray reviewlist = new JSONArray(data.getString("docs"));
			for (int i = 0; i < reviewlist.length(); i++) {
				JSONObject temp = (JSONObject) reviewlist.get(i);
				Map<String, Object> map = new HashMap<String, Object>();
				map = new HashMap<String, Object>();
				map.put("reviewid", temp.getString("_id"));
				map.put("user_pic_url", temp.getString("userpic"));
				map.put("user_name", temp.getString("username"));
				map.put("user_sex", temp.getString("usersex"));
				map.put("user_time", temp.getString("reviewtime"));
				map.put("praise_num", temp.getString("lightcount"));
				map.put("user_message", temp.getString("reviewcontent"));
				listData.add(map);
			}
			myAdapter.notifyDataSetChanged();
			loadingView.setVisibility(View.GONE);
			loadStateTextView.setVisibility(View.GONE);
			loadingView.clearAnimation();
			isLoading = false;
			// pullToRefreshLayout.loadmoreFinish(PullToRefreshLayout.SUCCEED);
		} else if (resultCode == 1001 || resultCode == 1002) {
			handler.sendEmptyMessage(LOADED_BOTTOM);
		}
	};

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
				Toast.makeText(ArticleActivity.this, "�����쳣��������������", Toast.LENGTH_SHORT).show();
			}
			Looper.loop();

		}
	}

	/**
	 * ���������б��߳�
	 */
	class reviewlistThread implements Runnable {

		private String ajaxUrl;
		private String ajaxData;
		private String action;

		public reviewlistThread(String ajaxUrl, String ajaxData, String action) {
			super();
			this.ajaxUrl = ajaxUrl;
			this.ajaxData = ajaxData;
			this.action = action;
		}

		public void run() {
			Looper.prepare();
			final String result = HttpMethod.loginByPost(ajaxUrl, ajaxData);
			Message msg = Message.obtain();
			if (result != null) {
				try {
					JSONObject data = new JSONObject(result);
					if (action == "load") {
						msg.what = LOAD_REVIEWS_SUCCESS;
					} else if (action == "loadmore") {
						msg.what = LOADMORE_REVIEWS_SUCCESS;
					} else if (action == "lightup") {
						msg.what = LOAD_LIGHTUPS_SUCCESS;
					}
					msg.obj = data;
					handler.sendMessage(msg);
				} catch (JSONException e) {
					msg.what = LOAD_REVIEWS_FAILURE;
					handler.sendMessage(msg);
				}
			} else {
				Toast.makeText(ArticleActivity.this, "�����쳣��������������", Toast.LENGTH_SHORT).show();
			}
			Looper.loop();
		}
	}

	/**
	 * �����߳�
	 */
	class praizeThread implements Runnable {

		private String ajaxUrl;
		private String ajaxData;

		public praizeThread(String ajaxUrl, String ajaxData) {
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
					msg.what = PRAIZE_SUCCESS;
					msg.obj = data;
					handler.sendMessage(msg);
				} catch (JSONException e) {
					msg.what = PRAIZE_FAILURE;
					handler.sendMessage(msg);
				}
			} else {
				Toast.makeText(ArticleActivity.this, "�����쳣��������������", Toast.LENGTH_SHORT).show();
			}
			Looper.loop();

		}
	}

	/**
	 * �ղ��߳�
	 */
	class collectThread implements Runnable {

		private String ajaxUrl;
		private String ajaxData;

		public collectThread(String ajaxUrl, String ajaxData) {
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
					msg.what = COLLECT_SUCCESS;
					msg.obj = data;
					handler.sendMessage(msg);
				} catch (JSONException e) {
					msg.what = COLLECT_FAILURE;
					handler.sendMessage(msg);
				}
			} else {
				Toast.makeText(ArticleActivity.this, "�����쳣��������������", Toast.LENGTH_SHORT).show();
			}
			Looper.loop();
		}
	}

	/**
	 * �����߳�
	 */
	class lightupThread implements Runnable {

		private String ajaxUrl;
		private String ajaxData;
		private TextView lightup_btn;
		private int position;
		private String type;

		public lightupThread(String ajaxUrl, String ajaxData, TextView lightup_btn, int position, String type) {
			super();
			this.ajaxUrl = ajaxUrl;
			this.ajaxData = ajaxData;
			this.lightup_btn = lightup_btn;
			this.position = position;
			this.type = type;
		}

		public void run() {
			Looper.prepare();
			final String result = HttpMethod.loginByPost(ajaxUrl, ajaxData);
			Message msg = Message.obtain();
			if (result != null) {
				try {
					JSONObject data = new JSONObject(result);
					data.put("lightup_btn", this.lightup_btn);
					data.put("position", this.position);
					data.put("type", this.type);
					msg.what = LIGHTUP_SUCCESS;
					msg.obj = data;
					handler.sendMessage(msg);
				} catch (JSONException e) {
					msg.what = LIGHTUP_FAILURE;
					handler.sendMessage(msg);
				}
			} else {
				Toast.makeText(ArticleActivity.this, "�����쳣��������������", Toast.LENGTH_SHORT).show();
			}
			Looper.loop();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// titlePopup.show(title_menu);
		return true;
	}
	
	public boolean onTouch(View v, MotionEvent event) {
		createVelocityTracker(event);
        if (event.getAction() == MotionEvent.ACTION_UP) {
        	recycleVelocityTracker();
            myView.requestDisallowInterceptTouchEvent(false);
        } else if(event.getAction() == MotionEvent.ACTION_DOWN){
        	xDown = event.getRawX();
        	myView.requestDisallowInterceptTouchEvent(true);
        } else if(event.getAction() == MotionEvent.ACTION_MOVE){
			xMove = event.getRawX();
			// ��ľ���
			int distanceX = (int) (xMove - xDown);
			// ��ȡ˳ʱ�ٶ�
			int xSpeed = getScrollVelocity();
			// �������ľ�����������趨����С�����һ�����˲���ٶȴ��������趨���ٶ�ʱ�����ص���һ��activity
			if (distanceX > XDISTANCE_MIN && xSpeed > XSPEED_MIN) {
				finish();
			}
        }
        return false;
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

}
