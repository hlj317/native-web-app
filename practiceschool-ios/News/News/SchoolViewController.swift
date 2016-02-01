//
//  SchoolViewController.swift
//  News
//
//  Created by huanglijun on 15/7/12.
//  Copyright (c) 2015年 huanglijun. All rights reserved.
//

import UIKit

@available(iOS 8.0, *)
class SchoolViewController: UIViewController,UIScrollViewDelegate {
    var tempArray = NSMutableArray(); //临时数组，存放controller对象
    var topNavButtons: [UIButton]?;   //头部导航按钮数组
    let topTabInfos = [("工作之道",4,"selected"),("热血泡妞",2,"unselected"),("健身无敌",1,"unselected"),("什么要问",3,"unselected")]  //头部导航
    let screenWidth = UIScreen.mainScreen().bounds.size.width;      //屏幕宽度
    let screenHeight = UIScreen.mainScreen().bounds.size.height;    //屏幕高度
    var topLine : UIView?;   //头部导航的下划线
    var scrollView : UIScrollView?;
    var currentPage = 0;

    override func viewDidLoad() {
        super.viewDidLoad()
        
        //创建顶部导航
        setupTopTabbar();
        
        //设置顶部导航位置
        setupFrameTopTabbar();
        
        //画一条线
        setupTopLine();
        
        self.view.backgroundColor = UIColor.whiteColor();
        
        //初始化scrollView
        scrollView = UIScrollView()
        scrollView!.frame = CGRectMake(0, 65, screenWidth, screenHeight-109);
        //为了能让内容横向滚动，设置横向内容宽度为3个页面的宽度总和
        scrollView!.contentSize = CGSizeMake(screenWidth * CGFloat(topTabInfos.count), screenHeight-109)
        scrollView!.pagingEnabled = true
        scrollView!.showsHorizontalScrollIndicator = false
        scrollView!.showsVerticalScrollIndicator = false
        scrollView!.delegate = self;
        
        //兼容IOS7以上，去掉scrollview顶部的空白部分
        if (self.respondsToSelector("setEdgesForExtendedLayout:")){
            self.edgesForExtendedLayout = UIRectEdge.None;
        }
        
        for (index,(_,channel,_)) in topTabInfos.enumerate(){
            let myViewController = RootTableViewController(channel:channel,wrapControl:self);
            //这里需要创建一个临时数组，存放controller对象，不然controller不被引用，会被释放
            tempArray.addObject(myViewController)
            myViewController.view.frame = CGRectMake(CGFloat(index)*screenWidth, 0, CGFloat(scrollView!.frame.size.width), CGFloat(scrollView!.frame.size.height))
            scrollView!.addSubview(myViewController.view)
        }
        self.view.addSubview(scrollView!);
        self.view.sendSubviewToBack(scrollView!);
        
    }
    
    //创建顶部导航
    func setupTopTabbar(){
        
        //创建一个空数组，类型是按钮，用来临时保存生成的按钮
        var buttons: [UIButton] = []
        
        //通过使用enumerate全局函数我们可以为timeButtonInfos创建一个包含index以及数组中元素（也是元祖）的元祖
        for (index,(title,_,status)) in topTabInfos.enumerate(){
            
            //创建一个按钮实例
            let button: UIButton = UIButton();
            button.tag = index //保存按钮的index
            
            //forState表示状态
            button.setTitle("\(title)", forState: UIControlState.Normal)
            button.titleLabel!.font = UIFont(name:button.titleLabel!.font.fontName,size:14.0);
            
            button.setTitleColor(UIColor(red: 139.0/255, green: 132.0/255, blue: 122.0/255, alpha: 1.0), forState: UIControlState.Normal)
            button.setTitleColor(UIColor(red: 224.0/255, green: 49.0/255, blue: 49.0/255, alpha: 1.0), forState: UIControlState.Highlighted)
            
            if(status == "selected"){
                button.setBackgroundImage(UIImage(named: "top_selected.png"),forState: UIControlState.Normal)
                button.setTitleColor(UIColor.whiteColor(), forState: UIControlState.Normal);
                button.setTitleColor(UIColor.whiteColor(), forState: UIControlState.Highlighted);
            }
            
            //我们通过addTarget:action:forControlEvents:方法给button添加了可以响应按下按钮并抬起操作的回调函数"timeButtonTapped"
            button.addTarget(self, action: "topNavSelecte:", forControlEvents: UIControlEvents.TouchUpInside)
            
            //我们将此按钮添加到临时按钮数组中，同时把此按钮添加到当前视图
            buttons.append(button)
            self.view.addSubview(button)
        }
        
        //我们把临时按钮数组赋值给timeButtons，这样在全局类中就可以调用
        topNavButtons = buttons
        
    }
    
    //设置顶部导航位置
    func setupFrameTopTabbar(){
        let gap = CGFloat((self.view.bounds.size.width - 280 )/5)
        for (index, button) in (topNavButtons!).enumerate() {
            let buttonLeft = gap * CGFloat(index+1) + 68 * CGFloat(index)
            button.frame = CGRectMake(buttonLeft,24,68,32)
        }
    }
    
    //创建顶部导航下划线
    func setupTopLine(){
        topLine = UIView();
        topLine!.backgroundColor = UIColor(red: 225.0/255, green: 225.0/255, blue: 225.0/255, alpha: 1.0);
        topLine!.frame = CGRectMake(0,64,screenWidth,1);
        self.view.addSubview(topLine!);
    }
    
    //头部导航按钮选择
    func topNavSelecte(sender: UIButton) {
        let index:Int = sender.tag;
        scrollView!.setContentOffset(CGPoint(x:screenWidth * CGFloat(index),y:0.0),animated:true);
        //var val:String = calButtonInfos[sender.tag];
        buttonSelected(index);
        currentPage = index;
    }
    
    //设置头部选中状态
    func buttonSelected(index:Int){
        let _index = index;
        for(index,button) in (topNavButtons!).enumerate(){
            if(index == _index){
                button.setBackgroundImage(UIImage(named: "top_selected.png"),forState: UIControlState.Normal)
                button.setTitleColor(UIColor.whiteColor(), forState: UIControlState.Normal);
                button.setTitleColor(UIColor.whiteColor(), forState: UIControlState.Highlighted);
            }else{
                button.setBackgroundImage(nil,forState: UIControlState.Normal)
                button.setTitleColor(UIColor.blackColor(), forState: UIControlState.Normal);
                button.setTitleColor(UIColor(red: 224.0/255, green: 49.0/255, blue: 49.0/255, alpha: 1.0), forState: UIControlState.Highlighted)
            }
        }
    }
    
    //即将滑动到下个页面触发的回调函数
    func scrollViewWillEndDragging(scrollView: UIScrollView, withVelocity velocity: CGPoint, targetContentOffset: UnsafeMutablePointer<CGPoint>){
        if(velocity.x>0 && currentPage < 3){
            ++currentPage;
            buttonSelected(currentPage);
        }else if(velocity.x<0 && currentPage > 0){
            --currentPage;
            buttonSelected(currentPage);
        }
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated);
        self.navigationController?.setNavigationBarHidden(true, animated: animated);
    }
    
    override func viewWillDisappear(animated: Bool) {
        super.viewWillDisappear(animated);
        //self.navigationController?.setNavigationBarHidden(true, animated: animated);
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
