require('../../css/vod/video');

require('cookie');
require('mobile-input');

var ImgsObj = {};    //图片对象集合
var doT = require('doT');
var $videoContainer = $('#videoContainer'); // video容器
var $video = $('#video'); // video
var $coverImg = $('#coverImg'); // 封面图片IMG
var $coverParent = $('#coverParent'); // 封面div
var $videoLoading = $('#videoLoading'); // 缓冲显示
var $optionTitle = $('#optionTitle'); // 选项标题
var $optionParent = $('#optionParent'); //  下方区域
var $options = {
    "0" : $('#option0'), // 简介
    "1" : $('#option1'), // 评论
    "2" : $('#option2')  // 地图
};

//获取图片对象
var getImgsObj = function(){

    ImgsObj = {
        "noCommit" : require("../../imgs/square/play/no_commit.png"),
        "defaultHeadPic" : require("../../imgs/square/play/defaultHeadPic.png"),
        "mapPointForS1" : require("../../imgs/square/play/mapPointForS1.png"),
        "emotion" : require("../../imgs/square/play/emotion.png"),
        "emotion1" : require("../../imgs/square/play/emotion1.png"),
        "emotion2" : require("../../imgs/square/play/emotion2.png"),
        "emotion3" : require("../../imgs/square/play/emotion3.png"),
        "emotion4" : require("../../imgs/square/play/emotion4.png"),
        "emotion5" : require("../../imgs/square/play/emotion5.png")
    }

    $("#video").attr("poster",require('../../imgs/square/play/defaultPoster.png'));
    $("#coverImg").attr("src",require('../../imgs/square/play/defaultPoster.png'));

}();


var $currentOptionTitle = $optionTitle.find(".active"); // 当前活动标题
var $currentOption = $optionParent.find(".active"); // 当前活动区域

var commentDes = document.createElement('DIV'); // 评论内容区域
var $commentDes = $( commentDes ); // 评论内容区域对象
var commentInputDiv = document.createElement('DIV'); // 评论输入区域

var $close = $('.footer .close'); // 底部广告关闭按钮

// 变量声明
var videoId = getQueryString('videoId'); // 从url获取videoId
var from = getQueryString('f'); // 从url获取f (s1特殊处理)
var username = getCookie('ASG_DisplayName') ; // 用户名 存在即判定为登录状态
var replyClass = '';
var windowWidth =  document.body.scrollWidth;
var windowHeight = document.body.scrollHeight;
var videoHeight = ( ( windowWidth / 16 ) * 9 ); // video 默认 16:9 宽高比
// 全局控制
var globalControl = {
    isMapInit: false,  // 地图是否已初始化
    isCommentShow: false, // 评论区域是否已显示
    isGetAllComment: false, // 评论是否已全部获得
    isGetCommentDown: false // 获取评论请求是否已回调
};
// 全局变量
var globalData = {
    replyStr : '',
    replyTo: '',
    replyCommentId: '',
    mappoint: null,
    commentFixedHeight : window.innerHeight-53-videoHeight-45-45, // 评论区域高度限制
    getCommentData: {
        topicId:videoId,
        page:0,
        pageSize:10
    },
    topicName: ''

};
// 如果是app内 补上高度
if(from == 'app'){
    //globalData.commentFixedHeight += 53;
}
// 其他情况显示广告头部、底部
else{
    //$('.top').removeClass('hide');
    //$('.footer').removeClass('hide');
}
// 评论区域
$options["1"].append( commentDes ).append( commentInputDiv );
// 增加评论内容置顶功能
var topBtn = document.createElement('DIV');
topBtn.classList.add('topBtn');
topBtn.classList.add('hide');
topBtn.innerText = '返回顶部';
$options["1"].append( topBtn );
// 评论区域限制高度commentFixedHeight
commentDes.style.height = (globalData.commentFixedHeight + 52) + 'px';//'240px';
commentDes.style.overflow = 'auto';
// test
//if(videoId == '0114af01b42ead7b4330a0465c91b5b835a5'){
//	alert('此页面正在开发调试-时间20150324');
//	username = 'test'
//}

// 如果是已登录状态，显示评论输入区域，否则调整评论内容高度

if(username && window.MobileInput){
    var  obj = {
        initDom: commentInputDiv,
        cancel: cancelInput,
        ok: addComment
    };
    window.mobileInput = new MobileInput( obj );
    replyClass = 'reply';
}else{
    commentDes.style.height = (globalData.commentFixedHeight + 35) + 'px';//'240px';
    $('.footer').animate({"bottom": "0"}, 300);
}

// 获取video信息
$.ajax({
    url: '/vod/square/get/' + videoId + '?type=H5' + '&random=' + Math.random(),
    type: 'GET',
    success: function(res){
        if(res.retcode == 0){
            renderVideo(res.videoRecord);
        }else{
            console.log('服务器繁忙');
        }
    },
    error: function(){
        console.log('网络繁忙');
    }
});

// 获取广告信息
var
    haveAd = false,
    insertAd = function(haveAd) {
        // 隐藏底部广告
        $('.footer').find('.close').on('tap', function(){
            $('.footer').addClass('hide');
        });

        if(haveAd) {
            $('.footer').removeClass('hide');
        } else if(from != 'app') {
            $('.footer').removeClass('hide');
        }
    };

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
                    '<div>',
                    '<a href="' + adObj.url1 + '">',
                    '<img src="' + adObj.url2 + '" width="' + imgW + '" height="' + imgH + '">',
                    '</a>',
                    '</div>',
                    '<div>',
                    '<span id ="closed" class="close">X</span>',
                    '</div>'].join('');

                $('.footer').html(footerStr).addClass('isAd');
            }
        }

        insertAd(haveAd);
    },
    error: function() {
        insertAd(haveAd);
    }
});

// 获取评论
function getCommentList(){
    $.ajax({
        url: '/vod/comments/list',
        type: 'POST',
        data: globalData.getCommentData,
        success: function(res){
            if(res.retcode == 0){
                renderOption1(res);
            }else{
                console.log('服务器繁忙');
            }
        },
        error: function(){
            console.log('网络繁忙');
        }
    });
}

function renderVideo(record){
    // 封面图片质量控制
    record.videoCoverUrl += '@480w_270h_80Q';
    // test
//	record.longitude = 116.404;
//	record.latitude = 39.915;
    // topicName
    globalData.topicName = record.title;
    window.shareLink = (document.location.href).replace(/[\?&]ff=liveList/, "");
    window.shareTitle = record.title;
    window.shareDesc = record.memo;
    // 标题设置
    var titleStr = record.title || '精彩短片';
    if( titleStr.length > 13 ){
        titleStr = titleStr.substr(0, 12) + '...';
    }
    if($.os.ios){
        titleStr = titleStr.substr(0, 6) + '...';
    }
    document.title = titleStr;
    // 简介内容填充
    renderOption0(record);
    // 封面可成功载入，设置
    var img = document.createElement('IMG');
    img.src = record.videoCoverUrl;
    img.onload = function(){
        $video.attr("poster", record.videoCoverUrl);
        $coverImg.attr("src" , record.videoCoverUrl);
        window.shareImg = record.videoCoverUrl;
    };
    // 设置playUrl
    var playUrl = record.videoFileUrl;
    $video.attr("src", playUrl);
    var video = $video[0];
    video.addEventListener('waiting', function(){
        $videoLoading.removeClass('nobackground').html("");
        $videoLoading.removeClass( 'hide' );
    }, false);
    video.addEventListener('canplaythrough', function(){
        $videoLoading.addClass( 'hide' );
    }, false);
    video.addEventListener('error', function(){
        console.log('video error');
        var str = '<p style="margin-top: 15%;">视频加载失败了T,T</p><p>请<span class="refreshPage">刷新</span>重试</p>';
        $videoLoading.addClass('nobackground').html( str );
    }, false);

    var tempRefreshCounter = 0;
    $videoLoading.on('tap', '.refreshPage', function(){
        if(tempRefreshCounter < 1){
            video.load();
            video.play();
        }else{
            location = location;
        }
        tempRefreshCounter++;
    });

    // 如果有地图，保存点位，显示地图
    var longitude = parseFloat(record.longitude);
    var latitude = parseFloat(record.latitude);
    if(longitude >= -180 && longitude <= 180 && latitude >= -90 && latitude <= 90){
        globalData.mappoint = {longitude:record.longitude, latitude: record.latitude};
    }
}
// 简介渲染
function renderOption0( record ){
    var templateHTML = '<div class="option0-title">\
		<p class="option0-ownerName">来自：{{=it.ownerName}}<span class="subscribe hide">关注TA</span></p>\
		<p>播放： {{=it.statPlay}}次 <i class="shareIcon hide"></i></p>\
		<p style="max-height:'+(globalData.commentFixedHeight+35-100-42)+'px;overflow: auto;" class="option0-memo">{{!it.memo}}<p></div>';
    var tempFn = doT.template(templateHTML);
    $options["0"].html(tempFn( record ) );

}
function renderOption1(res){
    // 如果page为0 清空评论内容区域
    if(globalData.getCommentData.page == 0){
        commentDes.scrollTop = 0;
        if(res.commentList.length < 1){
            commentDes.innerHTML = '<div style="text-align: center;"><img style="margin-top:20px;" src="'+ImgsObj["noCommit"]+'" width="25%"><p style="color: #666;">还没有评论哦， 快来抢个沙发吧~</p></div>';
            return;
        }else{
            commentDes.innerHTML = '';
        }
    }
    globalControl.isGetCommentDown = true;
    if(res.commentList.length < 1){
        globalControl.isGetAllComment = true;
    }
    var currentUserName = $.cookie('AS_Username');
    // 评论内容
    var templateHTML = '<div data-commentId="{{=it.commentId}}" data-nickname="{{=(it.currentUserName === it.account) ? \'我\' : (it.nickname || it.reviewer)}}" class="option1-commentItem '+replyClass+'">\
		<img class="option1-avatarPath" src="{{=it.avatarPathVodYs}}" />\
		<div class="option1-content">\
		<div class="option1-reviewer">{{=(it.currentUserName === it.account) ? \'我\' : (it.nickname || it.reviewer)}}\
		{{? it.replyUserComment && it.replyUserComment.reviewer}} <span style="color: #333;">回复</span>  {{=(it.currentUserName === it.replyUserComment.account) ? \'我\' : (it.replyUserComment.nickname || it.replyUserComment.reviewer)}}{{?}}：</div>\
		<div class="option1-createTime">{{!it.createTime}}</div>\
		{{=it.content}}</div>\
		</div>';
    var tempFn1 = doT.template(templateHTML);
    var html = '';
    for(var i = 0; i < res.commentList.length; i++){
        res.commentList[i].avatarPathVodYs = '//' + yundomain + res.commentList[i].avatarPath ;
        res.commentList[i].content = encodeHTMLSource( res.commentList[i].content );
        res.commentList[i].content = changeToEmotion( res.commentList[i].content );
        if(res.commentList[i].reviewer.length > 14){
            res.commentList[i].reviewer = res.commentList[i].reviewer.substring(0, 14) + '...';
        }
        res.commentList[i].createTime = dateFormat( res.commentList[i].createTime );
        res.commentList[i].currentUserName = currentUserName;
        html += tempFn1(res.commentList[i]);
    }
    commentDes.innerHTML += html;
    // 头像获取失败显示默认头像
    $commentDes.find('img').on('error', function(e){
        if('true' == this.getAttribute('data-errored')){
            return;
        }
        this.setAttribute('data-errored','true');
        this.src = ImgsObj["defaultHeadPic"];

    });
}
function renderOption2(record){
    var point = new BMap.Point(globalData.mappoint.longitude, globalData.mappoint.latitude);  // 创建点坐标
    // 百度地图
    var BMapDom = document.createElement('DIV');
    $options["0"].append( BMapDom );
    // BMapDom.style.height = (globalData.commentFixedHeight + 25 + 35) + 'px';
    // BMapDom.style.height = '90px';
    BMapDom.style.height = ($(window).height() - $('#option0').offset().top - $('.option0-title').height() - 10) + 'px';
    BMapDom.style.marginTop = '10px';
    var map = new BMap.Map( BMapDom );          // 创建地图实例
    map.centerAndZoom(point, 15);                 // 初始化地图，设置中心点坐标和地图级别
    // 点
    var iconUrl = ImgsObj["mapPointForS1"];
    var myIcon = new BMap.Icon(iconUrl, new BMap.Size(32, 45), {
        //offset: new BMap.Size(0, -5),    //相当于CSS精灵
        imageOffset: new BMap.Size(7, 0)    //图片的偏移量。为了是图片底部中心对准坐标点。
    });
    var marker = new BMap.Marker(point, {icon:myIcon});  // 创建标注
    map.addOverlay(marker);               // 将标注添加到地图中
    map.disableDragging();								// 禁止拖拽

    //添加地图覆盖
    (function() {
        var dom = document.createElement('DIV');
        dom.style.cssText = 'position:absolute; z-index: 99; width: 100%; height: 100%';
        dom.onclick = function() {
            location.href = 'bmap' + '?longitude=' + globalData.mappoint.longitude + '&latitude=' + globalData.mappoint.latitude;
        };
        $(BMapDom).append(dom);
    }());


    // map.addEventListener("click", function(){
    // 	location.href = location.origin + '/square/mobile/BMap.html' + '?longitude=' + globalData.mappoint.longitude + '&latitude=' + globalData.mappoint.latitude;
    // });
}
// 判断窗口的滚动条是否接近页面底部，获取更多评论列表
//$(window).on("scroll",function() {
//    if ($(window).scrollTop() + $(window).height() > $(document).height() - 150) {
//    	if(globalControl.isCommentShow && globalControl.isGetCommentDown && !globalControl.isGetAllComment){
//    		globalControl.isGetCommentDown = false;
//    		globalData.getCommentData.page++;
//    		getCommentList();
//    	}
//    }
//}) ;
// 判断评论区域的滚动条是否接近底部，获取更多评论列表
$commentDes.on('scroll', function(){
    if($commentDes.scrollTop() > 0){
        topBtn.classList.remove('hide');
    }else{
        topBtn.classList.add('hide');
    }
    if( $commentDes.scrollTop() + globalData.commentFixedHeight > commentDes.scrollHeight - 150 ){
        if(globalControl.isCommentShow && globalControl.isGetCommentDown && !globalControl.isGetAllComment){
            globalControl.isGetCommentDown = false;
            globalData.getCommentData.page++;
            getCommentList();
        }
    }
});
topBtn.addEventListener('click', function(e){
    commentDes.scrollTop = 0;
    e.preventDefault();
}, false);
// 切换选项事件
$optionTitle.on('tap', 'div', function(e){
    if(this.className == 'active'){
        return;
    }
    var i = $(this).index();
    $currentOptionTitle.removeClass("active");
    $currentOptionTitle = $(this);
    $currentOptionTitle.addClass("active");
    $currentOption.removeClass("active");
    if(i == 0){
        $currentOption = $options["1"];
    }else if(i == 1){
        $currentOption = $options["0"];
    }else{
        $currentOption = $options[i];
    }
    $currentOption.addClass("active");
    // 点击简介
    if(i == 1){
        switchFooter(2);
        // 如果有地图
        if( !globalControl.isMapInit && globalData.mappoint){
            globalControl.isMapInit = true;
            renderOption2();
        }
    }else{
        switchFooter(1);
    }
    // 点击评论
    if(i == 0){
        commentInit();
    }else{
        globalControl.isCommentShow = false;
    }

});

commentInit();
function commentInit(){
    globalControl.isCommentShow = true;
    // 重新获取评论列表
    globalControl.isGetCommentDown = false;
    globalData.getCommentData.page = 0;
    getCommentList();
}

// 取消评论
function cancelInput(){
    $commentDes.find('.reply').removeClass('selected');
}
// 发表评论
function addComment(v){
    v = $.trim( v );
    if(v == ''){
        return;
    }
    v = v.substring(0, 200);

    $commentDes.find('.reply').removeClass('selected');
    window.mobileInput && mobileInput.setPrompt();

    var data = {
        topicId: videoId,
        topicName: globalData.topicName,
        remarkFrom: username,
        content: v,
        replyTo: globalData.replyTo,
        replyCommentId: globalData.replyCommentId
    };
    globalData.replyStr = '';
    globalData.replyTo = '';
    globalData.replyCommentId = '';
    $.ajax({
        url: '/vod/comments/add',
        data: data,
        type: 'POST',
        success: function(res){
            if(res.retcode == 0){
                // 重新获取评论列表
                globalControl.isGetCommentDown = false;
                globalData.getCommentData.page = 0;
                getCommentList();
            } else if(res.retcode == 1001) {
                window.YsAppBridge && window.YsAppBridge.relogin();
            }
        },
        error: function(){

        }
    });
}

//将表情字符转换为图片
function changeToEmotion(str){
    var imgHtml,emotion,imgOffset;
    var width = 24 ;
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
// html转义
function encodeHTMLSource(text) {
    var encodeHTMLRules = { "&": "&#38;", "<": "&#60;", ">": "&#62;", '"': '&#34;', "'": '&#39;', "/": '&#47;' },
        matchHTML = /&(?!#?\w+;)|<|>|"|'|\//g;
    return text? text.replace(matchHTML, function(m) {return encodeHTMLRules[m] || m;}) : text;
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

//video使用16:9宽高比
function resetHeight(){
    $videoContainer.css({
        'height': videoHeight +'px'
    });
    $video.css({
        'height': videoHeight +'px'
    });
}
resetHeight();
setTimeout(resetHeight, 500); // 延时500、1000毫秒保证兼容性
setTimeout(resetHeight, 1000);
setTimeout(resetHeight, 3000);

// 进入页面默认调用一次计数接口
statplay();
function statplay(){
    $.ajax({
        url: '/vod/square/statplay/' + videoId,
        type: 'POST',
        data: {videoId : videoId},
        success: function(){}
    });
}

//增加回复功能
if(replyClass){
    $commentDes.on('click', '.reply', function(e){
        var $me = $(this);
        if($me.hasClass('selected')){
            $me.removeClass('selected');
            globalData.replyStr = '';
            globalData.replyTo = '';
            globalData.replyCommentId = '';
            if(window.mobileInput){
                mobileInput.setPrompt();
            }
        }else{
            $commentDes.find('.reply').removeClass('selected');
            $me.addClass('selected');
            globalData.replyStr = '@' + $me.attr('data-nickname') + '：';
            globalData.replyTo = $me.attr('data-nickname');
            globalData.replyCommentId = $me.attr('data-commentId');
            if(window.mobileInput){
                mobileInput.setPrompt( globalData.replyStr );
                mobileInput.startInputEvent();
            }
        }
        e.preventDefault();
    });
}

//切换底部
function switchFooter(type){
    if(from == 'app'){
        return;
    }
    if(!username){
        return;
    }
    // 1 隐藏底部广告 2 显示底部广告
    if(type == 1){
        $('.footer').animate({"bottom": "-100px" }, 100);
    }else{
        $('.footer').animate({"bottom": "0"}, 300);
    }

}

//获取location参数
function getQueryString(name) {
    var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i");
    var r = window.location.search.substr(1).match(reg);
    if (r != null) return unescape(r[2]); return null;
}
//html转义
function encodeHTMLSource(text) {
    var encodeHTMLRules = { "&": "&#38;", "<": "&#60;", ">": "&#62;", '"': '&#34;', "'": '&#39;', "/": '&#47;' },
        matchHTML = /&(?!#?\w+;)|<|>|"|'|\//g;
    return text? text.replace(matchHTML, function(m) {return encodeHTMLRules[m] || m;}) : text;
}
Date.prototype.format = function(fmt)
{
    var o = {
        "M+" : this.getMonth()+1,                 //月份
        "d+" : this.getDate(),                    //日
        "h+" : this.getHours(),                   //小时
        "m+" : this.getMinutes(),                 //分
        "s+" : this.getSeconds(),                 //秒
        "q+" : Math.floor((this.getMonth()+3)/3), //季度
        "S"  : this.getMilliseconds()             //毫秒
    };
    if(/(y+)/.test(fmt))
        fmt=fmt.replace(RegExp.$1, (this.getFullYear()+"").substr(4 - RegExp.$1.length));
    for(var k in o)
        if(new RegExp("("+ k +")").test(fmt))
            fmt = fmt.replace(RegExp.$1, (RegExp.$1.length==1) ? (o[k]) : (("00"+ o[k]).substr((""+ o[k]).length)));
    return fmt;
};
function dateFormat(milliseconds){
    var now = new Date();
    var duration = now.getTime() - milliseconds;
    var timeStr = '';
    if(duration < 1000*60){
        timeStr = '刚刚';
    }else if(duration < 1000*60*60){
        timeStr = new Date(duration).getMinutes() + '分钟前';
    }else {
        var createTime = new Date(milliseconds);
        if(now.getFullYear() == createTime.getFullYear()){
            timeStr = createTime.format("MM-dd hh:mm");
        }else{
            timeStr = createTime.format("yyyy-MM-dd hh:mm");
        }
    }
    return timeStr;
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
                desc: window.shareDesc || "来自萤石云的萤石直播分享",
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

/************配置H5页面，右上角APP菜单*************/
var shareObj_app = {
    menu: [
        {
            type: 'shareTo1',
            icon: '',
            text: '发送给朋友',
            bind: function () {

                YsAppBridge.shareTo({
                    type : 'url',
                    target : 'weixin',
                    title : window.shareTitle || '精彩短片',
                    desc :window.shareDesc || '来自萤石直播',
                    content : window.shareLink || location.href.replace(/f=app/, "s=app"),
                    img : window.shareImg
                });
            }
        }, {
            type: 'shareTo2',
            icon: '',
            text: '分享到朋友圈',
            bind: function () {
                YsAppBridge.shareTo({
                    type : 'url',
                    target : 'weixin_c',
                    title : window.shareTitle || '精彩短片',
                    desc :window.shareDesc || '来自萤石直播',
                    content : window.shareLink || location.href.replace(/f=app/, "s=app"),
                    img : window.shareImg
                });
            }
        }
    ]
};

if(window.YsAppBridge) {

    //APP调用，初始化
    YsAppBridge.init = function(){
        this.setPageView(shareObj_app);
    }
}
