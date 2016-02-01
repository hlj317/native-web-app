//
//  PublishEditViewController.swift
//  News
//
//  Created by huanglijun on 15/8/12.
//  Copyright (c) 2015年 huanglijun. All rights reserved.
//

import UIKit

@available(iOS 8.0, *)
class PublishEditViewController: UIViewController,UIImagePickerControllerDelegate,UINavigationControllerDelegate,UIPopoverControllerDelegate,UIGestureRecognizerDelegate,UITextViewDelegate {

    let screenWidth = UIScreen.mainScreen().bounds.size.width;      //屏幕宽度
    let screenHeight = UIScreen.mainScreen().bounds.size.height;    //屏幕高度
    var articleChannel : UILabel = UILabel();    //文章类别
    var articleCheckStatusLabel : UILabel = UILabel();    //文章审核状态(label)
    var articleCheckStatus : UILabel = UILabel();    //文章审核状态
    var articleFailureCause : UILabel = UILabel();   //文章未通过审核原因
    var articleTitle : UITextView = UITextView();    //文章标题
    var articleTitleLabel : UILabel = UILabel();    //文章标题(label)
    var articleDescription : UITextView = UITextView();    //文章描述
    var articleDescriptionLabel : UILabel = UILabel();    //文章描述(label)
    var articleContent : UITextView = UITextView();    //文章内容
    var articleContentLabel : UILabel = UILabel();    //文章内容(label)
    var articleCover : UIImageView = UIImageView();  //文章封面视图
    var articleCoverImg : UIImage?;         //文章封面图片
    var finishBtn : UIButton = UIButton();  //编辑发布按钮
    var articleid = NSString();  //获取传过来的值,文章ID号
    var articleItem = ArticleItem();   //创建一个文章模型对象
    var scrollView : UIScrollView?;    //外围容器
    var picker:UIImagePickerController?=UIImagePickerController()
    var popover:UIPopoverController?=nil
    var HUD : MBProgressHUD?;  //弱提示对话框
    
    override func viewDidLoad() {
        super.viewDidLoad()
        self.view.backgroundColor = UIColor.whiteColor();
        
        //初始化scrollView
        scrollView = UIScrollView()
        scrollView!.frame = CGRectMake(0,10,screenWidth,screenHeight-80);
        
        scrollView!.showsHorizontalScrollIndicator = false
        scrollView!.showsVerticalScrollIndicator = false
        
        //兼容IOS7以上，去掉scrollview顶部的空白部分
        if (self.respondsToSelector("setEdgesForExtendedLayout:")){
            self.edgesForExtendedLayout = UIRectEdge.None;
        }
        
        loadArticle();   //加载文章信息

        setupHUD();  //创建弱提示对话框
        
        setTextViewDelegate();  //为三个textview设置代理
        
    }
    
    //为三个textview设置代理
    func setTextViewDelegate(){
        
        articleTitle.delegate = self;
        articleTitle.scrollEnabled = true;  //允许开启滚动条
        
        articleDescription.delegate = self;
        articleDescription.scrollEnabled = true;  //允许开启滚动条
        
        articleContent.delegate = self;
        articleContent.scrollEnabled = true;  //允许开启滚动条
        
        //定义一个toolBar
        let topView = UIToolbar(frame: CGRectMake(0, 0, screenWidth,30));
        
        //设置style
        topView.barStyle = UIBarStyle.Default;
        
        //定义两个flexibleSpace的button，放在toolBar上，这样完成按钮就会在最右边
        let button1 = UIBarButtonItem(barButtonSystemItem: UIBarButtonSystemItem.FlexibleSpace, target: self, action: nil);
        let button2 = UIBarButtonItem(barButtonSystemItem: UIBarButtonSystemItem.FlexibleSpace, target: self, action: nil);
        
        //定义完成按钮
        let doneButton = UIBarButtonItem(title: "完成", style:UIBarButtonItemStyle.Done, target: self, action: "keyDone:")
        
        //在toolBar上加上这些按钮
        let buttonsArray:[UIBarButtonItem] = [button1,button2,doneButton];
        
        topView.setItems(buttonsArray, animated: true);
        
        articleTitle.inputAccessoryView = topView;
        articleDescription.inputAccessoryView = topView;
        articleContent.inputAccessoryView = topView;
    
    }
    
    //创建弱视图提示对话框
    func setupHUD(){
        HUD = MBProgressHUD(view: self.view);
        self.view.addSubview(HUD!);
        HUD!.dimBackground = true;
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    //加载文章
    func loadArticle() {
        
        //开始加载动画

        let requestUrl = "http://120.55.99.230/articleinfo";
        
        //请求参数
        let requestParams = ["articleid":articleid];
        
        //创建一个http请求实例，这里用第三方oc库AFNetworking
        let afManager = AFHTTPRequestOperationManager()
        let op =  afManager.POST(requestUrl,
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
                    
                    self.articleItem.description = infoData["description"] as! NSString
                    
                    self.articleItem.content = infoData["content"] as! NSString
                    
                    self.articleItem.channel = infoData["channel"] as! NSInteger
                    
                    self.articleItem.checkstatus = infoData["checkstatus"] as! NSString
                    
                    self.articleItem.failcause = infoData["failcause"] as! NSString
                    
                    self.articleItem.coverpic = infoData["cover"] as! NSString
                    
                    dispatch_async(dispatch_get_main_queue(), {
                        
                        self.setupArticle();   //创建文章
                        
                    })
                }else{
                    dispatch_async(dispatch_get_main_queue(), {
                        
                        //停止加载动画

                    })
                }
                
            },
            failure: {  (operation: AFHTTPRequestOperation!,
                error: NSError!) in
                print("post请求失败-error: \(error.localizedDescription)")
                dispatch_async(dispatch_get_main_queue(), {
                    
                    //停止加载动画

                })
        })
        //设置http请求返回格式
        op!.responseSerializer = AFHTTPResponseSerializer()
        op!.start()
    }

    //创建文章
    func setupArticle(){
    
        //文章类别
        articleChannel.font = UIFont(name:articleChannel.font.fontName,size:12.0);
        let articleChannelNum = self.articleItem.channel as Int;
        if (articleChannelNum == 1) {
            articleChannel.text = "类别：健身无敌";
        } else if (articleChannelNum == 2) {
            articleChannel.text = "类别：热血泡妞";
        } else if (articleChannelNum == 3) {
            articleChannel.text = "类别：有什么要问";
        } else if (articleChannelNum == 4) {
            articleChannel.text = "类别：职场之道";
        }
        articleChannel.frame = CGRectMake(15,0,screenWidth,15);
        articleChannel.textColor = UIColor(red: 153.0/255, green: 153.0/255, blue: 153.0/255, alpha: 1.0);
        scrollView!.addSubview(articleChannel);
 
        //文章审核状态(label)
        articleCheckStatusLabel.font = UIFont(name:articleCheckStatusLabel.font.fontName,size:12.0);
        articleCheckStatusLabel.text = "审核状态：";
        articleCheckStatusLabel.frame = CGRectMake(15,15,60,15);
        articleCheckStatusLabel.textColor = UIColor(red: 153.0/255, green: 153.0/255, blue: 153.0/255, alpha: 1.0);
        scrollView!.addSubview(articleCheckStatusLabel);
        
        //文章审核状态
        articleCheckStatus.font = UIFont(name:articleCheckStatus.font.fontName,size:12.0);
        articleCheckStatus.text = self.articleItem.checkstatus as String;
        articleCheckStatus.frame = CGRectMake(75,15,220,15);
        articleCheckStatus.textColor = UIColor(red: 255.0/255, green: 0.0/255, blue: 0.0/255, alpha: 1.0);
        scrollView!.addSubview(articleCheckStatus);
        
        //文章未通过审核原因
        let articleFailureText = self.articleItem.failcause as String;
        articleFailureCause.font = UIFont(name:articleFailureCause.font.fontName,size:12.0);
        articleFailureCause.text = self.articleItem.failcause as String;
        articleFailureCause.frame = CGRectMake(15,30,screenWidth-30,15);
        articleFailureCause.textColor = UIColor(red: 153.0/255, green: 153.0/255, blue: 153.0/255, alpha: 1.0);
        if(articleFailureText != ""){
            scrollView!.addSubview(articleFailureCause);
        }
        
        //编辑完保存
        finishBtn.setTitle("编辑完保存",forState:UIControlState.Normal);
        finishBtn.titleLabel!.font = UIFont(name:finishBtn.titleLabel!.font.fontName,size:14.0);
        finishBtn.backgroundColor = UIColor(red: 224.0/255, green: 49.0/255, blue: 49.0/255, alpha: 1.0);
        finishBtn.setTitleColor(UIColor.whiteColor(),forState:UIControlState.Normal);
        finishBtn.setTitleColor(UIColor.yellowColor(),forState:UIControlState.Highlighted);
        finishBtn.layer.cornerRadius = 5;
        finishBtn.addTarget(self, action: "finishAction:", forControlEvents: UIControlEvents.TouchUpInside)
        finishBtn.frame = CGRectMake(screenWidth-115,0,100,30);
        scrollView!.addSubview(finishBtn);

        //文章封面
        articleCover.sd_setImageWithURL(NSURL(string:articleItem.coverpic as String)!,placeholderImage: UIImage(named: "cell_photo.jpg"))
        articleCover.contentMode = UIViewContentMode.ScaleAspectFill;
        articleCover.clipsToBounds = true;
        articleCover.frame = CGRectMake(15,45,screenWidth - 30,180);
        scrollView!.addSubview(articleCover);
        picker!.delegate=self;
        articleCover.userInteractionEnabled = true;
        let singleTap = UITapGestureRecognizer(target: self, action:Selector("articlePicker"));
        singleTap.delegate = self;
        articleCover.addGestureRecognizer(singleTap);
        
        //文章标题(label)
        articleTitleLabel.font = UIFont(name:articleTitleLabel.font.fontName,size:14.0);
        articleTitleLabel.text = "标题：";
        articleTitleLabel.frame = CGRectMake(15,235,45,15);
        articleTitleLabel.textColor = UIColor(red: 153.0/255, green: 153.0/255, blue: 153.0/255, alpha: 1.0);
        scrollView!.addSubview(articleTitleLabel);
        
        //文章标题
        articleTitle.font = UIFont(name:"Heiti SC",size:14.0);
        articleTitle.text = self.articleItem.title as String;
        articleTitle.frame = CGRectMake(60,227,screenWidth-75,40);
        articleTitle.layer.borderWidth = 0;
        scrollView!.addSubview(articleTitle);
        
        //文章描述(label)
        articleDescriptionLabel.font = UIFont(name:articleDescriptionLabel.font.fontName,size:14.0);
        articleDescriptionLabel.text = "描述：";
        articleDescriptionLabel.frame = CGRectMake(15,285,45,15);
        articleDescriptionLabel.textColor = UIColor(red: 153.0/255, green: 153.0/255, blue: 153.0/255, alpha: 1.0);
        scrollView!.addSubview(articleDescriptionLabel);
 
        //文章描述
        articleDescription.font = UIFont(name:"Heiti SC",size:14.0);
        articleDescription.text = articleItem.description as String;
        articleDescription.frame = CGRectMake(60,277,screenWidth-75,40)
        articleDescription.layer.borderWidth = 0;
        scrollView!.addSubview(articleDescription);
        
        //文章内容(label)
        articleContentLabel.font = UIFont(name:articleContentLabel.font.fontName,size:14.0);
        articleContentLabel.text = "内容：";
        articleContentLabel.frame = CGRectMake(15,335,45,15);
        articleContentLabel.layer.borderWidth = 0;
        articleContentLabel.textColor = UIColor(red: 153.0/255, green: 153.0/255, blue: 153.0/255, alpha: 1.0);
        scrollView!.addSubview(articleContentLabel);
        
        //文章内容
        articleContent.font = UIFont(name:"Heiti SC",size:14.0);
        articleContent.text = articleItem.content as String;
        articleContent.frame = CGRectMake(60,327,screenWidth-75,300)
        scrollView!.addSubview(articleContent);
        
        scrollView!.contentSize = CGSizeMake(screenWidth,635)
        self.view.addSubview(scrollView!);
        self.view.sendSubviewToBack(scrollView!);
    
    }

    func finishAction(sender: UIButton){
        if(articleCoverImg == nil){
            userEditMessage();  //更改文章信息(不带封面)
        }else{
            userEditArticle();  //更改文章信息
        }
    }
    
    //拍照和选择图片
    func articlePicker()
    {
        let alert:UIAlertController=UIAlertController(title: "Choose Image", message: nil, preferredStyle: UIAlertControllerStyle.ActionSheet)
        
        let cameraAction = UIAlertAction(title: "拍照", style: UIAlertActionStyle.Default)
            {
                UIAlertAction in
                self.openCamera()
                
        }
        let gallaryAction = UIAlertAction(title: "选择本地图片", style: UIAlertActionStyle.Default)
            {
                UIAlertAction in
                self.openGallary()
        }
        let cancelAction = UIAlertAction(title: "取消", style: UIAlertActionStyle.Cancel)
            {
                UIAlertAction in
                
        }
        
        // Add the actions
        picker?.delegate = self
        alert.addAction(cameraAction)
        alert.addAction(gallaryAction)
        alert.addAction(cancelAction)
        // Present the controller
        if UIDevice.currentDevice().userInterfaceIdiom == .Phone
        {
            self.presentViewController(alert, animated: true, completion: nil)
        }
        else
        {
            popover=UIPopoverController(contentViewController: alert)
            popover!.presentPopoverFromRect(articleCover.frame, inView: self.view, permittedArrowDirections: UIPopoverArrowDirection.Any, animated: true)
        }
    }
    func openCamera()
    {
        if(UIImagePickerController .isSourceTypeAvailable(UIImagePickerControllerSourceType.Camera))
        {
            //picker!.allowsEditing = true;
            picker!.sourceType = UIImagePickerControllerSourceType.Camera
            self .presentViewController(picker!, animated: true, completion: nil)
        }
        else
        {
            openGallary()
        }
    }
    func openGallary()
    {
        picker!.sourceType = UIImagePickerControllerSourceType.PhotoLibrary
        if UIDevice.currentDevice().userInterfaceIdiom == .Phone
        {
            //picker!.allowsEditing = true;
            self.presentViewController(picker!, animated: true, completion: nil)
        }
        else
        {
            popover=UIPopoverController(contentViewController: picker!)
            popover!.presentPopoverFromRect(articleCover.frame, inView: self.view, permittedArrowDirections: UIPopoverArrowDirection.Any, animated: true)
        }
    }
    func imagePickerController(picker: UIImagePickerController, didFinishPickingMediaWithInfo info: [String : AnyObject])
    {
        picker.dismissViewControllerAnimated(true, completion: nil)
        articleCover.image=info[UIImagePickerControllerOriginalImage] as? UIImage
        articleCoverImg = (info[UIImagePickerControllerOriginalImage] as? UIImage)!;
        let imgSize = CGSizeMake(400.0, 400.0/(articleCoverImg!.size.width/articleCoverImg!.size.height));
        articleCoverImg = imageWithImageSimple(articleCoverImg!,newSize:imgSize);
    }
    func imagePickerControllerDidCancel(picker: UIImagePickerController)
    {
        picker.dismissViewControllerAnimated(true,completion: { () -> Void in
            print("picker cancel.")
        })
    }
    
    //压缩图片
    func imageWithImageSimple(image:UIImage,newSize:CGSize)->UIImage{
        UIGraphicsBeginImageContext(newSize);
        image.drawInRect(CGRectMake(0,0,newSize.width,newSize.height));
        let newImage = UIGraphicsGetImageFromCurrentImageContext();
        UIGraphicsEndImageContext();
        return newImage;
    }

    //更改文章信息(不带封面)
    func userEditMessage(){
    
        self.HUD!.labelText = "正在加载";
        self.HUD!.show(true);
        
        let requestUrl = "http://120.55.99.230/userEditMessage";
        
        let titleText = articleTitle.text;
        let descriptionText = articleDescription.text;
        let contentText = articleContent.text;
        
        //请求参数
        let paramDict = ["articleid":articleid,"title":titleText,"description":descriptionText,"content":contentText] as NSDictionary;
        
        //创建一个http请求实例，这里用第三方oc库AFNetworking
        let afManager = AFHTTPRequestOperationManager()
        let op =  afManager.POST(requestUrl,
            parameters:paramDict,
            success: {  (operation: AFHTTPRequestOperation!,   //回调函数
                responseObject: AnyObject!) in
                
                //请求成功返回的json
                let json = (try! NSJSONSerialization.JSONObjectWithData(responseObject as! NSData, options: NSJSONReadingOptions.MutableContainers)) as! NSDictionary
                
                //获取一组数据
                
                let newsCode = json["resultCode"] as! NSInteger;
                
                if(newsCode == 1000){
                    
                    dispatch_async(dispatch_get_main_queue(), {
                        
                        self.HUD!.hide(false);
                        self.HUD!.labelText = "保存成功";
                        self.HUD!.mode = MBProgressHUDMode.Text;
                        self.HUD!.showAnimated(true, whileExecutingBlock:  { () -> Void in
                            sleep(1);
                        })
                        
                    })
                }else{
                    dispatch_async(dispatch_get_main_queue(), {
                        
                        //停止加载动画
                        self.HUD!.hide(false);
                        self.HUD!.labelText = "保存失败";
                        self.HUD!.mode = MBProgressHUDMode.Text;
                        self.HUD!.showAnimated(true, whileExecutingBlock:  { () -> Void in
                            sleep(1);
                        })
                        
                    })
                }
                
            },
            failure: {  (operation: AFHTTPRequestOperation!,
                error: NSError!) in
                print("post请求失败-error: \(error.localizedDescription)")
                dispatch_async(dispatch_get_main_queue(), {
                    
                    //停止加载动画
                    self.HUD!.hide(false);
                    self.HUD!.labelText = "保存失败";
                    self.HUD!.mode = MBProgressHUDMode.Text;
                    self.HUD!.showAnimated(true, whileExecutingBlock:  { () -> Void in
                        sleep(1);
                    })
                    
                })
        })
        //设置http请求返回格式
        op!.responseSerializer = AFHTTPResponseSerializer()
        op!.start()
    
    }
    
    //更改文章信息(带封面)
    func userEditArticle(){
   
        self.HUD!.labelText = "正在加载";
        self.HUD!.show(true);
        
        let requestUrl = "http://120.55.99.230/userEditArticle";
        
        let titleText = articleTitle.text;
        let descriptionText = articleDescription.text;
        let contentText = articleContent.text;
        
        //请求参数
        let paramDict = ["articleid":articleid,"title":titleText,"description":descriptionText,"content":contentText] as NSDictionary;
        
        let request:NSMutableURLRequest = NSMutableURLRequest();
        request.URL = NSURL(string: requestUrl);
        request.HTTPMethod = "POST";
        request.timeoutInterval = 30;
        let body:NSMutableData = NSMutableData();
        
        //设置表单分隔符
        let boundary:NSString = "----------------------1465789351321346";
        let contentType = NSString(format: "multipart/form-data;boundary=%@", boundary);
        request.addValue(contentType as String, forHTTPHeaderField: "Content-Type");
        
        //写入Info内容
        for key in paramDict.allKeys{
            body.appendData(NSString(format: "--%@\r\n", boundary).dataUsingEncoding(NSUTF8StringEncoding)!);
            body.appendData(NSString(format: "Content-Disposition:form-data;name=\"%@\"\r\n\r\n", key as! NSString).dataUsingEncoding(NSUTF8StringEncoding)!);
            //如果有中文进行UTF8编码
            body.appendData("\(paramDict.objectForKey(key) as! String)\r\n".dataUsingEncoding(NSUTF8StringEncoding)!)
        }
        
        //写入图片内容
        body.appendData(NSString(format: "--%@\r\n", boundary).dataUsingEncoding(NSUTF8StringEncoding)!);
        body.appendData(NSString(format: "Content-Disposition:form-data;name=\"upload\";filename=\"upload.jpeg\"\r\n", "userfile").dataUsingEncoding(NSUTF8StringEncoding)!);
        let imageData:NSData = UIImageJPEGRepresentation(articleCoverImg!,0.9)!;
        body.appendData("Content-Type:image/jpeg\r\n\r\n".dataUsingEncoding(NSUTF8StringEncoding)!)
        body.appendData(imageData);
        body.appendData("\r\n".dataUsingEncoding(NSUTF8StringEncoding)!)
        
        //写入尾部
        body.appendData(NSString(format: "--%@--\r\n", boundary).dataUsingEncoding(NSUTF8StringEncoding)!);
        request.HTTPBody = body;
        
        //var urlResponse:NSHTTPURLResponse? = nil;
        //var error:NSError? = NSError();
        
        NSURLConnection.sendAsynchronousRequest(request, queue: NSOperationQueue()) { (response, data, error) -> Void in
            if(error == nil){
                let json:NSDictionary = (try! NSJSONSerialization.JSONObjectWithData(data!, options: NSJSONReadingOptions.MutableContainers)) as! NSDictionary;
                
                //获取请求返回号
                let newsCode = json["resultCode"] as! NSInteger;
                
                dispatch_async(dispatch_get_main_queue(), {
                    if(newsCode == 1000){
                        self.HUD!.hide(false);
                        self.HUD!.labelText = "保存成功";
                        self.HUD!.mode = MBProgressHUDMode.Text;
                        self.HUD!.showAnimated(true, whileExecutingBlock:  { () -> Void in
                            sleep(1);
                        })
                    }else{
                        self.HUD!.hide(false);
                        self.HUD!.labelText = "保存失败";
                        self.HUD!.mode = MBProgressHUDMode.Text;
                        self.HUD!.showAnimated(true, whileExecutingBlock:  { () -> Void in
                            sleep(1);
                        })
                    }
                })
            }else{
                dispatch_async(dispatch_get_main_queue(), {
                    self.HUD!.hide(false);
                    self.HUD!.labelText = "保存失败";
                    self.HUD!.mode = MBProgressHUDMode.Text;
                    self.HUD!.showAnimated(true, whileExecutingBlock:  { () -> Void in
                        sleep(1);
                    })
                })
            }
        }
        
    }
    
    //点击多选文本上增加的完成按钮触发
    func keyDone(sender: UIButton){
        //收起键盘
        articleTitle.resignFirstResponder();
        articleDescription.resignFirstResponder();
        articleContent.resignFirstResponder();
    }
    
    //点击多选文本框触发
    func textViewShouldBeginEditing (textView:UITextView) -> Bool{
        
        //解决键盘被遮住问题
        var offset = 0.0 as CGFloat;
        if(screenHeight > 567){     //iphone5,iphone6
            offset = -150.0;
        }else{                //iphone4
            offset = -180.0;
        }
        let animationDuration:NSTimeInterval = 0.3;
        UIView.beginAnimations("ResizeForKeyBoard",context:nil);
        UIView.setAnimationDuration(animationDuration);
        let _width = self.view.frame.size.width as CGFloat;
        let _height = self.view.frame.size.height as CGFloat;
        let rect:CGRect = CGRectMake(0.0 as CGFloat,offset,_width,_height);
        self.view.frame = rect;
        UIView.commitAnimations();
        
        return true;
        
    }
    
    //多选文本框消失之前触发
    func textViewShouldEndEditing (textView:UITextView) -> Bool{

        let offset = 60.0 as CGFloat;
        let animationDuration:NSTimeInterval = 0.3;
        UIView.beginAnimations("ResizeForKeyBoard",context:nil);
        UIView.setAnimationDuration(animationDuration);
        let _width = self.view.frame.size.width as CGFloat;
        let _height = self.view.frame.size.height as CGFloat;
        let rect:CGRect = CGRectMake(0.0 as CGFloat,offset,_width,_height);
        self.view.frame = rect;
        UIView.commitAnimations();
        return true;
    }
    
    override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated);

        //友盟统计
        MobClick.beginLogPageView("PublishEdit");
    }
    
    override func viewWillDisappear(animated: Bool) {
        super.viewWillDisappear(animated);
        
        //友盟统计
        MobClick.endLogPageView("PublishEdit");
    }

    /*
    // MARK: - Navigation

    // In a storyboard-based application, you will often want to do a little preparation before navigation
    override func prepareForSegue(segue: UIStoryboardSegue, sender: AnyObject?) {
        // Get the new view controller using segue.destinationViewController.
        // Pass the selected object to the new view controller.
    }
    */

}
