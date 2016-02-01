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

public class ContactActivity extends Activity{
	
	private long exitTime = 0;  //按返回键，退出时间
	private static final String TAG = "zhihu.ui.ContactActivity";
	
	public ImageButton title_move_btn;

	public SlidingMenu menu;

	@Override	
	public void onCreate(Bundle savedInstanceState){
		Log.v(TAG, "OnCreate");
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE); // 声明使用自定义标题
		setContentView(R.layout.contact_main);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.contact_title); // 自定义标题栏布局赋值
		
		LeftMenu.initSlidingMenu(ContactActivity.this,"contact");  //初始化左侧菜单
		
		/**
		 * 初始化切换左侧菜单按钮
		 */	
		title_move_btn = (ImageButton) findViewById(R.id.title_move_btn);
		title_move_btn.setOnClickListener(new OnClickListener() {

			public void onClick(View view) {

				LeftMenu.menu.toggle(true);

			}
		});
		
		MobclickAgent.updateOnlineConfig(this);  //友盟统计
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
		MobclickAgent.onResume(this);  //友盟统计
	}
	
	@Override
	public void onPause(){
		Log.v(TAG, "OnPause unregister progress receiver");
		super.onPause();			
		MobclickAgent.onPause(this);  //友盟统计
	}
		
	public void onStop(){
		Log.v(TAG, "OnStop");
		super.onStop();		
	}
	
	public void onDestroy(){
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