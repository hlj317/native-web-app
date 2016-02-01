package practiceschool.pullandrefresh;

import org.apache.http.HttpResponse;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ListView;

public class PullableListView extends ListView implements Pullable {

	public PullableListView(Context context) {
		super(context);
	}

	public PullableListView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public PullableListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public boolean canPullDown() {
		
		try {
			if (getCount() == 0) {
				// 没有item的时候也可以下拉刷新
				return true;
			} else if (getFirstVisiblePosition() == 0 && getChildAt(0).getTop() >= 0) {
				// 滑到ListView的顶部了
				return true;
			} else{
				return false;
			}
				
		} catch (Exception e) {
			return false;
		}
		

	}

	@Override
	public boolean canPullUp() {
		try {
			if (getCount() == 0) {
				// 没有item的时候也可以上拉加载
				return true;
			} else if (getLastVisiblePosition() == (getCount() - 1)) {
				// 滑到底部了
				if (getChildAt(getLastVisiblePosition() - getFirstVisiblePosition()) != null && getChildAt(getLastVisiblePosition() - getFirstVisiblePosition()).getBottom() <= getMeasuredHeight())
					return true;
			}
			return false;
		} catch (Exception e) {
			return false;
		}
	}
}
