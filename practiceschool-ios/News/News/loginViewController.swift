//
//  loginViewController.swift
//  News
//
//  Created by huanglijun on 15/7/27.
//  Copyright (c) 2015年 huanglijun. All rights reserved.
//

import UIKit

@available(iOS 8.0, *)
class loginViewController: UIViewController,UITextFieldDelegate, UIAlertViewDelegate,UIGestureRecognizerDelegate {
    
    @IBOutlet weak var closeBtn: UIButton!
    @IBOutlet weak var gotoRegBtn: UIButton!
    @IBOutlet weak var accountView: UIView!
    @IBOutlet weak var loginBtn: UIButton!
    @IBOutlet weak var accountEdit: UITextField!
    @IBOutlet weak var passwordEdit: UITextField!
    
    let screenWidth = UIScreen.mainScreen().bounds.size.width;      //屏幕宽度
    let screenHeight = UIScreen.mainScreen().bounds.size.height;    //屏幕高度
    var requestUrl = "http://120.55.99.230/login";   //登录请求url
    var HUD : MBProgressHUD?;  //弱提示对话框
    
    override func viewDidLoad() {
        super.viewDidLoad()
        accountView.layer.cornerRadius = 5; //背景视图设置圆角
        loginBtn.layer.cornerRadius = 5;    //按钮设置圆角
        passwordEdit.secureTextEntry = true;   //设置为密码框
        
        //点击用户登录
        loginBtn.addTarget(self, action: "loginBtnAction", forControlEvents: UIControlEvents.TouchUpInside)
        
        //点击跳转注册页面
        gotoRegBtn.addTarget(self, action: "gotoRegBtnAction", forControlEvents: UIControlEvents.TouchUpInside)
        
        //关闭用户登录页面
        closeBtn.addTarget(self, action: "closeBtnAction", forControlEvents: UIControlEvents.TouchUpInside)
        
        //创建弱提示对话框
        setupHUD();
        
        accountEdit.delegate = self;   //为昵称框设置代理
        accountEdit.returnKeyType = UIReturnKeyType.Done;  //设置键盘为完成状态
        passwordEdit.delegate = self;  //为密码框设置代理
        passwordEdit.returnKeyType = UIReturnKeyType.Done;  //设置键盘为完成状态

    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    //创建弱视图提示对话框
    func setupHUD(){
        HUD = MBProgressHUD(view: self.view);
        self.view.addSubview(HUD!);
        HUD!.dimBackground = true;
    }
    
    /**
    隐藏状态栏
    */
    override func prefersStatusBarHidden() -> Bool {
        return true;
    }
    
    //点击单选文本框触发
    func textFieldShouldBeginEditing(textField: UITextField) -> Bool{
        //解决键盘被遮住问题
        var offset = 0.0 as CGFloat;
        if(screenHeight > 666){       //iphone6
            return true;
        }else if(screenHeight > 567){     //iphone5
            offset = -100.0;
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
    
    //关闭用户登录页面
    func closeBtnAction(){
        self.dismissViewControllerAnimated(true, completion: nil);
    }
    
    //跳转到注册页面
    func gotoRegBtnAction(){
        
        //self.dismissViewControllerAnimated(true, completion: nil);
        let regPage = regViewController(nibName:"regViewController",bundle:nil);
        self.navigationController!.pushViewController(regPage,animated:true);

    }
    
    //用户登录
    func loginBtnAction() {

        self.HUD!.labelText = "正在登录中";
        self.HUD!.mode = MBProgressHUDMode.Indeterminate;
        self.HUD!.show(true);
        
        //请求参数
        let accountText = accountEdit.text!;
        let passwordText = passwordEdit.text!;
        let requestParams = ["account":accountText, "password":passwordText];
        
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
                    Constants["app_userid"] = infoData["_id"] as! NSString;
                    Constants["app_usersex"] = infoData["sex"] as! NSString;
                    Constants["app_userpic"] = infoData["userpic"] as! NSString;
                    Constants["app_username"] = infoData["username"] as! NSString;
                    Constants["app_userrank"] = infoData["rank"] as! NSInteger;
                    Constants["app_userrankText"] = SwitchRecommandText(infoData["score"] as! NSInteger) as NSString;
                    Constants["app_userscore"] = infoData["score"] as! NSInteger;
                    Constants["isLogin"] = true;
                    dispatch_async(dispatch_get_main_queue(), {
    
                        //停止加载动画
                        self.HUD!.hide(false);
                        self.dismissViewControllerAnimated(true, completion: nil);
    
                    })
                }else{
                    dispatch_async(dispatch_get_main_queue(), {

                        //错误提示
                        self.HUD!.hide(false);
                        self.HUD!.labelText = "账号或密码错误";
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

                    //错误提示
                    self.HUD!.hide(false);
                    self.HUD!.labelText = "登录失败";
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
    
    override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated);
        self.navigationController?.navigationBarHidden = true;
        if(Constants["isLogin"] == true){
            self.dismissViewControllerAnimated(false, completion: nil);
        }
        
        //友盟统计
        MobClick.beginLogPageView("Login");
    }
    
    override func viewWillDisappear(animated: Bool) {
        super.viewWillDisappear(animated);
        
        //友盟统计
        MobClick.endLogPageView("Login");
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
