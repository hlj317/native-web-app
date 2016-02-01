//
//  UserCenterViewController.swift
//  News
//
//  Created by huanglijun on 15/7/9.
//  Copyright (c) 2015年 huanglijun. All rights reserved.
//

import UIKit

@available(iOS 8.0, *)
class UserCenterViewController: UIViewController,UITextFieldDelegate,UIImagePickerControllerDelegate,UINavigationControllerDelegate,UIPopoverControllerDelegate,UIGestureRecognizerDelegate{
    @IBOutlet weak var loginBtn_small: UIButton!
    @IBOutlet weak var loginNotice: UILabel!
    @IBOutlet weak var defaultCover: UIImageView!
    @IBOutlet weak var userInfoView: UIView!
    @IBOutlet weak var scoreLabel: UILabel!
    @IBOutlet weak var rankLabel: UILabel!
    @IBOutlet weak var usernameLabel: UILabel!
    @IBOutlet weak var sexLabel: UILabel!
    @IBOutlet weak var saveBtN: UIButton!
    @IBOutlet weak var boyBtn: UIButton!
    @IBOutlet weak var girlBtn: UIButton!
    @IBOutlet weak var userScore: UILabel!
    @IBOutlet weak var userRank: UILabel!
    @IBOutlet weak var usernameEdit: UITextField!
    @IBOutlet weak var userPortrait: UIImageView!
    @IBOutlet weak var publishBtn: UIButton!
    @IBOutlet weak var collectBtn: UIButton!
    var userItem = UserItem();   //创建一个文章模型对象
    var sexSelected : String?;   //用户选中性别
    var picker:UIImagePickerController?=UIImagePickerController()
    var popover:UIPopoverController?=nil
    var userPortraitImg:UIImage?;   //上传的图片
    var HUD : MBProgressHUD?;  //弱提示对话框
    var me_userid = ""; // 用户ID号
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        //设置未登录的控件状态
        loginBtn_small.layer.cornerRadius = 5;
        defaultCover.layer.cornerRadius = 28;
        loginBtn_small.addTarget(self, action: "loginBtn_small_Action:", forControlEvents: UIControlEvents.TouchUpInside)
        
        if(Constants["isLogin"] == false){
            let loginPage = loginViewController(nibName:"loginViewController",bundle:nil);
            let navLoginController = UINavigationController(rootViewController:loginPage);
            self.presentViewController(navLoginController, animated: true, completion: nil)
        }
        self.edgesForExtendedLayout = UIRectEdge.None;
        
        usernameEdit.delegate = self;   //为昵称框设置代理
        usernameEdit.returnKeyType = UIReturnKeyType.Done;  //设置键盘为完成状态
        
        collectBtn!.addTarget(self, action: "collectAction:", forControlEvents: UIControlEvents.TouchUpInside)
        publishBtn!.addTarget(self, action: "publishAction:", forControlEvents: UIControlEvents.TouchUpInside)
        
        boyBtn!.addTarget(self, action: "boyAction:", forControlEvents: UIControlEvents.TouchUpInside)
        
        girlBtn!.addTarget(self, action: "girlAction:", forControlEvents: UIControlEvents.TouchUpInside)
        
        saveBtN!.addTarget(self, action: "saveAction:", forControlEvents: UIControlEvents.TouchUpInside)
        saveBtN!.layer.cornerRadius = 3;
        
        //更改头像
        userPortrait!.contentMode = UIViewContentMode.ScaleAspectFill;
        userPortrait!.clipsToBounds = true;
        userPortrait!.layer.cornerRadius = 32;
        picker!.delegate=self;
        userPortrait.userInteractionEnabled = true;
        let singleTap = UITapGestureRecognizer(target: self, action:Selector("userPortraitPicker"));
        singleTap.delegate = self;
        userPortrait.addGestureRecognizer(singleTap);
        // Do any additional setup after loading the view.
        
        //创建弱提示对话框
        setupHUD();
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
    
    override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated);
        self.navigationController?.setNavigationBarHidden(true, animated: animated);
        if((Constants["isLogin"]) == false){
            gotoLogin(false);
        }else{
            gotoLogin(true);
        }
        
        //友盟统计
        MobClick.beginLogPageView("UserCenter");
    }
    
    override func viewWillDisappear(animated: Bool) {
        super.viewWillDisappear(animated);
        
        //友盟统计
        MobClick.endLogPageView("UserCenter");
    }
    
    func saveAction(sender: UIButton){
        if(userPortraitImg == nil){
            userEditInfo();  //更改用户信息(不带头像)
        }else{
            updateuser();  //更改用户信息
        }
    }
    
    func boyAction(sender: UIButton){
        self.boyBtn.setBackgroundImage(UIImage(named: "boy_selected"),forState: UIControlState.Normal);
        self.girlBtn.setBackgroundImage(UIImage(named: "girl_unselected"),forState: UIControlState.Normal);
        self.sexSelected = "boy";
    }
    
    func girlAction(sender: UIButton){
        self.boyBtn.setBackgroundImage(UIImage(named: "boy_unselected"),forState: UIControlState.Normal);
        self.girlBtn.setBackgroundImage(UIImage(named: "girl_selected"),forState: UIControlState.Normal);
        self.sexSelected = "girl";
    }
    
    func collectAction(sender: UIButton){
        let collectView=CollectViewController()
        //传值
        collectView.me_userid=me_userid;
        //打开收藏列表页面，隐藏底部tabbar
        collectView.hidesBottomBarWhenPushed = true;
        //获取导航控制器,添加subView,入栈
        self.navigationController!.pushViewController(collectView,animated:true);
    }
    
    func publishAction(sender: UIButton){
        let publishView=PublishViewController()
        //传值
        publishView.me_userid=me_userid;
        //打开发布列表页面，隐藏底部tabbar
        publishView.hidesBottomBarWhenPushed = true;
        //获取导航控制器,添加subView,入栈
        self.navigationController!.pushViewController(publishView,animated:true);
    }
    
    //加载用户信息
    func loadUser() {
        
        //请求URL
        let requestUrl = "http://120.55.99.230/userinfo";
        
        //请求参数
        let requestParams = ["userid":me_userid];
        
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
                    
                    let infoData = json["userinfo"] as! NSDictionary;
                    
                    self.userItem.username = infoData["username"] as! NSString
                    
                    self.userItem.usersex = infoData["sex"] as! NSString
                    
                    self.userItem.userpic = infoData["userpic"] as! NSString
                    
                    self.userItem.userrank = infoData["rank"] as! NSInteger
                    
                    self.userItem.userscore = infoData["score"] as! NSInteger
                    
                    dispatch_async(dispatch_get_main_queue(), {
                        
                        self.usernameEdit.text = self.userItem.username as String;
                        
                        self.userScore.text = String(self.userItem.userscore as Int);
                        
                        if(self.userItem.usersex == "boy"){
                            self.boyBtn.setBackgroundImage(UIImage(named: "boy_selected"),forState: UIControlState.Normal);
                            self.girlBtn.setBackgroundImage(UIImage(named: "girl_unselected"),forState: UIControlState.Normal);
                            self.sexSelected = "boy";
                        }else{
                            self.boyBtn.setBackgroundImage(UIImage(named: "boy_unselected"),forState: UIControlState.Normal);
                            self.girlBtn.setBackgroundImage(UIImage(named: "girl_selected"),forState: UIControlState.Normal);
                            self.sexSelected = "girl";
                        }
                        
                        self.userPortrait!.sd_setImageWithURL(NSURL(string:self.userItem.userpic as String)!,placeholderImage: UIImage(named: "loading_small"))
                        
                        self.userRank.text = SwitchRankText(self.userItem.userrank);
                        
                        
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
    
    //点击单选文本框触发
    func textFieldShouldBeginEditing(textField: UITextField) -> Bool{
        //解决键盘被遮住问题
        var offset = 0.0 as CGFloat;
        offset = -186.0;
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
    
    
    //拍照和选择图片
    func userPortraitPicker()
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
            popover!.presentPopoverFromRect(userPortrait.frame, inView: self.view, permittedArrowDirections: UIPopoverArrowDirection.Any, animated: true)
        }
    }
    func openCamera()
    {
        if(UIImagePickerController.isSourceTypeAvailable(UIImagePickerControllerSourceType.Camera))
        {
            picker!.allowsEditing = true;
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
            picker!.allowsEditing = true;
            self.presentViewController(picker!, animated: true, completion: nil)
        }
        else
        {
            popover=UIPopoverController(contentViewController: picker!)
            popover!.presentPopoverFromRect(userPortrait.frame, inView: self.view, permittedArrowDirections: UIPopoverArrowDirection.Any, animated: true)
        }
    }
    func imagePickerController(picker: UIImagePickerController, didFinishPickingMediaWithInfo info: [String : AnyObject])
    {
        picker.dismissViewControllerAnimated(true, completion: nil)
        userPortrait.image=info[UIImagePickerControllerEditedImage] as? UIImage
        userPortraitImg = (info[UIImagePickerControllerEditedImage] as? UIImage)!;
        let imgSize = CGSizeMake(100.0, 100.0);
        userPortraitImg = imageWithImageSimple(userPortraitImg!,newSize:imgSize);
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
    
    //更改用户信息(不带头像)
    func userEditInfo(){
        
        self.HUD!.labelText = "正在加载";
        self.HUD!.show(true);
        
        let requestUrl = "http://120.55.99.230/userEditInfo";
        
        let usernameText = usernameEdit.text!;
        
        //请求参数
        let paramDict = ["username":usernameText,"userid":me_userid,"sex":sexSelected!] as NSDictionary;
        
        
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
    
    ////更改用户信息
    func updateuser(){
        
        self.HUD!.labelText = "正在加载";
        self.HUD!.show(true);
        
        let requestUrl = "http://120.55.99.230/updateuser";
        
        let usernameText = usernameEdit.text!;
        
        //请求参数
        let paramDict = ["username":usernameText,"userid":me_userid,"sex":sexSelected!] as NSDictionary;
        
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
        let imageData:NSData = UIImageJPEGRepresentation(userPortraitImg!,0.9)!;
        body.appendData("Content-Type:image/jpeg\r\n\r\n".dataUsingEncoding(NSUTF8StringEncoding)!)
        body.appendData(imageData);
        body.appendData("\r\n".dataUsingEncoding(NSUTF8StringEncoding)!)
        
        //写入尾部
        body.appendData(NSString(format: "--%@--\r\n", boundary).dataUsingEncoding(NSUTF8StringEncoding)!);
        request.HTTPBody = body;
        
        //let urlResponse:NSHTTPURLResponse? = nil;
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
    
    //用户是否登录状态
    func gotoLogin(isLogin:Bool){
        if(isLogin){
            self.view.backgroundColor = UIColor.whiteColor();
            userInfoView.hidden = false;
            scoreLabel.hidden = false;
            rankLabel.hidden = false;
            usernameLabel.hidden = false;
            sexLabel.hidden = false;
            saveBtN.hidden = false;
            boyBtn.hidden = false;
            girlBtn.hidden = false;
            userScore.hidden = false;
            userRank.hidden = false;
            usernameEdit.hidden = false;
            loginBtn_small.hidden = true;
            loginNotice.hidden = true;
            defaultCover.hidden = true;
            me_userid = Constants["app_userid"] as! String;
            if(Constants["loginStatus"] == false){
                loadUser();  //加载用户信息
                Constants["loginStatus"] = true;
            }
        }else{
            self.view.backgroundColor = UIColor(red: 238.0/255, green: 238.0/255, blue: 238.0/255, alpha: 1.0)
            userInfoView.hidden = true;
            scoreLabel.hidden = true;
            rankLabel.hidden = true;
            usernameLabel.hidden = true;
            sexLabel.hidden = true;
            saveBtN.hidden = true;
            boyBtn.hidden = true;
            girlBtn.hidden = true;
            userScore.hidden = true;
            userRank.hidden = true;
            usernameEdit.hidden = true;
            loginBtn_small.hidden = false;
            loginNotice.hidden = false;
            defaultCover.hidden = false;
        }
        
    }
    
    func loginBtn_small_Action(sender: UIButton){
        let loginPage = loginViewController(nibName:"loginViewController",bundle:nil);
        let navLoginController = UINavigationController(rootViewController:loginPage);
        self.presentViewController(navLoginController, animated: true, completion: nil)
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
