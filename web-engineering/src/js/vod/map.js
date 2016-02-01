require('../../css/vod/map');

var $YS = require('squareYs'),
    $SU = require('squareUtil'),
    cluWorker = new Worker('js/thirdparty/videos-map-worker.js'),//点聚合线程
    curLon,
    curLat,
    curCity = $YS.getParam['curCity'],
    currentCoord,
    maxZoom = 19,
    hasPosition = false,
    videoListInview = [],
    coordsDetailList = [],
    videoLimitZoom = 11;

//图片对象集合
var ImgsObj = {
    "errorMapCover" : require("../../imgs/square/city/video-default.png")
}

//上次地图区域
var lastViewArea = {
    eLon: 0,
    wLon: 0,
    nLat: 0,
    sLat: 0
};

//视频信息参数
var videoParam = {
    pageNo: 0,
    pageSize: 200,
    category:'',
    preFlag:(function(){
        return !($SU.from === 'app' && $SU.version < 2007000);
    })()
};
//视频详细信息参数
var videoDetailParam = {
    pageNo:0,
    pageSize:50
};
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
            var point = new BMap.Point(xyResult.x, xyResult.y);
            callback && callback(point);
        }
    }

    window.BMap = window.BMap || {};
    BMap.Convertor = {};
    BMap.Convertor.translate = translate;
})();

var init = function () {
    //当前位置坐标转换
    var lng = $YS.getParam['curLon'];//当前位置
    var lat = $YS.getParam['curLat'];
    if (lng != '' && lat != '') {
        hasPosition = true;
        curLon = lng;
        curLat = lat;
        initMap();
        hdlEvent.toggleType();
        hdlEvent.gotoMyPosition();
        hdlEvent.toggleVideoDetail();
        hdlEvent.bindOpenVideo();
    } else {
        initMap();
        hdlEvent.toggleType();
        hdlEvent.toggleVideoDetail();
        hdlEvent.bindOpenVideo();
    }
    //分类列表
    renderTypes();

    var shareObj = {
        title: document.title != '' ? document.title : '萤石直播',
        desc: '来自萤石直播分享',
        imgUrl: 'http://weixin.ys7.com/src/img/logo.png',
        link: 'http://' + window.location.host + '/map'
    };

    /************配置H5页面，右上角APP菜单*************/
    var shareObj_app ={
        menu: [
            {type: 'shareTo1',
                icon: '',
                text: '发送给朋友',
                bind: function(){
                    YsAppBridge.shareTo({
                        type: 'url',
                        target: 'weixin',
                        title: shareObj.title,
                        desc: shareObj.desc,
                        content: shareObj.link,
                        img: shareObj.imgUrl
                    });
                }
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

};
//分类列表
var coordTypeTemp = _.template([
    '<ul>',
    '<% for(var i=0;i<list.length;i++){%>',
    '<li data-type="<%=list[i].channelCode%>"><%=list[i].channelName%></li>',
    '<% } %>',
    '<li data-type="">全部分类</li>',
    '</ul>'
].join(''));
//视频点信息模板
var coordListTemp = _.template(
    ['<div class="coord <%if(isLast){%>no-border<%}%>" data-info="<%=info%>" data-videoinfo="<%=videoinfo%>">',
        '<img class="coord-img" src="<%=videoCoverUrl%>" alt="" onerror="javascript:this.src=\''+ImgsObj["errorMapCover"]+'\'">',
        '<div class="coord-des">',
        '<p class="coord-des-p coord-des-ttl"><%=title%></p>',
        '<p class="coord-des-p coord-des-addr"><%=address%></p>',
        '</div>',
        '<div class="coord-loc">',
        '<i class="coord-loc-icon"></i>',
        '<p class="coord-loc-dist"><%=distance%></p>',
        '</div>',
        '</div>'].join(''));
//分批获取视频点
var coordsRequest = function(thisArea,type){
    if(type === 'reset'){
        videoParam.pageNo = 0;
        videoListInview = []
    }else{
        videoParam.pageNo++;
    }
    $.ajax({
        url: '/H5/square/map',
        data: {
            pageNo: videoParam.pageNo,
            pageSize: videoParam.pageSize,
            category: videoParam.category,
            preFlag : videoParam.preFlag,
            startLongitude: thisArea.wLon,
            startLatitude: thisArea.sLat,
            endLongitude: thisArea.eLon,
            endLatitude: thisArea.nLat
        },
        type : 'post',
        dataType: 'json',
        success: function (d) {
            if (d.resultCode == 0) {
                var data = d.data;
                videoListInview = videoListInview.concat(data);
                if(data.length === videoParam.pageSize){
                    coordsRequest(thisArea,'add');
                }else{
                    cluWorker.postMessage({
                        method: 'pushClusterCoords',
                        data: videoListInview
                    });
                }
            }
        }
    });
};
//返回视频数据信息整理
var tidyVideoMessage = function(d,m){
    var obj = {};
    obj['info'] = encodeURIComponent(JSON.stringify(d));
    obj['videoinfo'] = encodeURIComponent(JSON.stringify($SU.getVideoInfo(d)));
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
    if(hasPosition){
        obj['distance'] = $YS.getFlatternDistance(curLat, curLon, d.latitude,d.longitude);
    }else{
        obj['distance'] = '未知'
    }
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
    obj['preFlag'] = d.preFlag;
    obj['isLast'] = (m ==='last' ? true : false);
    return obj;
};
//获取区域内视频点
var getAreaCoords = function (map) {
    var bounds = map.getBounds();
    var sw0 = bounds.getSouthWest();// 西南点
    var ne0 = bounds.getNorthEast();// 东北点
    var thisArea = {
        eLon: ne0.lng,
        wLon: sw0.lng,
        nLat: ne0.lat,
        sLat: sw0.lat
    };
    lastViewArea = thisArea;

    coordsRequest(thisArea,'reset');
};
//切换分类
var changeTypeCoords = function (type) {
    cluWorker.postMessage({
        method: 'changeTypeCoords',
        data: type
    });
};
//坐标覆盖物
var coordOverlay = function (map, o) {
    var myCompOverlay;
    if (o !== undefined) {
        if (o.num === 1) {
            myCompOverlay = new CustomOverlay({
                point: new BMap.Point(o.lon, o.lat),
                type: 'single',
                detail: o.detail,
                ids: o.ids[0],
                lon: o.lon,
                lat: o.lat
            });
            map.addOverlay(myCompOverlay);
        } else {
            myCompOverlay = new CustomOverlay({
                point: new BMap.Point(o.lon, o.lat),
                type: 'multiple',
                text: o.num,
                detail: o.detail,
                ids: o.ids,
                lon: o.lon,
                lat: o.lat
            });
            map.addOverlay(myCompOverlay);
        }
    }
};

var myPositionOverlay = function (map) {
    if (hasPosition) {
        var myPoint = new BMap.Point(curLon, curLat);
        var myCompOverlay = new CustomOverlay({
            point: myPoint,
            type: 'myPosition'
        });
        map.addOverlay(myCompOverlay);
        var circle = new BMap.Circle(myPoint, 500, {
            fillColor: "#1f9eff",
            strokeWeight: 1,
            fillOpacity: 0.14,
            strokeOpacity: 0.01
        });
        map.addOverlay(circle);
    }
};
//请求聚合
var askCluster = function (map, type) {
    var bounds = map.getBounds();
    var sw0 = bounds.getSouthWest();// 西南点
    var ne0 = bounds.getNorthEast();// 东北点
    var thisArea = {
        eLon: ne0.lng,
        wLon: sw0.lng,
        nLat: ne0.lat,
        sLat: sw0.lat
    };
    cluWorker.postMessage({
        method: type,
        data: {
            area: thisArea
        }
    });
};

//选中特定视频点
var getCoordsDetail = function (id,type,callback) {
    if(type==='reset'){
        videoDetailParam.pageNo=0;
        coordsDetailList = [];
    }else{
        videoDetailParam.pageNo++;
    }
    $.ajax({
        url: '/H5/square/get',
        type: 'post',
        data: {
            ids : id
        },
        dataType: 'json',
        success: function (data) {
            if(data.resultCode == 0){
                var _data = data.data;
                coordsDetailList = coordsDetailList.concat(_data);
                if(_data.length === videoDetailParam.pageSize){
                    getCoordsDetail(id,'add',callback);
                }else{
                    callback(coordsDetailList);
                }
            }
        }
    });
};
//判断地图挪动超过一半
var isDragMapHalf = function (area) {
    var lastCenterPoint = {
        lon: (lastViewArea.eLon + lastViewArea.wLon) / 2,
        lat: (lastViewArea.sLat + lastViewArea.nLat) / 2
    };
    var thisCenterPoint = {
        lon: (area.eLon + area.wLon) / 2,
        lat: (area.sLat + area.nLat) / 2
    };
    if (Math.abs(thisCenterPoint.lon - lastCenterPoint.lon) > ((lastViewArea.eLon - lastViewArea.wLon) / 2)) {
        return true;
    } else if (Math.abs(thisCenterPoint.lat - lastCenterPoint.lat) > ((lastViewArea.nLat - lastViewArea.sLat) / 2)) {
        return true;
    } else {
        return false;
    }
};

//分类列表
var renderTypes = function(){
    $.ajax({
        url:'/H5/channel/city',
        type : 'post',
        dataType : 'json',
        success : function(d){
            if(d.resultCode == 0){
                var typeHtml = coordTypeTemp({
                    list : d.channel
                });
                $('.type-select-options').html(typeHtml);
                hdlEvent.changeType();
            }
        }
    })
};
//地图距离
//绑定事件
var hdlEvent = {
    toggleType: function () {
        $('#type_select').find('.type-select-input').on('click', function () {
            $('#type_select').toggleClass('open');
        });
    },
    changeType : function(){
        $('.type-select-options').find('li').on('click', function () {
            var $this = $(this);
            $('#type_select').removeClass('open');
            var newType = $this.data('type');
            var category = (newType !== ''? newType*1 : '');
            if(videoParam.category !== category){
                $('#type_select').find('p').html($this.html());
                videoParam.category = newType;
                myMap.clearOverlays();
                myPositionOverlay(myMap);
                getAreaCoords(myMap);
            }
        });
    },
    gotoMyPosition: function () {
        $('.map-focus').removeClass('hide');
        $('.map-focus').on('click', function () {
            var myPoint = new BMap.Point(curLon * 1, curLat * 1);
            myMap.clearOverlays();
            if (myMap.getZoom() > videoLimitZoom) {
                getAreaCoords(myMap);
            }
            myPositionOverlay(myMap);
            myMap.centerAndZoom(myPoint, 14);
        });
    },
    toggleVideoDetail: function () {
        //移除详细信息
        var removeCoordDetail = function(){
            if (currentCoord) {
                $(currentCoord).removeClass('active');
                currentCoord = undefined;
            }
            $('.map-bottom').removeClass('inview');
            $('.map-focus').removeClass('inview');
        };

        $('.exp-mask').on('touchend',function(){
            $('#videoListExp').hide().find('.coords-list').html('');
        });
        $('#bdmap').on('touchend', function (e) {
            removeCoordDetail();
            $('#type_select').removeClass('open');
        });

        $('.map-overlay-sin').live('touchend', function () {
            if (!$(this).hasClass('active')) {
                $(this).addClass('active');
                $('.map-bottom').addClass('inview');
                $('.map-focus').addClass('inview');
                //去除已有的
                if (currentCoord) {
                    $(currentCoord).removeClass('active');
                    currentCoord = undefined;
                }
                currentCoord = this;
                var simData = JSON.parse(decodeURIComponent($(this).data('detail')));
                myMap.setCenter(new BMap.Point(simData.longitude * 1, simData.latitude * 1));
                getCoordsDetail(simData.videoId, 'reset',function (data) {
                    var _data = tidyVideoMessage(data[0]);
                    var dom = coordListTemp(_data);
                    $('#coord-detail').html(dom);
                });
            } else {
                removeCoordDetail();
            }
        });
        $('.map-overlay-mul').live('touchend', function () {
            var nowZoom = myMap.getZoom();
            if (nowZoom >= maxZoom) {
                var ids = JSON.parse(decodeURIComponent($(this).data('ids')));
                $('#videoListExp').show();
                getCoordsDetail(ids.join(), 'reset',function (data) {
                    var i,
                        dom,
                        m,
                        $list = $('#videoListExp').find('.coords-list');
                    for (i = 0; i < data.length; i++) {
                        if(i === data.length-1){
                            m = 'last';
                        }
                        var _data = tidyVideoMessage(data[i],m);
                        dom = coordListTemp(_data);
                        $list.append(dom);
                    }
                    $list.css('margin-top', -$list.height() / 2);
                });
            }else{
                var mulPoint = JSON.parse(decodeURIComponent($(this).data('point')));
                myMap.centerAndZoom(new BMap.Point(mulPoint.lon * 1, mulPoint.lat * 1), nowZoom+1);
            }
            removeCoordDetail();
        });
    },
    bindOpenVideo: function () {
        var openVideo = function(e){
            var _this = e.srcElement || e.target;
            var $this = $(_this).closest('.coord');
            if($this !== null && $this.length > 0){
                var data = JSON.parse(decodeURIComponent($this.data('videoinfo')));
                $SU.openPlayer(data);
            }
        }
        //点击打开视频
        $('.coords-list').on('click',function(e){
            openVideo(e);
        });
        $('#coord-detail').on('click',function(e){
            openVideo(e);
        });
    }
};
//初始化百度地图
var initMap = function () {
    // 百度地图
    window.myMap = new BMap.Map("bdmap");
    if (hasPosition) {
        var myPoint = new BMap.Point(curLon * 1, curLat * 1);
        myMap.centerAndZoom(myPoint, 12);
    } else {
        $('.map-focus').hide();
        myMap.centerAndZoom('杭州', 12);
    }

    var opts = {anchor: BMAP_ANCHOR_TOP_RIGHT, offset: new BMap.Size(9, 9)};

    myMap.addControl(new BMap.NavigationControl(opts));
    //myMap.addControl(new BMap.ZoomControl({
    //    anchor : BMAP_ANCHOR_TOP_RIGHT
    //}));


    //拖拽缩放刷新
    myMap.addEventListener("dragend", function () {
        if (myMap.getZoom() > videoLimitZoom) {
            setTimeout(function () {
                var bounds = myMap.getBounds();
                var sw0 = bounds.getSouthWest();// 西南点
                var ne0 = bounds.getNorthEast();// 东北点
                var thisArea = {
                    eLon: ne0.lng,
                    wLon: sw0.lng,
                    nLat: ne0.lat,
                    sLat: sw0.lat
                };
                if (isDragMapHalf(thisArea)) {
                    myMap.clearOverlays();
                    getAreaCoords(myMap);
                    myPositionOverlay(myMap);
                }
            }, 500);
        };
    }, false);
    myMap.addEventListener("zoomend", function () {
        myMap.clearOverlays();
        if (myMap.getZoom() > videoLimitZoom) {
            getAreaCoords(myMap);
        }
        myPositionOverlay(myMap);
    }, false);

    // 自定义地图覆盖物
    window.CustomOverlay = function (o) {
        this._point = o.point;
        this._type = o.type;
        this._text = o.text;
        this._ids = o.ids;
        this._detail = o.detail;
        this._lon = o.lon;
        this._lat = o.lat;
    };
    CustomOverlay.prototype = new BMap.Overlay();
    CustomOverlay.prototype.initialize = function (myMap) {
        this._map = myMap;
        var div;
        var overHtml = '';
        var coverImg;
        //图片地址
        if(this._detail){
            if(this._detail.videoType == 2){
                coverImg = 'http://' + yundomain + this._detail.videoCoverUrl + '_web.jpeg';
            }else if(this._detail.videoType == 1){
                coverImg = this._detail.videoCoverUrl + "@240w_135h_80Q";
            }
        }
        //var iconTmp = ['']
        if (this._type === 'single') {
            overHtml = ['<div class="map-overlay-icon map-overlay-sin" data-detail="' + encodeURIComponent(JSON.stringify(this._detail)) + '">',
                '<img src="'+coverImg+'" onerror="javascript:this.src=\''+ImgsObj["errorMapCover"]+'\'">',
                '</div>'].join('');
        } else if (this._type === 'multiple') {
            overHtml = ['<div class="map-overlay-mul" data-point="'+encodeURIComponent(JSON.stringify({lon:this._lon,lat:this._lat}))+'" data-ids="' + encodeURIComponent(JSON.stringify(this._ids)) + '">',
                '<img src="'+coverImg+'" onerror="javascript:this.src=\''+ImgsObj["errorMapCover"]+'\'">',
                '<p>' + this._text + '</p>',
                '</div>'].join('');

        } else if (this._type === 'current') {
            overHtml = ['<div class="map-overlay-icon map-overlay-sin active">',
                '<img src="' + this._text + '">',
                '</div>'].join('');
        } else if (this._type === 'myPosition') {
            overHtml = '<div class="my-position"></div>';
        }
        div = this._div = $(overHtml).get(0);
        myMap.getPanes().labelPane.appendChild(div);

        return div;
    };
    CustomOverlay.prototype.draw = function () {
        var left = 0, top = 0;
        var myMap = this._map;
        var pixel = myMap.pointToOverlayPixel(this._point);
        //var iconTmp = ['']
        if (this._type === 'single' || this._type === 'current') {
            left = pixel.x - 30;
            top = pixel.y - 78;
        } else if (this._type === 'multiple') {
            left = pixel.x - 17;
            top = pixel.y - 54;

        } else if (this._type === 'myPosition') {
            left = pixel.x - 10;
            top = pixel.y - 10;
        }
        this._div.style.left = left + "px";
        this._div.style.top = top + "px";
    };

    //侦听workers事件
    cluWorker.onmessage = function (e) {
        var resultCoords, i,
            _data = e.data;
        if (_data.method === 'pushClusterCoords') {
            askCluster(myMap, 'firstClusterCoords');
        } else if (_data.method === 'firstClusterCoords') {
            resultCoords = _data.data;
            for (i = 0; i < resultCoords.length; i++) {
                coordOverlay(myMap, resultCoords[i]);
            }
        } else if (_data.method === 'moveClusterCoords') {
            resultCoords = _data.data;
            for (i = 0; i < resultCoords.length; i++) {
                coordOverlay(myMap, resultCoords[i]);
            }
        } else if (_data.method === 'changeTypeCoords') {
            myMap.clearOverlays();
            askCluster(myMap, 'firstClusterCoords');
        }
    };
    setTimeout(function(){
        getAreaCoords(myMap);
    },500);
    //标记用户当前位置
    myPositionOverlay(myMap);
};
init();



