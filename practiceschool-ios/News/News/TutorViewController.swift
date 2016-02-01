//
//  ToturViewController.swift
//  News
//
//  Created by huanglijun on 15/7/11.
//  Copyright (c) 2015年 huanglijun. All rights reserved.
//

import UIKit

class TutorViewController: UITableViewController{
    
    //数据源
    var dataSource = [];
    var itemName:UILabel?;                   //导师名称
    var itemDescription:UILabel?;            //导师介绍
    var itemCover:UIImageView?;              //导师封面
    var itemRank:UILabel?;                   //导师推荐等级
    var itemCorner:UIImageView?;             //右下角角标视图
    var itemCornerImage:UIImage?;            //右下角角标图片
    var tableTitle:UILabel?;                 //表格头部标题
    let screenWidth = UIScreen.mainScreen().bounds.size.width;      //屏幕宽度
    let screenHeight = UIScreen.mainScreen().bounds.size.height;    //屏幕高度
    var wrapControl:TutorWrapperController?;    //TutorWrapperController，用来入栈和出栈
    var pagestart:Int = 0;                      //起始分页
    let pagesize:Int = 100;                     //每页加载条数

    //url请求地址
    let hackerNewsApiUrl = "http://120.55.99.230/tutorlist"
    
    init(wrapControl:TutorWrapperController){
        super.init(nibName: nil, bundle: nil);
        self.wrapControl = wrapControl;
    }
    
    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        
        //创建一个重用的单元格
        self.tableView!.registerClass(UITableViewCell.self,forCellReuseIdentifier: "tutor_cell");
        
        //列表刷新时调用回调函数
        self.tableView!.addPullToRefreshWithActionHandler { () -> Void in
            self.loadDataSource();
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
        let paramDict = ["pagestart":pagestart, "pagesize": pagesize];
        
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
                    let currentNewsDataSource = NSMutableArray()
                    for currentNews : AnyObject in newsDataSource {
                        
                        //创建一个实例对象
                        let totursItem = TotursItem();
                        //导师名称
                        totursItem.totursName = currentNews["name"] as! NSString
                        //导师描述
                        totursItem.totursDescription = currentNews["description"] as! NSString
                        //导师头像
                        totursItem.totursCover = currentNews["cover"] as! NSString
                        //导师推荐等级
                        totursItem.totursRank = currentNews["rank"] as! NSInteger
                        //导师ID号
                        totursItem.totursID = currentNews["_id"] as! NSString
                        currentNewsDataSource.addObject(totursItem)
                    }
                    
                    dispatch_async(dispatch_get_main_queue(), {
                        //这里返回主线程，写需要主线程执行的代码
                        self.dataSource = currentNewsDataSource
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
    
    //创建各单元显示内容，该方法是必须实现的方法(创建参数indexPath指定的单元)
    override func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        
        //获取单元格对象
        let cell = tableView.dequeueReusableCellWithIdentifier("tutor_cell", forIndexPath: indexPath) 
        
        //获取每个单元格的数据源，同时转成XHNewsItem类的实例
        let tutorsItem = dataSource[indexPath.row] as! TotursItem
        
        //遍历清除cell中的view
        for currentNews : AnyObject in cell.contentView.subviews{
            currentNews.removeFromSuperview();
        }
        
        //导师头像
        itemCover = UIImageView();
        itemCover!.sd_setImageWithURL(NSURL(string:tutorsItem.totursCover as String)!,placeholderImage: UIImage(named: "loading_small"))
        itemCover!.contentMode = UIViewContentMode.ScaleAspectFill;
        itemCover!.clipsToBounds = true;
        itemCover!.layer.cornerRadius = screenWidth/12;
        itemCover!.frame = CGRectMake(15,15,screenWidth/6,screenWidth/6);
        
        //导师名称
        itemName = UILabel();
        itemName!.font = UIFont(name:itemName!.font.fontName,size:14.0)  //设置默认字体和字号
        let itemNameText = tutorsItem.totursName as String;
        itemName!.text = itemNameText;
        itemName!.textColor = UIColor(red: 51.0/255, green: 51.0/255, blue: 51.0/255, alpha: 1.0);
        itemName!.frame = CGRectMake(30+screenWidth/6,15,screenWidth/6*5-20,(screenWidth/6-10)/3);
        itemName!.numberOfLines = 1;
        
        //导师描述
        itemDescription = UILabel()
        itemDescription!.font = UIFont(name:itemDescription!.font.fontName,size:12.0)  //设置默认字体和字号
        let itemDescriptionText = tutorsItem.totursDescription as String;
        itemDescription!.text = itemDescriptionText;
        itemDescription!.textColor = UIColor(red: 153.0/255, green: 153.0/255, blue: 153.0/255, alpha: 1.0);
        itemDescription!.frame = CGRectMake(30+screenWidth/6,(screenWidth/6-10)/3+20,screenWidth/6*5-20,(screenWidth/6-10)/3);
        itemDescription!.numberOfLines = 1;

        //推荐等级
        itemRank = UILabel();
        itemRank!.font = UIFont(name:itemRank!.font.fontName,size:12.0);
        let itemRankText = tutorsItem.totursRank as Int;
        itemRank!.text = SwitchRecommandText(itemRankText);
        itemRank!.textColor = UIColor(red: 224.0/255, green: 49.0/255, blue: 49.0/255, alpha: 1.0);
        itemRank!.frame = CGRectMake(30+screenWidth/6,(screenWidth/6-10)/3*2+25,screenWidth/6*5-20,(screenWidth/6-10)/3);
        itemDescription!.numberOfLines = 1;
        
        //右下角角标
        itemCorner = UIImageView();
        itemCornerImage = UIImage(named: "corner_mark_icon");
        itemCorner!.image = itemCornerImage;
        itemCorner!.frame = CGRectMake(screenWidth - itemCornerImage!.size.width - 15,cell.bounds.size.height-itemCornerImage!.size.height-15,itemCornerImage!.size.width,itemCornerImage!.size.height);
        
        cell.contentView.addSubview(itemCover!)
        cell.contentView.addSubview(itemName!)
        cell.contentView.addSubview(itemDescription!)
        cell.contentView.addSubview(itemRank!)
        cell.contentView.addSubview(itemCorner!)
        
        return cell
    }
    
    //设置table行高
    override func tableView(tableView: UITableView, heightForRowAtIndexPath indexPath: NSIndexPath) -> CGFloat {
        return screenWidth/6 + 30;
    }

    
    //选择一行
    override func tableView(tableView: UITableView, didSelectRowAtIndexPath indexPath: NSIndexPath){
        let row=indexPath.row as Int
        let data=self.dataSource[row] as! TotursItem
        let tutorView=TutorDetailViewController()
        //传值
        tutorView.tutorid=data.totursID
        //打开文章详情页面，隐藏底部tabbar
        tutorView.hidesBottomBarWhenPushed = true;
        //获取导航控制器,添加subView,入栈
        self.wrapControl!.navigationController!.pushViewController(tutorView,animated:true);
        tableView.deselectRowAtIndexPath(indexPath,animated:true);

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
        MobClick.beginLogPageView("TutorList");
    }

    override func viewWillDisappear(animated: Bool) {
        super.viewWillDisappear(animated);
        
        //友盟统计
        MobClick.endLogPageView("TutorList");
    }


}
