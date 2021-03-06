require('../../css/vod/collect');

var $SU = require('squareUtil');

({
    videoId : window.location.href.substr(window.location.href.indexOf("?videoId=")+9),
    mypage : 0,
    isLoading : true,

    //获取点赞用户列表
    getUserList : function(){

        var me = this;

        $.ajax({
            url: "/H5/favour/top",
            data : {
                "page":me.mypage++,
                "num":16,
                "videoId":me.videoId
            },
            type: "post",
            success: function(data) {
                if(data.resultCode == 0 && data.favour.length > 0){
                    me.render(data.favour);
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
    },

    //头像列表模板
    listTpl : ["<% for(var i = 0;i<data.length;i++){%>",
        "<% var item = data[i]%>",
        "<div class='praize_user'>",
        "<img class='praize_portrait' src='<%=item.avatar%>' />",
        "<span class='praize_name'><%=item.name%></span>",
        "</div>",
        "<%}%>"].join(""),

    //绑定浏览器事件
    handleEvent:function(){

        var me = this;

        //下拉加载更多点赞好友
        $SU.setScrollLoad(function(){
            if(!me.isLoading){
                me.isLoading = true;
                $("#loading").css({"display":"block"});
                me.getUserList();
            }
        })

    },

    //页面渲染
    render : function(data){

        var me = this,
            obj = {data:[]};

        for(var i = 0;i<data.length;i++){

            var name = data[i].nickname || data[i].username,     //昵称
                avatar = data[i].avatar;                         //头像地址

            if(avatar){
                avatar = 'http://' + yundomain + avatar;
            }else{
                avatar = "../../imgs/square/collect/user_default.png";
            }

            obj.data.push({
                "name" : name,
                "avatar" : avatar
            });

        }

        var $str = _.template(me.listTpl)(obj);
        $("body").html($str);
        $(".praize_portrait").on("error",me.getDefaultInfo);

    },

    //头像获取不到，取默认头像
    getDefaultInfo : function(e){
        var event = e || window.event;
        event.target.src = "../../imgs/square/collect/user_default.png";
    },

    //页面初始化
    init : function(){
        var me = this;
        me.handleEvent();     //绑定浏览器事件
        me.getUserList();     //获取点赞用户列表
    }

}).init();

