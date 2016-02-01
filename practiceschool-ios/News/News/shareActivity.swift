//
//  shareActivity.swift
//  News
//
//  Created by huanglijun on 15/8/8.
//  Copyright (c) 2015年 huanglijun. All rights reserved.
//

import Foundation
import UIKit
class shareActivity : UIActivity{

    override func activityType() -> String? {
        return shareActivity.self.description();
    }
    
    override func activityTitle() -> String? {
        return "职场修炼学院"
    }

    override func activityImage() -> UIImage? {
        return UIImage(named:"sharewx");
    }
    
    override func canPerformWithActivityItems(activityItems: [AnyObject]) -> Bool {
        return true;
    }
    
    override func prepareWithActivityItems(activityItems: [AnyObject]) {
        print("prepareWithActivityItems");
    }
    
    override func activityViewController() -> UIViewController? {
        print("activityViewController");
        return nil;
    }
    
    override func performActivity() {
        print("performActivity");
    }
    
    override func activityDidFinish(completed: Bool) {
        print("activitydidfinish");
    }
    

}
