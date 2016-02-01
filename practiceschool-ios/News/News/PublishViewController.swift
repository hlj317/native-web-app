//
//  PublishViewController.swift
//  News
//
//  Created by huanglijun on 15/8/10.
//  Copyright (c) 2015年 huanglijun. All rights reserved.
//

import UIKit

@available(iOS 8.0, *)
class PublishViewController: UIViewController,UITableViewDataSource, UITableViewDelegate {

    let screenWidth = UIScreen.mainScreen().bounds.size.width;      //屏幕宽度
    let screenHeight = UIScreen.mainScreen().bounds.size.height;    //屏幕高度
    var me_userid = NSString();              //获取用户ID
    var dataSource = [];                     //数据源
    var itemCheckStatus:UILabel?;            //文章审核状态
    var itemTitle:UILabel?;                  //文章标题
    var itemDescription:UILabel?;            //文章描述
    var itemImageView:UIImageView?;          //文章封面
    var itemPraize:UILabel?;                 //文章点赞数
    var itemPraizeIcon:UIImageView?;         //文章点赞icon
    var itemReview:UILabel?;                 //文章评论数
    var itemReviewIcon:UIImageView?;         //文章评论icon
    var channel: Int!                        //分类
    var pagestart:Int = 0;                   //起始分页
    let pagesize:Int = 10;                   //每页加载条数
    var isRefreshing = false;                //是否正在刷新
    var isLoading = false;                   //是否正在加载更多
    var publishlist : UITableView?;          //我的发布列表视图
    let requestUrl = "http://120.55.99.230/publishlist";
    var currentNewsDataSource:NSMutableArray!;    //当前表格的数据源
    
    var noticeView : UIImageView?;
    var noticeViewImg : UIImage?;
    var noticeText : UILabel?;
    
    override func viewDidLoad() {
        super.viewDidLoad()
        self.view.backgroundColor = UIColor.whiteColor();
        self.title = "我的发布";
        // Do any additional setup after loading the view.
        
        self.publishlist = UITableView();
        self.publishlist!.frame = CGRectMake(0,65,screenWidth,screenHeight-65);
        self.view.addSubview(self.publishlist!);
        
        //创建一个重用的单元格
        self.publishlist!.registerClass(UITableViewCell.self, forCellReuseIdentifier: "collectcell");
        
        //去除初始化时的表格线条
        self.publishlist!.tableFooterView = UIView();
        
        self.publishlist!.showsVerticalScrollIndicator = false;
        
        self.publishlist!.delegate = self;
        self.publishlist!.dataSource = self;
       
        //列表刷新时调用回调函数
        self.publishlist!.addPullToRefreshWithActionHandler { () -> Void in
            self.loadDataSource();
        }
        
        //列表加载更多时调用回调函数
        self.publishlist!.addInfiniteScrollingWithActionHandler { () -> Void in
            self.loadmoreSource();
        }
        
        //创建没有发布文章提示
        setupNoPublish();
        
        //初始化加载数据源
        loadDataSource()
        
        //页面载入即调用刷新控件
        self.publishlist!.pullToRefreshView.startAnimating();
        
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
        
        return dataSource.count
        
    }
    
    //加载最新数据源
    func loadDataSource() {
        
        //请求参数
        let paramDict = ["pagestart":0, "pagesize":(pagestart + 1)*pagesize,"userid":me_userid];
        
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
                    self.currentNewsDataSource = NSMutableArray();
                    let newsDataSource = json["docs"] as! NSArray;
                    for currentNews : AnyObject in newsDataSource {
                        
                        //创建一个实例对象
                        let newsItem = XHNewsItem();
                        //标题
                        newsItem.newsTitle = currentNews["title"] as! NSString
                        //描述
                        newsItem.newsDescription = currentNews["description"] as! NSString
                        //封面图片
                        newsItem.newsThumb = currentNews["cover"] as! NSString
                        //点赞数
                        newsItem.newsPraizecount = currentNews["praizecount"] as! NSInteger
                        //评论数
                        newsItem.newsReviewcount = currentNews["reviewcount"] as! NSInteger
                        //文章ID号
                        newsItem.newsID = currentNews["_id"] as! NSString
                        //文章审核状态
                        newsItem.newsCheckstatus = currentNews["checkstatus"] as! NSString
                        self.currentNewsDataSource.addObject(newsItem)
                    }
                    
                    dispatch_async(dispatch_get_main_queue(), {
                        //这里返回主线程，写需要主线程执行的代码
                        self.dataSource = self.currentNewsDataSource
                        //获取数据源后，刷新tableView
                        self.publishlist!.reloadData()
                        //结束刷新
                        self.publishlist!.pullToRefreshView.stopAnimating();
                        self.HideNoPublish(true);
                    })
                }else if(newsCode == 1002){
                    dispatch_async(dispatch_get_main_queue(), {
                        //这里返回主线程，写需要主线程执行的代码
                        self.publishlist!.infiniteScrollingView.stopAnimating();
                        self.publishlist!.pullToRefreshView.stopAnimating();
                        self.HideNoPublish(false);
                    })
                }else{
                    dispatch_async(dispatch_get_main_queue(), {
                        //这里返回主线程，写需要主线程执行的代码
                        self.publishlist!.pullToRefreshView.stopAnimating();
                        self.publishlist!.pullToRefreshView.stopAnimating();
                        self.HideNoPublish(false);
                    })
                }
                
            },
            failure: {  (operation: AFHTTPRequestOperation!,
                error: NSError!) in
                print("post请求失败-error: \(error.localizedDescription)")
                dispatch_async(dispatch_get_main_queue(), {
                    //这里返回主线程，写需要主线程执行的代码
                    self.publishlist!.pullToRefreshView.stopAnimating();
                    self.HideNoPublish(false);
                })
        })
        //设置http请求返回格式
        op!.responseSerializer = AFHTTPResponseSerializer()
        op!.start()
    }
    
    //数据加载更多
    func loadmoreSource() {
        
        //请求参数
        let paramDict = ["pagestart":++pagestart, "pagesize":pagesize,"userid":me_userid];
        
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
                    let newsDataSource = json["docs"] as! NSArray;
                    for currentNews : AnyObject in newsDataSource {
                        
                        //创建一个实例对象
                        let newsItem = XHNewsItem();
                        //标题
                        newsItem.newsTitle = currentNews["title"] as! NSString
                        //描述
                        newsItem.newsDescription = currentNews["description"] as! NSString
                        //封面图片
                        newsItem.newsThumb = currentNews["cover"] as! NSString
                        //点赞数
                        newsItem.newsPraizecount = currentNews["praizecount"] as! NSInteger
                        //评论数
                        newsItem.newsReviewcount = currentNews["reviewcount"] as! NSInteger
                        //文章ID号
                        newsItem.newsID = currentNews["_id"] as! NSString
                        //文章审核状态
                        newsItem.newsCheckstatus = currentNews["checkstatus"] as! NSString
                        self.currentNewsDataSource.addObject(newsItem)
                    }
                    
                    dispatch_async(dispatch_get_main_queue(), {
                        //这里返回主线程，写需要主线程执行的代码
                        self.dataSource = self.currentNewsDataSource
                        //获取数据源后，刷新tableView
                        self.publishlist!.reloadData()
                        //结束刷新
                        self.publishlist!.infiniteScrollingView.stopAnimating();
                        self.HideNoPublish(true);
                    })
                }else if(newsCode == 1001){
                    dispatch_async(dispatch_get_main_queue(), {
                        //这里返回主线程，写需要主线程执行的代码
                        self.publishlist!.infiniteScrollingView.stopAnimating();
                        self.publishlist!.showsInfiniteScrolling = false;
                    })
                }else{
                    dispatch_async(dispatch_get_main_queue(), {
                        //这里返回主线程，写需要主线程执行的代码
                        self.publishlist!.infiniteScrollingView.stopAnimating();
                    })
                }
                
            },
            failure: {  (operation: AFHTTPRequestOperation!,
                error: NSError!) in
                print("post请求失败-error: \(error.localizedDescription)")
                dispatch_async(dispatch_get_main_queue(), {
                    //这里返回主线程，写需要主线程执行的代码
                    self.publishlist!.infiniteScrollingView.stopAnimating();
                })
        })
        //设置http请求返回格式
        op!.responseSerializer = AFHTTPResponseSerializer()
        op!.start()
    }
    
    //创建各单元显示内容，该方法是必须实现的方法(创建参数indexPath指定的单元)
    func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        
        //获取单元格对象
        let cell = tableView.dequeueReusableCellWithIdentifier("collectcell", forIndexPath: indexPath) 
        
        //获取每个单元格的数据源，同时转成XHNewsItem类的实例
        let newsItem = dataSource[indexPath.row] as! XHNewsItem
        
        //遍历清除cell中的view
        for currentNews : AnyObject in cell.contentView.subviews{
            currentNews.removeFromSuperview();
        }
        
        //文章审核状态
        itemCheckStatus = UILabel();
        itemCheckStatus!.font = UIFont(name:itemCheckStatus!.font.fontName,size:12.0);
        let itemCheckStatusText = newsItem.newsCheckstatus as String;
        if(itemCheckStatusText == "通过审核"){
           itemCheckStatus!.textColor = UIColor(red: 255.0/255, green: 0.0/255, blue: 0.0/255, alpha: 1.0);
        }else{
            itemCheckStatus!.textColor = UIColor(red: 153.0/255, green: 153.0/255, blue: 153.0/255, alpha: 1.0);
        }
        itemCheckStatus!.text = "(\(itemCheckStatusText))";
        itemCheckStatus!.frame = CGRectMake(10,10,cell.bounds.size.width-20,15);
        
        //文章标题
        itemTitle = UILabel();
        itemTitle!.font = UIFont(name:itemTitle!.font.fontName,size:18.0)  //设置默认字体和字号
        //获取title文字
        let newsTitleText = newsItem.newsTitle as String;
        //设置获取字符串的匹配属性
        let titleAttributes = [NSFontAttributeName: itemTitle!.font]
        let titleOption = NSStringDrawingOptions.UsesLineFragmentOrigin;
        //获取字符串范围
        let titleRect = newsTitleText.boundingRectWithSize(CGSizeMake(cell.bounds.size.width-20, 500),options: titleOption, attributes: titleAttributes, context: nil);
        let newsTitleHeight = titleRect.size.height;
        itemTitle!.text = newsTitleText;
        itemTitle!.frame = CGRectMake(10,25,cell.bounds.size.width-20,newsTitleHeight)
        itemTitle!.numberOfLines = 0;   //不限行
        itemTitle!.sizeToFit();         //自适应尺寸
        
        //文章描述
        itemDescription = UILabel()
        itemDescription!.font = UIFont(name:itemDescription!.font.fontName,size:14.0)  //设置默认字体和字号
        //获取title文字
        let newsDesText = newsItem.newsDescription as String;
        //设置获取字符串的匹配属性
        let desAttributes = [NSFontAttributeName: itemDescription!.font]
        let desOption = NSStringDrawingOptions.UsesLineFragmentOrigin;
        //获取字符串范围
        let desRect = newsDesText.boundingRectWithSize(CGSizeMake(cell.bounds.size.width-20, 500),options: desOption, attributes: desAttributes, context: nil);
        let desItemHeight = desRect.size.height;
        itemDescription!.text = newsDesText;
        itemDescription!.numberOfLines = 0;   //不限行
        itemDescription!.sizeToFit();
        itemDescription!.frame = CGRectMake(10,cell.bounds.size.height-(desItemHeight+33),cell.bounds.size.width-20,desItemHeight)
        
        //文章封面
        itemImageView = UIImageView();
        itemImageView!.sd_setImageWithURL(NSURL(string:newsItem.newsThumb as String)!,placeholderImage: UIImage(named: "cell_photo.jpg"))
        itemImageView!.contentMode = UIViewContentMode.ScaleAspectFill;
        itemImageView!.clipsToBounds = true;
        itemImageView!.frame = CGRectMake(10,newsTitleHeight+35,cell.bounds.size.width-20,cell.bounds.size.height-(newsTitleHeight+desItemHeight+78));
        
        //评论数
        itemReview = UILabel();
        itemReview!.font = UIFont(name:itemReview!.font.fontName,size:12.0);
        let newsReviewText = newsItem.newsReviewcount as Int;
        itemReview!.text = String(newsReviewText);
        itemReview!.frame = CGRectMake(cell.bounds.size.width-30,cell.bounds.size.height-23,20,15);
        
        //评论图标
        itemReviewIcon = UIImageView();
        let _reviewImage = UIImage(named: "review_icon.png");
        itemReviewIcon!.image = _reviewImage;
        itemReviewIcon!.frame = CGRectMake(cell.bounds.size.width-48,cell.bounds.size.height-20,15,11);
        
        //点赞数
        itemPraize = UILabel();
        itemPraize!.font = UIFont(name:itemPraize!.font.fontName,size:12.0);
        let newsPraizeText = newsItem.newsPraizecount as Int;
        itemPraize!.text = String(newsPraizeText);
        itemPraize!.frame = CGRectMake(cell.bounds.size.width-78,cell.bounds.size.height-23,25,15);
        
        //点赞图标
        itemPraizeIcon = UIImageView();
        let _praizeImage = UIImage(named: "praize_icon.png");
        itemPraizeIcon!.image = _praizeImage
        itemPraizeIcon!.frame = CGRectMake(cell.bounds.size.width-93,cell.bounds.size.height-20,12,11);
        
        cell.contentView.addSubview(itemImageView!)
        cell.contentView.addSubview(itemTitle!)
        cell.contentView.addSubview(itemDescription!)
        cell.contentView.addSubview(itemPraizeIcon!)
        cell.contentView.addSubview(itemReviewIcon!)
        cell.contentView.addSubview(itemReview!)
        cell.contentView.addSubview(itemPraize!)
        cell.contentView.addSubview(itemCheckStatus!)
        
        return cell
    }
    
    //设置table行高
    func tableView(tableView: UITableView, heightForRowAtIndexPath indexPath: NSIndexPath) -> CGFloat {
        return 340
    }
    
    //选择一行
    func tableView(tableView: UITableView, didSelectRowAtIndexPath indexPath: NSIndexPath){
        
        let row=indexPath.row as Int
        let data=self.dataSource[row] as! XHNewsItem
        let publishEditView=PublishEditViewController()
        //传值
        publishEditView.articleid=data.newsID
        //获取导航控制器,添加subView,入栈
        self.navigationController!.pushViewController(publishEditView,animated:true);
        tableView.deselectRowAtIndexPath(indexPath,animated:true);
        
    }
    
    //创建没有发布文章提示
    func setupNoPublish(){
 
        noticeView = UIImageView();
        noticeViewImg = UIImage(named: "no_content");
        noticeView!.image = noticeViewImg;
        noticeView!.frame = CGRectMake(screenWidth/2-53,(screenHeight-120)/2-47,107,94);
        noticeView!.hidden = true;
        self.publishlist!.addSubview(noticeView!);

        noticeText = UILabel();
        noticeText!.font = UIFont(name:noticeText!.font.fontName,size:16.0)
        //获取title文字
        let noticeTextLabel = "您还没有发布文章哦";
        //设置获取字符串的匹配属性
        let desAttributes = [NSFontAttributeName: noticeText!.font]
        let desOption = NSStringDrawingOptions.UsesLineFragmentOrigin;
        //获取字符串范围
        let desRect = noticeTextLabel.boundingRectWithSize(CGSizeMake(screenWidth,30),options: desOption, attributes: desAttributes, context: nil);
        let desItemWidth = desRect.size.width;
        noticeText!.text = noticeTextLabel;
        noticeText!.numberOfLines = 0;   //不限行
        noticeText!.sizeToFit();
        noticeText!.frame = CGRectMake(screenWidth/2-desItemWidth/2,screenHeight/2-12,desItemWidth,30);
        noticeText!.hidden = true;
        self.publishlist!.addSubview(noticeText!);
       
    }
    
    //是否显示没有发布文章提示
    func HideNoPublish(hidden:Bool){
        self.noticeView!.hidden = hidden;
        self.noticeText!.hidden = hidden;
    }
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated);
        self.navigationController?.setNavigationBarHidden(false, animated: animated);
        
        //友盟统计
        MobClick.beginLogPageView("PublishList");
    }

    override func viewWillDisappear(animated: Bool) {
        super.viewWillDisappear(animated);
        
        //友盟统计
        MobClick.endLogPageView("PublishList");
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
