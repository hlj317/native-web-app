//
//  TutorDetailViewController.swift
//  News
//
//  Created by huanglijun on 15/8/14.
//  Copyright (c) 2015年 huanglijun. All rights reserved.
//

import UIKit

class TutorDetailViewController: UIViewController {

    let screenWidth = UIScreen.mainScreen().bounds.size.width;      //屏幕宽度
    let screenHeight = UIScreen.mainScreen().bounds.size.height;    //屏幕高度
    
    var tutorid = NSString();   //导师ID号
    var tutorpic : UIImageView = UIImageView();  //导师头像视图
    var tutorBgCover : UIImageView = UIImageView();  //导师背景视图
    var tutorBgCoverHeight : CGFloat?;   //背景视图高度
    var tutorBgImg : UIImage = UIImage();       //导师背景图片
    var tutorname : UILabel = UILabel();  //导师名称
    var tutorButtomView : UIView = UIView();   //背景下方透明图层
    let featureLabel : UILabel = UILabel();  //导师特点(label)
    var feature : UILabel = UILabel();  //导师特点
    let rankLabel : UILabel = UILabel();  //导师等级(label)
    var rank : UILabel = UILabel();  //导师等级
    let weixinLabel : UILabel = UILabel();  //微信号(label)
    var weixin : UILabel = UILabel();  //微信号
    let notice : UILabel = UILabel();  //标语
    var tutorItem = TotursItem();   //创建一个导师模型对象
    var HUD : MBProgressHUD?;  //弱提示对话框
    
    override func viewDidLoad() {
        super.viewDidLoad()
        self.view.backgroundColor = UIColor.whiteColor();
        loadArticle();   //加载文章
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    //加载文章
    func loadArticle() {
        
        //开始加载动画
        
        let requestUrl = "http://120.55.99.230/getTutor";
        
        //请求参数
        let requestParams = ["id":tutorid];
        
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
                    
                    let infoData = json["tutorinfo"] as! NSDictionary;
                    
                    self.tutorItem.totursName = infoData["name"] as! NSString
                    
                    self.tutorItem.totursDescription = infoData["description"] as! NSString
                    
                    self.tutorItem.totursFeature = infoData["feature"] as! NSString
                    
                    self.tutorItem.totursRank = infoData["rank"] as! NSInteger
                    
                    self.tutorItem.totursCover = infoData["cover"] as! NSString
                    
                    self.tutorItem.totursWeixin = infoData["weixin"] as! NSString
                    
                    dispatch_async(dispatch_get_main_queue(), {
                        
                        self.setupTutor();   //创建导师信息
                        
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
    
    //创建导师信息
    func setupTutor(){
        
        //导师背景
        let tutorBgCover = UIImageView();
        let tutorBgImg = UIImage(named: setRandomCover());
        tutorBgCover.image = tutorBgImg;
        tutorBgCoverHeight = screenWidth * 19 / 36;
        tutorBgCover.frame = CGRectMake(0,60,screenWidth,tutorBgCoverHeight!);
        self.view.addSubview(tutorBgCover);
        
        //导师头像
        tutorpic.sd_setImageWithURL(NSURL(string:self.tutorItem.totursCover as String)!,placeholderImage: UIImage(named: "loading_small"))
        tutorpic.contentMode = UIViewContentMode.ScaleAspectFill;
        tutorpic.clipsToBounds = true;
        tutorpic.frame = CGRectMake(tutorBgCover.frame.size.width/2-32,tutorBgCover.frame.size.height/2+28,64,64);
        tutorpic.layer.cornerRadius = 32;
        self.view.addSubview(tutorpic);
        
        //背景底部视图
        tutorButtomView.frame = CGRectMake(0,tutorBgCover.frame.size.height + 20,screenWidth,40);
        tutorButtomView.backgroundColor = UIColor(red: 51.0/255, green: 51.0/255, blue: 51.0/255, alpha: 0.6);
        self.view.addSubview(tutorButtomView);

        //导师名称及描述
        tutorname.font = UIFont.boldSystemFontOfSize(CGFloat(16.0));
        tutorname.textColor = UIColor.whiteColor();
        let tutornameText = "\(self.tutorItem.totursName)  \(self.tutorItem.totursDescription)";
        //设置获取字符串的匹配属性
        let tutornameAttributes = [NSFontAttributeName: tutorname.font]
        let tutornameOption = NSStringDrawingOptions.UsesLineFragmentOrigin;
        //获取字符串范围
        let tutornameRect = tutornameText.boundingRectWithSize(CGSizeMake(screenWidth, 40),options: tutornameOption, attributes: tutornameAttributes, context: nil);
        let tutornameWidth = tutornameRect.size.width;
        tutorname.text = tutornameText;
        tutorname.frame = CGRectMake((screenWidth - tutornameWidth)/2,10,tutornameWidth,20);
        tutorButtomView.addSubview(tutorname);
        
        //特点(label)
        featureLabel.font = UIFont(name:"Heiti SC",size:14.0);
        featureLabel.frame = CGRectMake(35,tutorBgCoverHeight!+75,45,20);
        featureLabel.textColor = UIColor(red: 102.0/255, green: 102.0/255, blue: 102.0/255, alpha: 1.0);
        featureLabel.text = "特点：";
        self.view.addSubview(featureLabel);
        
        //特点
        feature.font = UIFont.boldSystemFontOfSize(CGFloat(14.0));
        feature.textColor = UIColor.blackColor();
        let featureText = self.tutorItem.totursFeature;
        //设置获取字符串的匹配属性
        let featureAttributes = [NSFontAttributeName: feature.font]
        let featureOption = NSStringDrawingOptions.UsesLineFragmentOrigin;
        //获取字符串范围
        let featureRect = featureText.boundingRectWithSize(CGSizeMake(screenWidth - 95,600),options: featureOption, attributes: featureAttributes, context: nil);
        let featureHeight = featureRect.size.height;
        feature.text = featureText as String;
        feature.frame = CGRectMake(80,tutorBgCoverHeight!+75,screenWidth - 95,featureHeight);
        feature.numberOfLines = 0;   //不限行
        feature.sizeToFit();         //自适应尺寸
        self.view.addSubview(feature);
        
        //推荐值(label)
        rankLabel.font = UIFont(name:"Heiti SC",size:14.0);
        rankLabel.frame = CGRectMake(21,tutorBgCoverHeight!+85+featureHeight,65,20);
        rankLabel.textColor = UIColor(red: 102.0/255, green: 102.0/255, blue: 102.0/255, alpha: 1.0);
        rankLabel.text = "推荐值：";
        self.view.addSubview(rankLabel);
        
        //推荐值
        rank.font = UIFont(name:"Heiti SC",size:14.0);
        rank.frame = CGRectMake(80,tutorBgCoverHeight!+85+featureHeight,160,20);
        let rankText = self.tutorItem.totursRank as Int;
        rank.text = SwitchRecommandText(rankText);
        rank.textColor = UIColor(red: 224.0/255, green: 49.0/255, blue: 49.0/255, alpha: 1.0);
        self.view.addSubview(rank);
        
        //微信号(label)
        weixinLabel.font = UIFont(name:"Heiti SC",size:14.0);
        weixinLabel.frame = CGRectMake(21,tutorBgCoverHeight!+115+featureHeight,65,20);
        weixinLabel.textColor = UIColor(red: 102.0/255, green: 102.0/255, blue: 102.0/255, alpha: 1.0);
        weixinLabel.text = "微信号：";
        self.view.addSubview(weixinLabel);
        
        //微信号
        weixin.font = UIFont(name:"Heiti SC",size:14.0);
        weixin.textColor = UIColor.blackColor();
        weixin.frame = CGRectMake(80,tutorBgCoverHeight!+115+featureHeight,160,20);
        weixin.text = self.tutorItem.totursWeixin as String;
        self.view.addSubview(weixin);
        
        //标语
        notice.font = UIFont(name:"Heiti SC",size:14.0);
        notice.textColor = UIColor(red: 170.0/255, green: 170.0/255, blue: 170.0/255, alpha: 1.0);
        notice.frame = CGRectMake(21,tutorBgCoverHeight!+155+featureHeight,screenWidth-42,40);
        notice.text = "请搜索微信号关注导师，届时请注明由【职场修炼学院】APP推荐。"
        notice.numberOfLines = 0;   //不限行
        notice.sizeToFit();         //自适应尺寸
        self.view.addSubview(notice);
        
    }

    
    override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated);
        self.navigationController?.setNavigationBarHidden(false, animated: animated);
        
        //友盟统计
        MobClick.beginLogPageView("TutorDetail");
    }
    
    override func viewWillDisappear(animated: Bool) {
        super.viewWillDisappear(animated);
        
        //友盟统计
        MobClick.endLogPageView("TutorDetail");
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
