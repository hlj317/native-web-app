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
import com.umeng.message.PushAgent;

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

	private long exitTime = 0;  //�����ؼ����˳�ʱ��
	private ListView tutorlist; // ��ʦ�б�
	private List<Map<String, Object>> listData; // �б�����
	private MyAdapter myAdapter; // �Զ���������
	private ProgressBar loading; // ����loading����
	private ImageButton title_move_btn;
	private static final String TAG = "zhihu.ui.TutorListActivity";

	private final int UPDATE_DATA_SUCCESS = 1; // �������ݳɹ�
	private final int UPDATE_DATA_FAILURE = 2; // ��������ʧ��

	private int pagesize = 50; // ÿҳ���ؼ�������

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE); // ����ʹ���Զ������
		setContentView(R.layout.tutorlist_main);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.tutorlist_title); // �Զ�����������ָ�ֵ

		tutorlist = (ListView) findViewById(R.id.tutorlist);
		loading = (ProgressBar) findViewById(R.id.loading);

		myAdapter = new MyAdapter(this);

		// ��ȡ��Ҫ�󶨵��������õ�data��
		loading.setVisibility(View.VISIBLE);
		this.loadData(0, pagesize);

		this.initLeftBtn(); // ��ʼ���л����˵���ť
		
		LeftMenu.initSlidingMenu(TutorListActivity.this, "tutor"); // ��ʼ���л����˵�
		
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
				LeftMenu.menu.toggle(true);
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

					// ʹ�������ڲ��࣬��ʽ�����ⲿ�������ⲿ������Ҫfinal���Ρ�
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

	// �����б�����
	private void loadData(int pagestart, int pagesize) {

		String ajaxUrl = Constants.NODE_URL + "/tutorlist";
		String ajaxData = "pagestart=" + pagestart + "&pagesize=" + pagesize;

		// �ж��Ƿ�������
		if (Constants.isNetworkConnected(TutorListActivity.this)) {
			try {
				new Thread(new tutorlistThread(ajaxUrl, ajaxData)).start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	// ViewHolder��̬��
	static class ViewHolder {
		public TextView name; // ����
		public TextView description; // ���
		public TextView rank; // �Ƽ�ֵ
		public practiceschool.circlepic.CircularImage cover; // ͷ��
	}

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
				convertView = mInflater.inflate(R.layout.tutorlist_item, null);
				holder.cover = (practiceschool.circlepic.CircularImage) convertView.findViewById(R.id.cover);
				holder.name = (TextView) convertView.findViewById(R.id.name);
				holder.rank = (TextView) convertView.findViewById(R.id.rank);
				holder.description = (TextView) convertView.findViewById(R.id.description);
				// �����úõĲ��ֱ��浽�����У�������������Tag��Ա���淽��ȡ��Tag
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
			 * ��ʾͼƬ ����1��ͼƬurl ����2����ʾͼƬ�Ŀؼ� ����3����ʾͼƬ������ ����4��������
			 */
			Constants.imageLoader.cancelDisplayTask(holder.cover);
			Constants.imageLoader.displayImage(url, holder.cover, Constants.options, Constants.animateFirstListener);

			return convertView;
		}

	}

	/**
	 * ���ع㳡�����б��߳�
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
				Toast.makeText(TutorListActivity.this, "�����쳣��������������", Toast.LENGTH_SHORT).show();
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

	//�ٰ�һ�η��ؼ��˳�����
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){   
	        if((System.currentTimeMillis()-exitTime) > 2000){  
	            Toast.makeText(getApplicationContext(), "�ٰ�һ���˳�����", Toast.LENGTH_SHORT).show();                                
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