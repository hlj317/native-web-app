//
//  RootTableViewController.swift
//  News
//
//  Created by huanglijun on 15/6/27.
//  Copyright (c) 2015年 huanglijun. All rights reserved.
//

import UIKit

@available(iOS 8.0, *)
class RootTableViewController: UITableViewController{

    var dataSource = [];                     //数据源
    var itemTitle:UILabel?;                  //文章标题
    var itemDescription:UILabel?;            //文章描述
    var itemImageView:UIImageView?;          //文章封面
    var itemPraize:UILabel?;                 //文章点赞数
    var itemPraizeIcon:UIImageView?;         //文章点赞icon
    var itemReview:UILabel?;                 //文章评论数
    var itemReviewIcon:UIImageView?;         //文章评论icon
    var channel: Int!                        //分类
    var pagestart:Int = 0;                   //起始分页
    let pagesize:Int = 4;                    //每页加载条数
    var isRefreshing = false;                //是否正在刷新
    var isLoading = false;                   //是否正在加载更多
    var currentNewsDataSource:NSMutableArray!;              //当前表格的数据源
    var wrapControl:SchoolViewController?;   //SchoolViewController，用来入栈和出栈
    
    //url请求地址
    let hackerNewsApiUrl = "http://120.55.99.230/articlelist"
    
    init(channel: Int,wrapControl:SchoolViewController){
        super.init(nibName: nil, bundle: nil);
        self.channel = channel;
        self.wrapControl = wrapControl;
    }
    
    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        //创建一个重用的单元格
        self.tableView!.registerClass(UITableViewCell.self, forCellReuseIdentifier: "cell\(channel)");

        //去除初始化时的表格线条
        self.tableView.tableFooterView = UIView();
        
        //列表刷新时调用回调函数
        self.tableView!.addPullToRefreshWithActionHandler { () -> Void in
            self.loadDataSource();
        }
 
        //列表加载更多时调用回调函数
        self.tableView!.addInfiniteScrollingWithActionHandler { () -> Void in
            self.loadmoreSource();
        }

        //初始化加载数据源
        loadDataSource()
        
        //页面载入即调用刷新控件
        self.tableView!.pullToRefreshView.startAnimating();
        
    }
    
    // #pragma mark - Table view data source
    
    //返回section的个数
    override func numberOfSectionsInTableView(tableView: UITableView) -> Int {
        // #warning Potentially incomplete method implementation.
        // Return the number of sections.
        
        return 1
    }
    
    //返回行数
    override func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        // #warning Incomplete method implementation.
        // Return the number of rows in the section.
 
        return dataSource.count
        
    }
    
    //加载最新数据源
    func loadDataSource() {

        //请求参数
        let paramDict = ["pagestart":0, "pagesize":(pagestart + 1)*pagesize, "channel":channel, "sort":1];
        
        //创建一个http请求实例，这里用第三方oc库AFNetworking
        let afManager = AFHTTPRequestOperationManager()
        let op =  afManager.POST(hackerNewsApiUrl,
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
                        self.currentNewsDataSource.addObject(newsItem)
                    }
                    
                    dispatch_async(dispatch_get_main_queue(), {
                        //这里返回主线程，写需要主线程执行的代码
                        self.dataSource = self.currentNewsDataSource
                        //获取数据源后，刷新tableView
                        self.tableView.reloadData()
                        //结束刷新
                        self.tableView!.pullToRefreshView.stopAnimating();
                    })
                }else{
                    dispatch_async(dispatch_get_main_queue(), {
                        //这里返回主线程，写需要主线程执行的代码
                        self.tableView!.pullToRefreshView.stopAnimating();
                    })
                }

            },
            failure: {  (operation: AFHTTPRequestOperation!,
                error: NSError!) in
                print("post请求失败-error: \(error.localizedDescription)")
                dispatch_async(dispatch_get_main_queue(), {
                    //这里返回主线程，写需要主线程执行的代码
                    self.tableView!.pullToRefreshView.stopAnimating();
                })
        })
        //设置http请求返回格式
        op!.responseSerializer = AFHTTPResponseSerializer()
        op!.start()
    }
    
    //数据加载更多
    func loadmoreSource() {

        //请求参数
        let paramDict = ["pagestart":++pagestart, "pagesize":pagesize, "channel":channel, "sort":1];
        
        //创建一个http请求实例，这里用第三方oc库AFNetworking
        let afManager = AFHTTPRequestOperationManager()
        let op =  afManager.POST(hackerNewsApiUrl,
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
                        self.currentNewsDataSource.addObject(newsItem)
                    }
                    
                    dispatch_async(dispatch_get_main_queue(), {
                        //这里返回主线程，写需要主线程执行的代码
                        self.dataSource = self.currentNewsDataSource
                        //获取数据源后，刷新tableView
                        self.tableView.reloadData()
                        //结束刷新
                        self.tableView!.infiniteScrollingView.stopAnimating();
                    })
                }else if(newsCode == 1001){
                    dispatch_async(dispatch_get_main_queue(), {
                        //这里返回主线程，写需要主线程执行的代码
                        self.tableView!.infiniteScrollingView.stopAnimating();
                        self.tableView!.showsInfiniteScrolling = false;
                    })
                }else{
                    dispatch_async(dispatch_get_main_queue(), {
                        //这里返回主线程，写需要主线程执行的代码
                        self.tableView!.infiniteScrollingView.stopAnimating();
                    })
                }
                
            },
            failure: {  (operation: AFHTTPRequestOperation!,
                error: NSError!) in
                print("post请求失败-error: \(error.localizedDescription)")
                dispatch_async(dispatch_get_main_queue(), {
                    //这里返回主线程，写需要主线程执行的代码
                    self.tableView!.infiniteScrollingView.stopAnimating();
                })
        })
        //设置http请求返回格式
        op!.responseSerializer = AFHTTPResponseSerializer()
        op!.start()
    }
    
    //创建各单元显示内容，该方法是必须实现的方法(创建参数indexPath指定的单元)
    override func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        
        //获取单元格对象
        let cell = tableView.dequeueReusableCellWithIdentifier("cell\(channel)", forIndexPath: indexPath) 
        
        //获取每个单元格的数据源，同时转成XHNewsItem类的实例
        let newsItem = dataSource[indexPath.row] as! XHNewsItem

        //遍历清除cell中的view
        for currentNews : AnyObject in cell.contentView.subviews{
            currentNews.removeFromSuperview();
        }
        
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
        itemTitle!.frame = CGRectMake(10,10,cell.bounds.size.width-20,newsTitleHeight)
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
        itemImageView!.frame = CGRectMake(10,newsTitleHeight+20,cell.bounds.size.width-20,cell.bounds.size.height-(newsTitleHeight+desItemHeight+63));
        
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
        
        return cell
    }
    
    //设置table行高
    override func tableView(tableView: UITableView, heightForRowAtIndexPath indexPath: NSIndexPath) -> CGFloat {
        return 340
    }
    
    // #pragma mark - Segues
    override func prepareForSegue(segue: UIStoryboardSegue, sender: AnyObject?) {
        print("aa")
    }
    
    //选择一行
    override func tableView(tableView: UITableView, didSelectRowAtIndexPath indexPath: NSIndexPath){
        
        let row=indexPath.row as Int
        let data=self.dataSource[row] as! XHNewsItem
        let articleView=ArticleViewController()
        //传值
        articleView.articleid=data.newsID
        //打开文章详情页面，隐藏底部tabbar
        articleView.hidesBottomBarWhenPushed = true;
        //获取导航控制器,添加subView,入栈
        self.wrapControl!.navigationController!.pushViewController(articleView,animated:true);
        //self.wrapControl!.navigationController!.interactivePopGestureRecognizer.enabled = false;
        tableView.deselectRowAtIndexPath(indexPath,animated:true);
    }
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated);
        self.navigationController?.navigationBarHidden = true;
        
        //友盟统计
        MobClick.beginLogPageView("NewsList");
    }
    
    override func viewWillDisappear(animated: Bool) {
        super.viewWillDisappear(animated);
        //self.navigationController?.navigationBarHidden = true;
        
        //友盟统计
        MobClick.endLogPageView("NewsList");
    }
}