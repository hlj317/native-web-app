'use strict';

var path = require('path');
var fs = require('fs');

var webpack = require('webpack');
var _ = require('lodash');   //lodash是javascript的类库，相当于underscore.js
//有时候可能希望项目的样式能不要被打包到脚本中，而是独立出来作为.css，然后在页面中以<link>标签引入。这时候我们需要 extract-text-webpack-plugin
var ExtractTextPlugin = require('extract-text-webpack-plugin');  
var HtmlWebpackPlugin = require('html-webpack-plugin');   //通过模板生成html文件

var UglifyJsPlugin = webpack.optimize.UglifyJsPlugin;   //压缩js
var CommonsChunkPlugin = webpack.optimize.CommonsChunkPlugin;   //公共模块

var srcDir = path.resolve(process.cwd(), 'src');
var assets = 'assets/';
var sourceMap = require('./src/sourcemap.json');    //映射文件

var excludeFromStats = [
    /node_modules[\\\/]/
];

function makeConf(options) {
    options = options || {};

    var debug = options.debug !== undefined ? options.debug : true;
    var entries = genEntries();   //获取入口文件配置
    var chunks = Object.keys(entries);  //返回一个提取对象中key的数组，例如:obj={"a":1,"b":2}，返回["a","b"]
    var config = {
        entry: entries,

        output: {
            // 在debug模式下，__build目录是虚拟的，webpack的dev server存储在内存里
            path: path.resolve(debug ? '__build' : assets),
            filename: debug ? '[name].js' : 'js/[chunkhash:8].[name].min.js',
            chunkFilename: debug ? '[chunkhash:8].chunk.js' : 'js/[chunkhash:8].chunk.min.js',
            hotUpdateChunkFilename: debug ? '[id].[chunkhash:8].js' : 'js/[id].[chunkhash:8].min.js',
            publicPath: debug ? '/__build/' : '../'
        },
     
        resolve: {
            //在webpack配置项里，可以把node_modules路径添加到resolve search root列表里边，这样就可以直接load npm模块了
            root: [srcDir, './node_modules'], 
            alias: sourceMap,   //资源引用别名
            //extensions:可以用来指定模块的后缀，这样在引入模块时就不需要写后缀，会自动补全。
            extensions: ['', '.js', '.css', '.less', '.scss', '.tpl', '.png', '.jpg']
        },

        resolveLoader: {
            root: path.join(__dirname, 'node_modules')
        },

        //文件加载器
        module: {
            //确定一个模块中没有其它新的依赖 就可以配置这项，webpack将不再扫描这个文件中的依赖。
            noParse: ['zepto'],
            loaders: [{
                test: /\.(jpe?g|png|gif|svg)$/i,
                loaders: [
                    // 'image?{bypassOnDebug: true, progressive:true,optimizationLevel: 3, pngquant:{quality: "65-80", speed: 4}}',
                    // url-loader更好用，小于10KB的图片会自动转成dataUrl，
                    // 否则则调用file-loader，参数直接传入
                    'url?limit=10000&name=imgs/[hash:8].[name].[ext]'
                ]
            },
            {
                test: /\.json$/,
                loader: 'json'
            },
            {
                test: /\.(woff|eot|ttf)$/i,
                loader: 'url?limit=10000&name=fonts/[hash:8].[name].[ext]'
            }, {
                test: /\.(tpl|html)$/,
                loader: 'html'
            }, {
                test: /\.(ejs)$/,
                loader: 'ejs'
            }, {
                test: /\.js$/,
                exclude: /node_modules/,
                loader: 'jsx'
            }]
        },
        //定义了需要使用的插件，比如commonsPlugin在打包多个入口文件时会提取公用的部分，生成common.js;
        plugins: [
            //new CommonsChunkPlugin({
            //     name: 'vendors',  //将公共模块提取，生成名为`vendors`的chunk
            //     chunks: ['zepto'],   //['a','b','c']
            //     // Modules must be shared between all entries
            //     minChunks: chunks.length // 提取所有chunks共同依赖的模块
            //})
            //,
            new CommonsChunkPlugin({
                name: 'vendors',
                chunks: ['hot','city','bmap','class','collect','map','play','praize','search','video'],
                // Modules must be shared between all entries
                minChunks: 2 // 提取所有chunks共同依赖的模块
            })
            //new CommonsChunkPlugin({
            //    name: 'common-bc',
            //    chunks: ['vendors', 'b', 'c'],
            //    minChunks: 2
            //})
        ],

        devServer: {
            stats: {
                cached: false,
                exclude: excludeFromStats,
                colors: true
            }
        }
    };

    if (debug) {
        // 开发阶段，css直接内嵌
        var cssLoader = {
            test: /\.css$/,
            loader: 'style!css'
        };
        var sassLoader = {
            test: /\.scss$/,
            loader: 'style!css!sass'
        };
        var lessLoader = {
            test: /\.less/,
            loader: 'style!css!less'
        };

        config.module.loaders.push(cssLoader);
        config.module.loaders.push(sassLoader);
        config.module.loaders.push(lessLoader);
    } else {
        // 编译阶段，css分离出来单独引入
        var cssLoader = {
            test: /\.css$/,
            loader: ExtractTextPlugin.extract('style', 'css?minimize') // enable minimize
        };
        var sassLoader = {
            test: /\.scss$/,
            loader: ExtractTextPlugin.extract('style', 'css?minimize', 'sass')
        };
        var lessLoader = {
            test: /\.less$/,
            loader: ExtractTextPlugin.extract('style', 'css?minimize', 'less')
        };

        config.module.loaders.push(cssLoader);
        config.module.loaders.push(sassLoader);
        config.module.loaders.push(lessLoader);

        config.plugins.push(
            new ExtractTextPlugin('css/[contenthash:8].[name].min.css', {
                // 当allChunks指定为false时，css loader必须指定怎么处理
                // additional chunk所依赖的css，即指定`ExtractTextPlugin.extract()`
                // 第一个参数`notExtractLoader`，一般是使用style-loader
                // @see https://github.com/webpack/extract-text-webpack-plugin
                allChunks: false
            })
        );

        // 自动生成入口文件，入口js名必须和入口文件名相同
        // 例如，a页的入口文件是a.html，那么在js目录下必须有一个a.js作为入口文件
        var htmlDir = path.resolve(srcDir,'vod');   //相当于解析到此路径：src/js
        var pages = fs.readdirSync(htmlDir);

        pages.forEach(function(filename) {
            var m = filename.match(/(.+)\.ejs/);

            if (m) {
                // @see https://github.com/kangax/html-minifier
                var conf = {
                    template: path.resolve(htmlDir, filename),   //srcDir:'./src'(引用文件)
                    // @see https://github.com/kangax/html-minifier
                    // minify: {
                    //     collapseWhitespace: true,
                    //     removeComments: true
                    // },
                    filename: filename  //(输出文件)
                    // ,
                    // inject: 'body',
                    // chunks: ['vendors', 'a']
                };

                if (m[1] in config.entry) {
                    conf.inject = 'body';
                    conf.chunks = ['ys-common','vendors',m[1]];
                }
                //HtmlWebpackPlugin支持从模板生成html文件，生成的html里边可以正确解决js打包之后的路径、文件名问题
                config.plugins.push(new HtmlWebpackPlugin(conf));
            }
        });

        //config.plugins.push(new UglifyJsPlugin());    //js压缩
    }

    return config;
}

//遍历src目录下所有的js文件
function genEntries() {
    var jsDir = path.resolve(srcDir, 'js/view-learn');   //相当于解析到此路径：src/js
    var names = fs.readdirSync(jsDir);
    var map = {};

    //公共类库
    map["ys-common"] = ['commonJs','zepto','baidu','underscore','resetCss','commonCss'];

    names.forEach(function(name) {
        var m = name.match(/(.+)\.js$/);
        var entry = m ? m[1] : '';
        var entryPath = entry ? path.resolve(jsDir, name) : '';
        if (entry) map[entry] = entryPath;
    });

    //返回的map对象如下：
    // map = {
    //     "a":"E:\\webpack-bootstrap\\src\\js\\a.js",
    //     "b":"E:\\webpack-bootstrap\\src\\js\\b.js",
    //     "c":"E:\\webpack-bootstrap\\src\\js\\c.js"
    // }
    return map;
}

module.exports = makeConf;