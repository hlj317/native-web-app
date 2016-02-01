//
//  BaseFunction.swift
//  News
//
//  Created by huanglijun on 15/7/30.
//  Copyright (c) 2015年 huanglijun. All rights reserved.
//

import Foundation

//转换导师推荐值等级
func SwitchRecommandText(rank:Int) ->String{
    var rankText = "☆";
    if(rank==2){
        rankText = "★";
    }else if(rank==3){
        rankText = "★☆";
    }else if(rank==4){
        rankText = "★★";
    }else if(rank==5){
        rankText = "★★☆";
    }else if(rank==6){
        rankText = "★★★";
    }else if(rank==7){
        rankText = "★★★☆";
    }else if(rank==8){
        rankText = "★★★★";
    }else if(rank==9){
        rankText = "★★★★☆";
    }else if(rank==10){
        rankText = "★★★★★";
    }
    return rankText;
}


//转换用户头衔
func SwitchRankText(rank:Int) ->String{
    var rankText = "实习生(1)";
    if(rank>=2 && rank<5){
        rankText = "正式员工"+"("+String(rank)+")";
    }else if(rank>=5 && rank<9){
        rankText = "主管"+"("+String(rank)+")";
    }else if(rank>=10 && rank<15){
        rankText = "经理"+"("+String(rank)+")";
    }else if(rank>=16 && rank<20){
        rankText = "区域经理"+"("+String(rank)+")";
    }else if(rank>=21 && rank<25){
        rankText = "高级经理"+"("+String(rank)+")";
    }else if(rank>=26 && rank<32){
        rankText = "总监"+"("+String(rank)+")";
    }else if(rank>=33 && rank<39){
        rankText = "高级总监"+"("+String(rank)+")";
    }else if(rank>=40 && rank<47){
        rankText = "副总"+"("+String(rank)+")";
    }else if(rank>=48 && rank<55){
        rankText = "总经理"+"("+String(rank)+")";
    }else if(rank>=56 && rank<63){
        rankText = "CEO"+"("+String(rank)+")";
    }else if(rank>=64){
        rankText = "董事长"+"("+String(rank)+")";
    }
    return rankText;
}


//随机获取导师封面
func setRandomCover() -> String{
    let n=arc4random_uniform(9) + 1;
    var cover = "";
    switch (n) {
    case 1:
        cover = "tutor_banner_bg1.jpg";
    case 2:
        cover = "tutor_banner_bg2.jpg";
    case 3:
        cover = "tutor_banner_bg3.jpg";
    case 4:
        cover = "tutor_banner_bg4.jpg";
    case 5:
        cover = "tutor_banner_bg5.jpg";
    case 6:
        cover = "tutor_banner_bg6.jpg";
    case 7:
        cover = "tutor_banner_bg7.jpg";
    case 8:
        cover = "tutor_banner_bg8.jpg";
    case 9:
        cover = "tutor_banner_bg9.jpg";
    default:
        cover = "tutor_banner_bg1.jpg";
    }
    return cover;
}

