//
//  regViewController.swift
//  News
//
//  Created by huanglijun on 15/7/27.
//  Copyright (c) 2015年 huanglijun. All rights reserved.
//

import UIKit

@available(iOS 8.0, *)
class regViewController: UIViewController,UITextViewDelegate,UITextFieldDelegate,UIAlertViewDelegate,UIImagePickerControllerDelegate,UINavigationControllerDelegate,UIPopoverControllerDelegate,UIGestureRecognizerDelegate {

    @IBOutlet weak var username: UITextField!
    @IBOutlet weak var boySelectBtn: UIButton!
    @IBOutlet weak var girlSelectBtn: UIButton!
    @IBOutlet weak var closeBtn: UIButton!
    @IBOutlet weak var gotoLoginBtn: UIButton!
    @IBOutlet weak var regBtn: UIButton!
    @IBOutlet weak var passwordEdit: UITextField!
    @IBOutlet weak var accountEdit: UITextField!
    @IBOutlet weak var accountView: UIView!
    @IBOutlet weak var userPicBtn: UIImageView!
    
    var requestUrl = "http://120.55.99.230/register";
    let screenWidth = UIScreen.mainScreen().bounds.size.width;      //屏幕宽度
    let screenHeight = UIScreen.mainScreen().bounds.size.height;    //屏幕高度
    var picker:UIImagePickerController?=UIImagePickerController()
    var popover:UIPopoverController?=nil
    var uploadImg:UIImage?;   //上传的图片
    var sexChecked = "boy";   //用户选中性别
    var usernameText = "";     //昵称框内容
    var accountEditText = "";  //用户框内容
    var passwordEditText = "";  //密码框内容
    var HUD : MBProgressHUD?;  //弱提示对话框
    
    override func viewDidLoad() {
        super.viewDidLoad()
        accountView.layer.cornerRadius = 5; //背景视图设置圆角
        regBtn.layer.cornerRadius = 5;   //按钮设置圆角
        passwordEdit.secureTextEntry = true;   //设置为密码框
        
        userPicBtn!.layer.cornerRadius = 44;  //上传照片设置圆角
        
        username.delegate = self;   //为昵称框设置代理
        username.returnKeyType = UIReturnKeyType.Done;  //设置键盘为完成状态
        accountEdit.delegate = self;   //为用户框设置代理
        accountEdit.returnKeyType = UIReturnKeyType.Done;  //设置键盘为完成状态
        passwordEdit.delegate = self;  //为密码框设置代理
        passwordEdit.returnKeyType = UIReturnKeyType.Done;  //设置键盘为完成状态
        
        //点击用户注册
        regBtn.addTarget(self, action: "regBtnAction", forControlEvents: UIControlEvents.TouchUpInside)
        
        //点击跳转登录页面
        gotoLoginBtn.addTarget(self, action: "gotoLoginBtnAction", forControlEvents: UIControlEvents.TouchUpInside)
        
        //关闭用户注册页面
        closeBtn.addTarget(self, action: "closeBtnAction", forControlEvents: UIControlEvents.TouchUpInside)
        
        //选中男孩
        boySelectBtn.addTarget(self, action: "boySelectAction", forControlEvents: UIControlEvents.TouchUpInside)
        
        //选中女孩
        girlSelectBtn.addTarget(self, action: "girlSelectAction", forControlEvents: UIControlEvents.TouchUpInside)
        
        //拍照或选择本地图片
        userPicBtn.contentMode = UIViewContentMode.ScaleAspectFill;
        userPicBtn.clipsToBounds = true;
        picker!.delegate=self
        userPicBtn.userInteractionEnabled = true;
        let singleTap = UITapGestureRecognizer(target: self, action:Selector("userPicBtnPicker"));
        singleTap.delegate = self;
        userPicBtn.addGestureRecognizer(singleTap);
        
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
    
    //选中男孩
    func boySelectAction(){
        self.sexChecked = "boy";
        boySelectBtn.setBackgroundImage(UIImage(named: "reg_boy_selected"),forState: UIControlState.Normal);
        girlSelectBtn.setBackgroundImage(UIImage(named: "reg_girl_unselected"),forState: UIControlState.Normal);
    }
    
    //选中女孩
    func girlSelectAction(){
        self.sexChecked = "girl";
        girlSelectBtn.setBackgroundImage(UIImage(named: "reg_girl_selected"),forState: UIControlState.Normal);
        boySelectBtn.setBackgroundImage(UIImage(named: "reg_boy_unselected"),forState: UIControlState.Normal);
    }
    
    //跳转到登录页面
    func gotoLoginBtnAction(){
        self.navigationController?.popViewControllerAnimated(true);
    }
    
    //关闭用户登录页面
    func closeBtnAction(){
        self.navigationController?.popViewControllerAnimated(true);
    }
    
    //点击单选文本框触发
    func textFieldShouldBeginEditing(textField: UITextField) -> Bool{

        var offset = 0.0 as CGFloat;
        if(screenHeight > 666){       //iphone6
            return true;
        }else if(screenHeight > 567){     //iphone5
            offset = -100.0;
        }else{                //iphone4
            offset = -180.0;
        }
        //解决键盘被遮住问题
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
        
        if(screenHeight > 568){
            return true;
        }
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

    //用户注册
    func regBtnAction(){
        
        //获取用户表单信息
        usernameText = username.text!;
        accountEditText = accountEdit.text!;
        passwordEditText = passwordEdit.text!;
        
        if (uploadImg == nil) {
            self.HUD!.labelText = "请上传您的头像";
            self.HUD!.mode = MBProgressHUDMode.Text;
            self.HUD!.showAnimated(true, whileExecutingBlock:  { () -> Void in
                sleep(1);
            })
            return;
        }else if(usernameText == ""){
            self.HUD!.labelText = "昵称不能为空";
            self.HUD!.mode = MBProgressHUDMode.Text;
            self.HUD!.showAnimated(true, whileExecutingBlock:  { () -> Void in
                sleep(1);
            })
            return;
        }else if(usernameText.characters.count > 15){
            self.HUD!.labelText = "昵称不能超过15位";
            self.HUD!.mode = MBProgressHUDMode.Text;
            self.HUD!.showAnimated(true, whileExecutingBlock:  { () -> Void in
                sleep(2);
            })
            return;
        }else if(accountEditText == ""){
            self.HUD!.labelText = "密码不能为空";
            self.HUD!.mode = MBProgressHUDMode.Text;
            self.HUD!.showAnimated(true, whileExecutingBlock:  { () -> Void in
                sleep(1);
            })
            return;
        }else if(accountEditText == ""){
            self.HUD!.labelText = "账号不能为空";
            self.HUD!.mode = MBProgressHUDMode.Text;
            self.HUD!.showAnimated(true, whileExecutingBlock:  { () -> Void in
                sleep(1);
            })
            return;
        }else if(accountEditText.characters.count < 3 || accountEditText.characters.count > 10){
            self.HUD!.labelText = "账号过长，请输入3至10位";
            self.HUD!.mode = MBProgressHUDMode.Text;
            self.HUD!.showAnimated(true, whileExecutingBlock:  { () -> Void in
                sleep(2);
            })
            return;
        }else if(passwordEditText.characters.count < 6 || passwordEditText.characters.count > 15){
            self.HUD!.labelText = "密码过长，请输入6至15位";
            self.HUD!.mode = MBProgressHUDMode.Text;
            self.HUD!.showAnimated(true, whileExecutingBlock:  { () -> Void in
                sleep(2);
            })
            return;
        }
        
        //开始加载动画
        self.HUD!.labelText = "正在注册中";
        self.HUD!.mode = MBProgressHUDMode.Indeterminate;
        self.HUD!.show(true);
    
        //请求参数
        let paramDict = ["username":usernameText,"account":accountEditText,"password":passwordEditText,"sex":sexChecked] as NSDictionary;

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
        
        NSURLConnection.sendAsynchronousRequest(request, queue: NSOperationQueue()) { (response, data, error) -> Void in
            if(error == nil){
                let json:NSDictionary = (try! NSJSONSerialization.JSONObjectWithData(data!, options: NSJSONReadingOptions.MutableContainers)) as! NSDictionary;
                
                //获取请求返回号
                let newsCode = json["resultCode"] as! NSInteger;
                
                if(newsCode == 1000){
                    let infoData = json["userinfo"] as! NSDictionary;
                    Constants["app_userid"] = infoData["_id"] as! NSString;
                    Constants["app_usersex"] = infoData["sex"] as! NSString;
                    Constants["app_userpic"] = infoData["userpic"] as! NSString;
                    Constants["app_username"] = infoData["username"] as! NSString;
                    Constants["app_userrank"] = infoData["rank"] as! NSInteger;
                    Constants["app_userrankText"] = SwitchRecommandText(infoData["score"] as! NSInteger) as NSString;
                    Constants["app_userscore"] = infoData["score"] as! NSInteger;
                    Constants["isLogin"] = true;
                }
                
                dispatch_async(dispatch_get_main_queue(), {
                    if(newsCode == 1000){
                        self.HUD!.hide(false);
                        //跳转到个人中心页面
                        self.navigationController!.popToRootViewControllerAnimated(true);
                        
                    }else if(newsCode == 1001){
                        self.HUD!.hide(false);
                        self.HUD!.labelText = "账号重复，请重新填写";
                        self.HUD!.mode = MBProgressHUDMode.Text;
                        self.HUD!.showAnimated(true, whileExecutingBlock:  { () -> Void in
                            sleep(1);
                        })
                    }else if(newsCode == 1002){
                        self.HUD!.hide(false);
                        self.HUD!.labelText = "昵称重复，请重新填写";
                        self.HUD!.mode = MBProgressHUDMode.Text;
                        self.HUD!.showAnimated(true, whileExecutingBlock:  { () -> Void in
                            sleep(1);
                        })
                    }else if(newsCode == 1003){
                        self.HUD!.hide(false);
                        self.HUD!.labelText = "同一个IP，一小时之内只能注册一次";
                        self.HUD!.mode = MBProgressHUDMode.Text;
                        self.HUD!.showAnimated(true, whileExecutingBlock:  { () -> Void in
                            sleep(1);
                        })
                    }else{
                        //停止加载动画
                        self.HUD!.hide(false);
                        self.HUD!.labelText = "注册失败";
                        self.HUD!.mode = MBProgressHUDMode.Text;
                        self.HUD!.showAnimated(true, whileExecutingBlock:  { () -> Void in
                            sleep(1);
                        })
                    }

                })
            }else{
                dispatch_async(dispatch_get_main_queue(), {
                    print("注册失败");
                    self.HUD!.hide(false);
                    self.HUD!.labelText = "注册失败";
                    self.HUD!.mode = MBProgressHUDMode.Text;
                    self.HUD!.showAnimated(true, whileExecutingBlock:  { () -> Void in
                        sleep(1);
                    })

                })
            }
        }

    }
    
    //拍照和选择图片
    func userPicBtnPicker()
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
            popover!.presentPopoverFromRect(userPicBtn.frame, inView: self.view, permittedArrowDirections: UIPopoverArrowDirection.Any, animated: true)
        }
    }
    func openCamera()
    {
        if(UIImagePickerController .isSourceTypeAvailable(UIImagePickerControllerSourceType.Camera))
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
            popover!.presentPopoverFromRect(userPicBtn.frame, inView: self.view, permittedArrowDirections: UIPopoverArrowDirection.Any, animated: true)
        }
    }
    func imagePickerController(picker: UIImagePickerController, didFinishPickingMediaWithInfo info: [String : AnyObject])
    {
        picker.dismissViewControllerAnimated(true, completion: nil)
        userPicBtn.image=info[UIImagePickerControllerEditedImage] as? UIImage
        uploadImg = (info[UIImagePickerControllerEditedImage] as? UIImage)!;
        let imgSize = CGSizeMake(100.0, 100.0);
        uploadImg = imageWithImageSimple(uploadImg!,newSize:imgSize);
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
    
    /**
    隐藏状态栏
    */
    override func prefersStatusBarHidden() -> Bool {
        return true;
    }
    
    override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated);
        self.navigationController?.navigationBarHidden = true;
        
        //友盟统计
        MobClick.beginLogPageView("Register");
    }
    
    override func viewWillDisappear(animated: Bool) {
        super.viewWillDisappear(animated);
        
        //友盟统计
        MobClick.endLogPageView("Register");
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
