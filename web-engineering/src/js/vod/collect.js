require('../../css/vod/collect');

var $YS = require('squareYs'),
    $SU = require('squareUtil'),
    Base = require('base'),
    ImgsObj = {};    //图片对象集合

({
    mypage : 0,                 //当前页数
    mydata : {},                //数据源
    isLoading : true,           //是否请求完成
    user_portrait : '',         //用户头像地址
    isSupportPrevue : false,    //是否支持预告类型

    //获取图片对象
    getImgsObj : function(){

        ImgsObj = {
            "liveType" : require("../../imgs/square/collect/video-symbol-live.png"),
            "cover" : require("../../imgs/square/collect/default_cover.png"),
            "portrait" : require("../../imgs/square/collect/user_default.png"),
            "praizeLBtn" : require("../../imgs/square/collect/collect-praize-light-btn.png"),
            "praizeBtn" : require("../../imgs/square/collect/collect-praize-btn.png"),
            "shareBtn" : require("../../imgs/square/collect/collect-share-btn.png"),
            "cancelBtn" : require("../../imgs/square/collect/cancel_btn.png"),
            "prevueType" : require("../../imgs/square/collect/video-symbol-prevue-b.png"),
            "vcrType" : require("../../imgs/square/collect/video-symbol-vcr-b.png"),
            "collectOver" : require("../../imgs/square/collect/collect-over.png"),
            "reviewCount" : require("../../imgs/square/collect/collect-reviewcount.png"),
            "viewCount" : require("../../imgs/square/collect/collect-viewcount.png")
        }

        $("#noCollect_img").attr("src",require('../../imgs/square/collect/no-collect.png'));

    },

    //对APP版本做兼容性处理
    versionHandle : function(){

        var me = this,
            url = window.location.href;

        //判断当前APP是否支持预告
        if($SU.version >= 2007000){
            me.isSupportPrevue = true;
        }else{
            me.isSupportPrevue = false;
        }

        //判断当前APP是否支持sessionid失效时，自动跳转到APP登录页面
        if($SU.version >= 3000000){
            if(url.indexOf("errorcode=")>-1){
                var errorcode = window.location.href.substr(window.location.href.indexOf("errorcode=")+10);
                if(errorcode == "9000"){
                    YsAppBridge.relogin();
                }
            }
        }

    },

    //绑定事件
    handleEvent : function(){

        var me = this;
        window.chgVideoStatus = me.chgVideoStatusFun;
        window.coverError = me.coverErrorFun;
        window.praizeUserError = me.praizeUserErrorFun;
        window.userError = me.userErrorFun;
        $SU.setScrollLoad(me.setScrollLoadFun(me));

    },

    //增加app调用方法(当预览页面状态切换时，退出也要切换相应视频的播放状态)
    chgVideoStatusFun : function(videoId){
        $("#cover_"+videoId).find(".video_type img").attr("src",ImgsObj["liveType"]);
    },

    //封面获取不到，取默认封面
    coverErrorFun : function(e){
        window.event.target.src = ImgsObj["cover"];
    },

    //点赞头像获取不到，取默认头像
    praizeUserErrorFun : function(e){
        window.event.target.src = ImgsObj["portrait"];
    },

    //头像获取不到，取默认头像
    userErrorFun : function(e){
        window.event.target.src = ImgsObj["portrait"];
    },

    //下拉加载
    setScrollLoadFun : function(that){

        var me = that;
        //加载更多收藏视频
        return function(){
            if(!me.isLoading){
                me.isLoading = true;
                $("#loading").css({"display":"block"});
                $.ajax({
                    url: "/H5/favorites/list",
                    data : {
                        "pageNo":me.mypage++,
                        "pageSize":4,
                        "preFlag":me.isSupportPrevue
                    },
                    type: "post",
                    success: function(res) {
                        if(res.resultCode == 0 && res.data.length > 0){
                            me.render(res.data,res.currentTime);
                        }
                        me.isLoading = false;
                        $("#loading").css({"display":"none"});
                    },
                    error: function() {
                        me.mypage--;
                        me.isLoading = false;
                        $("#loading").css({"display":"none"});
                    }
                });
            }

        }


    },

    //获取用户个人信息
    getUserInfo : function(){

        var me = this;

        $.ajax({
            url: "/H5/user/avatar",
            data : {},
            type: "post",
            success: function(data) {
                if(data.resultCode == 0){
                    if(data.avatar){
                        me.user_portrait = 'http://' + yundomain + data.avatar;
                    }else{
                        me.user_portrait = ImgsObj["portrait"];
                    }
                }
            },
            error: function() {
                console.log('网络繁忙');
            }
        });

    },

    //获取用户收藏列表
    getCollectList : function(){

        var me = this;

        $.ajax({
            url: "/H5/favorites/list",
            data : {
                "pageNo":me.mypage++,
                "pageSize":4,
                "preFlag":me.isSupportPrevue
            },
            type: "post",
            success: function(res) {
                if(res.resultCode != 0){
                    $(".no-collect").css({"display":"block"});
                }else if(res.resultCode == 0 && res.data.length == 0){
                    $(".no-collect").css({"display":"block"});
                }else if(res.resultCode == 0 && res.data.length > 0){
                    $(".no-collect").css({"display":"none"});
                    me.render(res.data,res.currentTime);
                }
                me.isLoading = false;
                $("#loading").css({"display":"none"});
            },
            error: function() {
                me.mypage--;
                $(".no-collect").css({"display":"block"});
                me.isLoading = false;
                $("#loading").css({"display":"none"});
            }
        });

    },

    //获取点赞头像列表

    getPraizePortrait : function(videoId){

        var me = this;

        $.ajax({
            url: "/H5/favour/top",
            data : {
                "num":6,
                "videoId":videoId
            },
            type: "post",
            success: function(data) {
                me.mydata[data.videoId]["favourTotal"] = data.total || 0;
                if(data.resultCode == 0 && data.favour.length > 0){
                    me.praizeRender(data.favour,data.videoId,data.total);
                }
            },
            error: function() {
                console.log('网络繁忙');
            }
        });

    },

    //点播视频点赞
    videoPrize : function(videoId,obj){

        var me = this;

        $.ajax({
            url: "/vod/square/statfavour/"+videoId,
            data : {},
            type: "post",
            success: function(data) {
                if(data.retcode == 0){
                    me.addPortrait(obj,videoId);
                }
            },
            error: function() {
                console.log('网络繁忙');
            }
        });

    },

    //直播视频点赞
    livePrize : function(videoId,obj){

        var me = this;

        $.ajax({
            url: "/H5/favour/add",
            data : {
                "id":videoId
            },
            type: "post",
            success: function(data) {
                if(data.resultCode == 0){
                    me.addPortrait(obj,videoId);
                }
            },
            error: function() {
                console.log('网络繁忙');
            }
        });


    },

    //点赞成功，增加点赞头像
    addPortrait : function(obj,videoId){

        var me = this;

        obj.prev().find("img").attr("src",ImgsObj["praizeLBtn"]);
        var favourTotal = ++me.mydata[videoId]["favourTotal"];
        if(favourTotal<=6){
            var $praizeAvatar = $('<li><img src="'+me.user_portrait+'" onerror=praizeUserError(); /></li>');
            obj.append($praizeAvatar);
        }else if(favourTotal == 7){
            var $praize_num = $('<div class="praize_num">'+favourTotal+'</div>');
            $("#"+videoId).after($praize_num);
            $("#"+videoId).find("li")[$("#"+videoId).find("li").length-1].remove();
        }else if(favourTotal > 7){
            favourTotal = (favourTotal>99) ? "99+" : favourTotal;
            $("#"+videoId).next().html(favourTotal);
        }
        me.mydata[videoId]["hasfavour"] = true;

    },

    //取消收藏
    cancelCollect : function(favoritesId,obj){

        $.ajax({
            url: "/H5/favorites/delete",
            data : {
                "favoritesId":favoritesId
            },
            type: "post",
            success: function(data) {
                if(data.resultCode == 0){
                    obj.parent().parent().remove();
                }
            },
            error: function() {
                console.log('网络繁忙');
            }
        });

    },

    //分享视频
    shareVideo : function(that){

        var me = this,
            videoId = that.attr("id").substr(6),
            obj = me.mydata[videoId],
            shareObj = {
                type:"url",
                target:["weixin","weixin_c","qq","qzone"],
                title : obj["title"],
                desc : obj["desciption"] || "",
                content : obj["shareUrl"] || "",
                img : obj["videoCoverUrl"]
            };
        console.log(obj["shareUrl"]);   //打印分享地址
        YsAppBridge.share(shareObj);

    },

    //模板
    _template : {

        //头像列表模板
        "portrait" : ["<% for(var i = 0;i<data.length;i++){%>",
            "<% var item = data[i]%>",
            "<li>",
            "<img onerror=praizeUserError(); src='<%=item.avatar%>' />",
            "</li>",
            "<%}%>"].join("")

    },

    //点赞头像列表渲染
    praizeRender : function(data,videoId,total){

        var me = this,
            obj = {
                "data":[]
            },
            _html;

        for(var i = 0;i<data.length;i++){

            if(i == 6) break;
            var avatar = data[i].avatar;    //头像
            if(avatar){
                avatar = 'http://' + yundomain + avatar;
            }else{
                avatar = ImgsObj["portrait"];
            }
            obj.data.push({
                "avatar" : avatar
            })
        }

        var _html = _.template(me._template["portrait"])(obj);
        $("#"+videoId).html(_html);

        if(total > 6){
            total = (total>99) ? "99+" : total;
            var $praize_num = $('<div class="praize_num">'+total+'</div>');
            $("#"+videoId).after($praize_num);
            $("#"+videoId).find("li")[$("#"+videoId).find("li").length-1].remove();
            $praize_num.click(function(){
                var urlStr = "praize?videoId="+videoId;
                $SU.openUrl(urlStr);
            })
        }

    },

    //页面渲染
    render : function(data){

        var me = this;

        for(var i = 0;i<data.length;i++){

            var temp_obj = me.mydata[data[i].videoId] = {},
                deviceStatus = temp_obj["deviceStatus"] = data[i].deviceStatus,        //设备是否在线
                ownerAvatar = temp_obj["ownerAvatar"] =data[i].ownerAvatar,            //头像
                name = temp_obj["name"] =data[i].nickname || data[i].ownerName,        //昵称
                time = temp_obj["time"] =$YS.getGapTime(data[i].uploadTime),           //发布时间
                videoType = temp_obj["videoType"] =data[i].videoType,                  //视频类型
                title = temp_obj["title"] =$YS.encodeString(data[i].title),            //视频标题
                videoCoverUrl = temp_obj["videoCoverUrl"] =data[i].videoCoverUrl,      //视频封面
                statPlay = temp_obj["statPlay"] =$YS.getCountData(data[i].statPlay),   //视频播放次数
                hasfavour = temp_obj["hasfavour"] =data[i].hasfavour,                  //是否点赞
                statComment = temp_obj["statComment"] =data[i].statComment,            //评论次数
                statFavour = temp_obj["statFavour"] =data[i].statFavour,               //点赞次数
                videoId = temp_obj["videoId"] =data[i].videoId,                        //视频ID
                favoritesId = temp_obj["favoritesId"] =data[i].favoritesId,            //收藏ID
                desciption = temp_obj["desciption"] = data[i].meno,                    //视频描述
                videoinfo = temp_obj["videoinfo"] = $SU.getVideoInfo(data[i]),         //视频信息
                videoSymbol = temp_obj["videoSymbol"] = "",                            //视频角标
                isPrevue = temp_obj["isPrevue"] = data[i].prevue,                      //是否预告
                md5Serial =  temp_obj["md5Serial"] = data[i].md5Serial,                //md5图片数据
                praize_btn = temp_obj["praize_btn"] = "";                              //点赞按钮状态
                collectStr = "";                                                       //收藏视频列表html片段

            if(ownerAvatar){
                ownerAvatar = 'http://' + yundomain + ownerAvatar;
            }else{
                ownerAvatar = ImgsObj["portrait"];
            }

            if(hasfavour){
                praize_btn = '<div class="praize_btn"><img src="'+ ImgsObj["praizeLBtn"] +'" /></div>';
            }else{
                praize_btn = '<div class="praize_btn doing"><img src="'+ ImgsObj["praizeBtn"] +'" /></div>';
            }

            //1为点播，2位直播
            if(videoType == 2){

                if(!!videoCoverUrl){
                    videoCoverUrl = temp_obj["videoCoverUrl"] = videoCoverUrl + "@400w_225h";
                }else{
                    videoCoverUrl = temp_obj["videoCoverUrl"] = 'http://' + yundomain + md5Serial + '_web.jpeg';
                }

                temp_obj["shareUrl"] = 'http://' + window.location.host + '/square/mobile/play.jsp?squareid=' + videoId;
                if(isPrevue){
                    videoSymbol = '<div class="video_type"><img src="'+ImgsObj["prevueType"]+'" /></div>';
                }

            }else if(videoType == 1){
                videoCoverUrl = temp_obj["videoCoverUrl"] = videoCoverUrl + "@240w_135h_80Q";
                temp_obj["shareUrl"] = 'http://' + window.location.host + '/vod/h5/view/detail?videoId=' + videoId;
                videoSymbol = '<div class="video_type"><img src="'+ImgsObj["vcrType"]+'" /></div>';
            }

            if((videoType == 2 && deviceStatus == 1) || videoType == 1){
                me.getPraizePortrait(videoId);
                collectStr = $('<div class="collect_info"><div class="author_info"><div class="author_pic"><img src="'+ownerAvatar+'" onerror=userError(); /></div><div class="author_name">'+name+'</div><div class="author_time">'+time+'</div><div class="clear"></div></div><div class="video_info" id="cover_'+videoId+'"><div class="video_cover"><img onerror=coverError(); src="'+videoCoverUrl+'" /></div>'+videoSymbol+'<div class="video_title">'+title+'</div><div class="video_view_count"><img src="'+ImgsObj["viewCount"]+'" /><span>'+statPlay+'</span></div></div><div class="praize_info">'+praize_btn+'<ul class="praize_portrait_list" id="'+videoId+'"></ul><div class="collect_share" id="share_'+videoId+'"><img src="'+ImgsObj["shareBtn"]+'" /></div><div class="collect_line"></div><div class="collect_review" id="review_'+videoId+'"><div class="collect_review_icon"><img src="'+ImgsObj["reviewCount"]+'" /></div><div class="collect_review_num">'+ $YS.getCountData(statComment)+'</div></div><div class="clear"></div></div></div>');
            }else{
                collectStr = $('<div class="collect_info"><div class="author_info"><div class="author_pic"><img src="'+ownerAvatar+'" onerror=userError(); /></div><div class="author_name">'+name+'</div><div class="author_time">'+time+'</div><div class="clear"></div></div><div class="video_info"><div class="video_cover"><img onerror=coverError(); src="'+videoCoverUrl+'" /></div><div class="video_mask"></div><div class="video_over"><img src="'+ImgsObj["collectOver"]+'" /></div></div><div class="praize_info"><div class="cancel-btn" id="'+favoritesId+'"><img src="'+ImgsObj["cancelBtn"]+'" /></div></div></div>');
            }

            $("#collect_wrapper").append(collectStr);

            //播放视频
            $("#cover_"+videoId).on("click",function(){
                var videoId = $(this).attr("id").substr(6),
                    videoinfo = me.mydata[videoId]["videoinfo"];
                $SU.openPlayer(videoinfo);
            });

            //点击评论，进入播放视频页面
            $("#review_"+videoId).on("click",function(){
                var videoId = $(this).attr("id").substr(7),
                    videoinfo = me.mydata[videoId]["videoinfo"];
                $SU.openPlayer(videoinfo);
            });

            //点赞
            $("#"+videoId).prev().on("click",function(){
                var that = $(this).next(),
                    videoId = that.attr("id"),
                    videoType = me.mydata[videoId]["videoType"],
                    hasfavour = me.mydata[videoId]["hasfavour"];
                if(!hasfavour){
                    if(videoType == 2){
                        me.livePrize(videoId,that);
                    }else if(videoType == 1){
                        me.videoPrize(videoId,that);
                    }
                }
            })

            //取消收藏
            $("#"+favoritesId).on("click",function(){
                var that = $(this),
                    favoritesId = that.attr("id");
                me.cancelCollect(favoritesId,that);
            })

            //分享视频
            $("#share_"+videoId).on("click",function(){
                var that = $(this);
                me.shareVideo(that);
            })
        }

    },

    //喜欢页面初始化
    init : function(){
        var me = this;
        me.versionHandle();
        me.getImgsObj();
        me.handleEvent();
        me.getUserInfo();
        me.getCollectList();
    }

}).init();




