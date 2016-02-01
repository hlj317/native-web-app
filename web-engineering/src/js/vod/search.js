require('../../css/vod/search');

var $YS = require('squareYs'),
    $SU = require('squareUtil'),
    firstSearch = true,
    nowKey = '',
    top_img = $("#top_img").attr("src",require('../../imgs/square/hot/p0.png')),

    //获取搜索结果HTML
    searchResultHtml = (function() {
        var
            pageNo = 0,
            pageSize = 16,
            isLoaded = false,
            isLodaing = false,

        //模板
            tplFun_videoItem = (function() {
                var htmlStrs = [
                    '<div class="item" data-info="<%=info%>">',
                    '<div class="coverImg">',
                    '<img id="liveImg<%=id%>" src="<%=videoCoverUrl%>">',
                    '</div>',
                    '<div class="left">',
                    '<h1><%=title%></h1>',
                    '<p><%=address%></p>',
                    '<div class="view">',
                    '<div class="icon"><img src="../../square/hot/p1.png"></div>',
                    '<div class="viewCount"><%=statPlay%></div>',
                    '</div>',
                    '</div>',
                    '</div>'
                ];

                return _.template(htmlStrs.join(''));
            }()),

        //渲染
            renderSearchResult = function(htmlStr, id) {
                $('#videoDiv').append(htmlStr);
                $('#liveImg' + id).on('load', function(){
                    $(this).css('opacity', '1');
                });
            },

            get = function(key) {
                if(isLoaded || isLodaing) {
                    return;
                }

                isLodaing = true;
                $('#loading').addClass("show");

                $.ajax({
                    'url': $SU.urls.list,
                    'type': 'POST',
                    'data': {
                        'pageNo': pageNo,
                        'pageSize': pageSize,
                        'title': key
                    },
                    'success': function(data) {
                        if(data.resultCode == 0) {
                            var list = data.data;

                            for(var i = 0; i < list.length; i++) {
                                var obj = $SU.getVideoInfo(list[i], data.currentTime);
                                obj['info'] = encodeURIComponent(JSON.stringify(obj));
                                obj['address'] = $YS.encodeString(list[i].address);

                                renderSearchResult(tplFun_videoItem(obj), obj.id);
                            }

                            if(list.length < pageSize) {
                                isLoaded = true;

                                if(pageNo == 0 && list.length == 0) {
                                    $('#loading').removeClass("show");
                                    $('#noVideo').show();
                                }
                            }
                        }

                        isLodaing = false;
                        $('#loading').removeClass("show");
                        pageNo++;
                    }
                });
            },

            reset = function() {
                pageNo = 0;
                isLodaing = false;
                isLoaded = false;
                $('#noVideo').hide();
                $('#loading').removeClass("show");
                $('#videoDiv').html('');
            };

        return {
            'get': get,
            'reset': reset
        };
    }()),

    loadSearchResult = function(key) {
        var getResult = function() {
            searchResultHtml.get(key);
        };

        nowKey = key;

        if(firstSearch) {
            firstSearch = false;
            hideHistoryHot(getResult);
        } else {
            getResult();
        }

    },

    hideHistoryHot = function(callback) {
        $('#searchHistory, #searchHot').addClass('remove')
        $('#searchHot').on('webkitTransitionEnd', function() {
            $('#searchHistory, #searchHot').remove();
            callback && callback();
        });
    },

    createHistory = function() {
        var
            htmlStr = '',
            historys = $SU.getHistory($SU.names.storageSearch),
            historysL = historys.length,
            tplFun_historyItem = _.template('<li data-key="<%=key%>"><%=title%></li>');

        if(historysL > 0) {
            $("#searchHistory").show();
        }

        $('#searchHistory ul').html('');
        for(var i = 0; i < historysL; i++) {
            var obj = {};
            obj['title'] = obj['key'] = $YS.encodeString(historys[i].key);
            $('#searchHistory ul').append(tplFun_historyItem(obj));
        }
    },

    createHotList = function() {
        var
            p = {
                'pageNo': 0,
                'pageSize': 8,
                'orderBy': 1
            },
            tplFun_hotItem = (function() {
                var htmlStrs = [
                    '<li data-key="<%=key%>">',
                    '<span class="<%=classStr%>"><%=num%></span><%=title%>',
                    '</li>'
                ];

                return _.template(htmlStrs.join(''));
            }());

        $.ajax({
            'url': $SU.urls.list,
            'type': 'POST',
            'data': p,
            'success': function(data) {
                if(data.resultCode == '0') {
                    var datas = data.data;
                    for(var i = 0; i < datas.length; i++) {
                        var
                            classStr = '',
                            htmlStr = '',
                            obj = {};

                        if(i < 3) {
                            classStr = "child-" + i;
                        }

                        obj['classStr'] = classStr;
                        obj['num'] = i + 1;
                        obj['title'] = obj['key'] = $YS.encodeString(datas[i].title);
                        htmlStr =  tplFun_hotItem(obj)
                        $("#searchHot ul").append(htmlStr);
                    }
                }
            }
        });
    },

    setSearchHistory = function(key) {
        var key = key + '';
        if(key.replace(/\s/ig,'') !== '') {
            var obj = {};
            obj['key'] = key;
            $SU.setHistory(obj, $SU.names.storageSearch, 4);
        }
    },

    tapHandle_searchBtn = function() {
        var
            key = $('#searchValue').val(),
            str = '';

        if(key === '') {
            return;
        }

        //如果为空格不显示视频
        if(key.replace(/ /g, '') === '') {
            if(firstSearch) {
                firstSearch = false;
                hideHistoryHot(function() {
                    $('#noVideo').show();
                });
            } else {
                searchResultHtml.reset();
                $('#noVideo').show();
            }

            return;
        }

        setSearchHistory(key);

        searchResultHtml.reset();
        loadSearchResult(key);
    },

    keyupHandle_searchValue = function() {
        var val = $YS.encodeString($("#searchValue").val());

        if(val === '') {
            $('#searchBtn').removeClass('on');
        } else {
            $('#searchBtn').addClass('on');
        }
    },

    tapHandle_historyHot = function(e) {
        if(e.target.nodeName.toLowerCase() === 'li') {
            var key = $(e.target).data('key');

            setSearchHistory(key);

            $('#searchBtn').addClass('on');
            $('#searchValue').val(key);
            loadSearchResult(key);
        }
    },

    tapHandle_videoDiv = function(e) {
        var target = $(e.target).parents('.item');
        if(target[0]) {
            $SU.openVideo($(target).data('info'));
        }
    }

    init = function() {
        var scrollLoad = function() {
            if(!firstSearch) {
                console.log('212')
                searchResultHtml.get(nowKey);
            }
        };

        if(document.styleSheets[0].insertRule) {
            var coverImgH = Math.floor($(window).width() * 0.31 / 16 * 9);
            $SU.addCssRule('#videoDiv .item .coverImg {height: ' + coverImgH + 'px}');
            $SU.addCssRule('#videoDiv .item .coverImg img {height: ' + coverImgH + 'px}');
        }

        createHistory();
        createHotList();

        $("#searchBtn").on("tap", tapHandle_searchBtn);
        $("#searchValue").on("input keyup keydown keypress", keyupHandle_searchValue);
        $('#searchHistory ul, #searchHot ul').on('tap', tapHandle_historyHot);
        $('#videoDiv').on('tap', tapHandle_videoDiv);

        $SU.setScrollLoad(scrollLoad);
    };

$(init);
