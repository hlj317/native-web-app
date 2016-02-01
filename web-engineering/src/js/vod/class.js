require('../../css/vod/class');

var $YS = require('squareYs'),
    $SU = require('squareUtil'),
    cityCode = $YS.getParam['cityCode'] || false,
    channel = $YS.getParam['channel'] || false,
    channelLevel = $YS.getParam['channelLevel'] || false,
    title = $YS.getParam['title'] || '',
    lat = $YS.getParam['lat'] || '',
    lon = $YS.getParam['lon'] || '',
    videoSort = 0,
    page = 0,
    pageSize = 16,
    loading = false,
    loaded = false,
    search_img = $("#search_img").attr("src",require('../../imgs/square/hot/1_3_16_search.png')),
    sicon_arrows = $("#sicon_arrows").attr("src",require('../../imgs/square/hot/1_3_7_sicon_arrows0.png')),
    selected_img = $(".selected_img").attr("src",require('../../imgs/square/hot/1_3_7_sicon_selected.png')),
    p0_img = $("#p0_img").attr("src",require('../../imgs/square/hot/p0.png')),
    no_content_img = $("#no_content_img").attr("src",require('../../imgs/square/hot/1_3_7_noContent.jpg')),

    openSort = function() {
        $('#sortSwitch, #sortPage').addClass('on');
    },

    closeSort = function() {
        $('#sortSwitch, #sortPage').removeClass('on');
    },

    loadList = function() {
        var ajaxData = {};

        if(loaded) {
            return;
        }

        loading = true;

        ajaxData['pageNo'] = page;
        ajaxData['pageSize'] = pageSize;
        ajaxData['orderBy'] = videoSort;

        if(lat != '') {
            ajaxData['latitude'] = lat;
            ajaxData['longitude'] = lon;
        }

        if(cityCode) {
            ajaxData['cityCode'] = cityCode;
        }

        if(channelLevel && channel) {
            if(channelLevel == 1) {
                ajaxData['category'] = channel;
            } else if(channelLevel == 2) {
                ajaxData['subCategory'] = channel;
            }
        }
        $.ajax({
            url: $SU.urls.list,
            data: ajaxData,
            type: 'POST',
            success: function(data) {
                renderList(data);
            }
        });

        page++;
    },

    renderList = function(data) {
        if(data.resultCode == 0) {
            var
                htmlStr = '',
                imgIds = '',
                datas = data.data;

            for(var i = 0; i < datas.length; i++) {
                var videoData = $SU.getVideoInfo(datas[i], data.resultCode);

                videoData['info'] = encodeURIComponent(JSON.stringify(videoData));
                if (datas[i].prevue) {
                    videoData['markClass'] = 'onPrevue';
                    videoData['markStr'] = '预告';
                } else if(videoData['videoType'] == 2) {
                    videoData['markClass'] = '';
                    videoData['markStr'] = '';
                } else if(videoData['videoType'] == 1) {
                    videoData['markClass'] = 'on';
                    videoData['markStr'] = '短片';
                }
                videoData['imgId'] = 'listVideoImg' + videoData['id'];
                videoData['avatarId'] = videoData['imgId'] + '_avatar';

                htmlStr += $SU.tplFun_videoItem(videoData);
                imgIds += '#' + videoData['imgId'] + ',';
            }

            $('#videoBox').append(htmlStr);

            imgIds = imgIds.substr(0, imgIds.length - 1);
            $(imgIds).on('load', function(){
                $(this).addClass('on');
            });

            if(datas.length < pageSize) {
                loaded = true;
                $('#loading').removeClass("show");
            }

            if(datas.length <= 0 && page <= 1) {
                $('#main').hide();
                $('#noContent').show();
            }
        } else {
            $('#loading').removeClass("show");
        }

        loading = false;
    },

    tapHandle_search = function() {
        var
            paramStr = $.param($YS.getParam);
        urlStr = 'search' + (paramStr === '' ? '' : '?' + paramStr);
        $SU.openUrl(urlStr);
    },

    tapHandle_sortSwitch = function(e) {
        if($('#sortSwitch').hasClass('on')) {
            closeSort();
        } else {
            openSort();
        }
    },

    tapHandle_sortPage = function(e) {
        e.stopPropagation()
        var
            target = e.target,
            targetName = target.nodeName.toLowerCase()
        sortData = $(target).data('sort');

        if(targetName === 'li') {
            videoSort = sortData;
            $('#sortPage ul li').removeClass('on');
            $(target).addClass('on');

            $('#sortSwitch .text').text($(target).text());
            console.log()

            //重置参数
            page = 0;
            loaded = false;
            loading = false;
            $('#videoBox').html('');
            $('#loading').removeClass("show");
            loadList();
        }

        closeSort();
    },

    tapHandle_list = function(e) {
        var
            inVideo = false,
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
            };

        checkInVideo(target);

        if(inVideo) {
            var videoInfo = targetNode.data('info');
            videoInfo = decodeURIComponent(videoInfo);
            $SU.openPlayer(videoInfo);
        }
    },

    init = function() {
        if(title != '') {
            title = title.replace(/\./g, '%');
            title = decodeURIComponent(decodeURIComponent(title));
            document.title = title;
        }

        if(document.styleSheets[0].insertRule) {
            var
                windowW = $(window).width(),
                videoImgW = windowW * 0.4796,
                videoImgH = videoImgW / 16 * 9;

            $SU.addCssRule('dl.videoItem dt {height: ' + videoImgH + 'px;}');
            $SU.addCssRule('dl.videoItem dt img {height: ' + videoImgH + 'px;}');
        }

        $('#main').addClass('on');

        $('#loading').addClass("show");
        loadList();

        //如果没有坐标按默认排序
        if(lat === '') {
            $('#locationSort').data('sort', 0);
        }

        //事件绑定
        $('#search .input').on('click', tapHandle_search);
        $('#sortSwitch').on('click', tapHandle_sortSwitch);
        $('#sortPage').on('click', tapHandle_sortPage);
        $('#list').on('click', tapHandle_list);
        $SU.setScrollLoad(function() {
            if(!loading) {
                loadList();
            }
        });

        /************分享属性*************/
        !(function() {
            var
                param = '',
                getParam = function(name,url) {
                    var r = new RegExp("(\\?|#|&)"+name+"=(.*?)(#|&|$)");
                    var m = (url || location.href).match(r);
                    return (m ? m[2] : '');
                },
                addParam = function(key, decode) {
                    var
                        decode = decode || false,
                        val = getParam(key);

                    if(decode) {
                        val = decodeURIComponent(val).replace(/%/g, '.');
                    }

                    if(getParam(key) != '') {
                        if(param === '') {
                            param += '?'+ key +'=' + val;
                        } else {
                            param += '&'+ key +'=' + val;
                        }
                    }
                };

            addParam('channel');
            addParam('channelLevel');
            addParam('title', true);

            window.shareObj = {
                title: getParam('title') != '' ? decodeURIComponent(decodeURIComponent(getParam('title').replace(/\./g, '%'))) : '萤石直播',
                desc: '来自萤石直播分享',
                imgUrl: 'http://weixin.ys7.com/src/img/logo.png',
                link: encodeURI('http://' + window.location.host + '/class' + param)
            };
        }());

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
