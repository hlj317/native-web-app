//
//  WriteViewController.swift
//  News
//
//  Created by huanglijun on 15/7/11.
//  Copyright (c) 2015年 huanglijun. All rights reserved.
//

import UIKit

@available(iOS 8.0, *)
class WriteViewController: UIViewController,UITextViewDelegate,UITextFieldDelegate,UIAlertViewDelegate,UIImagePickerControllerDelegate,UINavigationControllerDelegate,UIPopoverControllerDelegate,UIGestureRecognizerDelegate{
    let screenWidth = UIScreen.mainScreen().bounds.size.width;      //屏幕宽度
    let screenHeight = UIScreen.mainScreen().bounds.size.height;    //屏幕高度
    @IBOutlet weak var buttonSend: UIButton!     //发送按钮
    @IBOutlet weak var buttonTakePic: UIImageView!   //拍照或选择图片按钮
    @IBOutlet weak var buttonWork: UIButton!         //工作之道按钮
    @IBOutlet weak var buttonGirls: UIButton!        //热血泡妞按钮
    @IBOutlet weak var buttonExercise: UIButton!     //健身无敌按钮
    @IBOutlet weak var buttonAsk: UIButton!          //什么要问按钮
    @IBOutlet weak var editTitle: UITextField!       //编辑标题框
    @IBOutlet weak var editContent: UITextView!      //编辑内容框
    var picker:UIImagePickerController?=UIImagePickerController()
    var popover:UIPopoverController?=nil
    var uploadImg:UIImage?;   //上传的图片
    var channelCheck = "4";    //默认选中工作之道
    var editTitleText = "";    //编辑标题框中的文本内容
    var editContentText = "";  //编辑内容框中的文本内容
    var HUD : MBProgressHUD?;  //弱提示对话框
    //当前用户信息
    var userid = "";       //用户ID号
    var username = "";     //用户昵称
    var usersex = "";      //用户性别
    var userpic = "";      //用户头像

    override func viewDidLoad() {
        super.viewDidLoad()
        
        editTitle.delegate = self;
        editTitle.returnKeyType = UIReturnKeyType.Done;
        
        editContent.delegate = self;
        editContent.scrollEnabled = true;  //允许开启滚动条
        
        //这两个通知可以捕捉到键盘弹出和收起的事件
//        NSNotificationCenter.defaultCenter().addObserver(self, selector: "keyboardDidShow:", name: UIKeyboardDidShowNotification, object: nil)
//        NSNotificationCenter.defaultCenter().addObserver(self, selector: "keyboardDidHide:", name: UIKeyboardDidHideNotification, object: nil)
        
        //定义一个toolBar
        let topView = UIToolbar(frame: CGRectMake(0, 0, screenWidth, 30));

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
        
        editContent.inputAccessoryView = topView;
        
        buttonTakePic.contentMode = UIViewContentMode.ScaleAspectFill;
        buttonTakePic.clipsToBounds = true;
        picker!.delegate=self
        buttonTakePic.userInteractionEnabled = true;
        let singleTap = UITapGestureRecognizer(target: self, action:Selector("btnImagePickerClicked"));
        singleTap.delegate = self;
        buttonTakePic.addGestureRecognizer(singleTap);
        buttonSend.addTarget(self, action: "btnImageUpload:", forControlEvents: UIControlEvents.TouchUpInside)
        buttonSend.layer.cornerRadius = 3;
        
        buttonWork.addTarget(self, action: "btnWork:", forControlEvents: UIControlEvents.TouchUpInside)
        buttonWork.layer.cornerRadius = 3;
        
        buttonGirls.addTarget(self, action: "btnGirls:", forControlEvents: UIControlEvents.TouchUpInside)
        buttonGirls.layer.cornerRadius = 3;
        
        buttonExercise.addTarget(self, action: "btnExercise:", forControlEvents: UIControlEvents.TouchUpInside)
        buttonExercise.layer.cornerRadius = 3;
        
        buttonAsk.addTarget(self, action: "btnAsk:", forControlEvents: UIControlEvents.TouchUpInside)
        buttonAsk.layer.cornerRadius = 3;
        
        //创建弱提示对话框
        setupHUD();
      
    }
    
    //创建弱视图提示对话框
    func setupHUD(){
        HUD = MBProgressHUD(view: self.view);
        self.view.addSubview(HUD!);
        HUD!.dimBackground = true;
    }
    
    //点击多选文本上增加的完成按钮触发
    func keyDone(sender: UIButton){
        //收起键盘
        editContent.resignFirstResponder();
    }
    
    //点击多选文本框触发
    func textViewShouldBeginEditing (textView:UITextView) -> Bool{
    
        //解决默认提示问题
        if(editContent.tag == 0) {
            editContent.text = "";
            editContent.textColor = UIColor.blackColor();
            editContent.tag = 1;
        }
        
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

        let offset = 0.0 as CGFloat;
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
    
    //点击单选文本框触发
    func textFieldShouldBeginEditing(textField: UITextField) -> Bool{
        //解决键盘被遮住问题
        var offset = 0.0 as CGFloat;
        if(screenHeight > 567){     //iphone5
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
    
    //单选文本框消失之前触发
    func textFieldShouldReturn(textField: UITextField) -> Bool{
        //收起键盘
        textField.resignFirstResponder();
        
        let offset = 0.0 as CGFloat;
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

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    
    override func viewWillAppear(animated: Bool) {
        super.viewDidAppear(animated);
        self.navigationController?.setNavigationBarHidden(true, animated: animated);
        
        //友盟统计
        MobClick.beginLogPageView("Write");
    }
    
    override func viewWillDisappear(animated: Bool) {
        super.viewDidDisappear(animated);
        self.navigationController?.setNavigationBarHidden(false, animated: animated);
        
        //友盟统计
        MobClick.endLogPageView("Write");
    }
  
    
    func btnImagePickerClicked()
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
            popover!.presentPopoverFromRect(buttonTakePic.frame, inView: self.view, permittedArrowDirections: UIPopoverArrowDirection.Any, animated: true)
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
            popover!.presentPopoverFromRect(buttonTakePic.frame, inView: self.view, permittedArrowDirections: UIPopoverArrowDirection.Any, animated: true)
        }
    }
    func imagePickerController(picker: UIImagePickerController, didFinishPickingMediaWithInfo info: [String : AnyObject])
    {
        picker.dismissViewControllerAnimated(true, completion: nil)
        //imageView.image=info[UIImagePickerControllerEditedImage] as? UIImage
        buttonTakePic.image=info[UIImagePickerControllerOriginalImage] as? UIImage
        //buttonTakePic.setBackgroundImage(info[UIImagePickerControllerOriginalImage] as? UIImage, forState: UIControlState.Normal)
        //myImg = (info[UIImagePickerControllerEditedImage] as? UIImage)!;
        uploadImg = (info[UIImagePickerControllerOriginalImage] as? UIImage)!;
        let imgSize = CGSizeMake(400.0, 400.0/(uploadImg!.size.width/uploadImg!.size.height));
        uploadImg = imageWithImageSimple(uploadImg!,newSize:imgSize);
    }
    func imagePickerControllerDidCancel(picker: UIImagePickerController)
    {
        picker.dismissViewControllerAnimated(true,completion: { () -> Void in
            print("picker cancel.")
        })
    }
    
    //图片上传
    func btnImageUpload(sender: UIButton){
        
        if((Constants["isLogin"]) == false){
            let loginPage = loginViewController(nibName:"loginViewController",bundle:nil);
            let navLoginController = UINavigationController(rootViewController:loginPage);
            self.presentViewController(navLoginController, animated: true, completion: nil)
            return;
        }
       
        //获取用户表单信息
        editTitleText = editTitle.text!;
        editContentText = editContent.text;
        
        if (uploadImg == nil) {
            self.HUD!.labelText = "请上传文章的封面";
            self.HUD!.mode = MBProgressHUDMode.Text;
            self.HUD!.showAnimated(true, whileExecutingBlock:  { () -> Void in
                sleep(1);
            })
            return;
        }else if(editTitleText == ""){
            self.HUD!.labelText = "标题不能为空";
            self.HUD!.mode = MBProgressHUDMode.Text;
            self.HUD!.showAnimated(true, whileExecutingBlock:  { () -> Void in
                sleep(1);
            })
            return;
        }else if(editContentText == "" || editContentText == "点击编辑文本"){
            self.HUD!.labelText = "内容不能为空";
            self.HUD!.mode = MBProgressHUDMode.Text;
            self.HUD!.showAnimated(true, whileExecutingBlock:  { () -> Void in
                sleep(1);
            })
            return;
        }
        
        //获取用户信息
        userid = Constants["app_userid"] as! String;
        username = Constants["app_username"] as! String;
        usersex = Constants["app_usersex"] as! String;
        userpic = Constants["app_userpic"] as! String;
        
        //开始加载动画
        self.HUD!.labelText = "正在发布";
        self.HUD!.mode = MBProgressHUDMode.Indeterminate;
        self.HUD!.show(true);
        
        //请求参数
        let paramDict = ["title":editTitleText,"description":getDescription(editContentText),"channel":"4","content":editContentText,"writeid":userid,"writename":username,"writesex":usersex,"writepic":userpic] as NSDictionary;
        
        let hackerNewsApiUrl = "http://120.55.99.230/addArticle";
        
        let request:NSMutableURLRequest = NSMutableURLRequest();
        request.URL = NSURL(string: hackerNewsApiUrl);
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
        //var ImgPath = NSHomeDirectory()+(carData.valueForKey("upload") as! String);
        //println(ImgPath)
        body.appendData(NSString(format: "--%@\r\n", boundary).dataUsingEncoding(NSUTF8StringEncoding)!);
        body.appendData(NSString(format: "Content-Disposition:form-data;name=\"upload\";filename=\"upload.jpeg\"\r\n", "userfile").dataUsingEncoding(NSUTF8StringEncoding)!);
        let imageData:NSData = UIImageJPEGRepresentation(uploadImg!,0.9)!;
        body.appendData("Content-Type:image/jpeg\r\n\r\n".dataUsingEncoding(NSUTF8StringEncoding)!)
        body.appendData(imageData);
        body.appendData("\r\n".dataUsingEncoding(NSUTF8StringEncoding)!)
        
        //写入尾部
        body.appendData(NSString(format: "--%@--\r\n", boundary).dataUsingEncoding(NSUTF8StringEncoding)!);
        request.HTTPBody = body;
        
        //var urlResponse:NSHTTPURLResponse? = nil;
        //var error:NSError? = NSError();
        
        //第三方判断网络是否连接
        //        if IJReachability.isConnectedToNetwork() {
        NSURLConnection.sendAsynchronousRequest(request, queue: NSOperationQueue()) { (response, data, error) -> Void in
            if(error == nil){
                let json:NSDictionary = (try! NSJSONSerialization.JSONObjectWithData(data!, options: NSJSONReadingOptions.MutableContainers)) as! NSDictionary;
                
                //获取请求返回号
                let newsCode = json["resultCode"] as! NSInteger;
                
                dispatch_async(dispatch_get_main_queue(), {
                    if(newsCode == 1000){
                        self.HUD!.hide(false);
                        self.HUD!.labelText = "发布成功";
                        self.HUD!.mode = MBProgressHUDMode.Text;
                        self.HUD!.showAnimated(true, whileExecutingBlock:  { () -> Void in
                            sleep(1);
                        })
                        let publishView=PublishViewController()
                        //传值
                        publishView.me_userid=self.userid;
                        //打开发布列表页面，隐藏底部tabbar
                        publishView.hidesBottomBarWhenPushed = true;
                        //获取导航控制器,添加subView,入栈
                        self.navigationController!.pushViewController(publishView,animated:true);
                    }else{
                        self.HUD!.hide(false);
                        self.HUD!.labelText = "发布失败";
                        self.HUD!.mode = MBProgressHUDMode.Text;
                        self.HUD!.showAnimated(true, whileExecutingBlock:  { () -> Void in
                            sleep(1);
                        })
                    }
                })
            }else{
                dispatch_async(dispatch_get_main_queue(), {
                    self.HUD!.hide(false);
                    self.HUD!.labelText = "发布失败";
                    self.HUD!.mode = MBProgressHUDMode.Text;
                    self.HUD!.showAnimated(true, whileExecutingBlock:  { () -> Void in
                        sleep(1);
                    })
                })
            }
        }
    }
    //        else{
    //            loading.mode = MBProgressHUDMode.CustomView;
    //            println("没有网络，上传失败");
    //        }
    
//    //该方法让文本视图的高度减去键盘的高度，保证合适的文本视图高度
//    func updateTextViewSizeForKeyboardHeight(keyboardHeight: CGFloat) {
//        println("view.frame.width=\(view.frame.width)");
//        println("view.frame.height=\(view.frame.height)");
//        editContent.frame = CGRect(x: 20, y: 311, width: view.frame.width, height: view.frame.height - keyboardHeight)
//    }
//    
//    func keyboardDidShow(notification: NSNotification) {
//        if let rectValue = notification.userInfo?[UIKeyboardFrameBeginUserInfoKey] as? NSValue {
//            let keyboardSize = rectValue.CGRectValue().size
//            println("keyboardSize.height=\(keyboardSize.height)");
//            updateTextViewSizeForKeyboardHeight(keyboardSize.height)
//            
//        }
//        
//        var _rect:CGRect = self.view.frame;
//        UIView.beginAnimations("_rect",context:nil);
//        UIView.setAnimationDuration(NSTimeInterval())
//        _rect.origin.y = -120.0;//view往上移动
//        self.view.frame = _rect;
//    }
//    
//    func keyboardDidHide(notification: NSNotification) {
//        var _rect:CGRect = self.view.frame;
//        UIView.beginAnimations("_rect",context:nil);
//        UIView.setAnimationDuration(NSTimeInterval())
//        _rect.origin.y = +120.0;//view往上移动
//        self.view.frame = _rect;
//        updateTextViewSizeForKeyboardHeight(0)
//    }
    
    //弹出框选择按钮触发
    func alertView(alertView: UIAlertView, clickedButtonAtIndex buttonIndex: Int) {
        if(buttonIndex == alertView.cancelButtonIndex){
            
        }else{
            
        }
    }
    
    //压缩图片
    func imageWithImageSimple(image:UIImage,newSize:CGSize)->UIImage{
        UIGraphicsBeginImageContext(newSize);
        image.drawInRect(CGRectMake(0,0,newSize.width,newSize.height));
        let newImage = UIGraphicsGetImageFromCurrentImageContext();
        UIGraphicsEndImageContext();
        return newImage;
    }

    //获取description内容
    func getDescription(str:String)->String {
        var newStr = str.stringByReplacingOccurrencesOfString("\n", withString:" ", options: NSStringCompareOptions.LiteralSearch, range: nil);
        if ((newStr.characters.count) > 50) {
            newStr = "\((newStr as NSString).substringToIndex(50))...";
        }
        return newStr;
    }

    //选中"工作之道"按钮触发
    func btnWork(sender: UIButton){
       channelCheck = "4"
       buttonWork.backgroundColor = UIColor(red: 235.0/255, green: 81.0/255, blue: 11.0/255, alpha: 1.0)
       buttonGirls.backgroundColor = UIColor(red: 151.0/255, green: 151.0/255, blue: 151.0/255, alpha: 1.0)
       buttonExercise.backgroundColor = UIColor(red: 151.0/255, green: 151.0/255, blue: 151.0/255, alpha: 1.0)
       buttonAsk.backgroundColor = UIColor(red: 151.0/255, green: 151.0/255, blue: 151.0/255, alpha: 1.0)
    }
    //选中"热血泡妞"按钮触发
    func btnGirls(sender: UIButton){
        channelCheck = "2"
        buttonWork.backgroundColor = UIColor(red: 151.0/255, green: 151.0/255, blue: 151.0/255, alpha: 1.0)
        buttonGirls.backgroundColor = UIColor(red: 235.0/255, green: 81.0/255, blue: 11.0/255, alpha: 1.0)
        buttonExercise.backgroundColor = UIColor(red: 151.0/255, green: 151.0/255, blue: 151.0/255, alpha: 1.0)
        buttonAsk.backgroundColor = UIColor(red: 151.0/255, green: 151.0/255, blue: 151.0/255, alpha: 1.0)
    }
    //选中"健身无敌"按钮触发
    func btnExercise(sender: UIButton){
        channelCheck = "1"
        buttonWork.backgroundColor = UIColor(red: 151.0/255, green: 151.0/255, blue: 151.0/255, alpha: 1.0)
        buttonGirls.backgroundColor = UIColor(red: 151.0/255, green: 151.0/255, blue: 151.0/255, alpha: 1.0)
        buttonExercise.backgroundColor = UIColor(red: 235.0/255, green: 81.0/255, blue: 11.0/255, alpha: 1.0)
        buttonAsk.backgroundColor = UIColor(red: 151.0/255, green: 151.0/255, blue: 151.0/255, alpha: 1.0)
    }
    //选中"什么要问"按钮触发
    func btnAsk(sender: UIButton){
        channelCheck = "3"
        buttonWork.backgroundColor = UIColor(red: 151.0/255, green: 151.0/255, blue: 151.0/255, alpha: 1.0)
        buttonGirls.backgroundColor = UIColor(red: 151.0/255, green: 151.0/255, blue: 151.0/255, alpha: 1.0)
        buttonExercise.backgroundColor = UIColor(red: 151.0/255, green: 151.0/255, blue: 151.0/255, alpha: 1.0)
        buttonAsk.backgroundColor = UIColor(red: 235.0/255, green: 81.0/255, blue: 11.0/255, alpha: 1.0)
    }


}
