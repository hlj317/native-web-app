//
//  BaseFunction.swift
//  News
//
//  Created by huanglijun on 15/7/30.
//  Copyright (c) 2015年 huanglijun. All rights reserved.
//

import Foundation

class XHNewsItem {
    var newsTitle = NSString()
    var newsDescription = NSString()
    var newsThumb = NSString()
    var newsID = NSString()
    var newsPraizecount = NSInteger()
    var newsReviewcount = NSInteger()
    var newsCheckstatus = NSString()
}

class TotursItem {
    var totursName = NSString()
    var totursDescription = NSString()
    var totursFeature = NSString()
    var totursRank = NSInteger()
    var totursID = NSString()
    var totursCover = NSString()
    var totursWeixin = NSString()
}

class ArticleItem {
    var title = NSString()
    var writeid = NSString()
    var writename = NSString()
    var writesex = NSString()
    var author_time = NSString()
    var praizecount = NSInteger()
    var collectcount = NSInteger()
    var reviewcount = NSInteger()
    var writepic = NSString()
    var coverpic = NSString()
    var content = NSString()
    var praizeusers = NSString()
    var storeusers = NSString()
    var checkstatus = NSString()
    var failcause = NSString()
    var channel = NSInteger()
    var description = NSString()
}

class ReviewItem {
    var userid = NSString()
    var userpic = NSString()
    var username = NSString()
    var usersex = NSString()
    var reviewtime = NSString()
    var lightcount = NSString()
    var reviewcontent = NSString()
    var reviewid = NSString()
    var contentHeight = CGFloat()
}

class UserItem {
    var username = NSString()
    var usersex = NSString()
    var userpic = NSString()
    var userrank = NSInteger()
    var userscore = NSInteger()
}

var Constants = [
    "NODE_URL":"http://120.55.99.230",
    "WXAPP_ID":"wxf8c2bf5f7edb595f",
    "app_userid":"",
    "app_username":"",
    "app_usersex":"",
    "app_userpic":"",
    "app_userrank":1,
    "app_userrankText":"",
    "app_userscore":0,
    "isLogin":false,
    "loginStatus":false
];








