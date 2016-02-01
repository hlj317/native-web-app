package practiceschool.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

/**
 * 为ScrollView增加下拉到底的监听事件
 */
public class MyScrollView extends ScrollView {
	

	// private ScrollViewListener scrollViewListener = null;

	public interface OnGetBottomListener {
		public void onBottom();
	}

	private OnGetBottomListener onGetBottomListener;

	public MyScrollView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public MyScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public MyScrollView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onScrollChanged(int x, int y, int oldx, int oldy) {
		super.onScrollChanged(x, y, oldx, oldy);

		// if(scrollViewListener != null) {
		// scrollViewListener.onScrollChanged(this, x, y, oldx, oldy);
		// }
		// 到底部，给listview放松消息，让其获取触摸事件
		if (getChildCount() >= 1 && getHeight() + getScrollY() == getChildAt(getChildCount() - 1).getBottom()) {
			onGetBottomListener.onBottom();
		}
	}

	public interface ScrollViewListener {
		void onScrollChanged(MyScrollView scrollView, int x, int y, int oldx, int oldy);
	}

	public void setBottomListener(OnGetBottomListener listener) {
		onGetBottomListener = listener;
	}

}