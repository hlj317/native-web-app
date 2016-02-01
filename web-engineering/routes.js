'use strict';

var proxy = require('koa-proxy'),
    hot = require('./controllers/vod/hot'),
    collect = require('./controllers/vod/collect'),
    logger	= require( './controllers/tools/logger' ),
    city = require('./controllers/vod/city'),
    play = require('./controllers/vod/play'),
    Router	= require( 'koa-router' ),
    httpTools = require( './controllers/tools/http-tools');

/*创建路由实例对象*/
var router = new Router();

module.exports = function(app) {

    /****************捕捉未知的异常处理**********************/
    app.use( function *(next){
        try{
            yield next;
        } catch (e){
            logger.error(e);
            this.body = { code: -1, msg: 'server error' };
        }

    });

    /****************页面请求处理**********************/
    router.get('/', hot.renderIndex);                  //热门页面
    router.get('/hot', hot.renderIndex);               //热门页面
    router.get('/class', hot.renderClass);             //分类页面
    router.get('/search', hot.renderSearch);           //搜索页面
    router.get('/city', city.renderCity);              //同城页面
    router.get('/map', city.renderMap);                //地图页面
    router.get('/collect', collect.renderCollect);     //喜欢页面
    router.get('/praize', collect.renderPraize);       //点赞的人页面
    router.get('/play',play.renderPlay);               //直播页面
    router.get('/video',play.renderVideo);             //点播页面
    router.get('/bmap',play.renderBmap);               //详细地图页面


    /****************接口请求处理**********************/

    /*热门页面*/
    router.all('/H5/channel/city', hot.getList);               //获取直播列表
    router.all('/H5/square/banner', hot.getBanner);            //获取banner
    router.all('/H5/square/special',hot.getWonderful);         //获取今日精彩
    router.all('/H5/square/query', hot.getMixList);            //获取视频列表（直播+点播）
    router.all('/H5/channel/hot',hot.getHotChannelList);       //获取热门频道列表

    /*喜欢页面*/
    router.all('/H5/user/avatar', collect.getUserInfo);           //获取用户个人信息
    router.all('/H5/favorites/list', collect.getCollectList);     //获取用户收藏列表
    router.all('/H5/favour/top', collect.getFavourList);          //获取点赞头像列表
    router.all('/H5/favour/add', collect.livePrize);              //直播视频点赞
    router.all('/vod/square/statfavour/:id', collect.videoPrize); //点播视频点赞
    router.all('/H5/favorites/delete', collect.cancelCollect);    //取消收藏

    /*同城页面*/
    router.all('/H5/square/get', city.getVideoInfo);        //获取单个视频详情信息
    router.all('/H5/city/hot', city.getHotCity);            //获取热门城市
    router.all('/H5/square/map', city.getMapList);          //分批获取视频点地图信息

    /*播放页面*/
    router.all('/api/user/notices/get', play.getAd);                   //获取萤石云广告
    router.all('/H5/square/recommend/get', play.getRecommend);         //获取推荐视频
    router.all('/H5/server/get/type', play.getServerType);             //获取server类型，用于广场评论
    router.all('/ys/user/other/checksubscribe', play.checkSubscribe);  //查询是否已关注
    router.all('/H5/square/view/add', play.addPlay);                   //直播播放次数统计+1
    router.all('/vod/square/statplay/:id', play.addVidoePlay);         //点播播放次数统计+1
    router.all('/remark/demoRemarkAction!getList.action', play.getRemark);   //获取直播评论列表
    router.all('/vod/comments/list', play.getViewoRemark);                   //获取点播评论
    router.all('/vod/comments/add', play.sendRemark);                        //发表点播评论
    router.all('/vod/square/get/:id', play.getVideo);                        //获取点播视频详情

    app.use( router.middleware() );

};