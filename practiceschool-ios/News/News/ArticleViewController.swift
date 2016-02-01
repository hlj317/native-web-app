//
//  ArticleViewController.swift
//  News
//
//  Created by huanglijun on 15/8/2.
//  Copyright (c) 2015年 huanglijun. All rights reserved.
//

import UIKit

@available(iOS 8.0, *)
class ArticleViewController: UIViewController,UITableViewDataSource,UITableViewDelegate,UITextViewDelegate,UITextFieldDelegate,UIGestureRecognizerDelegate,UIActionSheetDelegate,WXApiDelegate{

    let screenWidth = UIScreen.mainScreen().bounds.size.width;      //屏幕宽度
    let screenHeight = UIScreen.mainScreen().bounds.size.height;    //屏幕高度
    var titleView : UIView?;   //顶部标题栏
    var returnBtn : UIButton?;  //返回按钮
    var collectBtn : UIButton?;  //收藏按钮
    var collectNum : UILabel?;   //收藏数量
    var collectNumText : Int!;   //点赞数量
    var praizeBtn : UIButton?;   //点赞按钮
    var praizeNum : UILabel?;    //点赞数量
    var praizeNumText : Int!;    //点赞数量
    var shareBtn : UIButton?;    //分享按钮
    var articleContent : UILabel = UILabel();    //文章内容
    var articleTitle : UILabel = UILabel();    //文章标题
    var authorCover : UIImageView = UIImageView(); //作者头像视图
    var authorCoverImg : UIImage?;  //作者头像图片
    var authorname : UILabel = UILabel();    //作者昵称
    var authorid : String?;         //作者ID号
    var authorSex = "boy";        //作者性别
    var authorSexPic : UIImageView = UIImageView(); //作者性别视图
    var authorSexPicImg : UIImage = UIImage();  //作者性别图片
    var articleTime : UILabel = UILabel();   //文章发布时间
    var articleCover : UIImageView = UIImageView();  //文章封面视图
    var articleCoverImg : UIImage = UIImage();   //文章封面图片
    var titleUnderline : UIView?;   //标题下划线
    var requestArtUrl = "http://120.55.99.230/articleinfo";  //文章请求地址
    var loading:UIActivityIndicatorView?;  //定义loading
    var articleid = NSString();  //获取传过来的值,文章ID号
    var articleItem = ArticleItem();   //创建一个文章模型对象
    var reviewList = UITableView();  //评论列表
    var headerView:UIView?;  //顶部视图
    var reviewNum : UILabel?; //所有评论数量
    var reviewTotalText : Int!;  //所有评论数量
    var reviewTopline : UIView?;  //内容分隔线
    var headerHeight : CGFloat?;  //头部高度;
    
    var dataSource = [];  //评论列表数据源
    var currentNewsDataSource = NSMutableArray();  //创建多维数组对象
    var requestReviewUrl = "http://120.55.99.230/artreviewlist";  //评论列表请求地址
    var userPicView : UIImageView?;  //评论用户头像视图
    var userName : UILabel?;     //评论用户昵称
    var userSex : String?;       //评论用户性别
    var userSexView : UIImageView?;  //评论用户性别视图
    var userSexImg : UIImage?;  //评论用户性别图片
    var reviewTime : UILabel?;  //用户评论时间
    var lightCount : UILabel?;  //用户点亮次数
    var reviewContent : UILabel?;  //用户评论内容
    var reviewsTotal : Int?;  //用户评论数
    var pagestart:Int = 0;    //评论起始分页
    let pagesize:Int = 10;    //评论每页加载条数
    
    var reviewButtomView : UIView?;  //底部评论视图
    var reviewTextField : UITextField?;  //底部评论文本框
    var reviewSendBtn : UIButton?;  //底部发送评论按钮
    var sendLoading:UIActivityIndicatorView?;  //评论按钮loading
    var isPraized = false; //是否已点赞
    var isCollected = false; //是否已收藏
    
    var HUD : MBProgressHUD?;  //弱提示对话框
    
    /**
    * 当前用户信息
    */
    var me_userid = "";    // 用户ID号
    var me_username = "";  // 用户昵称
    var me_usersex = ""    // 用户性别
    var me_userpic = "";   // 用户头像

    override func viewDidLoad() {
        super.viewDidLoad();
        self.view.backgroundColor = UIColor.whiteColor();
        //创建标题栏
        setupTitle();
        
        //创建评论列表
        setupReviewList();
        
        //创建弱提示对话框
        setupHUD();
        
        headerView = UIView();
        
        // 定义一个uploading
        loading = UIActivityIndicatorView(activityIndicatorStyle: UIActivityIndicatorViewStyle.White)
        loading!.frame = CGRectMake(screenWidth/2 - 50, screenHeight/2 - 50, 100, 100)
        loading!.hidesWhenStopped = true
        loading!.color = UIColor.blackColor()
        self.view.addSubview(loading!)

        //兼容IOS7以上，去掉scrollview顶部的空白部分
        if (self.respondsToSelector("setEdgesForExtendedLayout:")){
            self.edgesForExtendedLayout = UIRectEdge.None;
        }
    
        //加载文章数据
        loadArticle();
        
        //创建底部评论栏
        setupReviewView();
        
        //回复滑动返回效果
        //self.navigationController!.interactivePopGestureRecognizer.delegate = self;
        
        //调用微信接口
        WXApi.registerApp("wxf8c2bf5f7edb595f");
       
     
    }
    
    //兼容滑动返回不灵活的问题
//    func gestureRecognizer(gestureRecognizer: UIGestureRecognizer, shouldRecognizeSimultaneouslyWithGestureRecognizer otherGestureRecognizer: UIGestureRecognizer) -> Bool{
//        return true;
//    }
//   
//    func gestureRecognizer(gestureRecognizer: UIGestureRecognizer, shouldBeRequiredToFailByGestureRecognizer otherGestureRecognizer: UIGestureRecognizer) -> Bool{
//    
//        return gestureRecognizer.isKindOfClass(UIScreenEdgePanGestureRecognizer);
//    }

    
    //创建弱视图提示对话框
    func setupHUD(){
        HUD = MBProgressHUD(view: self.view);
        self.view.addSubview(HUD!);
        HUD!.dimBackground = true;
    }
    
    //创建标题栏
    func setupTitle(){
    
        //标题view
        titleView = UIView();
        titleView!.frame = CGRectMake(0,20,screenWidth,40);
        self.view.addSubview(titleView!);
        
        //分享按钮
        shareBtn = UIButton();
        shareBtn!.setBackgroundImage(UIImage(named: "title_menu.png"), forState: UIControlState.Normal);
        shareBtn!.setBackgroundImage(UIImage(named: "title_menu_high.png"), forState: UIControlState.Highlighted);
        shareBtn!.frame = CGRectMake(screenWidth - 35,7,25,25);
        shareBtn!.addTarget(self, action: "shareAction:", forControlEvents: UIControlEvents.TouchUpInside)
        titleView!.addSubview(shareBtn!);
        
        //点赞数量
        praizeNum = UILabel();
        praizeNum!.font = UIFont(name:praizeNum!.font.fontName,size:10.0)
        praizeNum!.text = "125";
        praizeNum!.textColor = UIColor(red: 153.0/255, green: 153.0/255, blue: 153.0/255, alpha: 1.0);
        praizeNum!.frame = CGRectMake(screenWidth - 75,12,25,16);
        titleView!.addSubview(praizeNum!);
        
        //点赞按钮
        praizeBtn = UIButton();
        praizeBtn!.setBackgroundImage(UIImage(named: "praise_btn_grey.png"), forState: UIControlState.Normal);
        praizeBtn!.setBackgroundImage(UIImage(named: "praise_btn_grey_high.png"), forState: UIControlState.Highlighted);
        praizeBtn!.frame = CGRectMake(screenWidth - 95,11,18,18);
        praizeBtn!.addTarget(self, action: "praizeAction:", forControlEvents: UIControlEvents.TouchUpInside)
        titleView!.addSubview(praizeBtn!);
        
        //收藏数量
        collectNum = UILabel();
        collectNum!.font = UIFont(name:collectNum!.font.fontName,size:10.0)
        collectNum!.text = "425";
        collectNum!.textColor = UIColor(red: 153.0/255, green: 153.0/255, blue: 153.0/255, alpha: 1.0);
        collectNum!.frame = CGRectMake(screenWidth - 140,12,25,16);
        titleView!.addSubview(collectNum!);
        
        //收藏按钮
        collectBtn = UIButton();
        collectBtn!.setBackgroundImage(UIImage(named: "collect_btn_grey.png"), forState: UIControlState.Normal);
        collectBtn!.setBackgroundImage(UIImage(named: "collect_btn_grey_high.png"), forState: UIControlState.Highlighted);
        collectBtn!.frame = CGRectMake(screenWidth - 160,11,18,18);
        collectBtn!.addTarget(self, action: "collectAction:", forControlEvents: UIControlEvents.TouchUpInside)
        titleView!.addSubview(collectBtn!);
        
        
        //返回按钮
        returnBtn = UIButton();
        returnBtn!.setBackgroundImage(UIImage(named: "title_move_btn.png"), forState: UIControlState.Normal);
        returnBtn!.setBackgroundImage(UIImage(named: "title_move_btn_high.png"), forState: UIControlState.Highlighted);
        returnBtn!.frame = CGRectMake(10,7,25,25);
        returnBtn!.addTarget(self, action: "returnAction:", forControlEvents: UIControlEvents.TouchUpInside)
        titleView!.addSubview(returnBtn!);
        
        
        //标题下划线
        titleUnderline = UIView();
        titleUnderline!.backgroundColor = UIColor(red: 238.0/255, green:238.0/255, blue: 238.0/255, alpha: 1.0);
        titleUnderline!.frame = CGRectMake(0,39,screenWidth,1);
        titleView!.addSubview(titleUnderline!);
    
    }
    
    //点击收藏按钮触发回调函数
    func collectAction(sender: UIButton){
        
        if((Constants["isLogin"]) == false){
            let loginPage = loginViewController(nibName:"loginViewController",bundle:nil);
            let navLoginController = UINavigationController(rootViewController:loginPage);
            self.presentViewController(navLoginController, animated: true, completion: nil)
            return;
        }
        
        //获取用户信息
        me_userid = Constants["app_userid"] as! String;
        me_username = Constants["app_username"] as! String;
        me_usersex = Constants["app_usersex"] as! String;
        me_userpic = Constants["app_userpic"] as! String;
        
        let requestStoreUrl = "http://120.55.99.230/store";  //收藏请求地址
        
        //如果收藏为空，不能发送
        if (self.isCollected == true){
            return;
        }
        
        //请求参数
        let paramDict = ["articleid":articleid,"userid":me_userid];
        
        //创建一个http请求实例，这里用第三方oc库AFNetworking
        let afManager = AFHTTPRequestOperationManager()
        let op =  afManager.POST(requestStoreUrl,
            parameters:paramDict,
            success: {  (operation: AFHTTPRequestOperation!,   //回调函数
                responseObject: AnyObject!) in
                
                //请求成功返回的json
                let json = (try! NSJSONSerialization.JSONObjectWithData(responseObject as! NSData, options: NSJSONReadingOptions.MutableContainers)) as! NSDictionary
                
                //获取一组数据
                
                let newsCode = json["resultCode"] as! NSInteger;
                
                if(newsCode == 1000){
      
                    dispatch_async(dispatch_get_main_queue(), {
                        self.collectBtn!.setBackgroundImage(UIImage(named: "collect_btn_red"), forState: UIControlState.Normal);
                        self.isCollected = true;
                        
                        self.collectNumText! += 1;

                        self.collectNum!.text = String(self.collectNumText);
                        
                        self.HUD!.labelText = "收藏成功";
                        //加这句代码，表示纯文本提示，默认则是有loading框；
                        self.HUD!.mode = MBProgressHUDMode.Text;
                        self.HUD!.showAnimated(true, whileExecutingBlock: { () -> Void in
                            sleep(1);
                            })
                        
                    })
                    
                }else{
                    dispatch_async(dispatch_get_main_queue(), {
                        //这里返回主线程，写需要主线程执行的代码
                    })
                }
                
            },
            failure: {  (operation: AFHTTPRequestOperation!,
                error: NSError!) in
                print("post请求失败-error: \(error.localizedDescription)")
                dispatch_async(dispatch_get_main_queue(), {
                    //这里返回主线程，写需要主线程执行的代码
                })
        })
        //设置http请求返回格式
        op!.responseSerializer = AFHTTPResponseSerializer()
        op!.start()

        //HUD!.showAnimated(true, whileExecutingBlock: nil);
        
    }
    
    //点击点赞按钮触发回调函数
    func praizeAction(sender: UIButton){
        
        if((Constants["isLogin"]) == false){
            let loginPage = loginViewController(nibName:"loginViewController",bundle:nil);
            let navLoginController = UINavigationController(rootViewController:loginPage);
            self.presentViewController(navLoginController, animated: true, completion: nil)
            return;
        }
        
        //获取用户信息
        me_userid = Constants["app_userid"] as! String;
        me_username = Constants["app_username"] as! String;
        me_usersex = Constants["app_usersex"] as! String;
        me_userpic = Constants["app_userpic"] as! String;
        
        let requestPraizeUrl = "http://120.55.99.230/praise";  //点赞请求地址
        
        //如果点赞为空，不能发送
        if (self.isPraized == true){
            return;
        }
        
        //请求参数
        let paramDict = ["articleid":articleid,"userid":me_userid];
        
        //创建一个http请求实例，这里用第三方oc库AFNetworking
        let afManager = AFHTTPRequestOperationManager()
        let op =  afManager.POST(requestPraizeUrl,
            parameters:paramDict,
            success: {  (operation: AFHTTPRequestOperation!,   //回调函数
                responseObject: AnyObject!) in
                
                //请求成功返回的json
                let json = (try! NSJSONSerialization.JSONObjectWithData(responseObject as! NSData, options: NSJSONReadingOptions.MutableContainers)) as! NSDictionary
                
                //获取一组数据
                
                let newsCode = json["resultCode"] as! NSInteger;
                
                if(newsCode == 1000){
                    
                    dispatch_async(dispatch_get_main_queue(), {
                        self.praizeBtn!.setBackgroundImage(UIImage(named: "praise_btn_red"), forState: UIControlState.Normal);
                        self.isPraized = true;
                        
                        self.praizeNumText! += 1;
                        
                        self.praizeNum!.text = String(self.praizeNumText);
                        
                        self.HUD!.labelText = "点赞成功";
                        //加这句代码，表示纯文本提示，默认则是有loading框；
                        self.HUD!.mode = MBProgressHUDMode.Text;
                        self.HUD!.showAnimated(true, whileExecutingBlock:  { () -> Void in
                            sleep(1);
                        })
                        
                    })
                    
                }else{
                    dispatch_async(dispatch_get_main_queue(), {
                        //这里返回主线程，写需要主线程执行的代码

                    })
                }
                
            },
            failure: {  (operation: AFHTTPRequestOperation!,
                error: NSError!) in
                print("post请求失败-error: \(error.localizedDescription)")
                dispatch_async(dispatch_get_main_queue(), {
                    //这里返回主线程，写需要主线程执行的代码
                })
        })
        //设置http请求返回格式
        op!.responseSerializer = AFHTTPResponseSerializer()
        op!.start()
        
    }
    
    //点击返回按钮触发回调函数
    func returnAction(sender: UIButton){

        //返回上一级页面
        self.navigationController?.popViewControllerAnimated(true);
        
    }
    
    //分享按钮触发回调函数
    func shareAction(sender: UIButton){
        
        let actionSheet = UIActionSheet(title: "你要做什么", delegate: self, cancelButtonTitle: "取消分享", destructiveButtonTitle:nil,otherButtonTitles: "分享给微信好友","分享到朋友圈","举报该篇文章")
        actionSheet.showInView(self.view)
       
    }
    
    //分享不同场景的回调函数
    func actionSheet(actionSheet: UIActionSheet, clickedButtonAtIndex buttonIndex: Int) {
        if(buttonIndex == actionSheet.cancelButtonIndex){
            return;
        }
        let webpage = WXWebpageObject();
        webpage.webpageUrl = "http://120.55.99.230/articleshare?key=\(articleid)";
        
        let msg = WXMediaMessage();
        msg.mediaObject = webpage;
        let image = UIImage(named:"logo.png");
        let data = UIImageJPEGRepresentation(image!,1.0);
        msg.thumbData = data;
        msg.title = "职场修炼学院";
        msg.description = "想要泡到心仪的MM不？想要更好的身材不？想要向各种厉害的导师学习不？那就赶紧来职场修炼学院吧！";
        //msg.thumbData
        let sendMsg = SendMessageToWXReq();
        sendMsg.message = msg;
        sendMsg.bText = false;

        switch (buttonIndex){
  
            case 0:
                print("");
            case 1:
                print("分享给微信好友");
                sendMsg.scene = 0;
                WXApi.sendReq(sendMsg);
            
            case 2:
                print("分享到朋友圈");
                sendMsg.scene = 1;
                WXApi.sendReq(sendMsg);
            case 3:
                self.HUD!.labelText = "举报成功，会及时处理！";
                //加这句代码，表示纯文本提示，默认则是有loading框；
                self.HUD!.mode = MBProgressHUDMode.Text;
                self.HUD!.showAnimated(true, whileExecutingBlock: { () -> Void in
                    sleep(2);
                })
            default:
                break;
        }

    }
    
    //创建文章
    func setupArticle(){

        //文章标题
        articleTitle.font = UIFont(name:articleTitle.font.fontName,size:20.0);
        //获取title文字
        let articleTitle_text = articleItem.title;
        //设置获取字符串的匹配属性
        let articleTitle_attributes = [NSFontAttributeName: articleTitle.font]
        let articleTitle_option = NSStringDrawingOptions.UsesLineFragmentOrigin;
        //获取字符串范围
        let articleTitle_rect = articleTitle_text.boundingRectWithSize(CGSizeMake(screenWidth-30,500),options: articleTitle_option, attributes: articleTitle_attributes, context: nil);
        let articleTitle_height = articleTitle_rect.size.height;
        articleTitle.text = articleTitle_text as String;
        articleTitle.frame = CGRectMake(15,0,screenWidth-30,articleTitle_height)
        articleTitle.numberOfLines = 0;   //不限行
        articleTitle.sizeToFit();
        headerView!.addSubview(articleTitle);
        
        //作者头像
        authorCover.sd_setImageWithURL(NSURL(string:articleItem.writepic as String)!,placeholderImage: UIImage(named: "loading_small"))
        authorCover.contentMode = UIViewContentMode.ScaleAspectFill;
        authorCover.clipsToBounds = true;
        authorCover.frame = CGRectMake(15,articleTitle_height+10,24,24);
        authorCover.layer.cornerRadius = 12;
        headerView!.addSubview(authorCover);
        
        //作者昵称
        authorname.font = UIFont(name:authorname.font.fontName,size:12.0);
        //获取文字
        let authorname_text = articleItem.writename;
        //设置获取字符串的匹配属性
        let authorname_attributes = [NSFontAttributeName: authorname.font]
        let authorname_option = NSStringDrawingOptions.UsesLineFragmentOrigin;
        //获取字符串范围
        let authorname_rect = authorname_text.boundingRectWithSize(CGSizeMake(screenWidth,20),options: authorname_option, attributes: authorname_attributes, context: nil);
        let authorname_width = authorname_rect.size.width;
        authorname.text = authorname_text as String;
        authorname.frame = CGRectMake(47,articleTitle_height+12,authorname_width,20);
        headerView!.addSubview(authorname);
        
        //作者性别
        authorSex = articleItem.writesex as String;
        if(authorSex == "girl"){
            self.authorSexPicImg = UIImage(named: "girl_icon")!;
            self.authorSexPic.image = self.authorSexPicImg;
        }else{
            self.authorSexPicImg = UIImage(named: "boy_icon")!;
            self.authorSexPic.image = self.authorSexPicImg;
        }
        authorSexPic.frame = CGRectMake(56+authorname_width,articleTitle_height+15,12,12);
        headerView!.addSubview(authorSexPic);
        
        //文章发布时间
        articleTime.font = UIFont(name:articleTime.font.fontName,size:12.0);
        articleTime.text = articleItem.author_time as String;
        articleTime.frame = CGRectMake(screenWidth - 140,articleTitle_height+12,130,20);
        headerView!.addSubview(articleTime);
        
        //文章封面
        articleCover.sd_setImageWithURL(NSURL(string:articleItem.coverpic as String)!,placeholderImage: UIImage(named: "cell_photo.jpg"))
        articleCover.contentMode = UIViewContentMode.ScaleAspectFill;
        articleCover.clipsToBounds = true;
        articleCover.frame = CGRectMake(15,articleTitle_height+45,screenWidth - 30,180);
        headerView!.addSubview(articleCover);
        
        //文章内容
        articleContent.font = UIFont(name:articleContent.font.fontName,size:16.0);
        //获取内容文字
        let articleContent_text = articleItem.content as String;

        //设置获取字符串的匹配属性
        let articleContent_attributes = [NSFontAttributeName: articleContent.font]
        let articleContent_option = NSStringDrawingOptions.UsesLineFragmentOrigin;
        //获取字符串范围
        let articleContent_rect = articleContent_text.boundingRectWithSize(CGSizeMake(screenWidth-30,5000),options: articleContent_option, attributes: articleContent_attributes, context: nil);
        let articleContent_height = articleContent_rect.size.height;
        articleContent.text = articleContent_text as String;
        articleContent.numberOfLines = 0;   //不限行
        articleContent.sizeToFit();
        articleContent.frame = CGRectMake(15,articleTitle_height+235,screenWidth-30,articleContent_height)
        headerView!.addSubview(articleContent);
        
        //内容分隔线
        reviewTopline = UIView();
        reviewTopline!.backgroundColor = UIColor(red: 238.0/255, green:238.0/255, blue: 238.0/255, alpha: 1.0);
        reviewTopline!.frame = CGRectMake(15,articleTitle_height+255+articleContent_height,screenWidth-15,1);
        headerView!.addSubview(reviewTopline!);
        
        //评论数量标题
        reviewNum = UILabel();
        reviewNum!.font = UIFont.boldSystemFontOfSize(CGFloat(18.0));
        reviewTotalText = articleItem.reviewcount as Int;
        if(reviewTotalText == 0){
            reviewNum!.text = "暂无评论(0)"
        }else{
            reviewNum!.text = "所有评论(\(String(reviewTotalText)))"
        }
        reviewNum!.textColor = UIColor(red: 223.0/255, green:49.0/255, blue: 49.0/255, alpha: 1.0);
        reviewNum!.frame = CGRectMake(15,articleTitle_height+266+articleContent_height,screenWidth-15,40);
        headerView!.addSubview(reviewNum!);

        headerHeight = articleTitle_height+308+articleContent_height;
        headerView!.frame = CGRectMake(0,0,screenWidth,headerHeight!);
        self.reviewList.tableHeaderView = headerView;
    }
    
    //加载文章
    func loadArticle() {
        
        //开始加载动画
        loading!.startAnimating();
        
        //请求参数
        let requestParams = ["articleid":articleid];
        
        //创建一个http请求实例，这里用第三方oc库AFNetworking
        let afManager = AFHTTPRequestOperationManager()
        let op =  afManager.POST(requestArtUrl,
            parameters:requestParams,
            success: {  (operation: AFHTTPRequestOperation!,   //回调函数
                responseObject: AnyObject!) in
                
                //请求成功返回的json
                let json = (try! NSJSONSerialization.JSONObjectWithData(responseObject as! NSData, options: NSJSONReadingOptions.MutableContainers)) as! NSDictionary
                
                //获取一组数据
                
                let newsCode = json["resultCode"] as! NSInteger;
                
                if(newsCode == 1000){
                    
                    let infoData = json["articleinfo"] as! NSDictionary;
                    
                    self.articleItem.title = infoData["title"] as! NSString
      
                    self.articleItem.writeid = infoData["writeid"] as! NSString
                    
                    self.articleItem.writename = infoData["writename"] as! NSString
                    
                    self.articleItem.writesex = infoData["writesex"] as! NSString
                    
                    self.articleItem.author_time = infoData["articletime"] as! NSString

                    self.articleItem.praizecount = infoData["praizecount"] as! NSInteger
            
                    self.articleItem.collectcount = infoData["storecount"] as! NSInteger
                    
                    self.articleItem.reviewcount = infoData["reviewcount"] as! NSInteger
       
                    self.articleItem.writepic = infoData["writepic"] as! NSString
                    
                    self.articleItem.coverpic = infoData["cover"] as! NSString
                    
                    self.articleItem.content = infoData["content"] as! NSString
                    
                    self.articleItem.praizeusers = infoData["praizeusers"] as! NSString
                    
                    self.articleItem.storeusers = infoData["storeusers"] as! NSString
          
                    dispatch_async(dispatch_get_main_queue(), {
                        
                        //停止加载动画
                        self.loading!.stopAnimating();
                        //创建文章
                        self.setupArticle();
                        
                        //是否收藏
                        if (self.articleItem.storeusers.rangeOfString(self.me_userid).length > 0) {
                            self.collectBtn!.setBackgroundImage(UIImage(named: "collect_btn_red"), forState: UIControlState.Normal);
                            self.isCollected = true;
                        }
                        
                        //是否点赞
                        if (self.articleItem.praizeusers.rangeOfString(self.me_userid).length > 0) {
                            self.praizeBtn!.setBackgroundImage(UIImage(named: "praise_btn_red"), forState: UIControlState.Normal);
                            self.isPraized = true;
                        }
                        
                        self.collectNumText = self.articleItem.collectcount as Int;
                        self.collectNum!.text = String(self.collectNumText);
                        
                        self.praizeNumText = self.articleItem.praizecount as Int;
                        self.praizeNum!.text = String(self.praizeNumText);
                        
                        
                    })
                }else{
                    dispatch_async(dispatch_get_main_queue(), {
                        
                        //停止加载动画
                        self.loading!.stopAnimating();
                    })
                }
                
            },
            failure: {  (operation: AFHTTPRequestOperation!,
                error: NSError!) in
                print("post请求失败-error: \(error.localizedDescription)")
                dispatch_async(dispatch_get_main_queue(), {
                    
                    //停止加载动画
                    self.loading!.stopAnimating();
                })
        })
        //设置http请求返回格式
        op!.responseSerializer = AFHTTPResponseSerializer()
        op!.start()
    }


    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    //隐藏导航栏
    override func viewWillAppear(animated: Bool) {
        super.viewDidAppear(animated);
        self.navigationController?.setNavigationBarHidden(true, animated: animated);
        //self.navigationController!.interactivePopGestureRecognizer.enabled = true;
        
        //友盟统计
        MobClick.beginLogPageView("Article");
    }
    
    override func viewWillDisappear(animated: Bool) {
        super.viewDidDisappear(animated);
        //self.navigationController?.setNavigationBarHidden(true, animated: animated);
        
        //友盟统计
        MobClick.endLogPageView("Article");
    }
    
    override func viewDidAppear(animated: Bool) {
        super.viewDidAppear(animated);
        //self.navigationController!.interactivePopGestureRecognizer.enabled = true;
    }
    
    
    //创建评论列表
    func setupReviewList(){
        
        self.reviewList.frame = CGRectMake(0,70,screenWidth-15,screenHeight-110);
        self.view.addSubview(self.reviewList);
        
        //创建一个重用的单元格
        self.reviewList.registerClass(UITableViewCell.self, forCellReuseIdentifier: "review_cell");
        
        //去除初始化时的表格线条
        self.reviewList.tableFooterView = UIView();
        
        self.reviewList.showsVerticalScrollIndicator = false;
        
        self.reviewList.delegate = self;
        self.reviewList.dataSource = self;
        
        
//        //列表刷新时调用回调函数
//        self.reviewList.tableView!.addPullToRefreshWithActionHandler { () -> Void in
//            self.loadDataSource();
//        }
//        
//        //列表加载更多时调用回调函数
//        self.reviewList.tableView!.addInfiniteScrollingWithActionHandler { () -> Void in
//            self.loadmoreSource();
//        }
//        
        //初始化评论加载数据源
        loadReviewList()
    
    }
 
    //创建各单元显示内容，该方法是必须实现的方法(创建参数indexPath指定的单元)
    func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        
        //获取单元格对象
        let cell = self.reviewList.dequeueReusableCellWithIdentifier("review_cell", forIndexPath: indexPath) 
        
        //获取每个单元格的数据源，同时转成XHNewsItem类的实例
        let reviewItem = dataSource[indexPath.row] as! ReviewItem
        
        //遍历清除cell中的view
        for currentNews : AnyObject in cell.contentView.subviews{
            currentNews.removeFromSuperview();
        }
        
        //用户头像
        userPicView = UIImageView();
        userPicView!.sd_setImageWithURL(NSURL(string:reviewItem.userpic as String)!,placeholderImage: UIImage(named: "loading_small"))
        userPicView!.contentMode = UIViewContentMode.ScaleAspectFill;
        userPicView!.clipsToBounds = true;
        userPicView!.layer.cornerRadius = 20;
        userPicView!.frame = CGRectMake(15,15,40,40);
    
        //用户昵称
        userName = UILabel();
        userName!.font = UIFont(name:userName!.font.fontName,size:12.0);
        //获取文字
        let userNameText = reviewItem.username as String;
        //设置获取字符串的匹配属性
        let userName_attributes = [NSFontAttributeName: userName!.font]
        let userName_option = NSStringDrawingOptions.UsesLineFragmentOrigin;
        //获取字符串范围
        let userName_rect = userNameText.boundingRectWithSize(CGSizeMake(cell.bounds.size.width - 30,18),options: userName_option, attributes: userName_attributes, context: nil);
        let userName_width = userName_rect.size.width;
        userName!.textColor = UIColor(red: 51.0/255, green: 51.0/255, blue: 51.0/255, alpha: 1.0);
        userName!.text = userNameText;
        userName!.frame = CGRectMake(61,18,userName_width,20);

        //用户性别
        userSex = reviewItem.usersex as String;
        self.userSexView = UIImageView();
        if(userSex == "girl"){
            self.userSexImg = UIImage(named: "girl_icon")!;
            self.userSexView!.image = self.userSexImg;
        }else{
            self.userSexImg = UIImage(named: "boy_icon")!;
            self.userSexView!.image = self.userSexImg;
        }
        userSexView!.frame = CGRectMake(userName_width + 65,22,12,12);
        
        //用户发布时间
        reviewTime = UILabel();
        reviewTime!.font = UIFont(name:reviewTime!.font.fontName,size:12.0);
        let reviewTimeText = reviewItem.reviewtime as String;
        reviewTime!.text = reviewTimeText;
        reviewTime!.textColor = UIColor(red: 153.0/255, green: 153.0/255, blue: 153.0/255, alpha: 1.0);
        reviewTime!.frame = CGRectMake(62,36,120,20);
        
        //用户评论点亮数量
        lightCount = UILabel();
        lightCount!.font = UIFont(name:lightCount!.font.fontName,size:12.0);
        let lightCountText = reviewItem.lightcount as String;
        lightCount!.text = "亮了(\(lightCountText))";
        lightCount!.textColor = UIColor(red: 153.0/255, green: 153.0/255, blue: 153.0/255, alpha: 1.0);
        lightCount!.frame = CGRectMake(cell.bounds.size.width-50,25,50,20);
        lightCount!.userInteractionEnabled = true;
        let singleTap = UITapGestureRecognizer(target: self, action:Selector("lightCountClicked:"));
        lightCount!.addGestureRecognizer(singleTap);
        lightCount!.tag = indexPath.row;
        
        //用户评论内容
        reviewContent = UILabel();
        reviewContent!.font = UIFont(name:reviewContent!.font.fontName,size:16.0);
        //获取文字
        let reviewContentText = reviewItem.reviewcontent as String;
        //设置获取字符串的匹配属性
        let reviewContent_attributes = [NSFontAttributeName: reviewContent!.font]
        let reviewContent_option = NSStringDrawingOptions.UsesLineFragmentOrigin;
        //获取字符串范围
        let reviewContent_rect = reviewContentText.boundingRectWithSize(CGSizeMake(cell.bounds.size.width-30,500),options: reviewContent_option, attributes: reviewContent_attributes, context: nil);
        let reviewContent_height = reviewContent_rect.size.height;
        reviewContent!.text = reviewContentText;
        reviewContent!.textColor = UIColor(red: 51.0/255, green: 51.0/255, blue: 51.0/255, alpha: 1.0);
        reviewContent!.numberOfLines = 0;   //不限行
        reviewContent!.sizeToFit();
        reviewContent!.frame = CGRectMake(15,63,cell.bounds.size.width-30,reviewContent_height);
        
        cell.contentView.addSubview(userPicView!)
        cell.contentView.addSubview(userName!)
        cell.contentView.addSubview(userSexView!)
        cell.contentView.addSubview(reviewTime!)
        cell.contentView.addSubview(lightCount!)
        cell.contentView.addSubview(reviewContent!)
        
        //设置单元格的高度
        reviewItem.contentHeight = reviewContent_height + 78;
        
        return cell

    }
    
    func lightCountClicked(recognizer: UITapGestureRecognizer){
        let row = recognizer.view!.tag as Int;
        let data = self.dataSource[row] as! ReviewItem;
        let reviewid = data.reviewid;
        
        let requestLightUrl = "http://120.55.99.230/lightup";  //点亮请求地址
        
        //请求参数
        let paramDict = ["reviewid":reviewid,"userid":me_userid];
        
        //创建一个http请求实例，这里用第三方oc库AFNetworking
        let afManager = AFHTTPRequestOperationManager()
        let op =  afManager.POST(requestLightUrl,
            parameters:paramDict,
            success: {  (operation: AFHTTPRequestOperation!,   //回调函数
                responseObject: AnyObject!) in
                
                //请求成功返回的json
                let json = (try! NSJSONSerialization.JSONObjectWithData(responseObject as! NSData, options: NSJSONReadingOptions.MutableContainers)) as! NSDictionary
                
                //获取一组数据
                
                let newsCode = json["resultCode"] as! NSInteger;
                
                if(newsCode == 1000){
                    
                    dispatch_async(dispatch_get_main_queue(), {

                       
//                        if let aStatxus = self.dataSource[row] as? NSDictionary{
//                            if let lightcountText = aStatus["lightcount"] as? NSString{
//                                println(lightcountText);
//                            }
//                        }
//            
                        

//                        lightcountText += 1;
//                        
//                        println(self.dataSource[row]["lightcount"]);
//
//                        self.collectNumText! += 1;
//                        
//                        self.collectNum!.text = String(self.collectNumText);
                        
                        self.HUD!.labelText = "点亮成功";
                        //加这句代码，表示纯文本提示，默认则是有loading框；
                        self.HUD!.mode = MBProgressHUDMode.Text;
                        self.HUD!.showAnimated(true, whileExecutingBlock: { () -> Void in
                            sleep(1);
                        })
                        
                    })
                    
                }else if(newsCode == 1001){
                    dispatch_async(dispatch_get_main_queue(), {
                        //这里返回主线程，写需要主线程执行的代码
                    })
                }else{
                    dispatch_async(dispatch_get_main_queue(), {
                        //这里返回主线程，写需要主线程执行的代码
                    })
                }
                
            },
            failure: {  (operation: AFHTTPRequestOperation!,
                error: NSError!) in
                print("post请求失败-error: \(error.localizedDescription)")
                dispatch_async(dispatch_get_main_queue(), {
                    //这里返回主线程，写需要主线程执行的代码
                })
        })
        //设置http请求返回格式
        op!.responseSerializer = AFHTTPResponseSerializer()
        op!.start()
        

    }
    
    
    //返回section的个数
    func numberOfSectionsInTableView(tableView: UITableView) -> Int {
        // #warning Potentially incomplete method implementation.
        // Return the number of sections.
        
        return 1
    }
    
    //返回行数
    func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        // #warning Incomplete method implementation.
        // Return the number of rows in the section.
        
        return dataSource.count;
        
    }
    
    //设置table行高
    func tableView(tableView: UITableView, heightForRowAtIndexPath indexPath: NSIndexPath) -> CGFloat {
        //获取每个单元格的数据源
        let reviewItem = dataSource[indexPath.row] as! ReviewItem;
        let cellHeight = reviewItem.contentHeight;
        return cellHeight;
    }
    
    //选择一行
    func tableView(tableView: UITableView, didSelectRowAtIndexPath indexPath: NSIndexPath){
        
        //var row=indexPath.row as Int
        
        let itReport = UIMenuItem(title: "举报", action: "reportAction:");
        //获取当前单元格对象
        let cell = tableView.cellForRowAtIndexPath(indexPath);

        let menu = UIMenuController.sharedMenuController();
        menu.menuItems = [itReport];
        menu.setTargetRect(cell!.frame,inView:tableView);
        print(cell!.frame);
        print(self.headerHeight!);
        menu.setMenuVisible(true, animated: true);
        tableView.deselectRowAtIndexPath(indexPath,animated:true);
        
    }
    
    //举报回调函数
    func reportAction(sender: UIButton){
    
        self.HUD!.labelText = "举报成功，会及时处理！";
        //加这句代码，表示纯文本提示，默认则是有loading框；
        self.HUD!.mode = MBProgressHUDMode.Text;
        self.HUD!.showAnimated(true, whileExecutingBlock: { () -> Void in
            sleep(1);
        })
    
    }
    
    override func canBecomeFirstResponder() -> Bool{
        return true;
    }
    
    //加载评论列表数据源
    func loadReviewList() {
        
        //请求参数
        let paramDict = ["pagestart":pagestart, "pagesize": pagesize,"articleid":articleid];
        
        //创建一个http请求实例，这里用第三方oc库AFNetworking
        let afManager = AFHTTPRequestOperationManager()
        let op =  afManager.POST(requestReviewUrl,
            parameters:paramDict,
            success: {  (operation: AFHTTPRequestOperation!,   //回调函数
                responseObject: AnyObject!) in
                
                //请求成功返回的json
                let json = (try! NSJSONSerialization.JSONObjectWithData(responseObject as! NSData, options: NSJSONReadingOptions.MutableContainers)) as! NSDictionary
                
                //获取一组数据
                
                let newsCode = json["resultCode"] as! NSInteger;
                self.reviewsTotal = json["total"] as? Int;
                
                if(newsCode == 1000){
                    let newsDataSource = json["docs"] as! NSArray;
  
                    for currentNews : AnyObject in newsDataSource {
                        
                        //创建一个实例对象
                        let reviewItem = ReviewItem();
                        //评论ID号
                        reviewItem.reviewid = currentNews["_id"] as! NSString
                        //用户ID号
                        reviewItem.userid = currentNews["userid"] as! NSString
                        //用户头像url
                        reviewItem.userpic = currentNews["userpic"] as! NSString
                        //用户昵称
                        reviewItem.username = currentNews["username"] as! NSString
                        //用户性别
                        reviewItem.usersex = currentNews["usersex"] as! NSString
                        //用户评论时间
                        reviewItem.reviewtime = currentNews["reviewtime"] as! NSString
                        //用户评论点亮数量
                        reviewItem.lightcount = String(currentNews["lightcount"] as! NSInteger)
                        //用户评论内容
                        reviewItem.reviewcontent = currentNews["reviewcontent"] as! NSString
                        self.currentNewsDataSource.addObject(reviewItem);
                        
                    }
                    
                    dispatch_async(dispatch_get_main_queue(), {
                        //这里返回主线程，写需要主线程执行的代码
                        self.dataSource = self.currentNewsDataSource
                        //获取数据源后，刷新tableView
                        self.reviewList.reloadData()
                        //结束刷新
                        //self.tableView!.pullToRefreshView.stopAnimating();
                    })
                    
                }else{
                    dispatch_async(dispatch_get_main_queue(), {
                        //这里返回主线程，写需要主线程执行的代码
                        //self.tableView!.pullToRefreshView.stopAnimating();
                    })
                }
                
            },
            failure: {  (operation: AFHTTPRequestOperation!,
                error: NSError!) in
                print("post请求失败-error: \(error.localizedDescription)")
                dispatch_async(dispatch_get_main_queue(), {
                    //这里返回主线程，写需要主线程执行的代码
                    //self.tableView!.pullToRefreshView.stopAnimating();
                })
        })
        //设置http请求返回格式
        op!.responseSerializer = AFHTTPResponseSerializer()
        op!.start()
        
    }

    //创建底部评论栏
    func setupReviewView(){
    
        //底部评论视图
        reviewButtomView = UIView();
        reviewButtomView!.frame = CGRectMake(0,screenHeight-40,screenWidth,40);
        reviewButtomView!.backgroundColor = UIColor(red: 245.0/255, green:245.0/255, blue: 245.0/255, alpha: 1.0);
        self.view.addSubview(reviewButtomView!);
        
        //评论按钮
        reviewSendBtn = UIButton();
        reviewSendBtn!.backgroundColor = UIColor(red: 200.0/255, green:28.0/255, blue: 28.0/255, alpha: 1.0);
        reviewSendBtn!.layer.cornerRadius = 5;
        reviewSendBtn!.setTitle("发送", forState: UIControlState.Normal);
        reviewSendBtn!.titleLabel!.font = UIFont.boldSystemFontOfSize(CGFloat(14.0));
        reviewSendBtn!.frame = CGRectMake(screenWidth - 60,5,50,30);
        reviewSendBtn!.addTarget(self, action: "reviewSendAction:", forControlEvents: UIControlEvents.TouchUpInside)
        reviewButtomView!.addSubview(reviewSendBtn!);

        //评论文本框
        reviewTextField = UITextField();
        reviewTextField!.font = UIFont(name:praizeNum!.font.fontName,size:14.0)
        reviewTextField!.layer.cornerRadius = 3;
        reviewTextField!.placeholder = "我说…";
        reviewTextField!.delegate = self;
        reviewTextField!.returnKeyType = UIReturnKeyType.Done;
        reviewTextField!.backgroundColor = UIColor.whiteColor();
        reviewTextField!.frame = CGRectMake(10,5,screenWidth - 80,30);
        reviewButtomView!.addSubview(reviewTextField!);
        
        //评论发送按钮loading
        sendLoading = UIActivityIndicatorView(activityIndicatorStyle: UIActivityIndicatorViewStyle.White)
        sendLoading!.frame = CGRectMake(screenWidth - 40,5,30,30)
        sendLoading!.hidesWhenStopped = true
        sendLoading!.color = UIColor.blackColor()
        reviewButtomView!.addSubview(sendLoading!);
        
        //这两个通知可以捕捉到键盘弹出和收起的事件
        NSNotificationCenter.defaultCenter().addObserver(self, selector: "keyboardWillShow:", name: UIKeyboardWillShowNotification, object: nil);
        
        NSNotificationCenter.defaultCenter().addObserver(self, selector: "keyboardWillHide:", name: UIKeyboardWillHideNotification, object: nil);
    
    }
    
    //点击发布评论按钮触发回调函数
    func reviewSendAction(sender: UIButton){
        
        if((Constants["isLogin"]) == false){
            let loginPage = loginViewController(nibName:"loginViewController",bundle:nil);
            let navLoginController = UINavigationController(rootViewController:loginPage);
            self.presentViewController(navLoginController, animated: true, completion: nil)
            return;
        }
        
        //获取用户信息
        me_userid = Constants["app_userid"] as! String;
        me_username = Constants["app_username"] as! String;
        me_usersex = Constants["app_usersex"] as! String;
        me_userpic = Constants["app_userpic"] as! String;
        
        //隐藏键盘
        reviewTextField!.resignFirstResponder();
   
        //正在发送
        self.sendLoading!.startAnimating();
        self.reviewSendBtn!.hidden = true;
        
        let requestReviewSendUrl = "http://120.55.99.230/review";  //评论列表请求地址
        let contentText = reviewTextField!.text!;
        
        //如果评论为空，不能发送
        if (contentText == ""){
            self.HUD!.labelText = "评论为空";
            //加这句代码，表示纯文本提示，默认则是有loading框；
            self.HUD!.mode = MBProgressHUDMode.Text;
            self.HUD!.showAnimated(true, whileExecutingBlock: { () -> Void in
                sleep(1);
            })
            return;
        }

        //请求参数
        let paramDict = ["articleid":articleid,"userid":me_userid,"reviewcontent": contentText,"username":me_username,"usersex":me_usersex,"userpic":me_userpic];
        
        //创建一个http请求实例，这里用第三方oc库AFNetworking
        let afManager = AFHTTPRequestOperationManager()
        let op =  afManager.POST(requestReviewSendUrl,
            parameters:paramDict,
            success: {  (operation: AFHTTPRequestOperation!,   //回调函数
                responseObject: AnyObject!) in
                
                //请求成功返回的json
                let json = (try! NSJSONSerialization.JSONObjectWithData(responseObject as! NSData, options: NSJSONReadingOptions.MutableContainers)) as! NSDictionary
                
                //获取一组数据
                
                let newsCode = json["resultCode"] as! NSInteger;
                self.reviewsTotal = json["total"] as? Int;
                
                if(newsCode == 1000){

                    //创建一个实例对象
                    let reviewItem = ReviewItem();
                    //评论ID号
                    reviewItem.reviewid = json["reviewid"] as! NSString;
                    //用户ID号
                    reviewItem.userid = self.me_userid;
                    //用户头像url
                    reviewItem.userpic = self.me_userpic;
                    //用户昵称
                    reviewItem.username = self.me_username;
                    //用户性别
                    reviewItem.usersex = self.me_usersex;
                    //用户评论时间
                    reviewItem.reviewtime = json["reviewtime"] as! NSString;
                    //用户评论点亮数量
                    reviewItem.lightcount = "0";
                    //用户评论内容
                    reviewItem.reviewcontent = self.reviewTextField!.text!;
                    
                    self.currentNewsDataSource.insertObject(reviewItem, atIndex: 0);
                    
                    dispatch_async(dispatch_get_main_queue(), {
                        //这里返回主线程，写需要主线程执行的代码
                        self.dataSource = self.currentNewsDataSource
                        //获取数据源后，刷新tableView
                        self.reviewList.reloadData();
                        //清空评论框内容
                        self.reviewTextField!.text = "";
                        //总评论数+1
                        self.reviewTotalText! += 1;
                        self.reviewNum!.text = "所有评论(\(String(self.reviewTotalText)))"
                        //停止loading
                        self.sendLoading!.stopAnimating();
                        self.reviewSendBtn!.hidden = false;
                        //设置scrollview的高度，滑动到显示当前评论的位置；
                        self.reviewList.setContentOffset(CGPoint(x: 0, y:self.headerHeight!), animated: true)
                        
                        self.HUD!.labelText = "评论成功";
                        //加这句代码，表示纯文本提示，默认则是有loading框；
                        self.HUD!.mode = MBProgressHUDMode.Text;
                        self.HUD!.showAnimated(true, whileExecutingBlock: { () -> Void in
                            sleep(1);
                        })
                        
                    })
                    
                }else{
                    dispatch_async(dispatch_get_main_queue(), {
                        //这里返回主线程，写需要主线程执行的代码
                        self.sendLoading!.stopAnimating();
                        self.reviewSendBtn!.hidden = false;
                    })
                }
                
            },
            failure: {  (operation: AFHTTPRequestOperation!,
                error: NSError!) in
                print("post请求失败-error: \(error.localizedDescription)")
                dispatch_async(dispatch_get_main_queue(), {
                    //这里返回主线程，写需要主线程执行的代码
                    self.sendLoading!.stopAnimating();
                    self.reviewSendBtn!.hidden = false;
                })
        })
        //设置http请求返回格式
        op!.responseSerializer = AFHTTPResponseSerializer()
        op!.start()
        
    }
    
    //单选文本框消失之前触发
    func textFieldShouldReturn(textField: UITextField) -> Bool{
        //收起键盘
        textField.resignFirstResponder();
        return true;
    }


    //弹出键盘
    func keyboardWillShow(notification: NSNotification) {
        if (notification.userInfo?[UIKeyboardFrameBeginUserInfoKey] != nil){
            //let keyboardSize = rectValue.CGRectValue().size
            let animationDuration:NSTimeInterval = 0.3;
            UIView.beginAnimations("ResizeForKeyBoard",context:nil);
            UIView.setAnimationDuration(animationDuration);
            let _width = self.view.frame.size.width as CGFloat;
            let _height = self.view.frame.size.height as CGFloat;
            let rect:CGRect = CGRectMake(0.0 as CGFloat,-252.0,_width,_height);
            self.view.frame = rect;
            UIView.commitAnimations();
        }
    }

    //键盘消失
    func keyboardWillHide(notification: NSNotification) {
        let offset = 0.0 as CGFloat;
        let animationDuration:NSTimeInterval = 0.3;
        UIView.beginAnimations("ResizeForKeyBoard",context:nil);
        UIView.setAnimationDuration(animationDuration);
        let _width = self.view.frame.size.width as CGFloat;
        let _height = self.view.frame.size.height as CGFloat;
        let rect:CGRect = CGRectMake(0.0 as CGFloat,offset,_width,_height);
        self.view.frame = rect;
        UIView.commitAnimations();
    }
    
    //微信事件委托
    func application(application: UIApplication, didFinishLaunchingWithOptions launchOptions: [NSObject: AnyObject]?) -> Bool{
        print("registerApp");
        return WXApi.registerApp("wxf8c2bf5f7edb595f",withDescription:"Post");
    }

    func application(url:NSURL,sourceApplication:NSString,annotation: AnyObject?) -> Bool{
        print("handleOpenURL");
        return WXApi.handleOpenURL(url,delegate:self);
    }
    

    /*
    // MARK: - Navigation

    // In a storyboard-based application, you will often want to do a little preparation before navigation
    override func prepareForSegue(segue: UIStoryboardSegue, sender: AnyObject?) {
        // Ge new view controller using segue.destinationViewController.
        // Pass the selected object to the new view controller.
    }
    */

}
