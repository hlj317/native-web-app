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
import practiceschool.swipebacklayout.SwipeBackActivity;
import practiceschool.utils.Constants;

import com.example.practiceschool.R;
import com.umeng.analytics.MobclickAgent;
import com.umeng.message.PushAgent;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class CollectActivity extends SwipeBackActivity {

	private ListView collectList; // ���������б�
	private List<Map<String, Object>> listData; // �б�����
	private MyAdapter myAdapter; // �Զ���������
	private ProgressBar loading; // ����loading����
	private ImageButton title_move_btn;
	private LinearLayout no_collect;

	private final int UPDATE_DATA_SUCCESS = 1; // �������ݳɹ�
	private final int UPDATE_DATA_FAILURE = 2; // ��������ʧ��
	private final int LOADMORE_DATA_SUCCESS = 3; // ���ظ������ݳɹ�

	private PullToRefreshLayout pullToRefreshLayout; // ˢ�¼����ؿؼ�
	private int currentPage = 0; // ��ǰҳ��
	private int pagesize = 4; // ÿҳ���ؼ�������
	private String userid = Constants.app_userid;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE); // ����ʹ���Զ������
		super.onCreate(savedInstanceState);	
		setContentView(R.layout.collect_main);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.collect_title); // �Զ�����������ָ�ֵ

		// �����б�ˢ�¼����ؿؼ�
		pullToRefreshLayout = ((PullToRefreshLayout) findViewById(R.id.refresh_view));

		pullToRefreshLayout.setOnRefreshListener(new MyListener() {

			public void onRefresh(final PullToRefreshLayout pullToRefreshLayout) {
				// ���²���
				loadData(0, (currentPage + 1) * pagesize, "update");
			}

			public void onLoadMore(final PullToRefreshLayout pullToRefreshLayout) {
				// ���ز���
				loadData(++currentPage, pagesize, "loadmore");
			}

		});

		collectList = (ListView) findViewById(R.id.collectList);
		loading = (ProgressBar) findViewById(R.id.loading);
		no_collect = (LinearLayout) findViewById(R.id.no_collect);

		myAdapter = new MyAdapter(this);

		// ��ȡ��Ҫ�󶨵��������õ�data��
		loading.setVisibility(View.VISIBLE);
		this.loadData(0, pagesize, "update");

		this.initLeftBtn(); // ��ʼ���л����˵���ť
		
		//��������
		getSwipeBackLayout();
		
		MobclickAgent.updateOnlineConfig(this);  //����ͳ��
		PushAgent.getInstance(this).onAppStart();  //��������

	}

	/**
	 * ��ʼ���л����˵���ť
	 */
	private void initLeftBtn() {
		title_move_btn = (ImageButton) findViewById(R.id.title_move_btn);
		title_move_btn.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				finish();
			}
		});

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
	 * ���ݷ�������ȡ�ɹ�����Ⱦ�б�
	 */
	private void renderList(JSONObject data) throws JSONException {

		listData = new ArrayList<Map<String, Object>>();
		int resultCode = (Integer) data.getInt("resultCode");
		if (resultCode == 1000) {
			JSONArray docs = new JSONArray(data.getString("docs"));
			no_collect.setVisibility(View.INVISIBLE);
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
			collectList.setAdapter(myAdapter);
			pullToRefreshLayout.refreshFinish(PullToRefreshLayout.SUCCEED);
			collectList.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> adapter, View v, int position, long id) {

					// ʹ�������ڲ��࣬��ʽ�����ⲿ�������ⲿ������Ҫfinal���Ρ�
					final int _position = position;

					Intent intent = new Intent();
					Bundle bundle = new Bundle();
					bundle.putString("articleid", (String) listData.get(position).get("articleid"));
					bundle.putString("type", "collect");
					intent.putExtras(bundle);
					intent.setClass(CollectActivity.this, ArticleActivity.class);
					startActivity(intent);
				}

			});
		} else if (resultCode == 1002) {
			pullToRefreshLayout.refreshFinish(PullToRefreshLayout.SUCCEED);
			loading.setVisibility(View.INVISIBLE);
			no_collect.setVisibility(View.VISIBLE);
		}
	};

	/**
	 * ���ݷ��������ظ������ݣ���Ⱦ�б�
	 */
	private void AddRenderList(JSONObject data) throws JSONException {

		int resultCode = (Integer) data.getInt("resultCode");
		if (resultCode == 1000) {
			no_collect.setVisibility(View.INVISIBLE);
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
		} else if (resultCode == 1002) {
			no_collect.setVisibility(View.VISIBLE);
			pullToRefreshLayout.loadmoreFinish(PullToRefreshLayout.SUCCEED);
		}
	};

	// �����б�����
	private void loadData(int pagestart, int pagesize, String action) {

		String ajaxUrl = Constants.NODE_URL + "/collectlist";
		String ajaxData = "userid=" + userid + "&pagestart=" + pagestart + "&pagesize=" + pagesize;

		// �ж��Ƿ�������
		if (Constants.isNetworkConnected(CollectActivity.this)) {
			try {
				new Thread(new articlelistThread(ajaxUrl, ajaxData, action)).start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	// ViewHolder��̬��
	static class ViewHolder {
		public TextView title; // ����
		public ImageView cover; // ����
		public TextView url; // ��������
		public TextView content; // ����
		public TextView praise_text; // ������
		public TextView review_text; // ������
	}

	public class MyAdapter extends BaseAdapter {
		private LayoutInflater mInflater = null;

		private MyAdapter(Context context) {
			// ����context�����ļ��ز��֣��������MainActivity��������this
			this.mInflater = LayoutInflater.from(context);
			// mInflater = (LayoutInflater)
			// context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public int getCount() {
			// �ڴ��������������������ݼ��е���Ŀ��
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
				convertView = mInflater.inflate(R.layout.article_list, null);
				holder.cover = (ImageView) convertView.findViewById(R.id.cover);
				holder.title = (TextView) convertView.findViewById(R.id.title);
				holder.content = (TextView) convertView.findViewById(R.id.content);
				holder.praise_text = (TextView) convertView.findViewById(R.id.praise_text);
				holder.review_text = (TextView) convertView.findViewById(R.id.review_text);
				// �����úõĲ��ֱ��浽�����У�������������Tag��Ա���淽��ȡ��Tag
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			holder.title.setText((String) listData.get(position).get("title"));
			holder.content.setText((String) listData.get(position).get("content"));
			holder.praise_text.setText((String) listData.get(position).get("praise_text"));
			holder.review_text.setText((String) listData.get(position).get("review_text"));

			// �������������Ϊ�˽��convertView�����õ�ʱ��ͼƬԤ�������
			holder.cover.setImageResource(R.drawable.youth);

			final String url = (String) listData.get(position).get("url");
			holder.cover.setTag(url);

			/**
			 * ��ʾͼƬ ����1��ͼƬurl ����2����ʾͼƬ�Ŀؼ� ����3����ʾͼƬ������ ����4��������
			 */
			Constants.imageLoader.cancelDisplayTask(holder.cover);
			Constants.imageLoader.displayImage(url, holder.cover, Constants.options_big, Constants.animateFirstListener);

			return convertView;
		}

	}

	/**
	 * ���ع㳡�����б��߳�
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
				Toast.makeText(CollectActivity.this, "�����쳣��������������", Toast.LENGTH_SHORT).show();
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
		MobclickAgent.onResume(this);  //����ͳ��
	}

	@Override
	public void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);  //����ͳ��
	}

	public void onStop() {
		super.onStop();
	}

	public void onDestroy() {
		super.onDestroy();
	}

}