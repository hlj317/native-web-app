require('../../css/vod/hot');


var component = "dialog";
if('dialog' === component) {
    require.ensure([], function(require) {
        var dialog = require('base');
    });
}

var swiper = require('swiper'),
    $YS = require('squareYs'),
    $SU = require('squareUtil'),
    channelHotData = [],
    channelPage = 0,
    channelPageSize = 2,
    channelPageLoading = false,
    channelPageLoaded = false,
    lat = '',
    lon = '',
    search_img = $("#search_img").attr("src",require('../../imgs/square/hot/1_3_16_search.png')),
    Mall_img = $("#Mall_img").attr("src",require('../../imgs/square/hot/1_3_16_mall.png')),
    News_img = $("#News_img").attr("src",require('../../imgs/square/hot/1_3_16_news.png')),

//videoBox模板
tplFun_videoBox = (function() {
    var htmlStr = ''
    htmlStr += '<div id="<%=id%>" class="videoBox">';
    htmlStr += '<div class="title<%=className%>" data-title="<%=title%>" data-channel="<%=channel%>" data-channellevel="<%=channelLevel%>">';
    htmlStr += '<img src="<%=titleImg%>">';
    htmlStr += '</div>';
    htmlStr += '<ul class="videos"><%=videos%></ul>';
    htmlStr += '</div>';

    return _.template(htmlStr);
}()),

//创建banner
createBanner = function() {
    $.ajax({
        'url': $SU.urls.banner,
        'type': 'POST',
        'success': function(data) {
            if(data.resultCode == '0') {
                var datas = data.data;

                //banner列表为空则隐藏
                if(!datas) {
                    $('#banner, #bannerIndex').hide();
                    return;
                }

                var getHtml = function(d, currentTime) {
                    var
                        bannerId = d.id,
                        bannerSrc = d.bannerImgUrl,
                        bannerVideo = '',
                        bannerType = d.type,
                        htmlStrs = [],

                        obj = {
                            //连接
                            '1': function() {
                                bannerUrl = d.bannerLinkUrl;
                            },

                            //直播
                            '2': function() {
                                bannerUrl = $SU.urls.livePlayer + d.refId;
                                bannerVideo = !!d.video ? encodeURIComponent(JSON.stringify($SU.getVideoInfo(d.video, currentTime))) : '';
                            },

                            //点播
                            '3': function() {
                                bannerUrl = $SU.urls.vodPlayer + d.refId;
                            }
                        };

                    obj[d.type]();

                    htmlStrs.push('<div class="swiper-slide">');
                    htmlStrs.push('<img src="' + bannerSrc + '" data-id="' + bannerId + '" data-url="' + bannerUrl + '" data-type="' + bannerType + '" data-video="' + bannerVideo + '">');
                    htmlStrs.push('</div>');

                    return htmlStrs.join('');
                };

                var
                    bannerList = [],
                    bannerIndex = [],
                    spanW = 0,
                    datasLength = datas.length;

                for(var i = 0; i < datasLength; i++) {
                    if(!!datas[i].bannerImgUrl) {
                        spanW += 14.5;
                        bannerList.push(getHtml(datas[i], data.currentTime));
                        bannerIndex.push('<span></span>');
                    }
                }

                //设置索引容器宽度
                $('#bannerIndex').find('.swiper-pagination').width(spanW);

                if(datasLength > 1) {
                    $('#bannerIndex').find('.swiper-pagination').html(bannerIndex.join(''));
                    $('#bannerDiv').html(bannerList.join(''));

                    //创建跑马灯效果
                    bannerSwiper = new swiper('#banner', {
                        mode:'horizontal',
                        loop: true,
                        autoplay: 3000,
                        pagination: '.swiper-pagination',
                        paginationClickable: true,
                        autoplayDisableOnInteraction: false
                    });
                } else if(datasLength == 1) {
                    $('#bannerIndex').find('.swiper-pagination').html('');
                    $('#bannerDiv').html('<ul>' + list[0] + '</ul>');
                } else {
                    $('#banner, #bannerIndex').hide();
                }
            }
        }
    });
},

//创建精彩视频
createSpecial = function() {
    var tplData = {
        'id': 'special',
        'title': '今日精彩',
        'channel': 'null',
        'channelLevel': 'null',
        'className': ' noBtn',
        'titleImg': staticPath + '/base/imgs/mobile/1_3_16_title.jpg'
    };

    $.ajax({
        'url': $SU.urls.special,
        'type': 'POST',
        'data': {
            'pageNo': 0,
            'pageSize': 20
        },
        'success': function(data) {
            if(data.resultCode == '0' && data.data) {

                var
                    imgIds = '',
                    datas = data.data,
                    datasLength = datas.length;

                if(datasLength > 4) {
                    datasLength = 4;
                }

                tplData['videos'] = '';

                for(var i = 0; i < datasLength; i++) {
                    var obj;

                    if(datas[i]) {
                        obj = $SU.getVideoInfo(datas[i], data.currentTime);

                        //播放必要参数转为字符串放入DOM
                        obj['info'] = encodeURIComponent(JSON.stringify(obj));

                        //角标处理
                        if (obj.prevue) {
                            obj['markClass'] = 'onPrevue';
                            obj['markStr'] = '预告';
                        } else if(obj.videoType == 2) {
                            obj['markClass'] = '';
                            obj['markStr'] = '';
                        } else if(obj.videoType == 1) {
                            obj['markClass'] = 'on';
                            obj['markStr'] = '短片';
                        }

                        //ID加入DOM用于处理图片加载异常
                        obj['imgId'] = 'specialVideoImg' + obj['id'];
                        imgIds += '#' + obj['imgId'] + ',';

                        tplData['videos'] += $SU.tplFun_videoItem(obj);
                    }
                }

                $('#specialList').append(tplFun_videoBox(tplData));
                showVideoBox(imgIds, '#special');

                if(datasLength <= 0) {
                    $('#specialList').hide();
                }
            }
        }
    });
},

//创建视频块列表
createVideoBoxList = function() {
    $.ajax({
        'url': $SU.urls.channelHot,
        'type': 'POST',
        'success': function(data) {
            if(data.resultCode == '0') {

                var data = channelHotData = data.channel;

                if(data.length == 0) {
                    $('#loading').removeClass("show");
                    return;
                }

                for(var i  = 0; i < data.length; i++) {
                    var obj = {
                        'id': 'channel' + data[i].channelCode,
                        'title': data[i].channelName,
                        'channel': data[i].channelCode,
                        'channelLevel': data[i].channelLevel,
                        'className': '',
                        'videos': '',
                        'titleImg': !!data[i].cityIcon ? data[i].cityIcon.replace('.png', '-title.jpg') : ''
                    };
                    $('#boxList').append(tplFun_videoBox(obj));
                }

                loadVideoBox();
            }
        }
    });
},

//载入视频列表框
loadVideoBox = function() {
    var
        i = channelPage * channelPageSize,
        maxLength = i + channelPageSize,
        ajaxUrl = $SU.urls.list,
        loadTimes = 0;

    channelPageLoading = true;

    if(channelPageLoaded) {
        return;
    }

    if(maxLength >= channelHotData.length) {
        maxLength = channelHotData.length;
        channelPageLoaded = true;
        $('#loading').removeClass("show");
    }

    for(i; i < maxLength; i++) {
        var channelData = channelHotData[i];

        (function(obj) {
            var paramObj = {};

            if(channelData.channelLevel == 1) {
                paramObj['category'] = channelData.channelCode;
            } else if(channelData.channelLevel == 2) {
                paramObj['subCategory'] = channelData.channelCode;
            }

            paramObj['pageNo'] = 0;
            paramObj['pageSize'] = 4;

            $.ajax({
                url: ajaxUrl,
                data: paramObj,
                type: 'POST',
                success: function(data) {
                    renderVideoBox(data, paramObj['category']);
                    loadTimes++;

                    if(loadTimes == channelPageSize) {
                        channelPage++;
                        channelPageLoading = false;
                    }
                },
                error: function() {
                    console && console.log('接口出错！');
                    loadTimes++;

                    if(loadTimes == channelPageSize) {
                        channelPage++;
                        channelPageLoading = false;
                    }
                }
            });
        }());
    }
},

//渲染视频列表框
renderVideoBox = function(data, category) {
    if(data.resultCode == 0) {
        var
            htmlStr = '',
            imgIds = '',
            datas = data.data;

        for(var i = 0; i < datas.length; i++) {
            var videoData = $SU.getVideoInfo(datas[i], data.currentTime);
            videoData['info'] = encodeURIComponent(JSON.stringify(videoData));

            if (videoData.prevue) {
                videoData['markClass'] = 'onPrevue';
                videoData['markStr'] = '预告';
            } else if(videoData.videoType == 2) {
                videoData['markClass'] = '';
                videoData['markStr'] = '';
            } else if(videoData.videoType == 1) {
                videoData['markClass'] = 'on';
                videoData['markStr'] = '短片';
            }

            videoData['imgId'] = 'listVideoImg' + videoData['id'];
            videoData['avatarId'] = videoData['imgId'] + '_avatar';

            htmlStr += $SU.tplFun_videoItem(videoData);
            imgIds += '#' + videoData['imgId'] + ',';
        }

        if(imgIds != '') {
            $('#channel' + category + ' .videos').html(htmlStr);
            showVideoBox(imgIds, '#channel' + category);
        }
    }
},

//显示视频列表框
showVideoBox = function(imgIds, id) {
    imgIds = imgIds.substr(0, imgIds.length - 1);
    $(imgIds).on('load', function(){
        $(this).addClass('on');
    });

    $(id).show();
},

tapHandle_search = function() {
    var
        paramStr = $.param($YS.getParam),
        urlStr = 'search' + (paramStr === '' ? '' : '?' + paramStr);

    $SU.openUrl(urlStr);
},

tapHandle_banner = function(e) {
    if(e.target.nodeName.toLowerCase() === 'img') {
        var
            baseData = {},
            emt = $(e.target),
            urlStr = emt.data('url'),
            videoData = emt.data('video'),
            bannerType = emt.data('type'),
            data = $.extend({
                systemName : "app_h5_banner_click",   //统计类型
                bannerFrom : window.location.href,    //banner来源
                bannerUrl : emt.data('url'),          //banner跳转地址
                bannerId : emt.data('id')             //bannerid
            }, baseData),
            img = new Image();

        data.timestamp = new Date().getTime();
        img.src = "https://log.ys7.com/statistics.do?" + $.param(data);

        if($SU.from == 'app' && bannerType == 2 && !!videoData) {
            videoData = decodeURIComponent(videoData);
            $SU.openPlayer(videoData);
            return;
        } else if(!!urlStr) {
            if($SU.from != '' || $SU.version != 0) {
                if(urlStr.indexOf('?') == -1) {
                    urlStr += '?';
                } else {
                    urlStr += '&';
                }

                if($SU.from != '' && $SU.version != 0) {
                    urlStr += 'f=' + $YS.getParam['f'] + '&version=' + $YS.getParam['version'];
                } else if($SU.from != '') {
                    urlStr += 'f=' + $YS.getParam['f'];
                } else if($SU.version != 0) {
                    urlStr += 'version=' + $YS.getParam['version'];
                }
            }

            $SU.openUrl(urlStr);
        } else {
            console && console.log('链接为空！');
        }
    }
},

tapHandle_mall = function() {
    var urlStr = "http://www.ys7.com/mobile/?from=app";
    $SU.openUrl(urlStr);
},

tapHandle_news = function() {
    var urlStr = "http://app.ys7.com/news/site/newslist.html";
    $SU.openUrl(urlStr);
},

tapHandle_list = function(e) {
    var
        inVideo = false,
        inTitle = false,
        target = e.target,
        targetNode = null,
        checkInVideo = function(target) {
            var
                nodeName = target.nodeName.toLocaleLowerCase(),
                nodeId = $(target).attr('id'),
                className = $(target).attr('class');

            if(nodeName === 'dl' && className === 'videoItem') {
                inVideo = true;
                targetNode = $(target);
                return;
            } else if(nodeId === 'list') {
                inVideo = false;
                return;
            } else {
                checkInVideo(target.parentNode);
            }
        },
        checkInTitle = function(target) {
            var
                nodeName = target.nodeName.toLocaleLowerCase(),
                nodeId = $(target).attr('id'),
                className = $(target).attr('class');

            if(nodeName === 'div' && className === 'title') {
                inTitle = true;
                targetNode = $(target);
                return;
            } else if(nodeId === 'list') {
                inTitle = false;
                return;
            } else {
                checkInTitle(target.parentNode);
            }
        };

    checkInVideo(target);
    checkInTitle(target);

    if(inTitle) {
        //点击标题
        var
            channel = targetNode.data('channel'),
            channelLevel = targetNode.data('channellevel'),
            title = targetNode.data('title');

        if(_.isNumber(channel)) {
            var paramStr, urlStr;

            paramStr = $YS.getParam;
            paramStr['channel'] = channel;
            paramStr['channelLevel'] = channelLevel;
            paramStr['title'] = encodeURIComponent(title);
            if(lat != '') {
                paramStr['lat'] = lat;
                paramStr['lon'] = lon;
            }
            paramStr = $.param(paramStr);

            urlStr = 'class' + (paramStr === '' ? '' : '?' + paramStr);

            $SU.openUrl(urlStr);
        }
    } else if(inVideo) {
        //点击视频
        var videoInfo = targetNode.data('info');
        videoInfo = decodeURIComponent(videoInfo);
        $SU.openPlayer(videoInfo);
    }
},

init = function() {
    //界面设置
    if(document.styleSheets[0].insertRule) {
        var
            windowW = $(window).width(),
            bannerH = Math.floor(windowW / 16 * 9),
            videoImgW = windowW * 0.4796,
            videoImgH = videoImgW / 16 * 9;

        if($SU.osType === 1) {
            $SU.addCssRule('.videoItem dt span {padding-bottom: 3.5px !important;}')
        }

        $SU.addCssRule('#banner {height: ' + bannerH + 'px;}');
        $SU.addCssRule('#banner img {height: ' + bannerH + 'px;}');
        $SU.addCssRule('dl.videoItem dt {height: ' + videoImgH + 'px;}');
        $SU.addCssRule('dl.videoItem dt img {height: ' + videoImgH + 'px;}');
    }
    $('#main').addClass('on');

    createBanner();
    createSpecial();
    createVideoBoxList();
    $('#loading').addClass("show");

    //获取坐标
    $SU.getCurrentPosition(function(obj) {
        if(obj.success) {
            lat = obj.lat;
            lon = obj.lon;
        }
    });

    //事件绑定
    $('#search').on('click', tapHandle_search);
    $('#bannerDiv').on('click', tapHandle_banner);
    $('#btnMall').on('click', tapHandle_mall);
    $('#btnNews').on('click', tapHandle_news);
    $('#list').on('click', tapHandle_list);
    $SU.setScrollLoad(function() {
        if(!channelPageLoading && channelHotData.length > 0) {
            loadVideoBox();
        }
    });

    //暴露放给app调用
    window.chgVideoStatus = function(videoId) {
        $('#dl' + videoId + ' span').html('直播');
        $('#dl' + videoId + ' span').hide();
    }

    /************分享属性*************/
    var getParam = function(name,url) {
        var r = new RegExp("(\\?|#|&)"+name+"=(.*?)(#|&|$)");
        var m = (url || location.href).match(r);
        return decodeURIComponent(m ? m[2] : '');
    };
    window.shareObj = {
        title: document.title != '' ? document.title : '萤石直播',
        desc: '来自萤石直播分享',
        imgUrl: 'http://weixin.ys7.com/src/img/logo.png',
        link: 'http://' + window.location.host + '/hot'
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

$(init);