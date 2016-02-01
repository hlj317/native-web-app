//
//  ContactViewController.swift
//  News
//
//  Created by huanglijun on 15/7/11.
//  Copyright (c) 2015年 huanglijun. All rights reserved.
//

import UIKit

class ContactViewController: UIViewController {
    
    let screenWidth = UIScreen.mainScreen().bounds.size.width;      //屏幕宽度
    let screenHeight = UIScreen.mainScreen().bounds.size.height;    //屏幕高度
    var scrollView : UIScrollView?;

    override func viewDidLoad() {
        super.viewDidLoad()
        
        self.view.backgroundColor = UIColor.whiteColor();
        
        //初始化scrollView
        scrollView = UIScrollView()
        scrollView!.frame = CGRectMake(0,35,screenWidth,screenHeight-94);
        
        scrollView!.showsHorizontalScrollIndicator = false
        scrollView!.showsVerticalScrollIndicator = false
        
        //兼容IOS7以上，去掉scrollview顶部的空白部分
        if (self.respondsToSelector("setEdgesForExtendedLayout:")){
            self.edgesForExtendedLayout = UIRectEdge.None;
        }
        
        //标题一
        let title_one = UILabel();
        title_one.textColor = UIColor(red: 224.0/255, green: 49.0/255, blue: 49.0/255, alpha: 1.0);
        title_one.font = UIFont(name:title_one.font.fontName,size:16.0);
        title_one.text = "1.学院宗旨：";
        title_one.frame = CGRectMake(15,0,screenWidth-30,30);
        scrollView!.addSubview(title_one);
        
        //内容一
        let content_one = UILabel()
        content_one.font = UIFont(name:content_one.font.fontName,size:14.0);
        //获取title文字
        let content_one_text = "在这里，扯扯淡，吐吐槽，每天开心一点点，每天进步一点点！";
        //设置获取字符串的匹配属性
        let content_one_attributes = [NSFontAttributeName: content_one.font]
        let content_one_option = NSStringDrawingOptions.UsesLineFragmentOrigin;
        //获取字符串范围
        let content_one_rect = content_one_text.boundingRectWithSize(CGSizeMake(screenWidth-30,800),options: content_one_option, attributes: content_one_attributes, context: nil);
        let content_one_height = content_one_rect.size.height;
        content_one.text = content_one_text;
        content_one.numberOfLines = 0;   //不限行
        content_one.sizeToFit();
        content_one.frame = CGRectMake(15,30,screenWidth-30,content_one_height)
        scrollView!.addSubview(content_one);
        
        
        //标题二
        let title_two = UILabel();
        title_two.textColor = UIColor(red: 224.0/255, green: 49.0/255, blue: 49.0/255, alpha: 1.0);
        title_two.font = UIFont(name:title_two.font.fontName,size:16.0);
        title_two.text = "2.积分规则：";
        title_two.frame = CGRectMake(15,45+content_one_height,screenWidth-30,30);
        scrollView!.addSubview(title_two);
        
        //内容二
        let content_two = UILabel()
        content_two.font = UIFont(name:content_two.font.fontName,size:14.0);
        //获取title文字
        let content_two_text = "发布文章通过审核+50分；评论+2分；别人为你的评论点亮+1分；别人为你的文章点赞+1分。";
        //设置获取字符串的匹配属性
        let content_two_attributes = [NSFontAttributeName: content_two.font]
        let content_two_option = NSStringDrawingOptions.UsesLineFragmentOrigin;
        //获取字符串范围
        let content_two_rect = content_two_text.boundingRectWithSize(CGSizeMake(screenWidth-30,800),options: content_two_option, attributes: content_two_attributes, context: nil);
        let content_two_height = content_two_rect.size.height;
        content_two.text = content_two_text;
        content_two.numberOfLines = 0;   //不限行
        content_two.sizeToFit();
        content_two.frame = CGRectMake(15,75+content_one_height,screenWidth-30,content_two_height)
        scrollView!.addSubview(content_two);
        
        
        //标题三
        let title_three = UILabel();
        title_three.textColor = UIColor(red: 224.0/255, green: 49.0/255, blue: 49.0/255, alpha: 1.0);
        title_three.font = UIFont(name:title_three.font.fontName,size:16.0);
        title_three.text = "3.等级制度：";
        title_three.frame = CGRectMake(15,90+content_one_height+content_two_height,screenWidth-30,30);
        scrollView!.addSubview(title_three);
        
        //内容三
        let content_three = UILabel()
        content_three.font = UIFont(name:content_three.font.fontName,size:14.0);
        //获取title文字
        let content_three_text = "实习生→正式员工→主管→经理→区域经理→高级经理→总监→高级总监→副总→总经理→CEO，朝着CEO的目标前进吧！等级在后面的优化版本中会起到非常核心的作用，大家可以期待！";
        //设置获取字符串的匹配属性
        let content_three_attributes = [NSFontAttributeName: content_three.font]
        let content_three_option = NSStringDrawingOptions.UsesLineFragmentOrigin;
        //获取字符串范围
        let content_three_rect = content_three_text.boundingRectWithSize(CGSizeMake(screenWidth-30,800),options: content_three_option, attributes: content_three_attributes, context: nil);
        let content_three_height = content_three_rect.size.height;
        content_three.text = content_three_text;
        content_three.numberOfLines = 0;   //不限行
        content_three.sizeToFit();
        content_three.frame = CGRectMake(15,120+content_one_height+content_two_height,screenWidth-30,content_three_height)
        scrollView!.addSubview(content_three);
        
        
        //标题四
        let title_four = UILabel();
        title_four.textColor = UIColor(red: 224.0/255, green: 49.0/255, blue: 49.0/255, alpha: 1.0);
        title_four.font = UIFont(name:title_four.font.fontName,size:16.0);
        title_four.text = "4.强大的导师团：";
        title_four.frame = CGRectMake(15,135+content_one_height+content_two_height+content_three_height,screenWidth-30,30);
        scrollView!.addSubview(title_four);
        
        //内容四
        let content_four = UILabel()
        content_four.font = UIFont(name:content_four.font.fontName,size:14.0);
        //获取title文字
        let content_four_text = "导师团的成员会不断增加，相信不管是在职场的道路上，抑或在人生的旅途中，都会给大家带来巨大的帮助！";
        //设置获取字符串的匹配属性
        let content_four_attributes = [NSFontAttributeName: content_four.font]
        let content_four_option = NSStringDrawingOptions.UsesLineFragmentOrigin;
        //获取字符串范围
        let content_four_rect = content_four_text.boundingRectWithSize(CGSizeMake(screenWidth-30,800),options: content_four_option, attributes: content_four_attributes, context: nil);
        let content_four_height = content_four_rect.size.height;
        content_four.text = content_four_text;
        content_four.numberOfLines = 0;   //不限行
        content_four.sizeToFit();
        content_four.frame = CGRectMake(15,165+content_one_height+content_two_height+content_three_height,screenWidth-30,content_four_height)
        scrollView!.addSubview(content_four);
        
        
        //标题五
        let title_five = UILabel();
        title_five.textColor = UIColor(red: 224.0/255, green: 49.0/255, blue: 49.0/255, alpha: 1.0);
        title_five.font = UIFont(name:title_five.font.fontName,size:16.0);
        title_five.text = "5.导师寄语：";
        title_five.frame = CGRectMake(15,180+content_one_height+content_two_height+content_three_height+content_four_height,screenWidth-30,30);
        scrollView!.addSubview(title_five);
        
        //内容五
        let content_five = UILabel()
        content_five.font = UIFont(name:content_five.font.fontName,size:14.0);
        //获取title文字
        let content_five_text = "最后，希望大家在不断提高自己职场素养的同时，能够玩得开心，这才是最重要的！";
        //设置获取字符串的匹配属性
        let content_five_attributes = [NSFontAttributeName: content_five.font]
        let content_five_option = NSStringDrawingOptions.UsesLineFragmentOrigin;
        //获取字符串范围
        let content_five_rect = content_five_text.boundingRectWithSize(CGSizeMake(screenWidth-30,800),options: content_five_option, attributes: content_five_attributes, context: nil);
        let content_five_height = content_five_rect.size.height;
        content_five.text = content_five_text;
        content_five.numberOfLines = 0;   //不限行
        content_five.sizeToFit();
        content_five.frame = CGRectMake(15,210+content_one_height+content_two_height+content_three_height+content_four_height,screenWidth-30,content_five_height)
        scrollView!.addSubview(content_five);
        
        
        //底图
//        let buttonImageView = UIImageView();
//        let buttonIconImage = UIImage(named: "contact_bg.jpg");
//        buttonImageView.image = buttonIconImage;
//        buttonImageView.frame = CGRectMake(screenWidth/2 - buttonIconImage!.size.width/2,220+content_one_height+content_two_height+content_three_height+content_four_height+content_five_height,buttonIconImage!.size.width,buttonIconImage!.size.height);
//        scrollView!.addSubview(buttonImageView);
        
        scrollView!.contentSize = CGSizeMake(screenWidth,220+content_one_height+content_two_height+content_three_height+content_four_height+content_five_height)
        self.view.addSubview(scrollView!);
        self.view.sendSubviewToBack(scrollView!);
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    override func viewDidAppear(animated: Bool) {
        super.viewDidAppear(animated);
        //self.navigationController?.navigationBarHidden = true;
    }
    
    override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated);
        self.navigationController?.setNavigationBarHidden(true, animated: animated);
        
        //友盟统计
        MobClick.beginLogPageView("Contact");
    }
    
    override func viewWillDisappear(animated: Bool) {
        super.viewDidDisappear(animated);
        
        //友盟统计
        MobClick.endLogPageView("Contact");
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
