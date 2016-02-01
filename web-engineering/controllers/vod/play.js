var httpTools = require( '../tools/http-tools'),
    request = require('koa-request'),
    config = require( '../../config' );

//渲染直播页面
function *renderPlay() {
    yield this.render('play',{
        "title":"直播",
        "entry":"play",
        "yundomain":config.yundomain,
        "staticPath":config.staticPath,
        "basePath":config.basePath
    });
}

//渲染点播页面
function *renderVideo() {
    yield this.render('video',{
        "title":"点播",
        "entry":"video",
        "yundomain":config.yundomain,
        "staticPath":config.staticPath,
        "basePath":config.basePath
    });
}

//渲染详细地图页面
function *renderBmap() {
    yield this.render('bmap',{
        "title":"地图详情",
        "entry":"bmap",
        "yundomain":config.yundomain,
        "staticPath":config.staticPath,
        "basePath":config.basePath
    });
}

//获取萤石云广告
function *getAd(){
    var options = {
            method: "POST",
            data: this.body,
            url : "https://i.ys7.com/api/user/notices/get"
        };
    options.form = options.data;
    options.headers = this.req.headers;
    var ret = yield request(options);
    this.body = JSON.parse(ret.body);
}

//获取推荐视频
function *getRecommend(){
    var ret = yield httpTools.redirect(this.req,this.res,"/H5/square/recommend/get","POST",this);
    this.body = ret;
}

//获取server类型，用于广场评论
function *getServerType(){
    var ret = yield httpTools.redirect(this.req,this.res,"/H5/server/get/type","POST",this);
    this.body = ret;
}

//查询是否已关注
function *checkSubscribe(){
    var ret = yield httpTools.redirect(this.req,this.res,"/ys/user/other/checksubscribe","POST",this);
    this.body = ret;
}

//直播播放次数统计+1
function *addPlay(){
    var ret = yield httpTools.redirect(this.req,this.res,"/H5/square/view/add","POST",this);
    this.body = ret;
}

//获取评论列表
function *getRemark(){
    var ret = yield httpTools.redirect(this.req,this.res,"/remark/demoRemarkAction!getList.action","POST",this);
    this.body = ret;
}

//发表评论
function *sendRemark(){
    var ret = yield httpTools.redirect(this.req,this.res,"/vod/comments/add","POST",this);
    this.body = ret;
}

//点播播放次数统计+1
function *addVidoePlay(){
    var url = "/vod/square/statplay/" + this.params.id;
    var ret = yield httpTools.redirect(this.req, this.res,url,"POST",this);
    this.body = ret;
}

//获取点播视频详情
function *getVideo(){
    var url = "/vod/square/get/" + this.params.id;
    var ret = yield httpTools.redirect(this.req, this.res,url,"GET",this);
    this.body = ret;
}

//发表评论
function *getViewoRemark(){
    var ret = yield httpTools.redirect(this.req,this.res,"/vod/comments/list","POST",this);
    this.body = ret;
}

module.exports = {
    getAd : getAd,
    getRecommend : getRecommend,
    getServerType : getServerType,
    checkSubscribe : checkSubscribe,
    addPlay : addPlay,
    getRemark : getRemark,
    sendRemark : sendRemark,
    addVidoePlay : addVidoePlay,
    getVideo : getVideo,
    getViewoRemark : getViewoRemark,
    renderPlay : renderPlay,
    renderVideo : renderVideo,
    renderBmap : renderBmap
}
