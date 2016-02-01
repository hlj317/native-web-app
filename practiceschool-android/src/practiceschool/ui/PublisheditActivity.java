package practiceschool.ui;

import java.io.File;
import java.lang.ref.WeakReference;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

import practiceschool.network.HttpMethod;
import practiceschool.network.UploadUtil;
import practiceschool.network.UploadUtil.OnUploadProcessListener;
import practiceschool.swipebacklayout.SwipeBackActivity;
import practiceschool.utils.Constants;
import practiceschool.utils.ImageUtils;
import com.example.practiceschool.R;
import com.umeng.analytics.MobclickAgent;
import com.umeng.message.PushAgent;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class PublisheditActivity extends SwipeBackActivity implements OnUploadProcessListener{

	private static final String TAG = "zhihu.ui.PublisheditActivity";

	private final int TO_UPLOAD_FILE = 1; // 去上传文件
	private final int UPLOAD_FILE_DONE = 2; // 上传文件响应
	private final int TO_SELECT_PHOTO = 3; // 选择文件
	private final int UPLOAD_INIT_PROCESS = 4; // 上传初始化
	private final int UPLOAD_IN_PROCESS = 5; // 上传中
	private final int LOAD_ARTICLE_SUCCESS = 6; // 加载文章详情成功
	private final int LOAD_ARTICLE_FAILURE = 7; // 加载文章详情失败
	private final int UPDATE_ARTICLE_SUCCESS = 8; // 更新文章成功
	private final int UPDATE_ARTICLE_FAILURE = 9; // 更新文章失败
	private String requestImgUrl = Constants.NODE_URL + "/userEditArticle"; // 请求服务器路径
	private Button publishBtn; // 编辑完发布按钮控件
	private EditText title; // 文章标题（控件）
	private EditText description; // 文章描述（控件）
	private EditText content; // 文章内容（控件）
	private TextView checkstatus; // 审核状态（控件）
	private TextView failcause; // 失败原因（控件）
	private TextView mychannel; // 文章类别（控件）

	private String titleText; // 文章标题
	private String descriptionText; // 文章描述
	private String contentText; // 文章内容
	private String failcauseText; // 文章未通过审核原因
	private int mychannelText; // 文章类别
	private String coverUrl; // 文章封面路径

	private ImageView cover; // 文章封面
	private ProgressDialog progressDialog; // 加载loading对话框
	private String articleid; // 文章ID

	private String targetPath;
	private String avatorpath = Environment.getExternalStorageDirectory() + "/articlecover/";
	private Bitmap bitmap;
	public ImageButton title_move_btn;
	/* 封面名称 */
	private final String IMAGE_FILE_NAME = "xueyuan_" + new Date().getTime() + ".jpg";

	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE); // 声明使用自定义标题
		super.onCreate(savedInstanceState);		
		setContentView(R.layout.publishedit_main);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.publishedit_title); // 自定义标题栏布局赋值

		// 初始化布局控件
		this.initComponent();

		// 初始化加载文章详情数据、点亮评论数据、所有评论数据
		Bundle bundle = this.getIntent().getExtras();
		if (bundle != null) {
			articleid = bundle.getString("articleid");
			loadArticleInfo(articleid); // 加载文章详情数据
		}

		/**
		 * 初始化切换左侧菜单按钮
		 */
		title_move_btn = (ImageButton) findViewById(R.id.title_move_btn);
		title_move_btn.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				Intent intent = new Intent();
				intent.setClass(PublisheditActivity.this, PublishActivity.class);
				PublisheditActivity.this.startActivity(intent);
				PublisheditActivity.this.finish();
			}
		});
		
		//滑动返回
		getSwipeBackLayout();
		
		MobclickAgent.updateOnlineConfig(this);  //友盟统计
		PushAgent.getInstance(this).onAppStart();  //友盟推送
	}

	/**
	 * 加载文章详情
	 */
	private void loadArticleInfo(String articleid) {
		String ajaxUrl = Constants.NODE_URL + "/articleinfo";
		String ajaxData = "articleid=" + articleid;

		// 判断是否有网络
		if (Constants.isNetworkConnected(PublisheditActivity.this)) {
			try {
				new Thread(new articleinfoThread(ajaxUrl, ajaxData)).start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 初始化布局中的控件
	 */
	private void initComponent() {

		cover = (ImageView) findViewById(R.id.cover);
		publishBtn = (Button) findViewById(R.id.publish_btn);
		progressDialog = new ProgressDialog(this);
		title = (EditText) findViewById(R.id.title);
		description = (EditText) findViewById(R.id.description);
		content = (EditText) findViewById(R.id.content);
		checkstatus = (TextView) findViewById(R.id.checkstatus);
		failcause = (TextView) findViewById(R.id.failcause);
		mychannel = (TextView) findViewById(R.id.mychannel);

		// 上传封面
		cover.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				showImagePickDialog();
			}
		});

		// 发布文章
		publishBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {

				titleText = title.getText().toString().trim();
				descriptionText = description.getText().toString().trim();
				contentText = content.getText().toString().trim();
				
				if (titleText.equals("")) {
					Toast.makeText(PublisheditActivity.this, "标题不能为空", Toast.LENGTH_SHORT).show();
				}else if (titleText.length()>25) {
					Toast.makeText(PublisheditActivity.this, "标题不能超过25个字", Toast.LENGTH_SHORT).show();
				}else if (descriptionText.equals("")) {
					Toast.makeText(PublisheditActivity.this, "描述不能为空", Toast.LENGTH_SHORT).show();
				}else if (contentText.equals("")) {
					Toast.makeText(PublisheditActivity.this, "内容不能为空", Toast.LENGTH_SHORT).show();
				}else{				
					// 判断是否有网络
					if (Constants.isNetworkConnected(PublisheditActivity.this)) {
						if ((ImageUtils.picPath != null) || (targetPath != null)) {
							handler.sendEmptyMessage(TO_UPLOAD_FILE);
						} else {
							try {
								String ajaxUrl = Constants.NODE_URL + "/userEditMessage";
								String ajaxData = "articleid=" + articleid + "&title=" + URLEncoder.encode(titleText, "utf-8") + "&description=" + URLEncoder.encode(descriptionText, "utf-8")
										+ "&content=" + URLEncoder.encode(contentText, "utf-8");
								progressDialog.setMessage("正在发布中，请稍后...");
								progressDialog.show();
								new Thread(new UpdateThread(ajaxUrl, ajaxData)).start();
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}				
					
				}

			}
		});

	}

	/**
	 * 获取文章封面
	 */
	public void showImagePickDialog() {

		String title = "获取图片方式";
		String[] choices = new String[] { "拍照", "从手机中选择" };
		new AlertDialog.Builder(this).setTitle(title).setItems(choices, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				switch (which) {
				case 0:
					ImageUtils.openCameraImage(PublisheditActivity.this);
					break;
				case 1:
					ImageUtils.openLocalImage(PublisheditActivity.this);
					break;
				}
			}
		}).setNegativeButton("返回", null).show();
	}

	/**
	 * 拍照、从手机中选择图片，返回码处理
	 */
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		// 如果返回码是取消，则返回
		if (resultCode == RESULT_CANCELED) {
			return;
		}

		switch (requestCode) {
		// 拍照获取图片
		case ImageUtils.GET_IMAGE_BY_CAMERA:
			// uri传入与否影响图片获取方式,以下二选一
			// 方式一,自定义Uri(ImageUtils.imageUriFromCamera),用于保存拍照后图片地址
			if (ImageUtils.imageUriFromCamera != null) {
				showImage(ImageUtils.imageUriFromCamera);
			}

			break;
		// 手机相册获取图片
		case ImageUtils.GET_IMAGE_FROM_PHONE:
			if (data != null && data.getData() != null) {
				showImage(data.getData());
			}
			break;
		// 裁剪图片后结果
		case ImageUtils.CROP_IMAGE:
			if (ImageUtils.cropImageUri != null) {

				File fileDir = new File(avatorpath);
				if (!fileDir.exists()) {
					fileDir.mkdirs();// 创建文件夹
				}
				targetPath = avatorpath + IMAGE_FILE_NAME;

				ImageUtils.compressBitmap(ImageUtils.picPath, targetPath, 640); // 压缩
				bitmap = ImageUtils.decodeBitmap(targetPath, 100); // 分解
				cover.setImageBitmap(bitmap); // 显示

			}
			break;
		default:
			break;
		}
	}

	/**
	 * 显示拍照/图库中的图片
	 */
	private void showImage(Uri imageUriFromCamera) {
		File fileDir = new File(avatorpath);
		if (!fileDir.exists()) {
			fileDir.mkdirs();// 创建文件夹
		}
		targetPath = avatorpath + IMAGE_FILE_NAME;
		ImageUtils.picPath = ImageUtils.getRealPathFromURI(imageUriFromCamera, PublisheditActivity.this);
		ImageUtils.compressBitmap(ImageUtils.picPath, targetPath, 640); // 压缩
		bitmap = ImageUtils.decodeBitmap(targetPath, 100); // 分解
		cover.setImageBitmap(bitmap); // 显示
	}

	/**
	 * 提交用户信息到服务器
	 */
	private void toUploadFile() {
		// uploadImageResult.setText("正在上传中...");
		progressDialog.setMessage("正在发布中，请稍后...");
		progressDialog.show();
		String fileKey = "upload";
		UploadUtil uploadUtil = UploadUtil.getInstance();
		uploadUtil.setOnUploadProcessListener(this); // 设置监听器监听上传状态
		Map<String, String> params = new HashMap<String, String>();
		params.put("articleid", articleid);
		params.put("title", titleText);
		params.put("content", contentText);
		params.put("description", descriptionText);

		// 开一个子线程，做请求处理
		uploadUtil.uploadFile(targetPath, fileKey, requestImgUrl, params);
	}

	/**
	 * 异步消息回调处理器
	 */
	static class MyHandler extends Handler {
		WeakReference<Activity> mActivityReference;

		MyHandler(Activity activity) {
			mActivityReference = new WeakReference<Activity>(activity);
		}
	}

	private Handler handler = new MyHandler(this) {
		@Override
		public void handleMessage(Message msg) {
			final Activity activity = mActivityReference.get();
			if (activity != null) {
				switch (msg.what) {
				case TO_UPLOAD_FILE:
					toUploadFile();
					break;
				case UPLOAD_INIT_PROCESS:
					// progressBar.setMax(msg.arg1);
					break;
				case UPLOAD_IN_PROCESS:
					// progressBar.setProgress(msg.arg1);
					break;
				case UPLOAD_FILE_DONE:
					// String result = "响应码：" + msg.arg1 + "\n响应信息：" + msg.obj +
					// "\n耗时：" + UploadUtil.getRequestTime() + "秒";
					ImageUtils.picPath = null;
					udpateSuccss();
					break;
				case LOAD_ARTICLE_SUCCESS:
					try {
						renderInfo((JSONObject) msg.obj);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				case LOAD_ARTICLE_FAILURE:
					break;
				case UPDATE_ARTICLE_SUCCESS:
					try {
						udpateArticle((JSONObject) msg.obj);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				case UPDATE_ARTICLE_FAILURE:
					break;
				default:
					break;
				}
			}
			super.handleMessage(msg);
		}

	};

	/**
	 * 文章更新成功后的动作
	 */
	private void udpateSuccss() {
		Toast.makeText(PublisheditActivity.this, "文章发布成功，请等待审核...", Toast.LENGTH_SHORT).show();
		Intent intent = new Intent();
		intent.setClass(PublisheditActivity.this, PublishActivity.class);
		PublisheditActivity.this.startActivity(intent);
		PublisheditActivity.this.finish();
	}

	/**
	 * 文章更新成功(不带封面)
	 */
	private void udpateArticle(JSONObject data) throws JSONException {
		int resultCode = (Integer) data.getInt("resultCode");
		if (resultCode == 1000) {
			udpateSuccss();
		} else if (resultCode == 0) {
			Toast.makeText(PublisheditActivity.this, "更新失败", Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * 数据服务器读取成功后，渲染文章详情
	 */
	private void renderInfo(JSONObject data) throws JSONException {

		int resultCode = (Integer) data.getInt("resultCode");
		if (resultCode == 1000) {

			JSONObject infoData = (JSONObject) data.get("articleinfo");
			titleText = (String) infoData.getString("title");
			descriptionText = (String) infoData.getString("description");
			contentText = (String) infoData.getString("content");
			title.setText(titleText);
			description.setText(descriptionText);
			content.setText(contentText);

			checkstatus.setText((String) infoData.getString("checkstatus"));
			failcauseText = (String) infoData.getString("failcause");
			mychannelText = (Integer) infoData.getInt("channel");
			if (failcauseText.equals("")) {
				failcause.setVisibility(View.GONE);
			} else {
				failcause.setVisibility(View.VISIBLE);
				failcause.setText("未通过原因：" + failcauseText);
			}

			if (mychannelText == 1) {
				mychannel.setText("类别：健身无敌");
			} else if (mychannelText == 2) {
				mychannel.setText("类别：热血泡妞");
			} else if (mychannelText == 3) {
				mychannel.setText("类别：有什么要问");
			} else if (mychannelText == 4) {
				mychannel.setText("类别：职场之道");
			}

			/**
			 * 显示图片 参数1：图片url 参数2：显示图片的控件 参数3：显示图片的设置 参数4：监听器
			 */
			coverUrl = (String) infoData.getString("cover");
			Constants.imageLoader.displayImage(coverUrl, cover, Constants.options_big, Constants.animateFirstListener);

		}
	};

	/**
	 * 上传响应，实现OnUploadProcessListener接口
	 */
	public void onUploadDone(int responseCode, String message) {
		progressDialog.dismiss();
		Message msg = Message.obtain();
		msg.what = UPLOAD_FILE_DONE;
		msg.arg1 = responseCode;
		msg.obj = message;
		handler.sendMessage(msg);
	}

	/**
	 * 上传中，实现OnUploadProcessListener接口
	 */
	public void onUploadProcess(int uploadSize) {
		Message msg = Message.obtain();
		msg.what = UPLOAD_IN_PROCESS;
		msg.arg1 = uploadSize;
		handler.sendMessage(msg);
	}

	/**
	 * 准备上传，实现OnUploadProcessListener接口
	 */
	public void initUpload(int fileSize) {
		Message msg = Message.obtain();
		msg.what = UPLOAD_INIT_PROCESS;
		msg.arg1 = fileSize;
		handler.sendMessage(msg);
	}

	/**
	 * 加载文章详情线程
	 */
	class articleinfoThread implements Runnable {

		private String ajaxUrl;
		private String ajaxData;

		public articleinfoThread(String ajaxUrl, String ajaxData) {
			super();
			this.ajaxUrl = ajaxUrl;
			this.ajaxData = ajaxData;
		}

		public void run() {
			Looper.prepare();
			final String result = HttpMethod.loginByPost(ajaxUrl, ajaxData);
			Message msg = Message.obtain();
			if (result != null) {
				try {
					JSONObject data = new JSONObject(result);
					msg.what = LOAD_ARTICLE_SUCCESS;
					msg.obj = data;
					handler.sendMessage(msg);
				} catch (JSONException e) {
					msg.what = LOAD_ARTICLE_FAILURE;
					handler.sendMessage(msg);
				}
			} else {
				Toast.makeText(PublisheditActivity.this, "网络异常，请检查您的网络", Toast.LENGTH_SHORT).show();
			}
			Looper.loop();
		}
	}

	/**
	 * 更新文章线程(不带封面)
	 */
	class UpdateThread implements Runnable {

		private String ajaxUrl;
		private String ajaxData;

		public UpdateThread(String ajaxUrl, String ajaxData) {
			super();
			this.ajaxUrl = ajaxUrl;
			this.ajaxData = ajaxData;
		}

		public void run() {
			Looper.prepare();
			final String result = HttpMethod.loginByPost(ajaxUrl, ajaxData);
			Message msg = Message.obtain();
			if (result != null) {
				try {
					JSONObject data = new JSONObject(result);
					msg.what = UPDATE_ARTICLE_SUCCESS;
					msg.obj = data;
					handler.sendMessage(msg);
				} catch (JSONException e) {
					msg.what = UPDATE_ARTICLE_FAILURE;
					handler.sendMessage(msg);
				}
			} else {
				Toast.makeText(PublisheditActivity.this, "网络异常，请检查您的网络", Toast.LENGTH_SHORT).show();
			}
			Looper.loop();
		}
	}
	

	@Override
	public void onStart() {
		super.onStart();
	}
	
	@Override
	public void onResume() {
		Log.v(TAG, "OnResume");
		super.onResume();
		MobclickAgent.onResume(this);  //友盟统计
	}

	@Override
	public void onPause() {
		Log.v(TAG, "OnPause unregister progress receiver");
		super.onPause();
		MobclickAgent.onPause(this);  //友盟统计
	}

	public void onStop() {
		Log.v(TAG, "OnStop");
		super.onStop();
	}

	public void onDestroy() {
		Log.v(TAG, "Destroy");
		super.onDestroy();
	}

}
