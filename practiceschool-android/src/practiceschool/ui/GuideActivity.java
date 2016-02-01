package practiceschool.ui;

import com.example.practiceschool.R;
import com.umeng.analytics.MobclickAgent;
import com.umeng.message.PushAgent;
import com.umeng.update.UmengUpdateAgent;

import android.app.Activity;  
import android.os.Bundle;  
import android.content.Intent;  
import android.graphics.PixelFormat;  
import android.os.Handler;  
import android.view.WindowManager;  
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
  
public class GuideActivity extends Activity {  
      
    //time for picture display  
    private static final int LOAD_DISPLAY_TIME = 2500; 
	private final String DATABASE_PATH = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();  //资源存放根路径
      
    /** Called when the activity is first created. */  
    @Override  
    public void onCreate(Bundle savedInstanceState) {  
        super.onCreate(savedInstanceState);  
        UmengUpdateAgent.setUpdateOnlyWifi(false);
        UmengUpdateAgent.update(this);
        getWindow().setFormat(PixelFormat.RGBA_8888);  
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DITHER);  
        setContentView(R.layout.guide_main);  
        
        //友盟消息推送
        PushAgent mPushAgent = PushAgent.getInstance(this);
        mPushAgent.enable();
  
        TextView guide_text3 = (TextView) findViewById(R.id.guide_text3);
//        Animation animation3 = AnimationUtils.loadAnimation(this,R.anim.guide_text3);
//        guide_text3.startAnimation(animation3);
        
        TextView guide_text2 = (TextView) findViewById(R.id.guide_text2);
//        Animation animation2 = AnimationUtils.loadAnimation(this,R.anim.guide_text2);
//        guide_text2.startAnimation(animation2);
        
        TextView guide_text1 = (TextView) findViewById(R.id.guide_text1);
//        Animation animation1 = AnimationUtils.loadAnimation(this,R.anim.guide_text1);
//        guide_text1.startAnimation(animation1);
        
        ImageView guide_logo1 = (ImageView) findViewById(R.id.guide_logo1);
//        Animation animation4 = AnimationUtils.loadAnimation(this,R.anim.guide_logo1);
//        guide_logo1.startAnimation(animation4);
        
        ImageView guide_logo2 = (ImageView) findViewById(R.id.guide_logo2);
//        Animation animation5 = AnimationUtils.loadAnimation(this,R.anim.guide_logo2);
//        guide_logo2.startAnimation(animation5);
        
        ImageView guide_logo3 = (ImageView) findViewById(R.id.guide_logo3);
//        Animation animation6 = AnimationUtils.loadAnimation(this,R.anim.guide_logo3);
//        guide_logo3.startAnimation(animation6);
        
        ImageView guide_logo4 = (ImageView) findViewById(R.id.guide_logo4);
//        Animation animation7 = AnimationUtils.loadAnimation(this,R.anim.guide_logo4);
//        guide_logo4.startAnimation(animation7);
        
        ImageView guide_logo5 = (ImageView) findViewById(R.id.guide_logo5);
//        Animation animation8 = AnimationUtils.loadAnimation(this,R.anim.guide_logo5);
//        guide_logo5.startAnimation(animation8);
        
        ImageView guide_logo6 = (ImageView) findViewById(R.id.guide_logo6);
//        Animation animation9 = AnimationUtils.loadAnimation(this,R.anim.guide_logo6);
//        guide_logo6.startAnimation(animation9);
          
        new Handler().postDelayed(new Runnable() {  
            public void run() {  
                //Go to main activity, and finish load activity  
                Intent mainIntent = new Intent(GuideActivity.this, LoginActivity.class);  
                GuideActivity.this.startActivity(mainIntent);  
                GuideActivity.this.finish();  
            }  
        }, LOAD_DISPLAY_TIME);   
        
        MobclickAgent.updateOnlineConfig(this);  //友盟统计
        PushAgent.getInstance(this).onAppStart();  //友盟推送
    }  
  
	@Override
	public void onStart() {
		super.onStart();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);  //友盟统计
	}

	@Override
	public void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);  //友盟统计
	}

	public void onStop() {
		super.onStop();
	}

	public void onDestroy() {
		super.onDestroy();
	}


}  