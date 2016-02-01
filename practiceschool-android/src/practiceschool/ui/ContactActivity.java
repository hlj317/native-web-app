package practiceschool.ui;

import practiceschool.utils.LeftMenu;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.practiceschool.R;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.umeng.analytics.MobclickAgent;
import com.umeng.message.PushAgent;

public class ContactActivity extends Activity{
	
	private long exitTime = 0;  //�����ؼ����˳�ʱ��
	private static final String TAG = "zhihu.ui.ContactActivity";
	
	public ImageButton title_move_btn;

	public SlidingMenu menu;

	@Override	
	public void onCreate(Bundle savedInstanceState){
		Log.v(TAG, "OnCreate");
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE); // ����ʹ���Զ������
		setContentView(R.layout.contact_main);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.contact_title); // �Զ�����������ָ�ֵ
		
		LeftMenu.initSlidingMenu(ContactActivity.this,"contact");  //��ʼ�����˵�
		
		/**
		 * ��ʼ���л����˵���ť
		 */	
		title_move_btn = (ImageButton) findViewById(R.id.title_move_btn);
		title_move_btn.setOnClickListener(new OnClickListener() {

			public void onClick(View view) {

				LeftMenu.menu.toggle(true);

			}
		});
		
		MobclickAgent.updateOnlineConfig(this);  //����ͳ��
		PushAgent.getInstance(this).onAppStart();  //��������
	}

	@Override
	public void onStart() {
		Log.v(TAG, "OnStart");
		super.onStart();
	}
	
	@Override
	public void onResume(){
		Log.v(TAG, "OnResume");
		super.onResume();	
		MobclickAgent.onResume(this);  //����ͳ��
	}
	
	@Override
	public void onPause(){
		Log.v(TAG, "OnPause unregister progress receiver");
		super.onPause();			
		MobclickAgent.onPause(this);  //����ͳ��
	}
		
	public void onStop(){
		Log.v(TAG, "OnStop");
		super.onStop();		
	}
	
	public void onDestroy(){
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