package practiceschool.ui;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import practiceschool.network.HttpMethod;
import practiceschool.utils.Constants;
import practiceschool.utils.LeftMenu;

import com.example.practiceschool.R;
import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class TutorListActivity extends Activity {

	private long exitTime = 0;  //按返回键，退出时间
	private ListView tutorlist; // 导师列表
	private List<Map<String, Object>> listData; // 列表数据
	private MyAdapter myAdapter; // 自定义适配器
	private ProgressBar loading; // 加载loading动画
	private ImageButton title_move_btn;
	private static final String TAG = "zhihu.ui.TutorListActivity";

	private final int UPDATE_DATA_SUCCESS = 1; // 更多数据成功
	private final int UPDATE_DATA_FAILURE = 2; // 更新数据失败

	private int pagesize = 50; // 每页加载几条数据

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE); // 声明使用自定义标题
		setContentView(R.layout.tutorlist_main);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.tutorlist_title); // 自定义标题栏布局赋值

		tutorlist = (ListView) findViewById(R.id.tutorlist);
		loading = (ProgressBar) findViewById(R.id.loading);

		myAdapter = new MyAdapter(this);

		// 获取将要绑定的数据设置到data中
		loading.setVisibility(View.VISIBLE);
		this.loadData(0, pagesize);

		this.initLeftBtn(); // 初始化切换左侧菜单按钮
		
		LeftMenu.initSlidingMenu(TutorListActivity.this, "tutor"); // 初始化切换左侧菜单
		
		MobclickAgent.updateOnlineConfig(this);  //友盟统计

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
				case UPDATE_DATA_SUCCESS:
					try {
						renderList((JSONObject) msg.obj);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					break;
				case UPDATE_DATA_FAILURE:
					break;
				default:
					break;
				}
			}
			super.handleMessage(msg);
		}

	};

	/**
	 * 数据服务器读取成功后，渲染列表
	 */
	private void renderList(JSONObject data) throws JSONException {

		listData = new ArrayList<Map<String, Object>>();
		int resultCode = (Integer) data.getInt("resultCode");
		if (resultCode == 1000) {
			JSONArray docs = new JSONArray(data.getString("docs"));
			loading.setVisibility(View.INVISIBLE);
			for (int i = 0; i < docs.length(); i++) {
				JSONObject temp = (JSONObject) docs.get(i);
				Map<String, Object> map = new HashMap<String, Object>();
				map = new HashMap<String, Object>();
				map.put("tutorid", temp.getString("_id"));
				map.put("name", temp.getString("name"));
				map.put("url", temp.getString("cover"));
				map.put("description", temp.getString("description"));
				map.put("rank", temp.getInt("rank"));
				listData.add(map);
			}
			tutorlist.setAdapter(myAdapter);
			tutorlist.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> adapter, View v, int position, long id) {

					// 使用匿名内部类，隐式调用外部变量，外部变量需要final修饰。
					final int _position = position;

					Intent intent = new Intent();
					Bundle bundle = new Bundle();
					bundle.putString("tutorid", (String) listData.get(position).get("tutorid"));
					intent.putExtras(bundle);
					intent.setClass(TutorListActivity.this, TutorActivity.class);
					startActivity(intent);
				}

			});
		} else if (resultCode == 0) {
			loading.setVisibility(View.INVISIBLE);
		}
	};

	// 加载列表数据
	private void loadData(int pagestart, int pagesize) {

		String ajaxUrl = Constants.NODE_URL + "/tutorlist";
		String ajaxData = "pagestart=" + pagestart + "&pagesize=" + pagesize;

		// 判断是否有网络
		if (Constants.isNetworkConnected(TutorListActivity.this)) {
			try {
				new Thread(new tutorlistThread(ajaxUrl, ajaxData)).start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	// ViewHolder静态类
	static class ViewHolder {
		public TextView name; // 标题
		public TextView description; // 简介
		public TextView rank; // 推荐值
		public practiceschool.circlepic.CircularImage cover; // 头像
	}

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
				convertView = mInflater.inflate(R.layout.tutorlist_item, null);
				holder.cover = (practiceschool.circlepic.CircularImage) convertView.findViewById(R.id.cover);
				holder.name = (TextView) convertView.findViewById(R.id.name);
				holder.rank = (TextView) convertView.findViewById(R.id.rank);
				holder.description = (TextView) convertView.findViewById(R.id.description);
				// 将设置好的布局保存到缓存中，并将其设置在Tag里，以便后面方便取出Tag
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			holder.name.setText((String) listData.get(position).get("name"));
			holder.description.setText((String) listData.get(position).get("description"));
			holder.rank.setText(Constants.SwitchRecommandText((Integer) listData.get(position).get("rank")));

			final String url = (String) listData.get(position).get("url");
			holder.cover.setTag(url);

			/**
			 * 显示图片 参数1：图片url 参数2：显示图片的控件 参数3：显示图片的设置 参数4：监听器
			 */
			Constants.imageLoader.cancelDisplayTask(holder.cover);
			Constants.imageLoader.displayImage(url, holder.cover, Constants.options, Constants.animateFirstListener);

			return convertView;
		}

	}

	/**
	 * 加载广场文章列表线程
	 */
	class tutorlistThread implements Runnable {

		private String ajaxUrl;
		private String ajaxData;

		public tutorlistThread(String ajaxUrl, String ajaxData) {
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
					msg.what = UPDATE_DATA_SUCCESS;
					msg.obj = data;
					handler.sendMessage(msg);
				} catch (JSONException e) {
					msg.what = UPDATE_DATA_FAILURE;
					handler.sendMessage(msg);
				}
			} else {
				Toast.makeText(TutorListActivity.this, "网络异常，请检查您的网络", Toast.LENGTH_SHORT).show();
			}
			Looper.loop();
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