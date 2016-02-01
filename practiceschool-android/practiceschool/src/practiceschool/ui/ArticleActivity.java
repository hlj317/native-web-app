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

	private LoaderListView reviewList; // 评论列表
	private List<Map<String, Object>> listData; // 评论列表数据
	private MyAdapter myAdapter; // 评论适配器
	private int reviewsTotal; // 评论总数
	private int currentPage = 0; // 当前评论页数
	private int pagesize = 5; // 每页加载几条评论数据
	private View view_line; // 评论标题上方的横线
	private ProgressBar loading; // 加载loading动画

	private LoaderListView lightupList; // 点亮列表
	private List<Map<String, Object>> lightupData; // 点亮列表数据
	private LightUpAdapter lightupAdapter; // 点亮适配器

	private View loadMoreView; // loading加载动画
	private ImageView loadingView; // 正在加载的图标
	private TextView loadStateTextView; // 加载提示
	private RotateAnimation refreshingAnimation; // 均匀旋转动画
	private boolean isLoading = false; // 是否已加载
	private boolean isBottom = false; // 是否已到底部

	private final int LOAD_ARTICLE_SUCCESS = 1; // 加载文章成功
	private final int LOAD_ARTICLE_FAILURE = 2; // 加载文章失败

	private final int LOAD_REVIEWS_SUCCESS = 3; // 加载评论成功
	private final int LOADMORE_REVIEWS_SUCCESS = 4; // 加载更多评论成功
	private final int LOAD_REVIEWS_FAILURE = 5; // 加载评论失败
	private final int LOADED_BOTTOM = 6; // 加载评论到底
	private final int LOAD_LIGHTUPS_SUCCESS = 7; // 加载点亮评论成功

	private final int PRAIZE_SUCCESS = 8; // 点赞成功
	private final int PRAIZE_FAILURE = 9; // 点赞失败
	private final int COLLECT_SUCCESS = 10; // 收藏成功
	private final int COLLECT_FAILURE = 11; // 收藏失败
	private final int LIGHTUP_SUCCESS = 12; // 点亮成功
	private final int LIGHTUP_FAILURE = 13; // 点亮失败

	private TextView article_title; // 文章标题
	private practiceschool.circlepic.CircularImage author_portrait; // 作者头像_控件
	private TextView author_name; // 作者名字_控件
	private ImageView author_sex; // 作者性别_控件
	private TextView author_time; // 作者发布时间
	private ImageView article_cover; // 文章封面
	private TextView article_content; // 文章内容
	private String articleid; // 文章ID
	private String writename; // 作者名字
	private String writeid; // 作者ID
	private String writesex; // 作者性别
	private String writepic; // 作者头像
	private String storeusers; // 这篇文章收藏的用户
	private String praizeusers; // 这篇文章点赞的用户

	/**
	 * 当前用户信息
	 */
	private String userid = Constants.app_userid; // 用户ID号
	private String username = Constants.app_username; // 用户昵称
	private String usersex = Constants.app_usersex; // 用户性别
	private String userpic = Constants.app_userpic; // 用户头像

	private LinearLayout hot_title; // 评论点亮标题
	private LinearLayout all_review_title; // 所有评论标题背景
	private TextView all_review_text; // 所有评论标题文字
	private ImageButton send_review_title_small; // 写评论按钮

	private ImageButton send_review_title_medium; // 标题写评论按钮
	private TextView art_praise_num; // 标题点赞数量_控件
	private TextView art_collect_num; // 标题收藏数量_控件
	private int art_praise_num_text; // 标题点赞数量
	private int art_collect_num_text; // 标题收藏数量
	private ImageButton art_praise_btn; // 标题点赞按钮
	private ImageButton art_collect_btn; // 标题收藏按钮
	private Boolean isPraising = false; // 是否正在点赞
	private Boolean isCollecting = false; // 是否正在收藏
	private String type; // 文章类型

	private static final String TAG = "zhihu.ui.ArticleActivity";
	private ImageButton title_move_btn;
	private ImageButton title_menu;
	private TextView discovery_title_text;
	public SlidingMenu menu;
	
	private MyScrollView myView;

	// 手指向右滑动时的最小速度
	private static final int XSPEED_MIN = 200;

	// 手指向右滑动时的最小距离
	private static final int XDISTANCE_MIN = 200;

	// 记录手指按下时的横坐标。
	private float xDown;

	// 记录手指移动时的横坐标。
	private float xMove;

	// 用于计算手指滑动的速度。
	private VelocityTracker mVelocityTracker;
	
	private TitlePopup titlePopup;
	private IWXAPI api;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE); // 声明使用自定义标题
		setContentView(R.layout.article_main);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.article_title); // 自定义标题栏布局赋值

		reviewList = (LoaderListView) findViewById(R.id.reviewList);
		lightupList = (LoaderListView) findViewById(R.id.lightupList);

		myAdapter = new MyAdapter(this);
		lightupAdapter = new LightUpAdapter(this);

		// 设置列表底部视图
		loadMoreView = getLayoutInflater().inflate(R.layout.review_load, null);
		reviewList.addFooterView(loadMoreView);

		loadingView = (ImageView) loadMoreView.findViewById(R.id.loading_icon2);
		loadStateTextView = (TextView) loadMoreView.findViewById(R.id.loadstate_tv);
		refreshingAnimation = (RotateAnimation) AnimationUtils.loadAnimation(this, R.anim.rotating);
		// 添加匀速转动动画
		LinearInterpolator lir = new LinearInterpolator();
		refreshingAnimation.setInterpolator(lir);

		// 计算过ListView高度后，必须让ScrollView重新置顶
		reviewList.setFocusable(false);

		myView = (MyScrollView) findViewById(R.id.article_wrapper);
		myView.setBottomListener(this);
		myView.setOnTouchListener(this);
		myView.smoothScrollTo(0,20);

		// 初始化布局控件
		this.initComponent();

		// 初始化加载文章详情数据、点亮评论数据、所有评论数据
		Bundle bundle = this.getIntent().getExtras();
		if (bundle != null) {
			articleid = bundle.getString("articleid");
			type = bundle.getString("type");
			discovery_title_text.setText(Constants.getType(type));
			loading.setVisibility(View.VISIBLE);
			loadArticleInfo(articleid); // 加载文章详情数据
			loadReviews(articleid, 0, 3, "lightup"); // 加载点亮评论数据
			loadReviews(articleid, 0, pagesize, "load"); // 加载所有评论数据
		}

		api = WXAPIFactory.createWXAPI(this, Constants.APP_ID,false);
		
		MobclickAgent.updateOnlineConfig(this);  //友盟统计

	}

	/**
	 * 初始化界面控件
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

		// 写评论
		send_review_title_small.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				openWinReview();
			}
		});

		// 标题中，写评论
		send_review_title_medium.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				openWinReview();
			}
		});

		// 标题中，点赞
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
				// 判断是否有网络
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

		// 标题中，收藏
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
				// 判断是否有网络
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

		// LeftMenu.initSlidingMenu(ArticleActivity.this, "time"); // 初始化左侧菜单

		/**
		 * 初始化切换左侧菜单按钮
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
				webpage.webpageUrl = "http://120.55.99.230/articleshare?key=" + articleid;
				
				//用WXMediaMessage对象初始化一个WXMediaMessage对象
				WXMediaMessage msg = new WXMediaMessage(webpage);
				msg.title = "职场修炼学院";
				msg.description = "想要泡到心仪的MM不？想要更好的身材不？想要向各种厉害的导师学习不？那就赶紧来职场修炼学院吧！";
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

		loading = (ProgressBar) findViewById(R.id.article_loading);

	}
	


	/**
	 * 打开评论对话框
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
	 * 重写onActivityResult方法 评论提交后执行
	 */
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		// 可以根据多个请求代码来作相应的操作
		if (1000 == resultCode) {
			String _reviewcontent = data.getExtras().getString("reviewcontent");
			String _reviewtime = data.getExtras().getString("reviewtime");
			String _reviewid = data.getExtras().getString("reviewid");
			all_review_text.setText("所有评论(" + (++reviewsTotal) + ")");
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
	 * 加载文章详情
	 */
	private void loadArticleInfo(String articleid) {
		String ajaxUrl = Constants.NODE_URL + "/articleinfo";
		String ajaxData = "articleid=" + articleid;

		// 判断是否有网络
		if (Constants.isNetworkConnected(ArticleActivity.this)) {
			try {
				new Thread(new articleinfoThread(ajaxUrl, ajaxData)).start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 加载评论列表
	 */
	private void loadReviews(String articleid, int pagestart, int pagesize, String action) {
		String ajaxUrl;
		String ajaxData = "articleid=" + articleid + "&pagestart=" + pagestart + "&pagesize=" + pagesize;
		if (action == "lightup") {
			ajaxUrl = Constants.NODE_URL + "/lightuplist";
		} else {
			ajaxUrl = Constants.NODE_URL + "/artreviewlist";
		}
		// 判断是否有网络
		if (Constants.isNetworkConnected(ArticleActivity.this)) {
			try {
				new Thread(new reviewlistThread(ajaxUrl, ajaxData, action)).start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 下拉评论触发
	 */
	public void onBottom() {

		// 如果正在加载就跳出
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
	 * 各种网络请求状态，回调处理
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
	 * 评论已加载全部
	 */
	private void loadBottom() {
		loadingView.setVisibility(View.GONE);
		loadStateTextView.setVisibility(View.VISIBLE);
		loadingView.clearAnimation();
		loadStateTextView.setText(R.string.load_bottom);
		isLoading = false;
		isBottom = true;
	}

	// ViewHolder静态类
	static class ViewHolder {

		public practiceschool.circlepic.CircularImage user_pic; // 用户头像
		public TextView user_name; // 用户名称
		public ImageView user_sex; // 用户性别
		public TextView user_time; // 用户留言时间
		public TextView lightup_num; // 点亮数
		public TextView lightup_btn; // 点亮按钮
		public TextView user_message; // 留言内容

	}

	/**
	 * 自定义评论适配器
	 */
	public class MyAdapter extends BaseAdapter {
		private LayoutInflater mInflater = null;

		private MyAdapter(Context context) {
			// 根据context上下文加载布局，这里的是MainActivity本身，即this
			this.mInflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			// 在此适配器中所代表的数据集中的条目数
			return listData.size();
		}

		@Override
		public Object getItem(int position) {
			// 获取数据集中与指定索引对应的数据项
			return position;
		}

		@Override
		public long getItemId(int position) {
			// 获取在列表中与指定索引对应的行id
			return position;
		}

		// 获取一个在数据集中指定索引的视图来显示数据
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			// 如果缓存convertView为空，则需要创建View
			if (convertView == null) {
				holder = new ViewHolder();
				// 根据自定义的Item布局加载布局
				convertView = mInflater.inflate(R.layout.review_list, null);
				holder.user_pic = (practiceschool.circlepic.CircularImage) convertView.findViewById(R.id.user_pic);
				holder.user_name = (TextView) convertView.findViewById(R.id.user_name);
				holder.user_sex = (ImageView) convertView.findViewById(R.id.user_sex);
				holder.user_time = (TextView) convertView.findViewById(R.id.user_time);
				holder.lightup_num = (TextView) convertView.findViewById(R.id.praise_num);
				holder.lightup_btn = (TextView) convertView.findViewById(R.id.praise_pic);
				holder.user_message = (TextView) convertView.findViewById(R.id.user_message);
				// 将设置好的布局保存到缓存中，并将其设置在Tag里，以便后面方便取出Tag
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

			// 这句代码的作用是为了解决convertView被重用的时候，图片预设的问题
			holder.user_pic.setImageResource(R.drawable.default_portrait);

			final String user_pic_url = (String) listData.get(position).get("user_pic_url");
			holder.user_pic.setTag(user_pic_url);

			// 给Button添加单击事件 添加Button之后ListView将失去焦点，需要直接把Button的焦点去掉
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
			 * 显示图片 参数1：图片url 参数2：显示图片的控件 参数3：显示图片的设置 参数4：监听器
			 */
			Constants.imageLoader.cancelDisplayTask(holder.user_pic);
			Constants.imageLoader.displayImage(user_pic_url, holder.user_pic, Constants.options, Constants.animateFirstListener);

			return convertView;
		}

	}

	/**
	 * 自定义点亮评论适配器
	 */
	public class LightUpAdapter extends BaseAdapter {
		private LayoutInflater mInflater = null;

		private LightUpAdapter(Context context) {
			// 根据context上下文加载布局，这里的是MainActivity本身，即this
			this.mInflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			// 在此适配器中所代表的数据集中的条目数
			return lightupData.size();
		}

		@Override
		public Object getItem(int position) {
			// 获取数据集中与指定索引对应的数据项
			return position;
		}

		@Override
		public long getItemId(int position) {
			// 获取在列表中与指定索引对应的行id
			return position;
		}

		// 获取一个在数据集中指定索引的视图来显示数据
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			// 如果缓存convertView为空，则需要创建View
			if (convertView == null) {
				holder = new ViewHolder();
				// 根据自定义的Item布局加载布局
				convertView = mInflater.inflate(R.layout.review_list, null);
				holder.user_pic = (practiceschool.circlepic.CircularImage) convertView.findViewById(R.id.user_pic);
				holder.user_name = (TextView) convertView.findViewById(R.id.user_name);
				holder.user_sex = (ImageView) convertView.findViewById(R.id.user_sex);
				holder.user_time = (TextView) convertView.findViewById(R.id.user_time);
				holder.lightup_num = (TextView) convertView.findViewById(R.id.praise_num);
				holder.lightup_btn = (TextView) convertView.findViewById(R.id.praise_pic);
				holder.user_message = (TextView) convertView.findViewById(R.id.user_message);
				// 将设置好的布局保存到缓存中，并将其设置在Tag里，以便后面方便取出Tag
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

			// 这句代码的作用是为了解决convertView被重用的时候，图片预设的问题
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
			 * 显示图片 参数1：图片url 参数2：显示图片的控件 参数3：显示图片的设置 参数4：监听器
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
		MobclickAgent.onResume(this);  //友盟统计
	}

	@Override
	public void onPause() {
		Log.v(TAG, "OnPause unregister progress receiver");
		super.onPause();
		MobclickAgent.onPause(this);  //友盟统计
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
	 * 点赞是否成功
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
	 * 收藏是否成功
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
	 * 评论是否点亮成功
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
				map.put("praise_num", lightup_number); // 将新的文件名添加到Map以替换旧文件名
				listData.set(position, map); // 替换listItems中原来的map
				myAdapter.notifyDataSetChanged(); // 通知Adapter数据改变
			} else if (type.equals("lightup")) {
				String lightup_number = String.valueOf(Integer.parseInt((String) lightupData.get(position).get("praise_num")) + 1);
				Map<String, Object> map = lightupData.get(position);
				map.put("praise_num", lightup_number); // 将新的文件名添加到Map以替换旧文件名
				lightupData.set(position, map); // 替换listItems中原来的map
				lightupAdapter.notifyDataSetChanged(); // 通知Adapter数据改变
			}
			Toast.makeText(getApplicationContext(), description, Toast.LENGTH_SHORT).show();
		} else if (resultCode == 1001) {
			Toast.makeText(getApplicationContext(), description, Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(getApplicationContext(), description, Toast.LENGTH_SHORT).show();
		}
	};

	/**
	 * 用户点亮
	 */
	private void gotoLightup(String reviewid, TextView lightup_btn, int position, String type) {
		String ajaxUrl = Constants.NODE_URL + "/lightup";
		String ajaxData = null;
		try {
			ajaxData = "reviewid=" + reviewid + "&userid=" + userid;
		} catch (Exception e) {
			e.printStackTrace();
		}
		// 判断是否有网络
		if (Constants.isNetworkConnected(ArticleActivity.this)) {
			try {
				new Thread(new lightupThread(ajaxUrl, ajaxData, lightup_btn, position, type)).start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 数据服务器读取成功后，渲染文章详情
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
			 * 显示图片 参数1：图片url 参数2：显示图片的控件 参数3：显示图片的设置 参数4：监听器
			 */
			writepic = (String) infoData.getString("writepic");
			Constants.imageLoader.displayImage(infoData.getString("cover"), article_cover, Constants.options_big, Constants.animateFirstListener);
			Constants.imageLoader.displayImage(writepic, author_portrait, Constants.options, Constants.animateFirstListener);

		}
	};

	/**
	 * 数据服务器读取成功后，渲染点亮评论列表
	 */
	private void renderLightups(JSONObject data) throws JSONException {

		int resultCode = (Integer) data.getInt("resultCode");
		lightupData = new ArrayList<Map<String, Object>>();
		if (resultCode == 1000) {
			// 评论列表
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
	 * 数据服务器读取成功后，渲染评论列表
	 */
	private void renderReviews(JSONObject data) throws JSONException {

		int resultCode = (Integer) data.getInt("resultCode");
		listData = new ArrayList<Map<String, Object>>();
		all_review_title.setVisibility(View.VISIBLE);
		view_line.setVisibility(View.VISIBLE);
		if (resultCode == 1000) {
			// 评论列表
			currentPage++;
			JSONArray reviewlist = new JSONArray(data.getString("docs"));
			reviewsTotal = data.getInt("total");
			all_review_text.setText("所有评论(" + reviewsTotal + ")");
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
			all_review_text.setText("暂无评论(" + reviewsTotal + ")");
		}
		reviewList.setAdapter(myAdapter);
	};

	/**
	 * 数据服务器加载更多数据，渲染评论列表
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
	 * 加载文章详情线程
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
				Toast.makeText(ArticleActivity.this, "网络异常，请检查您的网络", Toast.LENGTH_SHORT).show();
			}
			Looper.loop();

		}
	}

	/**
	 * 加载评论列表线程
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
				Toast.makeText(ArticleActivity.this, "网络异常，请检查您的网络", Toast.LENGTH_SHORT).show();
			}
			Looper.loop();
		}
	}

	/**
	 * 点赞线程
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
				Toast.makeText(ArticleActivity.this, "网络异常，请检查您的网络", Toast.LENGTH_SHORT).show();
			}
			Looper.loop();

		}
	}

	/**
	 * 收藏线程
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
				Toast.makeText(ArticleActivity.this, "网络异常，请检查您的网络", Toast.LENGTH_SHORT).show();
			}
			Looper.loop();
		}
	}

	/**
	 * 点亮线程
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
				Toast.makeText(ArticleActivity.this, "网络异常，请检查您的网络", Toast.LENGTH_SHORT).show();
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
			// 活动的距离
			int distanceX = (int) (xMove - xDown);
			// 获取顺时速度
			int xSpeed = getScrollVelocity();
			// 当滑动的距离大于我们设定的最小距离且滑动的瞬间速度大于我们设定的速度时，返回到上一个activity
			if (distanceX > XDISTANCE_MIN && xSpeed > XSPEED_MIN) {
				finish();
			}
        }
        return false;
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

}
