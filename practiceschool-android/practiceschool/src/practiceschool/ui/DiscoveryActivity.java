package practiceschool.ui;

import java.util.ArrayList;

import practiceschool.menu.ActionItem;
import practiceschool.menu.TitlePopup;
import practiceschool.menu.TitlePopup.OnItemOnClickListener;
import practiceschool.utils.Constants;
import practiceschool.utils.LeftMenu;


import com.example.practiceschool.R;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.SendMessageToWX;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.mm.sdk.openapi.WXMediaMessage;
import com.tencent.mm.sdk.openapi.WXWebpageObject;
import com.umeng.analytics.MobclickAgent;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.Toast;

//修炼广场
public class DiscoveryActivity extends FragmentActivity {

	private long exitTime = 0;  //按返回键，退出时间
	private ViewPager viewPager;
	private Fragment[] fragments;
	private ArrayList<RadioButton> btnTitles;
	private ImageButton title_move_btn;
	private ImageButton title_menu;
	private TitlePopup titlePopup;
	private IWXAPI api;

	private static final String TAG = "zhihu.ui.DiscoveryActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE); // 声明使用自定义标题
		setContentView(R.layout.discovery_main);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.discovery_title); // 自定义标题栏布局赋值
		this.initViewPager(); // 初始化viewpager容器
		this.initTitle(); // 初始化radiobutton
		this.initLeftBtn(); // 初始化切换左侧菜单按钮
		this.setCurrentPage(0); // 初始化页面默认tabbar
		LeftMenu.initSlidingMenu(DiscoveryActivity.this, "time"); // 初始化切换左侧菜单
	  //LeftMenu.menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
		
		/**
		 * 初始化右上角菜单
		 */  
		api = WXAPIFactory.createWXAPI(this, Constants.APP_ID,false);
		titlePopup = new TitlePopup(this, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		titlePopup.addAction(new ActionItem(this, "推荐给好友", R.drawable.mm_title_btn_share_normal));
		titlePopup.addAction(new ActionItem(this, "分享到朋友圈", R.drawable.mm_title_btn_weixin_normal));	

		titlePopup.setItemOnClickListener(new OnItemOnClickListener(){
 
			@Override
			public void onItemClick(ActionItem item, int position) {
				api.registerApp(Constants.APP_ID);
				WXWebpageObject webpage = new WXWebpageObject();
				webpage.webpageUrl = Constants.NODE_URL + "/download";
				
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
	 * 初始化viewpager
	 */
	private void initViewPager() {

		viewPager = (ViewPager) findViewById(R.id.pager);
		// 创建两个子容器
		Fragment fragment1 = new GirlsActivity();
		Fragment fragment2 = new LearningActivity();
		Fragment fragment3 = new ExerciseActivity();
		Fragment fragment4 = new AskActivity();
		fragments = new Fragment[] { fragment1, fragment2, fragment3, fragment4 };
		MyFragmentPagerAdapter adapter = new MyFragmentPagerAdapter(getSupportFragmentManager(), fragments);

		viewPager.setAdapter(adapter);

		// 设置预加载个数，有几个fragment就加几个
		viewPager.setOffscreenPageLimit(4);

		// ViewPager有两个操作，一个是用手指滑动翻页，一个是直接setCurrentItem（一般用于点击上面的tab直接setCurrentItem）
		viewPager.setOnPageChangeListener(new OnPageChangeListener() {

			// 这个方法有一个参数position，代表哪个页面被选中。当用手指滑动翻页的时候，如果翻动成功了（滑动的距离够长），
			// 手指抬起来就会立即执行这个方法，position就是当前滑动到的页面。
			public void onPageSelected(int position) {
				//Toast.makeText(DiscoveryActivity.this, "onPageSelected", Toast.LENGTH_SHORT).show();
				btnTitles.get(position).setChecked(true);// 保持页面跟按钮的联动
				// 下面这个条件语句里面的是针对侧滑效果的
				if (position == 0) {
					// 如果当前是第一页，触摸范围为全屏
					LeftMenu.menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);// 设置成全屏响应
				} else {
					// 如果不是第一页，触摸范围是边缘60px的地方
					LeftMenu.menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
				}
			}

			// 这个方法会在屏幕滚动过程中不断被调用。
			// 有三个参数，第一个position，这个参数要特别注意一下。
			// 当用手指滑动时，如果手指按在页面上不动，position和当前页面index是一致的；
			// 如果手指向左拖动（相应页面向右翻动），这时候position大部分时间和当前页面是一致的，只有翻页成功的情况下最后一次调用才会变为目标页面；
			// 如果手指向右拖动（相应页面向左翻动），这时候position大部分时间和目标页面是一致的，只有翻页不成功的情况下最后一次调用才会变为原页面。
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
				//Toast.makeText(DiscoveryActivity.this, "onPageScrolled", Toast.LENGTH_SHORT).show();
			}

			// 这个方法在手指操作屏幕的时候发生变化。有三个值：0（END）,1(PRESS) , 2(UP) 。
			// 当用手指滑动翻页时，手指按下去的时候会触发这个方法，state值为1;
			// 手指抬起时如果发生了滑动（即使很小），这个值会变为2，然后最后变为0 。总共执行这个方法三次。
			public void onPageScrollStateChanged(int state) {
				//Toast.makeText(DiscoveryActivity.this, "onPageScrollStateChanged", Toast.LENGTH_SHORT).show();

			}

			/*
			 * 三个方法的执行顺序为：用手指拖动翻页时，最先执行一遍onPageScrollStateChanged（1）;
			 * 然后不断执行onPageScrolled，放手指的时候，直接立即执行一次onPageScrollStateChanged（2）;
			 * 然后立即执行一次onPageSelected，然后再不断执行onPageScrollStateChanged;
			 * 最后执行一次onPageScrollStateChanged（0）; 其它的情况由这个可以推出来，不再赘述。
			 */
		});
	}

	/**
	 * 初始化切换左侧菜单按钮
	 */
	private void initLeftBtn() {
		title_move_btn = (ImageButton) findViewById(R.id.title_move_btn);
		title_move_btn.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				LeftMenu.menu.toggle(true);
			}
		});
	}

	/**
	 * 初始化用来显示title的RadioButton
	 */
	private void initTitle() {
		// 创建tabbar按钮
		btnTitles = new ArrayList<RadioButton>();
		btnTitles.add((RadioButton) findViewById(R.id.radio_girls));
		btnTitles.add((RadioButton) findViewById(R.id.radio_learning));
		btnTitles.add((RadioButton) findViewById(R.id.radio_exercise));
		btnTitles.add((RadioButton) findViewById(R.id.radio_ask));
		for (int i = 0; i < btnTitles.size(); i++) {
			btnTitles.get(i).setOnClickListener(new MyOnClickListener(i));
		}
	}

	/**
	 * tabbar切换fragment
	 */
	public class MyOnClickListener implements OnClickListener {
		private int index;

		public MyOnClickListener(int index) {
			this.index = index;
		}

		@Override
		public void onClick(View v) {
			DiscoveryActivity.this.setCurrentPage(index);
		}
	}

	/**
	 * 根据id设置切换页面和底部菜单
	 * 
	 * @param currentId
	 */
	public void setCurrentPage(int currentId) {
		viewPager.setCurrentItem(currentId);
		btnTitles.get(currentId).setChecked(true);
	}

	/**
	 * 封装一个加载Fragment的适配器类
	 */
	public class MyFragmentPagerAdapter extends FragmentPagerAdapter {
		private Fragment[] fragmentArray;

		public MyFragmentPagerAdapter(FragmentManager fm, Fragment[] myFragmentArray) {
			super(fm);
			// TODO Auto-generated constructor stub
			if (null == myFragmentArray) {
				this.fragmentArray = new Fragment[] {};
			} else {
				this.fragmentArray = myFragmentArray;
			}
		}

		@Override
		public Fragment getItem(int arg0) {
			// TODO Auto-generated method stub
			return fragmentArray[arg0];
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return fragmentArray.length;
		}

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
		//友盟统计
	    MobclickAgent.onPageStart("SplashScreen");   //统计页面(仅有Activity的应用中SDK自动调用，不需要单独写)
	    MobclickAgent.onResume(this);                //统计时长
	}

	@Override
	public void onPause() {
		Log.v(TAG, "OnPause unregister progress receiver");
		super.onPause();
		//友盟统计
	    MobclickAgent.onPageEnd("SplashScreen"); // （仅有Activity的应用中SDK自动调用，不需要单独写）保证 onPageEnd 在onPause 之前调用,因为 onPause 中会保存信息 ）
	    MobclickAgent.onPause(this);
	}

	public void onStop() {
		Log.v(TAG, "OnStop");
		super.onStop();
	}

	public void onDestroy() {
		Log.v(TAG, "Destroy");
		super.onDestroy();
	}
	 
	//再按一次返回键退出程序
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){   
	        if((System.currentTimeMillis()-exitTime) > 2000){  
	            Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();                                
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
