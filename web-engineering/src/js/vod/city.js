require('../../css/vod/city');

var $YS = require('squareYs'),
    $SU = require('squareUtil'),
    FastClick = require('fastclick'),
    Base64 = require('base64');

//为所有的dom都添加fastClick
FastClick.attach(document.body);

//图片对象集合
var ImgsObj = {
    "defCamera" : require("../../imgs/square/city/def-camera.jpg")
}

var browserVision = $YS.getBrowserVision();
var videoParam = {
    pageSize : 10,
    pageNo : 0,
    orderBy : 0,
    cityCode : 179
};
var curPosition = {
    position : {},
    isKnown : false,
    curCity : '',
    curCityCode : '',
    positionIsAsked : false
};
var allPageLoaded = false;
var onScrollLoading = false;//滑动后正在加载

var citiesObj = {
    hotCities : [],
    wholeCities : [
        {'cityName': '全国', 'cityId': '1000000'}
    ]
};
var touchObj = {
    onAnimate : false,
    isStarted : false,
    touchDirect : undefined,
    startPoint : {},
    barPoint : {},
    moveX : 0,
    index : 0,
    maxIndex : $('#order_nav').find('.order-tab').length-1,
    barMoveX : 0,
    deltaBarMove : $('#order_nav').width()/3,
    videoListW : $('#order_nav').width(),
    duration : 200
};

var TMP = {
    cities: _.template(['' +
    '<div class="title1">热门城市</div>',
        '<ul>',
        '<%for(var i=0;i<hotCities.length;i++){%>',
        '<li id="city_<%=hotCities[i].cityId%>" data-id="<%=hotCities[i].cityId%>"><span><%=hotCities[i].cityName%></span></li>',
        '<%}%>',
        '</ul>'].join('')),
    types: _.template([
        '<ul>',
        '<%for(var i=0;i<channel.length;i++){%>',
        '<li data-code="<%=channel[i].channelCode%>" data-channellevel="<%=channel[i].channelLevel%>">',
        '<img src="<%=channel[i].cityIcon%>" alt=""/>',
        '<p class="type-title"><%=channel[i].channelName%></p>',
        '</li>',
        '<%}%>',
        '</ul>'].join('')),
    videoList : _.template([
        '<div id="video<%=videoId%>" class="video flex-box" data-info="<%=info%>" data-videoinfo="<%=videoinfo%>">',
        '<div class="video-img">',
        '<%if(prevue === true || videoType === 1){%>',
        '<%if(prevue === true){%>',
        '<div class="corner-text ">预告</div>',
        '<%}else{%>',
        '<div class="corner-text blue">短片</div>',
        '<%}%>',
        '<%}%>',
        '<img class="video-img-c" src="<%=videoCoverUrl%>" onerror="javascript:this.src=\''+ImgsObj['defCamera']+ '\'" alt=""/>',
        //'<img class="owner-img" src="<%=ownerAvatar%>" onerror="javascript:this.src=\'' + staticPath + '/base/imgs/mobile/user_default.png\'" />',
        //'<div class="favor flex-box">',
        //'<i></i><p><%=statFavour%></p>',
        //'</div>',
        '</div>',
        '<div class="video-des flex1">',
        '<p class="video-title"><%=title%></p>',
        '<div class="video-location flex-box">',
        '<p class="video-address flex1"><%=address%></p>',
        '<p class="video-distance <%if(distance === \'\'){%>no-border<%}%>"><%=distance%></p>',
        '</div>',
        '<div class="video-other flex-box">',
        '<p class="update-time"><%=updateTime%></p>',
        '<div class="viewed-count flex1">',
        '<p><span></span><%=viewedCount%></p>',
        '</div>',
        '</div>',
        '</div>',
        '</div>'].join(''))
};

//渲染分类列表
var renderTypes = function(){
    $.ajax({
        url:'/H5/channel/city',
        type : 'post',
        dataType : 'json',
        success : function(d){
            if(d.resultCode == 0){
                var typeHtml = TMP.types({
                    channel : d.channel
                });
                $('#type_nav').html(typeHtml);
            }
        }
    })
};

//渲染城市列表
var renderCities = function(){
    $.ajax({
        url : '/H5/city/hot',
        type : 'POST',
        success : function(d){
            if(d.resultCode == 0){
                citiesObj.hotCities = d.data;
                var citiesHtml = TMP.cities(citiesObj);
                $('#city_nav').find('.city-list').html(citiesHtml);
                $('#city_nav').find('.city-list').find('li').on('click',function(){
                    //北京城市过滤
                    if($(this).attr("id") == "city_131") return;
                    changeCity($(this).find('span').html());
                    toggleCityNav('close');
                });
            }
        },
        error : function(){

        }
    });
};

//视频列表渲染
var renderVideoList = function(d,type,callback){
    if(type === 'refresh'){
        $('#no-video').addClass('hide');
        $('#video_list').html('');
        $('.final-page').hide();
        videoParam.pageNo = 0;
        allPageLoaded = false;
        $('#list_loading').removeClass('hide');
    }
    var param = _.extend(videoParam,d);
    $.ajax({
        url:'/H5/square/query?r='+Math.random(),
        type : 'post',
        data : param,
        dataType : 'json',
        success : function(d){
            if(d.resultCode == 0){
                var data = d.data;
                if(videoParam.pageNo === 0 && data.length === 0){
                    $('#no-video').removeClass('hide');
                    $('#list_loading').addClass('hide');
                    allPageLoaded = true;
                }else{
                    if(videoParam.pageNo === 0){
                        $('#video_list').html('');
                    }
                    for(var i=0;i< data.length;i++){
                        var obj = tidyVideoMessage(data[i], d.currentTime);
                        var dom = TMP.videoList(obj);
                        $('#video_list').append(dom);
                    }

                    if(data.length<videoParam.pageSize){
                        $('#list_loading').addClass('hide');
                        $('.final-page').show();
                        allPageLoaded = true;
                    }
                    if(callback){
                        callback();
                    }
                }
            }
        }
    })
};

//返回视频数据信息整理
var tidyVideoMessage = function(d,serverTime){
    var obj = {};
    obj['info'] = encodeURIComponent(JSON.stringify(d));
    obj['videoinfo'] = encodeURIComponent(JSON.stringify($SU.getVideoInfo(d,serverTime)));
    //图片地址
    if(d.videoType == 2){
        if(!!d.videoCoverUrl){
            obj['videoCoverUrl'] = d.videoCoverUrl + "@240w_135h_80Q";
        }else{
            obj['videoCoverUrl'] = 'http://' + yundomain + d.md5Serial + '_web.jpeg';
        }
    }else{
        obj['videoCoverUrl'] = d.videoCoverUrl + "@240w_135h_80Q";
    }
    //距离
    if(curPosition.isKnown){
        obj['distance'] = $YS.getFlatternDistance(curPosition.position.lat, curPosition.position.lon, d.latitude,d.longitude);
    }else{
        obj['distance'] = '';
    }
    ////头像地址
    //obj['ownerAvatar'] = d.ownerAvatar = 'http://' + yundomain + d.ownerAvatar + '_web.jpeg';
    ////点赞数
    //obj['statFavour'] = d.statFavour;
    //浏览数
    obj['viewedCount'] = $YS.getCountData(d.statPlay);
    //发布时间
    obj['updateTime'] = $YS.getGapTime(d.uploadTime);
    //视频类型
    obj['videoType'] = d.videoType;
    //标题
    obj['title'] = $YS.rejectScript(d.title);
    //地址
    obj['address'] = $YS.rejectScript(d.address);
    obj['prevue'] = d.prevue;
    obj['videoId'] = d.videoId;
    return obj;
};


//切换城市
var changeCity = function(mark){
    var cityCode;
    var cityName;
    var cities = citiesObj.hotCities;
    for(var i=0;i<cities.length;i++){
        if(cities[i].cityName == mark || cities[i].cityId == mark){
            cityCode = cities[i].cityId;
            cityName = cities[i].cityName;
            break;
        }
    }
    $('#city_selector').find('p').html(cityName);
    $('#city_nav').find('.city-list').find('li').removeClass('active');
    $('#city_'+cityCode).addClass('active');
    curPosition.curCity = cityName;
    videoParam.cityCode = cityCode;
    curPosition.curCityCode = cityCode;
    renderVideoList({},'refresh');
};

//城市列表弹出收回
var toggleCityNav = function(t){
    var $nav = $('#city_nav').find('.city-nav-section');
    var $bg = $('#city_nav').find('.city-nav-bg');
    var $arrow = $('#city_selector');
    if(browserVision.mobile && browserVision.android){
        if(t === 'close'){
            $nav.removeClass('a-open');
            $bg.addClass('hide');
            $arrow.removeClass('a-open');
        }else{
            $nav.addClass('a-open');
            $bg.removeClass('hide');
            $arrow.addClass('a-open');
        }
    }else{
        if(t === 'close'){
            $nav.addClass('close');
            $nav.removeClass('open');
            $bg.addClass('hide');
            $arrow.addClass('close');
            $arrow.removeClass('open');
        }else{
            $nav.addClass('open');
            $nav.removeClass('close');
            $bg.removeClass('hide');
            $arrow.addClass('open');
            $arrow.removeClass('close');
        }
    }
    //阻止滑动
    if(t === 'close'){
        $('body').off('touchmove');
    }else{
        $('body').on('touchmove',function() {
            return false;
        });
    }
};
//获取用户当前位置的回调函数
var positionCallback = function(data) {
    var obj,videoData={};
    $('#getting_position').addClass('hide');
    curPosition.positionIsAsked = true;
    if(data.success) {
        var cityCode = data.result.cityCode;
        var hotCities = citiesObj.hotCities;
        var isHotCity = false;

        for(var i = 0; i < hotCities.length; i++) {
            if(hotCities[i].cityId == cityCode) {
                isHotCity = true;
                break;
            }
        }
        //北京城市过滤
        if(cityCode == "131"){
            isHotCity = false;
        }
        if(isHotCity) {
            changeCity(cityCode);
        } else {
            changeCity('全国');
        }

        obj = {
            curLon : data.result.location.lng,
            curLat : data.result.location.lat,
            dataType : data.dataType
        };
        BMap.Convertor.translate({
            lng : obj.curLon,
            lat : obj.curLat
        }, (obj.dataType === 'n' ? 2 : 0), function(result){
            obj.curLon = Base64.decode(result.lng || '');
            obj.curLat = Base64.decode(result.lat || '');
            curPosition.isKnown = true;
            curPosition.position = {
                lon : obj.curLon,
                lat : obj.curLat
            };
            videoData.longitude = obj.curLon;
            videoData.latitude = obj.curLat;
            //绑定地图事件
            $('#location').removeClass('hide');
            $('#gettingLocation').addClass('hide');
            $('#location').on('click', function() {
                var urlStr = 'map';
                obj.curCity = curPosition.curCity;
                if(window.location.search != "") {
                    urlStr += window.location.search + '&' + $.param(obj);
                } else {
                    urlStr += '?' + $.param(obj);
                }
                $SU.openUrl(urlStr);
            });
        });

    } else {
        changeCity('全国');
        obj = {
            curLon : '',
            curLat : '',
            dataType : ''
        };
    }

};
//初始化
var init = function(){
    //转换成百度坐标
    (function () {
        function load_script(xyUrl, callback) {
            var head = document.getElementsByTagName('head')[0];
            var script = document.createElement('script');
            script.type = 'text/javascript';
            script.src = xyUrl;
            //借鉴了jQuery的script跨域方法
            script.onload = script.onreadystatechange = function () {
                if ((!this.readyState || this.readyState === "loaded" || this.readyState === "complete")) {
                    callback && callback();
                    // Handle memory leak in IE
                    script.onload = script.onreadystatechange = null;
                    if (head && script.parentNode) {
                        head.removeChild(script);
                    }
                }
            };
            // Use insertBefore instead of appendChild  to circumvent an IE6 bug.
            head.insertBefore(script, head.firstChild);
        }

        function translate(point, type, callback) {
            var callbackName = 'cbk_' + Math.round(Math.random() * 10000);    //随机函数名
            var xyUrl = "http://api.map.baidu.com/ag/coord/convert?from=" + type + "&to=4&x=" + point.lng + "&y=" + point.lat + "&callback=BMap.Convertor." + callbackName;
            //动态创建script标签
            load_script(xyUrl);
            BMap.Convertor[callbackName] = function (xyResult) {
                delete BMap.Convertor[callbackName];    //调用完需要删除改函数
                var point = {
                    lng : xyResult.x,
                    lat : xyResult.y
                };
                callback && callback(point);
            }
        }

        window.BMap = window.BMap || {};
        BMap.Convertor = {};
        BMap.Convertor.translate = translate;
    })();
    //================
    if($SU.from === 'app' && $SU.version < 2007000){
        videoParam.preFlag = false;
    }else{
        videoParam.preFlag = true;
    };
    window.chgVideoStatus = function(id){
        $('#video'+id).find('.corner-text').remove();
    };
    renderCities();
    renderTypes();
    //获取当前城市并进行切换
    $SU.getAddress(positionCallback);
    renderVideoList({},'refresh');
    $('#list_loading').removeClass('hide');

    /************分享属性*************/
    window.shareObj = {
        title: document.title != '' ? document.title : '萤石直播',
        desc: '来自萤石直播分享',
        imgUrl: 'http://weixin.ys7.com/src/img/logo.png',
        link: 'http://' + window.location.host + '/city'
    };

    /************微信分享*************/
    !(function() {
        var ua = navigator.userAgent.toLowerCase();
        if(ua.match(/MicroMessenger/i) == "micromessenger") {
            var wxReady = function() {
                wx.showOptionMenu();
                wx.onMenuShareTimeline(shareObj);
                wx.onMenuShareAppMessage(shareObj);
                wx.onMenuShareQQ(shareObj);
                wx.onMenuShareWeibo(shareObj);
            };

            window.weixinVerifyCallback = function(res) {
                if(res.result && res.result.code == 200) {
                    wx.config({
                        appId: res.result.data.appId,
                        timestamp: Number( res.result.data.timestamp ),
                        nonceStr: res.result.data.noncestr,
                        signature: res.result.data.signature,
                        jsApiList: ['showOptionMenu',
                            'onMenuShareTimeline',
                            'onMenuShareAppMessage',
                            'onMenuShareQQ',
                            'onMenuShareWeibo']
                    });

                    wx.ready(wxReady);
                }
            }

            var script = document.createElement('script');
            var pageUrl = window.location.href.split('#')[0].replace(/&/g, "%26");

            script.src = "http://weixin.ys7.com/api/js/getJsonpSignature?jsoncallback=weixinVerifyCallback&pageUrl=" + pageUrl;
            document.body.appendChild(script);
        }
    }());

    /************配置H5页面，右上角APP菜单*************/
    var shareObj_app =　{
        menu: [
            {type: 'shareTo1',
                icon: '',
                text: '发送给朋友',
                bind: function()　{
            YsAppBridge.shareTo({
                type: 'url',
                target: 'weixin',
                title: shareObj.title,
                desc: shareObj.desc,
                content: shareObj.link,
                img: shareObj.imgUrl
            });}
        },{
            type: 'shareTo2',
                icon: '',
                text: '分享到朋友圈',
                bind: function() {
                YsAppBridge.shareTo({
                    type: 'url',
                    target: 'weixin_c',
                    title: shareObj.title,
                    desc: shareObj.desc,
                    content: shareObj.link,
                    img: shareObj.imgUrl
                });
            }}
        ]
    };

    if(window.YsAppBridge) {

        //APP调用，初始化
        YsAppBridge.init = function(){
            this.setPageView(shareObj_app);
        }
    }
};

//滑动效果不足，弹回
var resetTouch = function(){
    touchObj.onAnimate = true;
    $('#video_list').animate({
        '-webkit-transform' : 'translate3d(0, 0, 0)'
    }, touchObj.duration);
    $('#active_bar').animate({
        '-webkit-transform' : 'translate3d('+touchObj.index*touchObj.deltaBarMove+'px, 0, 0)'
    },  touchObj.duration, function(){
        touchObj.onAnimate = false;
        $('#order_nav').find('.order-tab').removeClass('active');
        $('#order_nav').find('.order-tab').eq(touchObj.index).addClass('active');
        $('#active_bar').addClass('hide');
    });
};
//滑动列表移动
var listMove = function(t,callback){
    if(t === 'next'){
        touchObj.index++;
        $('#video_list').animate({
            '-webkit-transform' : 'translate3d('+(-touchObj.videoListW)+'px, 0, 0)'
        },  touchObj.duration,callback);
    }else{
        touchObj.index--;
        $('#video_list').animate({
            '-webkit-transform' : 'translate3d('+touchObj.videoListW+'px, 0, 0)'
        },  touchObj.duration,callback);
    }
    $('#active_bar').animate({
        '-webkit-transform' : 'translate3d('+touchObj.index*touchObj.deltaBarMove+'px, 0, 0)'
    },  touchObj.duration, function(){
        touchObj.onAnimate = false;
        $('#order_nav').find('.order-tab').removeClass('active');
        $('#order_nav').find('.order-tab').eq(touchObj.index).addClass('active');
        $('#active_bar').addClass('hide');
    });
};

//列表移动重载
var refreshVideoList = function(){
    var d = {};
    videoParam.pageNo = 0;
    allPageLoaded = false;
    var index = touchObj.index;
    if(index === 0){
        videoParam.orderBy = 0;
    }else if(index === 1){
        videoParam.orderBy = 1;
    }else if(index === 2){
        videoParam.orderBy = 2;
    }else{
        videoParam.orderBy = 0;
    }
    renderVideoList(d,'refresh');
};
//================================事件=================================
//打开热门城市列表
$('#city_selector').on('click', function () {
    var $nav = $('#city_nav').find('.city-nav-section');
    if ($nav.hasClass('open') || $nav.hasClass('a-open')) {
        toggleCityNav('close');
    } else {
        toggleCityNav('open');

    }
});

//滚动加载视频
$(document).on('scroll', function(){
    if(onScrollLoading){
        return;
    }else{
        var $body = $('body'),
            winHeight = Math.abs($(window).height()),
            totalHeight = Math.abs($('body').height()),
            scrollTop = Math.abs($body.scrollTop());
        if(!allPageLoaded && (totalHeight < scrollTop+winHeight+100)) {
            onScrollLoading = true;
            videoParam.pageNo++;
            renderVideoList({},'add',function(){
                onScrollLoading = false;
            });
        }
    }
});
//随意点击收起城市框
$('.city-nav-bg').on('click',function(){
    toggleCityNav('close');
});
//点击打开视频
$('#video_list').on('click',function(e){
    var _this = e.srcElement || e.target;
    var $this = $(_this).closest('.video');
    if($this !== null && $this.length > 0){
        var data = JSON.parse(decodeURIComponent($this.data('videoinfo')));
        $SU.openPlayer(data);
    }
});
//分类标签跳转
$('#type_nav').on('click',function(e){
    var _this = e.srcElement || e.target;
    var $this = $(_this).closest('li');
    if($this !== null && $this.length >0){
        var urlStr,
            obj = $YS.getParam;
        obj.cityCode = curPosition.curCityCode;
        obj.lon = curPosition.isKnown ? curPosition.position.lon : '';
        obj.lat = curPosition.isKnown ? curPosition.position.lat : '';
        obj.channel = $this.data('code');
        obj.channelLevel = $this.data('channellevel');
        obj.title = encodeURIComponent($this.find('.type-title').html());
        urlStr = 'class?'+ $.param(obj);
        $SU.openUrl(urlStr);
    }
});
//列表tab切换
$('#order_nav').find('.order-tab').on('click',function(){
    if(!$(this).hasClass('active')){
        $('#order_nav').find('.order-tab').removeClass('active');
        touchObj.index = $(this).index();
        $(this).addClass('active');
        refreshVideoList();
    }
});

//搜索框
$('#search_input').on('click',function(){
    var urlStr = 'search'+window.location.search;
    $SU.openUrl(urlStr);
});

init();




