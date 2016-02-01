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
import practiceschool.pullandrefresh.MyListener;
import practiceschool.pullandrefresh.PullToRefreshLayout;
import practiceschool.utils.Constants;


import com.example.practiceschool.R;
import com.umeng.analytics.MobclickAgent;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class GirlsActivity extends Fragment {

	private ListView girlsList; // 热血泡妞列表
	private List<Map<String, Object>> listData; // 列表数据
	private MyAdapter myAdapter; // 自定义适配器
	private ProgressBar loading; // 加载loading动画

	private final int UPDATE_DATA_SUCCESS = 1; // 更多数据成功
	private final int UPDATE_DATA_FAILURE = 2; // 更新数据失败
	private final int LOADMORE_DATA_SUCCESS = 3; // 加载更多数据成功

	private PullToRefreshLayout pullToRefreshLayout; // 刷新及下载控件
	private int currentPage = 0; // 当前页数
	private int pagesize = 4; // 每页加载几条数据

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		View rootView = inflater.inflate(R.layout.girls_main, container, false);

		// 创建列表刷新及下载控件
		pullToRefreshLayout = ((PullToRefreshLayout) rootView.findViewById(R.id.refresh_view));

		pullToRefreshLayout.setOnRefreshListener(new MyListener() {

			public void onRefresh(final PullToRefreshLayout pullToRefreshLayout) {
				// 更新操作
				loadData(0, (currentPage + 1) * pagesize, "update");
			}

			public void onLoadMore(final PullToRefreshLayout pullToRefreshLayout) {
				// 加载操作
				loadData(++currentPage, pagesize, "loadmore");
			}

		});

		girlsList = (ListView) rootView.findViewById(R.id.girlsList);
		loading = (ProgressBar) rootView.findViewById(R.id.loading);

		myAdapter = new MyAdapter(getActivity());

		// 获取将要绑定的数据设置到data中
		loading.setVisibility(View.VISIBLE);
		this.loadData(0, pagesize, "update");

		return rootView;
	}

	/**
	 * 异步消息回调处理器
	 */
	static class MyHandler extends Handler {
		WeakReference<Fragment> mActivityReference;

		MyHandler(Fragment fragment) {
			mActivityReference = new WeakReference<Fragment>(fragment);
		}
	}

	private Handler handler = new MyHandler(this) {
		@Override
		public void handleMessage(Message msg) {
			final Fragment fragment = mActivityReference.get();
			if (fragment != null) {
				switch (msg.what) {
				case UPDATE_DATA_SUCCESS:
					try {
						renderList((JSONObject) msg.obj);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					break;
				case LOADMORE_DATA_SUCCESS:
					try {
						AddRenderList((JSONObject) msg.obj);
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
				map.put("articleid", temp.getString("_id"));
				map.put("title", temp.getString("title"));
				map.put("url", temp.getString("cover"));
				map.put("content", temp.getString("description"));
				map.put("praise_text", temp.getString("praizecount"));
				map.put("review_text", temp.getString("reviewcount"));
				listData.add(map);
			}
			girlsList.setAdapter(myAdapter);
			pullToRefreshLayout.refreshFinish(PullToRefreshLayout.SUCCEED);
			girlsList.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> adapter, View v, int position, long id) {

					// 使用匿名内部类，隐式调用外部变量，外部变量需要final修饰。
					final int _position = position;

					Intent intent = new Intent();
					Bundle bundle = new Bundle();
					bundle.putString("articleid", (String) listData.get(position).get("articleid"));
					bundle.putString("type", "girls");
					intent.putExtras(bundle);
					intent.setClass(getActivity(), ArticleActivity.class);
					startActivity(intent);
					//getActivity().overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
				}

			});
		}
	};

	/**
	 * 数据服务器加载更多数据，渲染列表
	 */
	private void AddRenderList(JSONObject data) throws JSONException {

		int resultCode = (Integer) data.getInt("resultCode");
		if (resultCode == 1000) {
			JSONArray docs = new JSONArray(data.getString("docs"));
			for (int i = 0; i < docs.length(); i++) {
				JSONObject temp = (JSONObject) docs.get(i);
				Map<String, Object> map = new HashMap<String, Object>();
				map = new HashMap<String, Object>();
				map.put("articleid", temp.getString("_id"));
				map.put("title", temp.getString("title"));
				map.put("url", temp.getString("cover"));
				map.put("content", temp.getString("description"));
				map.put("praise_text", temp.getString("praizecount"));
				map.put("review_text", temp.getString("reviewcount"));
				listData.add(map);
			}
			myAdapter.notifyDataSetChanged();
			pullToRefreshLayout.loadmoreFinish(PullToRefreshLayout.SUCCEED);
		} else if (resultCode == 1001) {
			pullToRefreshLayout.loadmoreFinish(PullToRefreshLayout.LOAD_ALL);
		}
	};

	// 加载列表数据
	private void loadData(int pagestart, int pagesize, String action) {

		String ajaxUrl = Constants.NODE_URL + "/articlelist";
		String ajaxData = "channel=2&sort=3&pagestart=" + pagestart + "&pagesize=" + pagesize;

		// 判断是否有网络
		if (Constants.isNetworkConnected(getActivity())) {
			try {
				new Thread(new articlelistThread(ajaxUrl, ajaxData, action)).start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	// ViewHolder静态类
	static class ViewHolder {
		public TextView title; // 标题
		public ImageView cover; // 封面
		public TextView url; // 封面链接
		public TextView content; // 内容
		public TextView praise_text; // 点赞数
		public TextView review_text; // 留言数
	}

	public class MyAdapter extends BaseAdapter {
		private LayoutInflater mInflater = null;

		private MyAdapter(Context context) {
			// 根据context上下文加载布局，这里的是MainActivity本身，即this
			this.mInflater = LayoutInflater.from(context);
			// mInflater = (LayoutInflater)
			// context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
				convertView = mInflater.inflate(R.layout.article_list, null);
				holder.cover = (ImageView) convertView.findViewById(R.id.cover);
				holder.title = (TextView) convertView.findViewById(R.id.title);
				holder.content = (TextView) convertView.findViewById(R.id.content);
				holder.praise_text = (TextView) convertView.findViewById(R.id.praise_text);
				holder.review_text = (TextView) convertView.findViewById(R.id.review_text);
				// 将设置好的布局保存到缓存中，并将其设置在Tag里，以便后面方便取出Tag
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			holder.title.setText((String) listData.get(position).get("title"));
			holder.content.setText((String) listData.get(position).get("content"));
			holder.praise_text.setText((String) listData.get(position).get("praise_text"));
			holder.review_text.setText((String) listData.get(position).get("review_text"));

			// 这句代码的作用是为了解决convertView被重用的时候，图片预设的问题
			holder.cover.setImageResource(R.drawable.youth);

			final String url = (String) listData.get(position).get("url");
			holder.cover.setTag(url);

			/**
			 * 显示图片 参数1：图片url 参数2：显示图片的控件 参数3：显示图片的设置 参数4：监听器
			 */
			Constants.imageLoader.cancelDisplayTask(holder.cover);
			Constants.imageLoader.displayImage(url, holder.cover, Constants.options_big, Constants.animateFirstListener);

			return convertView;
		}

	}

	/**
	 * 加载广场文章列表线程
	 */
	class articlelistThread implements Runnable {

		private String ajaxUrl;
		private String ajaxData;
		private String action;

		public articlelistThread(String ajaxUrl, String ajaxData, String action) {
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
					if (action == "update") {
						msg.what = UPDATE_DATA_SUCCESS;
					} else if (action == "loadmore") {
						msg.what = LOADMORE_DATA_SUCCESS;
					}
					msg.obj = data;
					handler.sendMessage(msg);
				} catch (JSONException e) {
					msg.what = UPDATE_DATA_FAILURE;
					handler.sendMessage(msg);
				}
			} else {
				Toast.makeText(getActivity(), "网络异常，请检查您的网络", Toast.LENGTH_SHORT).show();
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
		super.onResume();
		MobclickAgent.onPageStart("MainScreen");  //友盟统计，统计页面
	}

	@Override
	public void onPause() {
		super.onPause();
		MobclickAgent.onPageEnd("MainScreen"); 
	}

	public void onStop() {
		super.onStop();
	}

	public void onDestroy() {
		super.onDestroy();
	}


}
