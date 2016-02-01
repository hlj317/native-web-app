package practiceschool.swipebacklayout;

import com.example.practiceschool.R;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;

/**
 * @author Yrom
 *
 */
public class SwipeBackActivityHelper {
    private Activity mActivity;
    private SwipeBackLayout mSwipeBackLayout;

    public SwipeBackActivityHelper(Activity activity) {
        mActivity = activity;
    }
    
    @SuppressWarnings("deprecation")
    public void onActivtyCreate(){
    	//��õ���Activity��Window������ɫΪ��ɫ
        mActivity.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        //��õ���Activity��Window�ĸ���ͼDecorView������ɫ��
        mActivity.getWindow().getDecorView().setBackgroundDrawable(null);
        //���ز���
        mSwipeBackLayout = (SwipeBackLayout) LayoutInflater.from(mActivity).inflate(R.layout.swipeback_layout,null);
    }
    /**
     * 
     */
    public void onPostCreate(){
        mSwipeBackLayout.attachToActivity(mActivity);
    }
    
    public View findViewById(int id) {
        if (mSwipeBackLayout != null) {
            return mSwipeBackLayout.findViewById(id);
        }
        return null;
    }
    
    public SwipeBackLayout getSwipeBackLayout() {
        return mSwipeBackLayout;
    }

}
