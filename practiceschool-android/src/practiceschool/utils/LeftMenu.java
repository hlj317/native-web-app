package practiceschool.utils;

import practiceschool.ui.ContactActivity;
import practiceschool.ui.DiscoveryActivity;
import practiceschool.ui.TutorListActivity;
import practiceschool.ui.UsercenterActivity;
import practiceschool.ui.WritingActivity;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.IBinder;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.practiceschool.R;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

public class LeftMenu {
	
	public static SlidingMenu menu; 
	private static LinearLayout left_user_btn;
	private static LinearLayout left_tutor_btn;
	private static LinearLayout left_time_btn;
	private static LinearLayout left_write_btn;
	private static LinearLayout left_contact_btn; 
	private static RelativeLayout left_myuser;
	
	/** ��ȡ����ͼƬ·�� */
	public static String picPath;

	public static practiceschool.circlepic.CircularImage left_user_cover;   //���˵�-�û�ͷ��
	public static TextView  left_user_name;    //���˵�-�û��ǳ�
	public static TextView  left_user_rank;    //���˵�-�û��ȼ�

	public static ImageButton discoveryBtn;  //�л��˵���ť
	/**
	 * ��ʼ����Դ���SlidingMenu
	 */
	public static void initSlidingMenu(final Activity activity,String type) {
		menu = new SlidingMenu(activity); // ʵ���������˵�����
		menu.setMode(SlidingMenu.LEFT);
		menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
		menu.setShadowWidthRes(R.dimen.shadow_width); // ���ò˵���Ե�Ľ�����ɫ��� ����ӰЧ����ȣ�
		menu.setShadowDrawable(R.drawable.slidingmenu_shadow); // ���û�����Ӱ��ͼ����Դ
		menu.setBehindOffsetRes(R.dimen.slidingmenu_offset); // ���û����˵���ͼ�Ŀ��
		menu.setFadeDegree(0.35f);// �߿�ĽǶȣ�����ָ�߽�ط������ý��뽥��Ч����ֵ ��
		menu.attachToActivity(activity, SlidingMenu.SLIDING_CONTENT); // �Ѳ໬����������ǰ��Activity
		menu.setMenu(R.layout.left_menu); // ���õ�ǰ����ͼ
		menu.setMode(SlidingMenu.LEFT);
		WindowManager wManager = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
		long screenWidth = wManager.getDefaultDisplay().getWidth();// ��ȡ��Ļ�Ŀ��
		menu.setBehindWidth((int) (screenWidth * 0.7));// ������ҳ�Ŀ��
		
		// ʹ��DisplayImageOptions.Builder()����DisplayImageOptions

		left_user_cover = (practiceschool.circlepic.CircularImage) menu.getMenu().findViewById(R.id.left_user_cover); 
		left_user_name = (TextView) menu.getMenu().findViewById(R.id.left_user_name);
		left_user_rank = (TextView) menu.getMenu().findViewById(R.id.left_user_rank);	
		left_user_name.setText(subStr(Constants.app_username));
		left_user_rank.setText("�ȼ���"+Constants.app_userrankText);
		Constants.imageLoader.cancelDisplayTask(left_user_cover);
		Constants.imageLoader.displayImage(Constants.app_userpic, left_user_cover, Constants.options, Constants.animateFirstListener);
		
		//���˵����¼�
		left_user_btn = (LinearLayout) menu.getMenu().findViewById(R.id.left_user_btn);
		left_tutor_btn = (LinearLayout) menu.getMenu().findViewById(R.id.left_tutor_btn);
		left_time_btn = (LinearLayout) menu.getMenu().findViewById(R.id.left_time_btn);
		left_write_btn = (LinearLayout) menu.getMenu().findViewById(R.id.left_write_btn);
		left_contact_btn = (LinearLayout) menu.getMenu().findViewById(R.id.left_black_btn);
		left_myuser = (RelativeLayout) menu.getMenu().findViewById(R.id.left_myuser);
		
		left_user_btn.setOnClickListener(new MyOnClickListener(0, true,activity));
		left_time_btn.setOnClickListener(new MyOnClickListener(1, true,activity));
		left_tutor_btn.setOnClickListener(new MyOnClickListener(2, true,activity));
		left_write_btn.setOnClickListener(new MyOnClickListener(3, true,activity));
		left_contact_btn.setOnClickListener(new MyOnClickListener(4, true,activity));
		left_myuser.setOnClickListener(new MyOnClickListener(5, true,activity));

		
		//��ఴťѡ����ʽ
		if(type.equals("user")){
			left_user_btn.setBackgroundColor(0XEEEEEEEE);
		}else if(type.equals("tutor")){
			left_tutor_btn.setBackgroundColor(0XEEEEEEEE);
		}else if(type.equals("time")){
			left_time_btn.setBackgroundColor(0XEEEEEEEE);
		}else if(type.equals("write")){
			left_write_btn.setBackgroundColor(0XEEEEEEEE);
		}else if(type.equals("contact")){
			left_contact_btn.setBackgroundColor(0XEEEEEEEE);
		}
	
		
	}

	
	/**
	 * ��дOnClickListener����Ӧ��������ҪĿ�ľ���ʵ�ֵ��titleʱ��pager�������Ӧ�л�
	 */
	public static class MyOnClickListener implements OnClickListener {
		private int index;
		private boolean isMenuBtn;
		private Activity activity;

		public MyOnClickListener(int index, boolean isMenubtn,Activity activity) {
			this.index = index;
			this.isMenuBtn = isMenubtn;
			this.activity = activity;
		}

		@Override
		public void onClick(View v) {
			if (isMenuBtn) {
				switch (index) {
				case 0:
					Intent intent = new Intent();
					intent.setClass(activity, UsercenterActivity.class);
					activity.startActivity(intent);
					activity.finish();
					break;
				case 1:
					Intent intent2 = new Intent();
					intent2.setClass(activity, DiscoveryActivity.class);	
					activity.startActivity(intent2);
					activity.finish();
					break; 
				case 2:
					Intent intent3 = new Intent();
					intent3.setClass(activity, TutorListActivity.class);	
					activity.startActivity(intent3);
					activity.finish();
					break;
				case 3:
					Intent intent4 = new Intent();
					intent4.setClass(activity, WritingActivity.class);	
					activity.startActivity(intent4);
					activity.finish();
					break;
				case 4:
					Intent intent5 = new Intent();
					intent5.setClass(activity, ContactActivity.class);	
					activity.startActivity(intent5);
					activity.finish();
					break;
				case 5:
					Intent intent6 = new Intent();
					intent6.setClass(activity, UsercenterActivity.class);
					activity.startActivity(intent6);
					activity.finish();
					break;
				default:
					break;
				}
			} else {
				((DiscoveryActivity) activity).setCurrentPage(index);
			}
		}
	}

	/**
	 * ��ȡ�û����Ƴ���
	 */
	public static String subStr(String str){
		if(str.length()>10){
			str = str.substring(0,10)+"...";
		}
		return str;
	}


}
