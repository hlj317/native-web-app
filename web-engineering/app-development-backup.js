'use strict';

// load native modules
var http = require('http');
var path = require('path');
var httpProxy = require('http-proxy');
var logger	= require( './controllers/tools/logger' );
var proxy = httpProxy.createProxyServer({});

// load 3rd modules
var koa = require('koa');
var bodyParser	= require('koa-bodyparser');
var serve = require('koa-static');
var colors = require('colors');

// load local modules
var pkg = require('./package.json');
//指向当前shell的环境变量，比如process.env.HOME，可以在cmd中配置：set NODE_ENV=development(开发环境)，set NODE_ENV=production(生产环境)
var env = process.env.NODE_ENV;   
var debug = !env || env === 'development';
var viewDir = debug ? 'src' : 'assets';

// load routes
var routes = require('./routes');

// init framework
var app = koa();

colors.setTheme({
    silly: 'rainbow',
    input: 'grey',
    verbose: 'cyan',
    prompt: 'grey',
    info: 'green',
    data: 'grey',
    help: 'cyan',
    warn: 'yellow',
    debug: 'blue',
    error: 'red'
});

app.keys = [pkg.name, pkg.description];  //设置签名Cookie密钥，该密钥会被传递给 eyGrip
app.proxy = true;  //当 app.proxy 设置为 true 时，支持 X-Forwarded-Host

// 为了实现自定义错误处理逻辑，可以添加 "error" 事件监听器。
app.on('error', function(err, ctx) {
    err.url = err.url || ctx.request.url;
    console.error(err, ctx);
});

app.use(function *(next){
    if (this.url.match(/favicon\.ico$/)) this.body = '';
    yield next;
});

//设置中间件，为http请求对象传入参数
app.use(bodyParser()).use( function *(next){
    this.body = this.request.body;
    yield next;
});

// use routes 调用路由
routes( app );

if(debug) {
    console.log("进入开发调试环境");
    //HMR，将webpack开发服务器以中间件的形式集成到local webserver
    var webpackDevMiddleware = require('koa-webpack-dev-middleware');
    var webpack = require('webpack');
    var webpackDevConf = require('./webpack-dev.config');

    app.use(webpackDevMiddleware(webpack(webpackDevConf), {
        contentBase: webpackDevConf.output.path,
        publicPath: webpackDevConf.output.publicPath,
        hot: true,   //热启动，页面引用静态资源放在内存中，方便调试
        // stats: webpackDevConf.devServer.stats
        stats: {
            cached: false,
            colors: true
        }
    }));
}else{
    console.log("进入生产环境");
}

// handle static files
app.use(serve(path.resolve(__dirname, viewDir), {
    maxage: 0
}));

app = http.createServer(app.callback());

app.listen(3005, '0.0.0.0', function() {

    /*日志脚本测试*/
    logger.trace('This is a Log4js-Test');
    logger.debug('We Write Logs with log4js');
    logger.info('You can find logs-files in the log-dir');
    logger.warn('log-dir is a configuration-item in the log4js.json');
    logger.error('In This Test log-dir is : \'./logs/log_test/\'');

    console.log('app listen success.');
});
