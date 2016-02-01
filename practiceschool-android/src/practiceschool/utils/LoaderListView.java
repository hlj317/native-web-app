package practiceschool.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

/**
 * 在ScrollView中嵌套ListView，必须重新计算ListView高度
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
		// 根据模式计算每个child的高度和宽度
		int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
		super.onMeasure(widthMeasureSpec, expandSpec);
	}

	// 重新计算ListView高度
	public void setListViewHeightBasedOnChildren(ListView listView) {
		// 获取ListView对应的Adapter
		ListAdapter listAdapter = listView.getAdapter();
		if (listAdapter == null) {
			return;
		}

		int totalHeight = 0;
		for (int i = 0, len = listAdapter.getCount(); i < len; i++) {
			// listAdapter.getCount()返回数据项的数目
			View listItem = listAdapter.getView(i, null, listView);
			// 计算子项View 的宽高
			listItem.measure(0, 0);
			// 统计所有子项的总高度
			totalHeight += listItem.getMeasuredHeight();
		}

		ViewGroup.LayoutParams params = listView.getLayoutParams();
		params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
		// listView.getDividerHeight()获取子项间分隔符占用的高度
		// params.height最后得到整个ListView完整显示需要的高度
		listView.setLayoutParams(params);
	}
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
		//阻止父类拦截事件
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
			// 首先拦截down事件,记录y坐标
			mLastMotionY = y;
			break;
		case MotionEvent.ACTION_MOVE:
			// deltaY > 0 是向下运动,< 0是向上运动
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
							&& Math.abs(top - padding) <= 8) {//这里之前用3可以判断,但现在不行,还没找到原因
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
