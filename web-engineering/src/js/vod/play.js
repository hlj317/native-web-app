require('../../css/vod/play');
require('socketio');
require('mobile-input');

var $YS = require('squareYs');
var $Util = require('util');
var FastClick = require('fastclick');
var globalHost = '';
var globalHostS = 'http://square.ys7.com';
var videoFile;
var ImgsObj = {};    //图片对象集合
var windowWidth = $(window).width() || document.body.scrollWidth;
var windowHeight = $(window).height() || document.body.scrollHeight;
var videoHeight = ( ( windowWidth / 16 ) * 9 ); // video 默认 16:9 宽高比
var username = getCookie('ASG_DisplayName') || ''; // 查看用户名称，未登录用户为空
var nickname = username || ''; // 用户昵称，未登录用户可以由socket获得临时昵称
FastClick.attach(document.body);   //为所有的dom都添加fastClick

//获取图片对象
var getImgsObj = function(){

    ImgsObj = {
        "defaultPoster" : require("../../imgs/square/play/defaultPoster.png"),
        "mapPointForS1" : require("../../imgs/square/play/mapPointForS1.png"),
        "shareTipAlipay" : require("../../imgs/square/play/shareTipAlipay.png"),
        "defaultHeadPic" : require("../../imgs/square/play/defaultHeadPic.png"),
        "actPoint" : require("../../imgs/square/play/act_point.png"),
        "curPoint" : require("../../imgs/square/play/cur_point.png"),
        "emotion" : require("../../imgs/square/play/emotion.png"),
        "emotion1" : require("../../imgs/square/play/emotion1.png"),
        "emotion2" : require("../../imgs/square/play/emotion2.png"),
        "emotion3" : require("../../imgs/square/play/emotion3.png"),
        "emotion4" : require("../../imgs/square/play/emotion4.png"),
        "emotion5" : require("../../imgs/square/play/emotion5.png"),
        "videoItemIcon1" : require("../../imgs/square/play/videoItemIcon_1.jpg"),
        "videoItemIcon2" : require("../../imgs/square/play/videoItemIcon_2.jpg"),
        "videoItemIcon3" : require("../../imgs/square/play/videoItemIcon_3.jpg")
    }

    $("#shade_img").attr("src",require('../../imgs/square/play/shadeTip.png'));
    $("#loading_img").attr("src",require('../../imgs/square/play/loading.png'));
    $("#play_none_img").attr("src",require('../../imgs/square/play/1_3_16_playNone.jpg'));

}();

var urlObj = require('urlObj'),
    squareId = urlObj.urlParam.squareid,
    from  =$YS.getParam["f"],
    O = {
        //播放列表
        "liveListUrl": (function() {
            var url = "";
            if(from === "app") {
                url = globalHost +"/square/demoCameraApplyAction!queryDemoCameraList.action?serverType=60&type=200";
            } else if(from === "alipay") {
                url = globalHost + "/square/demoCameraApplyAction!queryDemoCameraList.action?type=200";
            } else if(from === "weixin"){
                url = globalHost + "/square/demoCameraApplyAction!queryDemoCameraList.action?type=199";
            } else {
                url = globalHost + "/square/demoCameraApplyAction!queryDemoCameraList.action?type=200";
            }
            return url;
        }())
    },
    tmp = ['<div class="top-play"><div id="videoLoading" class="hide"></div>',
        '<video src="${playAddress}"  controls="controls" poster="${imgPath}" webkit-playsinline preload="none" ></video><img id="videoImg" class="video-img" style="display: none;" src="${imgPath}"/><h2 class="hide">直播中 - ${name}</h2>',].join(''),
    tabTmp = ['<div class="mr25"><p class="">来自：${ownerName}<span class="subscribe hide">关注TA</span></p><p class="count"><strong>播放： </strong>${count}次<a id="share" class="share hide"><span></span></a></p><p class="address"><strong>地址： </strong>${address}</p></div><div class="desc" id="desc"><p class="txt desc-box">${desc}</p></div>'].join(''),
    cameraList = [],
    itemTmp = ['<li><div class="img"><img src="${imgPath}" alt="${name}"/></div><div class="info"><p class="name">${name}${remarkTo}：</p><p class="txt">${content}</p>${picUrl}</div></li>'
    ].join(''),
    True = true,
    start = 0,
    recommed=[],
    recommendAdIsRemove = false,
    recommendAd;

var commentType = 1;// 1 普通评论 2 滚动评论
// 全局控制
var globalControl = {
    hasInitMap: false, // 地图是否已初始化
    hasShowCurrentComment: false  // 是否已隐藏即时评论区域
};
// 地图DOM
var BMapElement = document.createElement("DIV");
window.jsonpCallback = function(d){
    var str = '<p style="margin-top: 15%;">'+d.desc+'</p><p>请<span class="refreshPage">刷新</span>重试</p>'
    $('#videoLoading').addClass('nobackground').html( str );
};

//获取视频信息
$.ajax({
    'url': '/H5/square/get',
    'type': 'POST',
    'data': {
        'ids': squareId,
        'prevue': true
    },
    'success': function(data) {
        if(data.resultCode == 0) {
            var cameraInfo = data.data[0];

            if(!!cameraInfo) {
                var
                    UUID = require('uuid'),
                    //file = cameraInfo.videoFileUrl + (UUID? ("?" + UUID.create()) : ''),
                    file = cameraInfo.hlsVideoUrl,

                    obj = {
                        ownerName: $Util.encode(cameraInfo.nickname || cameraInfo.ownerName),
                        name: $Util.encode(cameraInfo.title),
                        playAddress: file,
                        imgPath:(function() {
                            var imgStr = 'http://' + yundomain + (cameraInfo.md5Serial||'/')+'_web.jpeg';
                            if(!!cameraInfo.videoCoverUrl) {
                                imgStr = cameraInfo.videoCoverUrl + '@400w_225h';
                            }
                            return imgStr;
                        }()),
                        md5Serial:cameraInfo.md5Serial,
                        mobileRand:cameraInfo.mobileRand||'',
                        desc: $Util.encode(cameraInfo.meno),
                        count: cameraInfo.statPlay,
                        address: $Util.encode(cameraInfo.address =="" ? cameraInfo.address="未知":cameraInfo.address =cameraInfo.address)
                    };

                videoFile = file;
                window.console && console.log(file);

                //标题处理
                var titleStr = obj.name;
                if( titleStr.length > 13 ){
                    titleStr = titleStr.substr(0, 12) + '...';
                }
                document.title = titleStr;

                //渲染视频
                $('#playHolder').prepend(
                    tmp.replace(/\$\{(\w+)\}/g,function(res,n){
                        return obj[n];
                    })
                );

                //封面错误事件绑定
                $('#videoImg').on('error', function() {
                    console.log('图片加载失败！');
                    $(this).attr('src',ImgsObj["defaultPoster"]);
                });

                resetHeight();
                getRealSteam(file);
                var video = $('video')[0];

                //预告状态处理
                if(cameraInfo.prevue) {
                    $(video).hide();
                    $('#videoImg').show();

                    var
                        prevue = cameraInfo.prevue,
                        pStartTime = cameraInfo.startDate,
                        pRemainTime = cameraInfo.startDate - data.currentTime;

                    pStartTime = new Date(pStartTime * 1);

                    var
                        timeStr = '',
                        getTimeStr = function(num) {
                            var str = num + '';
                            if(str.length <= 1) {
                                str = '0' + str;
                            }
                            return str;
                        };

                    timeStr += pStartTime.getFullYear() + '年';
                    timeStr += (pStartTime.getMonth() + 1) + '月';
                    timeStr += pStartTime.getDate() + '日';

                    timeStr += ' ' + getTimeStr(pStartTime.getHours());
                    timeStr += ':' + getTimeStr(pStartTime.getMinutes());

                    $('.top-play').append('<div class="prevueStr">直播开播时间：' + timeStr + '</div>');

                    pRemainTime = pRemainTime * 1;
                    if(pRemainTime < 0) {
                        pRemainTime = 0;
                    }

                    if(pRemainTime < 86400000) {
                        setTimeout(function() {
                            $('.top-play .prevueStr').remove();
                            $('#videoImg').hide();
                            $(video).show();
                        }, pRemainTime);
                    }

                }

                //video事件绑定
                var $videoLoading = $('#videoLoading');
                video.addEventListener('waiting', function(){
                    $videoLoading.removeClass('nobackground').html("");
                    $videoLoading.removeClass( 'hide' );
                }, false);

                video.addEventListener('canplaythrough', function(){
                    $videoLoading.addClass( 'hide' );
                }, false);

                video.addEventListener('error', function(){
                    var scriptTag=document.createElement("script");
                    //scriptTag.src = 'http://10.1.17.191:8881/hcnp/500383271_1_2_1_0_183.136.184.7_6500.m3u8?2352c374-dbfd-4be6-b733-453262a73f60&errorcode&'+(new Date().getTime());
                    scriptTag.src= videoFile+'&errorcode&freshtime='+(new Date().getTime());
                    scriptTag.charset = 'gb2312';
                    document.getElementsByTagName("head")[0].appendChild(scriptTag);
                }, false);

                //加载失败重新加载处理
                var tempRefreshCounter = 0;
                $videoLoading.on('click', '.refreshPage', function(){
                    if(tempRefreshCounter < 1){
                        video.load();
                        video.play();
                    }else{
                        location = location;
                    }
                    tempRefreshCounter++;
                });

                //创建Tab与简介
                $('#playHolder .top-play').after('<div class="tab" id="Tab"><div class="title"><span class="on">简介</span><span>评论</span><span class="showBMap hide">地图</span><span id="recommendBtn">推荐</span></div><div class="content"></div></div>');
                window.$content = $('#playHolder .content');
                var jianjieDom = document.createElement('DIV');
                jianjieDom.innerHTML = tabTmp.replace(/\$\{(\w+)\}/g,function(res,n){
                    return obj[n];
                });
                $content.append(jianjieDom);

                //隐藏loading
                $(".load").hide();
                //头部链接与底部广告处理
                if($YS.getParam['thirdparty'] !== 'huawei') {

                    $('#playHolder').before('<!--<div class="top hide"><div class="logo"><span>萤石直播</span><p>视频直播、精彩短片，尽收眼底</p><div class="go"><a href="hot?f='+from+'">随便逛逛</a></div></div></div>-->');

                    var
                        haveAd = false,
                        footerStr = '<div class="footer hide"><div class="footer-box">海量直播，想看就看<a href="http://a.app.qq.com/o/simple.jsp?pkgname=com.videogo&g_f=991653" class="download">下载萤石云APP</a> <span id ="closed" class="close"><em></em></span></div></div>',
                        insertAd = function() {
                            $('#playHolder').after(footerStr);
                            if(haveAd) {
                                $('.footer').removeClass('hide');
                            } else if(from != 'app'){
                                $('.footer').removeClass('hide');
                            }
                            $("#closed").on('click',function(){
                                $(".footer").addClass("hide");
                            });
                        };

                    //获取萤石云广告
                    $.ajax({
                        url: '/api/user/notices/get',
                        //url: 'https://i.ys7.com/api/user/notices/get',
                        type: 'post',
                        dataType: 'json',
                        data: {clientType: '1'},
                        success: function(resData) {
                            if(!!resData.notices) {
                                var adObj;

                                //测试数据
                                //resData.notices.push({"infoType":"8","infoContant":"IOS的loading广告（746*640）","url1":"http://www.ys7.com/mobile/?from=app","url2":"http://i.ys7.com/static/images/notices/185067a0dc5740f9b4d571fe28224420.jpeg","ratio":"746:640"});

                                for(var i = 0; i < resData.notices.length; i++ ) {
                                    if(resData.notices[i].infoType == '8') {
                                        adObj = resData.notices[i];
                                        break;
                                    }
                                }

                                if(!!adObj) {
                                    var
                                        imgW = $(window).width(),
                                        imgH = parseInt(imgW / 160 * 41);

                                    haveAd = true;

                                    footerStr = [
                                        '<div class="footer isAd hide">',
                                        '<div>',
                                        '<a href="' + adObj.url1 + '">',
                                        '<img src="' + adObj.url2 + '" width="' + imgW + '" height="' + imgH + '">',
                                        '</a>',
                                        '</div>',
                                        '<div>',
                                        '<span id ="closed" class="close"><em></em></span>',
                                        '</div>',
                                        '</div>'].join('');
                                }
                            }

                            insertAd();
                        },
                        error: function() {
                            insertAd();
                        }
                    });
                }

                //微信分享参数
                window.shareTitle = obj.name;// + " - 萤石直播";
                window.shareImg = obj.imgPath;
                window.shareDesc = obj.desc;

                //封面图片加载失败 使用默认图片
                var tempImg = document.createElement('IMG');
                tempImg.src = obj.imgPath;
                tempImg.onerror = function(){
                    window.shareImg = '';
                    $('video')[0].poster = ImgsObj["defaultPoster"];
                }

                //设置分享相关内容
                var pageH = $(window).height(),
                    pageW = $(window).width();
                shareFun(pageH,pageW);

                //Tab事件绑定与处理
                $('#Tab .title span').on('click',function(){
                    //如果为选中状态则不执行
                    if($(this).hasClass('on')){
                        return;
                    }

                    $(this).addClass("on").siblings().removeClass("on");
                    var i = $(this).index();

                    $content.removeClass('noMT');

                    //添加正在加载中提示
                    if( i== 1 ){
                        start =0;
                        getCommitList($content,0,true,cameraInfo.statComment);
                        // 获取即时评论
                        getCurrentComment($content);
                        switchFooter(1);
                    }
                    else{
                        // 清除即时评论
                        clearCurrentComment();
                    }

                    // 地图
                    if( i == 2 ){
                        $content.addClass('noMT');

                        $content.children().remove();
                        $(".load").show();
                        // 如果百度地图未初始化
                        if(!globalControl.hasInitMap){
                            $content.append( BMapElement );
                            BMapElement.style.height = (globalData.commentFixedHeight + 35) + 'px';
                            window.map = new BMap.Map( BMapElement );          // 创建地图实例
                            if(globalData.originalPoint){
                                var originalPoint = new BMap.Point(globalData.originalPoint.locationX, globalData.originalPoint.locationY);
                                var originalPointUrl = ImgsObj["mapPointForS1"];
                                var originalPointIcon = new BMap.Icon(originalPointUrl, new BMap.Size(32, 45), {
                                    //offset: new BMap.Size(0, -5),    //相当于CSS精灵
                                    imageOffset: new BMap.Size(7, 0)    //图片的偏移量。为了是图片底部中心对准坐标点。
                                });
                                var marker = new BMap.Marker(originalPoint,{icon:originalPointIcon});
                                map.addOverlay(marker);
                            }else{
                                var originalPoint = new BMap.Point(116.404,39.915);
                            }
                            map.centerAndZoom(originalPoint, 15);                 // 初始化地图，设置中心点坐标和地图级别

                            createContrail( globalData.gps.shift() );
                            setInterval(function(){
                                createContrail( globalData.gps.shift() )
                            }, 1000);
                            globalControl.hasInitMap = true;
                            // 模拟数据
                            //              var temp = {locationX: '116.404',locationY:'39.915'};
                            //              globalData.gps.push(temp)
                            //              temp = {locationX: '116.412',locationY:'39.920'};
                            //              globalData.gps.push(temp)
                            //              temp = {locationX: '116.416',locationY:'39.926'};
                            //              globalData.gps.push(temp)

                        }else{
                            $content.append( BMapElement );
                        }

                        $(".load").hide();
                        switchFooter(3);
                    }

                    if( i == 3 ){
                        $content.addClass('noMT');

                        $content.children().remove();
                        if(recommendAdIsRemove) {
                            $content.append('<div class="more-title" id="recommendTitle"><div id="recommendText">同样使用该摄像机的直播还有</div></div><div class="view-list mr25" id="viewList"><ul></ul></div>');
                        } else {
                            if(!!recommendAd) {
                                $content.append('<div class="more-title" id="recommendTitle"><div id="recommendAd"><span class="closeBtn" onclick="removeRecommendAd(this)">X</span><a id="recommendAdUrl"><img id="recommendAdImg"></a></div><div id="recommendText">同样使用该摄像机的直播还有</div></div><div class="view-list mr25" id="viewList"><ul></ul></div>');
                            }
                        }
                        $(".load").show();

                        console.log(recommed);

                        for(var j = 0; j < recommed.length; j++) {
                            var htmlStr = [
                                '<li>',
                                '<a href="'+recommed[j].playAddress+'">',
                                '<img src="'+recommed[j].imgPath+'" alt="'+recommed[j].name+'"/>',
                                '<div class="txt">',
                                '<span class="title">'+recommed[j].name+'</span>',
                                '<p class="count">',
                                '<img class="p1" src="'+ImgsObj["videoItemIcon1"]+'"> ' + $YS.getCountData(recommed[j].statPlay),
                                '　|　<img class="p2" src="'+ImgsObj["videoItemIcon2"]+'"> ' + $YS.getCountData(recommed[j].statFavour),
                                '　|　<img class="p3" src="'+ImgsObj["videoItemIcon3"]+'"> ' + $YS.getCountData(recommed[j].statComment),
                                '</p>',
                                '</div>',
                                '</a>',
                                '</li>'
                            ].join('');

                            $("#viewList ul").append(htmlStr);
                        }

                        $(".load").hide();

                        //渲染广告
                        console.log(recommendAd)
                        if(!!recommendAd) {
                            $('#recommendAdUrl').attr('href', recommendAd.url);
                            $('#recommendAdImg').attr('src', recommendAd.img);
                        }

                        switchFooter(3);
                    }

                    if( i == 0 ){
                        $content.children().remove();
                        $(".load").show();
                        $content.append( jianjieDom );
                        $(".load").hide();
                        switchFooter(2);
                    }
                });

                //获取推荐视频
                $.ajax({
                    'url': '/H5/square/recommend/get',
                    'type': 'POST',
                    'data': {
                        'subSerial': cameraInfo.deviceSubSerail
                    },
                    'success': function(data) {
                        if(data.resultCode == 0 && data.data) {
                            recommendAd = {
                                'url': data.data.recommend.recommendUrl,
                                'img': data.data.recommend.recommendImg
                            }
                            var videoes = data.data.videoes;

                            for(var i = 0; i < videoes.length; i++) {
                                var
                                    file = 'play?squareid='+videoes[i].videoId+'&f='+from,
                                    opt = {
                                        name: $Util.encode(videoes[i].title),
                                        playAddress: file,
                                        statPlay: videoes[i].statPlay,
                                        statComment: videoes[i].statComment,
                                        statFavour: videoes[i].statFavour,
                                        imgPath:(function() {
                                            var imgStr = 'http://' + yundomain + (videoes[i].md5Serial||'/')+'_web.jpeg';
                                            if(!!videoes[i].videoCoverUrl) {
                                                imgStr = videoes[i].videoCoverUrl + '@400w_225h';
                                            }
                                            return imgStr;
                                        }())
                                    };
                                recommed.push(opt);
                            }

                            $('#recommendBtn').css('display', 'block');
                        }
                    }

                });


                // 原始点位 如果没有默认天安门
                var lo = parseFloat(cameraInfo.longitude);
                var la = parseFloat(cameraInfo.latitude);
                if(lo >= -180 && lo <= 180 && la >= -90 & la <= 90){
                    globalData.originalPoint = {locationX: cameraInfo.longitude, locationY: cameraInfo.latitude};
                }
                console.log('原始位置->' + lo + ','+ la)  ;

                // 如果类别为9或10 滚评模式启动 显示地图模块
                if(cameraInfo.category == 9 || cameraInfo.category == 10){
                    // 开始连接socket 发送序列号和用户名（如果有）

                    $.ajax({
                        'url': '/H5/server/get/type',
                        'type': 'POST',
                        'data': {'serverType':12},
                        'success': function(data) {
                            if(data.resultCode == 0) {
                                var webSocketUrl = data.serverInfo.domain + ':' + data.serverInfo.httpPort;
                                doLinkSocket(webSocketUrl, cameraInfo.deviceSubSerail);
                                // 滚动评论启动
                                changeToRoll();
                                // 显示地图 调整标题宽度
                                $('.showBMap').removeClass('hide');
                                // $('#Tab .title span').css({
                                //     "width": "25%"
                                // });
                                $content[0].style.height = (globalData.commentFixedHeight + 52) + 'px';
                            }

                        }
                    });

                }else{
                    if(globalData.originalPoint){
                        // 显示地图 调整标题宽度
                        $('.showBMap').removeClass('hide');
                        // $('#Tab .title span').css({
                        //     "width": "25%"
                        // });
                    }
                    //$content[0].style.height = (globalData.commentFixedHeight + 35) + 'px';
                }

                // 如果是登录状态，增加关注功能
                doGuanZhu(cameraInfo.ownerName, cameraInfo.ownerId, jianjieDom);

                // 如果是s1app 去掉底部广告，切换头部
                if(from == 'sportapp'){
                    $('.header').removeClass('hide');
                    $('.headerBG').removeClass('hide');
                }else

                // 如果是萤石云app 不显示头部
                if(from == 'app'){
                    //$('.top').removeClass('hide');
                }
                // 其他情况显示头部，底部广告
                else{
                    //$('.top').removeClass('hide');
                    //$('.footer').removeClass('hide');
                }

            } else {
                console.log('数据异常!');
            }
        } else {
            $('.load').hide();

            var str = ''
            switch(data.resultCode) {
                case '10004':
                    str = '当前视频不存在';
                    break;

                case '-1':
                    str = '服务器异常';
                    break;

                case '-6':
                    str = '参数错误';
                    break;

                default:
                    str = '当前视频不存在';
                    break;
            }

            $('#playNone').find('.text').text(str);
            $('#playNone').show();
        }
    }
});

var shareFun = function (pageH,pageW) {
    if(from == "alipay") {
        $('#shade img').attr('src', ImgsObj["shareTipAlipay"]).css({
            "position":"absolute",
            "bottom":"10px"
        });
        $('#share').removeClass("hide").on('click',function(){

            $('#shade').css({
                    'width':pageW,
                    'height':pageH
                }
            ).show();
            $('#videoImg').show().css('height',$('video').height());
            $('video').css('visibility','hidden');
        });
        $('#shade').on('click',function(){
            $(this).hide();
            $('#videoImg').hide();
            $('video').css('visibility','visible');
            return false;
        });
    }


};
//获取重定向流媒体播放地址
var getRealSteam = function(oldFile){
    $.getJSON(oldFile + "&clientType=1&jsoncallback=?", function(data) {
        if (data.resultCode === '0') {
            $('video').get(0).src = data.url;
            videoFile = data.url;
        }
    });
};
var getCommitList = function ($content,pageStart,isFirst,count){
    if(commentType == 2){
        return;
    }
    if(isFirst){
        $content.children().remove();
        $content.append('<div class="commit more-title"><span> '+count+'</span>人参与评论<a href="" id="commitBtns">我来评</a></div>');
        if(count ==0){
            $content.append('<div class="no-commit"><div class="img"></div><p>还没有评论哦， 快来抢个沙发吧~</p></div>');
        }else{
            $content.find("#commitBtns").text("我也评");
            $content.append('<div id="desc"><ul class="commit-list"><li class="last" style="display: none;"><a href="http://a.app.qq.com/o/simple.jsp?pkgname=com.videogo&g_f=991653" class="more downUrl">下载萤石云APP查看更多评论</a></li></ul></div>');
        }
    }
    $(".load").show();
    True = false;
    $.getJSON( globalHostS + '/remark/demoRemarkAction!getList.action?random='+Math.random()+'&squareId=' +squareId+ '&pageStart='+pageStart+'&pageSize=10&jsoncallback=?',function(data){
        var addList = data.demoRemarks;
        create(addList, isFirst, $("#desc ul"));
        data.totalCount >10 ? $("#desc .last").hide():$("#desc .last").show();
        scrollFun($content);
        True = true;
        $(".load").hide();
    })
    $("#commitBtns").on('click',function(){
        var dailog =$("#dailog"),
            h = $("body").height()>$(window).height() ? $('body').height(): $(window).height();
        dailog.length == 0?
            $("body").append('<div id="dailog" class="popped" style="height:'+h+'px"><div class="daliog-box"><h2>马上下载萤石云APP，参与评论吧</h2><a href="http://a.app.qq.com/o/simple.jsp?pkgname=com.videogo&g_f=991653" id="downNow">马上下载</a><span id="doClose">残忍拒绝</span></div></div>'):
            dailog.show();
        $(".top-play").find("video").hide();
        $("#videoImg").show();
        $("body").css({"overflow":"hidden","height":$(window).height()+"px"});
        dailogClickFun();
        return false;
    })
}
var dailogClickFun = function (){
    $("#dailog").find("span").on("click",function(){
        $("body").css({"overflow":"","height":""});
        $(".top-play").find("video").show();
        $("#videoImg").hide();
        $("#dailog").hide();
    })
}
var scrollFun = function ($content){
    $(window).unbind().on("scroll",function(){
        var scroll =$(window).scrollTop()/($('body').height()-$(window).height()),
            num =parseInt(($("#desc li").length -1)/10);
        if( 0.95 <scroll<1 && True && num-1 == start){
            start++;
            $("#desc .last").hide();
            getCommitList($content,start);
            return false;
        }
        if(scroll ==1) {
            $("#desc .last").show();
            $(".load").hide();
        }
    })
}
var create = function (addList, isFirst,$content) {
    $.each(addList, function (index, item) {
        cameraList.push(item);
        item.content = $Util.encode(item.content);
        item.cameraName = $Util.encode(item.cameraName);
        item.content = changeToEmotion(item.content, 24);
        var commitObj = {
            imgPath: '//' + yundomain + item.avatarPath,
            name: item.nickname || item.remarkFrom,
            content: item.content
        };
        // 如果有回复人，显示回复XXX
        if(item.replyRemark && item.replyRemark.remarkFrom){
            commitObj.remarkTo = ' <span style="color: #333;">回复</span> ' + (item.replyRemark.nickname || item.replyRemark.remarkFrom);
        }else{
            commitObj.remarkTo = '';
        }
        // 如果有图片，显示图片
        if(item.tinyPicUrl){
            commitObj.picUrl = '<img style="width: 160px;height: 90px;max-width: 160px;max-height: 90px;min-width: 160px;min-height: 90px;" src="'+item.tinyPicUrl+'" alt="截图">';
        }else{
            commitObj.picUrl = '';
        }
        $content.find(".last").before(itemTmp.replace(/\$\{(\w+)\}/g,function(res,n){
                return commitObj[n];
            })
        );
        $('#desc').find('.img img').on('error', function(){
            this.src = ImgsObj["defaultHeadPic"];
        })
    });
}



// 全局数据存储
var globalData = {
    placeholder : '',
    replyStr : '',
    currentCommentItem : null,
    commentFixedHeight : window.innerHeight-47-videoHeight-45-45, // 评论区域高度限制
    commentArr: [],
    originalPoint: null,
    gps: []
}
if(from != 'sportapp'){
    globalData.commentFixedHeight -= 6;
}
// 如果是app内 隐藏头部补上高度
if(from == 'app'){
    globalData.commentFixedHeight += 47;
}
function doLinkSocket(webSocketUrl, serial){
//    	 webSocketUrl = '172.7.205.24:82';
    var socketUrl = webSocketUrl + '?username='+username+'&deviceid=' + serial;
    window.socket = io(socketUrl);
    // 如果是未登录用户，监听返回的临时用户名
    socket.on('name', function(data){
        globalData.placeholder = '欢迎您加入：游客'+data;
        if(globalData.replyStr == ''){
            setPlaceholder(globalData.placeholder);
        }
    });
    socket.on('recvmsg', function (data) {
        var anonymous = false;
        if(data.hasOwnProperty('anonymous')){
            anonymous = data.anonymous;
        }
        data.userName = encodeHTMLSource(data.userName);
        data.msg = encodeHTMLSource(decodeURIComponent(data.msg));
        var username = anonymous ? '游客' + data.userName : data.userName ;
        var str = '<span data-nickname="'+username+'" style="color: blue;">' + username + '：</span>' + data.msg;
        str = changeToEmotion(str);

        // 如果评论区域已显示，刷新评论，否则存储数据 10条
        if(globalControl.hasShowCurrentComment){
            refreshComment( str );
        }else{
            globalData.commentArr.push( str );
            if(globalData.commentArr.length > 10){
                globalData.commentArr = globalData.commentArr.slice(globalData.commentArr.length - 10 );
            }
        }

    });

    socket.on('gps', function(data){
        // 存入gps记录
        globalData.gps = globalData.gps.concat( data["gps"] );

    });
    socket.on('error', function(data){
        console.log('socket错误' + data);
    });
}
function doGuanZhu(indexCode, followeredId, jianjieDom){
    if(username && username != indexCode){
        var $btn = $(jianjieDom).find('.subscribe');
        // 查询是否已关注
        $.ajax({
            url: '/ys/user/other/checksubscribe',
            type: 'POST',
            data: {followeredId: followeredId},
            success: function(res){
                if(res.resultCode == 0){
                    // followered为true 已关注
                    if(res.followered){
                        $btn.addClass('unsubscribe');
                        $btn.html('已关注');
                    }
                }
                $btn.removeClass('hide');
            },
            error: function(){
                $btn.removeClass('hide');
            }
        });
        $btn.on('click',  function(){
            var me = this;
            if(me.classList.contains('loading')){
                return;
            }
            var url = '/ys/user/other/subscribe';
            if(me.classList.contains('unsubscribe')){
                url = '/ys/user/other/unsubscribe';
            }
            //var parent = this.parentNode;
            me.classList.add('loading');
            $.ajax({
                url: url,
                type: 'POST',
                data: {followeredId: followeredId},
                success: function(res){
                    if(res.resultCode == 0){
                        if(me.classList.contains('unsubscribe')){
                            me.innerHTML = '关注TA';
                            me.classList.remove('unsubscribe');
                        }else{
                            me.classList.add('unsubscribe');
                            me.innerHTML = '已关注';
                        }

                    }
                    // 用户重复关注
                    if(res.resultCode == 1067){
                        me.classList.add('unsubscribe');
                        me.innerHTML = '已关注';
                    }
                    me.classList.remove('loading');
                },
                error: function(){
                    me.classList.remove('loading');
                }
            })
        });

    }
}


// 开启滚动评论模式
function changeToRoll(){
    commentType = 2;
    window.$commentFooter = $( '<div>' ).addClass( 'commentFooter hide' );
    $('body').append( $commentFooter );
    // 如果已登录用户有昵称
    if(nickname){
        globalData.placeholder = '欢迎您加入：'+nickname;
        setPlaceholder(globalData.placeholder);
    }
    // 此模式下优先显示评论模块
    $('.showBMap').prev().trigger('click');
    // 初始化输入组件
    var  obj = {
        initDom: $commentFooter[0],
        cancel: cancelInput,
        ok: addComment
    };
    window.mobileInput = new MobileInput( obj );

}
// 取消评论
function cancelInput(){
    if(globalData.currentCommentItem){
        globalData.currentCommentItem.removeClass('selected');
    }
}
// 发表评论
function addComment(v){
    v = $.trim( v );
    if(v == ''){
        return;
    }
    // 字数限制20 表情算1个字
    var inputLimit = 20;
    var emMatch = v.match(/\[em\_\d{1,2}\]/g);
    var po = 0;
    if(emMatch){
        for(var i = 0, len = emMatch.length; i < len; i++){
            po = v.indexOf(emMatch[i], po);
            if(po < inputLimit){
                inputLimit += emMatch[i].length - 1;
                po += emMatch[i].length;
            }else{
                break;
            }
        }
    }
    v = v.substring(0, inputLimit) + (inputLimit < v.length ? '...' : '');
    v = encodeURIComponent ( v );
    if(socket){
        if(globalData.replyStr){
            v = globalData.replyStr + v;
            globalData.replyStr = '';
            globalData.currentCommentItem.removeClass('selected');
        }
        setPlaceholder(globalData.placeholder);
        socket.emit('sendmsg',{msg: v});
    }

    v = v.substring(0, 200);
    window.mobileInput && mobileInput.setPrompt();

}
// 滚动评论父元素
var commentParent = document.createElement('DIV');
commentParent.style.height = globalData.commentFixedHeight + 'px';
commentParent.style.overflow = 'auto';
commentParent.style.position = 'relative';
commentParent.style.wordBreak = 'break-all';
commentParent.style.workWrap = 'break-word';

var commentDivArr = [];
for(var i = 0; i < 100; i++){
    commentDivArr[i] = document.createElement('DIV');
    commentDivArr[i].className = 'commentItem';
    commentParent.appendChild( commentDivArr[i] );
}
var commentDivOffset = 0;

// 获取即时评论
function getCurrentComment($content){
    if(commentType != 2){
        return;
    }
    $content[0].style.height = (globalData.commentFixedHeight + 52) + 'px';
    $(".load").show();
    // 如果有存储的评论，刷新评论区域
    while(globalData.commentArr.length > 0){
        refreshComment( globalData.commentArr.shift() );
    }
    $content.children().remove();
    $content.append(commentParent);
    // 显示
    commentParent.lastChild && commentParent.lastChild.scrollIntoView();
    // 即时评论区域已显示
    globalControl.hasShowCurrentComment = true;

    $(".load").hide();
}
// 滚动评论刷新
function refreshComment( str ){
    if(!str){
        return;
    }
    if(commentDivOffset > 99){
        commentDivOffset = 0;
    }
    commentParent.removeChild( commentDivArr[commentDivOffset] );
    commentDivArr[commentDivOffset].innerHTML = str;
    commentParent.appendChild( commentDivArr[commentDivOffset] );
    commentDivArr[commentDivOffset].scrollIntoView();
    commentDivOffset++;
    //var color = '#'+('00000'+(Math.random()*0x1000000<<0).toString(16)).slice(-6);

}

function clearCurrentComment(){
    if(commentType != 2){
        return;
    }
    globalControl.hasShowCurrentComment = false;
    $content[0].style.height = (globalData.commentFixedHeight + 35) + 'px';
}
// 切换底部
function switchFooter(type){
    if(typeof $commentFooter == "undefined"){
        $commentFooter = $();
    }
    // 1 评论底部 2 广告底部 3 全部隐藏
    if(type == 1){
        $('.footer').css({
            "bottom": "-190px"
        }, 100, function(){
            $commentFooter.removeClass('hide');
        })
    }else if(type == 2){
        $commentFooter.addClass('hide');
        $('.footer').css({"bottom": "0"}, 300);
    }else if(type == 3){
        $commentFooter.addClass('hide');
        $('.footer').css({
            "bottom": "-190px"
        }, 100)
    }

}


//读取cookies
function getCookie(name)
{
    var arr,reg=new RegExp("(^| )"+name+"=([^;]*)(;|$)");

    if(arr=document.cookie.match(reg))

        return unescape(arr[2]);
    else
        return null;
}

// 百度地图画轨迹
function createContrail( point ){
    if(!point || $.isEmptyObject(point) || !point.hasOwnProperty('locationX') || !point.hasOwnProperty('locationY')){
        return;
    }
    var lo = parseFloat( point.locationX );
    var la = parseFloat( point.locationY );
    if(lo < -180 || lo > 180 || la < -90 || la > 90){
        return;
    }
    console.log(globalData.gps)
    point = new BMap.Point(lo, la);
    var iconUrl = ImgsObj["actPoint"];
    var iconUrl2 = ImgsObj["curPoint"];
    var myIcon = new BMap.Icon(iconUrl, new BMap.Size(32, 45), {
        //offset: new BMap.Size(0, -5),    //相当于CSS精灵
        imageOffset: new BMap.Size(8, 10)    //图片的偏移量。为了是图片底部中心对准坐标点。
    });
    var myIcon2 = new BMap.Icon(iconUrl2, new BMap.Size(32, 45), {
        //offset: new BMap.Size(0, -5),    //相当于CSS精灵
        imageOffset: new BMap.Size(8, 10)    //图片的偏移量。为了是图片底部中心对准坐标点。
    });
    map.centerAndZoom(point, 15);                 // 初始化地图，设置中心点坐标和地图级别
    // 原始位置
    if(!window.ysMarker){
        map.addControl(new BMap.NavigationControl()); // 增加缩放控件

        window.ysMarker = new BMap.Marker(point,{icon:myIcon});
        map.addOverlay(ysMarker);
    }else{
        ysMarker.setIcon( myIcon2 );
        window.ysMarker = new BMap.Marker(point,{icon:myIcon});
        map.addOverlay(ysMarker);
        var polyline = new BMap.Polyline([prevPoint, point], {strokeColor:"green", strokeWeight:3, strokeOpacity:0.5});   //创建折线
        map.addOverlay(polyline);
    }
    window.prevPoint = point;
}
// 将表情字符转换为图片
function changeToEmotion(str, width){
    var imgHtml,emotion,imgOffset;
    var width = width || 24;
    for(var i = 1; i < 351; i++){
        if(i<21){
            imgOffset = width * (i - 1);
            emotion = ImgsObj["emotion"];
        }else if(i>100 && i<201){
            imgOffset = width * (i-101);
            emotion = ImgsObj["emotion1"];
        }else if(i>200 && i<231){
            imgOffset = width * (i-201);
            emotion = ImgsObj["emotion2"];
        }else if(i>230 && i<251){
            imgOffset = width * (i-231);
            emotion = ImgsObj["emotion3"];
        }else if(i>250 && i<301){
            imgOffset = width * (i-251);
            emotion = ImgsObj["emotion4"];
        }else if(i>300 && i<351){
            imgOffset = width * (i-301);
            emotion = ImgsObj["emotion5"];
        }
        imgHtml = '<div style="width: '+width+'px;height:'+width+'px;display: inline-block;margin-bottom: -4px;background:url('+emotion+') no-repeat  -' + imgOffset + 'px 0;background-size: cover;"></div>';
        var regStr = '\\[em\\_'+i+'\\]';
        var reg = new RegExp(regStr, 'g');
        str = str.replace(reg, imgHtml);
    }
    return str;
}
//    // 将表情图片转为字符
//    function changeToFaceCode( str ){
//    	return str.replace(/\<img src\=\"\.\/emotion\/img\/\d+\.png\" alt=\"/g, '').replace(/\" height\=\"24px\"\>/g, '');
//    }

// 进入页面默认调用一次计数接口
statplay();
function statplay(){
    $.ajax({
        url: '/H5/square/view/add',
        type: 'POST',
        data: {id : squareId},
        success: function(){}
    });
}

// 头部返回按钮事件
$('#backBtn').click(function(){
    history.go(-1);
});

//video使用16:9宽高比
function resetHeight(){
    $('video').css({
        'height': videoHeight +'px'
    });
    $('.top-play').css({
        'height': videoHeight +'px'
    });
    setTimeout(resetHeight, 500); // 延时500毫秒保证个别手机有效
}
// 增加回复功能
$(commentParent).on('click', '.commentItem', function(){
    var $me = $(this);
    if($me.hasClass('selected')){
        $me.removeClass('selected');
        globalData.replyStr = '';
        setPrompt();
    }else{
        if(globalData.currentCommentItem){
            globalData.currentCommentItem.removeClass('selected');
        }
        $me.addClass('selected');
        globalData.replyStr = '@' + $(this).find('span').attr('data-nickname') + '：';
        setPrompt(globalData.replyStr);
        window.mobileInput && mobileInput.startInputEvent();
    }
    globalData.currentCommentItem = $me;
});
// 设置placeholder
function setPlaceholder(str){
    window.mobileInput && mobileInput.setPlaceholder(str);
}
// 设置prompt
function setPrompt(str){
    window.mobileInput && mobileInput.setPrompt(str);
}
// html转义
function encodeHTMLSource(text) {
    var encodeHTMLRules = { "&": "&#38;", "<": "&#60;", ">": "&#62;", '"': '&#34;', "'": '&#39;', "/": '&#47;' },
        matchHTML = /&(?!#?\w+;)|<|>|"|'|\//g;
    return text? text.replace(matchHTML, function(m) {return encodeHTMLRules[m] || m;}) : text;
}

//移除推荐广告
window.removeRecommendAd = function(elemt) {
    recommendAdIsRemove = true;
    $(elemt).parent().remove();
}

/************微信分享*************/
!function(){
    var ua = navigator.userAgent.toLowerCase();
    if(ua.match(/MicroMessenger/i)=="micromessenger") {
        var wxReady = function(){
            wx.showOptionMenu();
            var obj = {
                title: window.shareTitle || "萤石直播",
                link: window.shareLink || document.location.href,
                desc: window.shareDesc || "来自萤石直播分享",
                imgUrl: window.shareImg || 'http://weixin.ys7.com/src/img/logo.png'
            }
            wx.onMenuShareTimeline(obj);
            wx.onMenuShareAppMessage(obj);
            wx.onMenuShareQQ(obj);
            wx.onMenuShareWeibo(obj);
        };
        window.weixinVerifyCallback = function(res){
            if(res.result && res.result.code == 200){
                wx.config({
                    appId: res.result.data.appId,
                    timestamp: Number( res.result.data.timestamp ),
                    nonceStr: res.result.data.noncestr,
                    signature: res.result.data.signature,
                    jsApiList: ['onMenuShareTimeline',
                        'onMenuShareAppMessage',
                        'onMenuShareQQ',
                        'onMenuShareWeibo']
                })
                wx.ready(wxReady);
            }
        }
        var script = document.createElement('script');
        var pageUrl = location.href.split('#')[0].replace(/&/g, "%26");
        script.src = "http://weixin.ys7.com/api/js/getJsonpSignature?jsoncallback=weixinVerifyCallback&pageUrl=" + pageUrl;
        document.body.appendChild(script);

    }
}()
