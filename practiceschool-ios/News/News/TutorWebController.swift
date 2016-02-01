//
//  WebViewController.swift
//  UITableViewControllerDemo
//

import UIKit

class TutorWebController: UIViewController {
    
    var detailID = NSString()  //获取传过来的值
    var detailURL = "http://120.55.99.230/tutorshare?key="
    var webView : UIWebView?
    
    override func viewDidLoad() {
        super.viewDidLoad()
        webView=UIWebView()
        webView!.frame=self.view.frame
        webView!.backgroundColor=UIColor.grayColor()
        self.view.addSubview(webView!)
        loadDataSource()
        
        // Do any additional setup after loading the view.
    }
    
    //载入页面
    func loadDataSource() {
        let urlString = detailURL + "\(detailID)"
        //var url = NSURL(string:urlString);
        let urlRequest = NSURLRequest(URL:NSURL(string:urlString)!)
        webView!.loadRequest(urlRequest)
    }
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated);
        self.navigationController?.setNavigationBarHidden(false, animated: animated);
    }
    
    /*
    // #pragma mark - Navigation
    
    // In a storyboard-based application, you will often want to do a little preparation before navigation
    override func prepareForSegue(segue: UIStoryboardSegue?, sender: AnyObject?) {
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
    }
    */
    
}