//
//  TutorWrapperController.swift
//  News
//
//  Created by huanglijun on 15/7/20.
//  Copyright (c) 2015年 huanglijun. All rights reserved.
//

import UIKit

class TutorWrapperController: UIViewController {
    var tempArray = NSMutableArray();  //临时数组，存放controller对象
    var tableTitle:UILabel?;           //表格头部标题
    let screenWidth = UIScreen.mainScreen().bounds.size.width;      //屏幕宽度
    let screenHeight = UIScreen.mainScreen().bounds.size.height;    //屏幕高度
    
    override func viewDidLoad() {
        super.viewDidLoad()
       
        self.view.backgroundColor = UIColor.whiteColor();
        
        //兼容IOS7以上，去掉scrollview顶部的空白部分
        if (self.respondsToSelector("setEdgesForExtendedLayout:")){
            self.edgesForExtendedLayout = UIRectEdge.None;
        }
        
        //设置表头
        tableTitle = UILabel();
        tableTitle!.font = UIFont(name:tableTitle!.font.fontName,size:14.0)
        tableTitle!.text = "推荐导师.Recommend tutors";
        tableTitle!.textColor = UIColor(red: 51.0/255, green: 51.0/255, blue: 51.0/255, alpha: 1.0);
        tableTitle!.frame = CGRectMake(10,30,200,30);
        self.view.addSubview(tableTitle!);
        
        let myViewController = TutorViewController(wrapControl:self);
        //这里需要创建一个临时数组，存放controller对象，不然controller不被引用，会被释放
        tempArray.addObject(myViewController)
        myViewController.view.frame = CGRectMake(0,55,screenWidth,screenHeight-55);
        
        self.view.addSubview(myViewController.view);
        self.view.sendSubviewToBack(myViewController.view);
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated);
        self.navigationController?.setNavigationBarHidden(true, animated: animated);
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
