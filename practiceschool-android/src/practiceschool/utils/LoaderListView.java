package practiceschool.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

/**
 * ��ScrollView��Ƕ��ListView���������¼���ListView�߶�
 */
public class LoaderListView extends ListView{
	
	/**
	 * pull state,pull up or pull down;PULL_UP_STATE or PULL_DOWN_STATE
	 */	
	int mLastMotionY ;
	boolean bottomFlag;
	
	public LoaderListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public LoaderListView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public LoaderListView(Context context) {
		super(context);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// ����ģʽ����ÿ��child�ĸ߶ȺͿ��
		int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
		super.onMeasure(widthMeasureSpec, expandSpec);
	}

	// ���¼���ListView�߶�
	public void setListViewHeightBasedOnChildren(ListView listView) {
		// ��ȡListView��Ӧ��Adapter
		ListAdapter listAdapter = listView.getAdapter();
		if (listAdapter == null) {
			return;
		}

		int totalHeight = 0;
		for (int i = 0, len = listAdapter.getCount(); i < len; i++) {
			// listAdapter.getCount()�������������Ŀ
			View listItem = listAdapter.getView(i, null, listView);
			// ��������View �Ŀ��
			listItem.measure(0, 0);
			// ͳ������������ܸ߶�
			totalHeight += listItem.getMeasuredHeight();
		}

		ViewGroup.LayoutParams params = listView.getLayoutParams();
		params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
		// listView.getDividerHeight()��ȡ�����ָ���ռ�õĸ߶�
		// params.height���õ�����ListView������ʾ��Ҫ�ĸ߶�
		listView.setLayoutParams(params);
	}
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
		//��ֹ���������¼�
		if(bottomFlag){
			getParent().requestDisallowInterceptTouchEvent(true);
		}
		
		return super.onInterceptTouchEvent(ev);
	}
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
		int y = (int) ev.getRawY();
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			// ��������down�¼�,��¼y����
			mLastMotionY = y;
			break;
		case MotionEvent.ACTION_MOVE:
			// deltaY > 0 �������˶�,< 0�������˶�
			int deltaY = y - mLastMotionY;
			
			if(deltaY>0){
				View child = getChildAt(0);
				if(child!=null){
					
				
					if (getFirstVisiblePosition() == 0
							&& child.getTop() == 0) {
							bottomFlag = false;
							getParent().requestDisallowInterceptTouchEvent(false); 
						}
					
					int top = child.getTop();
					int padding = getPaddingTop();
					if (getFirstVisiblePosition() == 0
							&& Math.abs(top - padding) <= 8) {//����֮ǰ��3�����ж�,�����ڲ���,��û�ҵ�ԭ��
							bottomFlag = false;
							getParent().requestDisallowInterceptTouchEvent(false); 
				
						}
				}
			}
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			break;
		}	
	
		return super.onTouchEvent(ev);
	}
	
	public void setBottomFlag(boolean flag){
		bottomFlag = flag;
	}

}
