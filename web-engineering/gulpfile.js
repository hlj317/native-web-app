'use strict';

var gulp = require('gulp');
var webpack = require('webpack');

var gutil = require('gulp-util');  //主要是用来打印日志，抛出异常错误
var notify = require('gulp-notify');     //更改提醒
var imagemin = require('gulp-imagemin'); //图片压缩
var cache = require('gulp-cache');       //图片缓存，只有图片替换了才压缩

var webpackConf = require('./webpack.config');
var webpackDevConf = require('./webpack-dev.config');

//返回当前进程的工作目录
var src = process.cwd() + '/src';
var assets = process.cwd() + '/assets';

// js错误语法提示
gulp.task('hint', function() {
    var jshint = require('gulp-jshint');
    var stylish = require('jshint-stylish');

    return gulp.src([
            '!' + src + '/js/lib/**/*.js',
            src + '/js/**/*.js'
        ])
        .pipe(jshint())
        .pipe(jshint.reporter(stylish));
});

// 清除缓存(原有目录和资源文件)
gulp.task('clean', ['hint'], function() {
    var clean = require('gulp-clean');

    return gulp.src(assets, {read: true}).pipe(clean());
});

// 这里的done不加，就不会执行gulp.task('default'……中的回调函数)
gulp.task('pack', ['clean'], function(done) {
    webpack(webpackConf, function(err, stats) {
        if(err) throw new gutil.PluginError('webpack', err);
        gutil.log('[webpack]', stats.toString({colors: true}));
        done();
    });
});

//引用第三方资源(非压缩)
gulp.task('thirdparty',['pack'],function() {
  return gulp.src('./src/js/thirdparty/*.js')
    .pipe(gulp.dest('./assets/js/thirdparty'));
});

//压缩图片(启用webpack压缩)
//gulp.task('imgs',['pack'],function() {
//  return gulp.src('./src/imgs/**')
//    .pipe(imagemin({ optimizationLevel: 5, progressive: true, interlaced: true }))
//    .pipe(gulp.dest('./assets/imgs'))
//    .pipe(notify({ message: 'Images task complete' }));
//});

// html process
gulp.task('default', ['thirdparty'], function() {
    var replace = require('gulp-replace');   //资源替换，这里的作用是把开发环境引用的资源替换掉(去掉)
    var htmlmin = require('gulp-htmlmin');   //压缩html文件
    return gulp
        .src(assets + '/*.ejs')
        .pipe(replace(/<script(.+)?data-debug(.+)?><\/script>/g,''))
        // @see https://github.com/kangax/html-minifier
        // ejs和html文件压缩
        //.pipe(htmlmin({
        //    collapseWhitespace: true,
        //    removeComments: true
        //}))
        .pipe(gulp.dest(assets));
});

// deploy assets to remote server
gulp.task('deploy', function() {
    var sftp = require('gulp-sftp');

    return gulp.src(assets + '/**')
        .pipe(sftp({
            host: '',
            remotePath: 'c:/',   //www/app/
            user: '',
            pass: ''
        }));
});

// run HMR on `cli` mode
// @see http://webpack.github.io/docs/webpack-dev-server.html
gulp.task('hmr', function(done) {
    var WebpackDevServer = require('webpack-dev-server');
    var compiler = webpack(webpackDevConf);
    var devSvr = new WebpackDevServer(compiler, {
        contentBase: webpackConf.output.path,
        publicPath: webpackDevConf.output.publicPath,
        hot: true,
        stats: webpackDevConf.devServer.stats
    });

    devSvr.listen(8080, '0.0.0.0', function(err) {
        if(err) throw new gutil.PluginError('webpack-dev-server', err);

        gutil.log('[webpack-dev-server]','http://localhost:8080/index.html');

        // keep the devSvr alive
        // done();
    });
});
