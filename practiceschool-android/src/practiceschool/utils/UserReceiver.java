package practiceschool.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/** 
 * 自定义静态注册广播消息接收器 
 * 
 */  
public class UserReceiver extends BroadcastReceiver {  
  
    @Override  
    public void onReceive(Context context, Intent intent) {  
    	
       if(intent.getAction().equals("book.receiver.userReceiver")){  
            String _userscore = intent.getStringExtra("score");
            Constants.app_userscore = String.valueOf(Integer.parseInt(Constants.app_userscore)+Integer.parseInt(_userscore));
        }  
    	
    }  
}  
