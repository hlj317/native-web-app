'use strict';

require('../../css/components/toast.css');
var Observers = require('base').Observers;
var Tools = require('base').Tools;

/*Toast组件*/
/*
 * 自定义了两个事件
 *
 * @name show
 * 对话框显示触发
 * @param config
 *
 * @name hide
 * 对话框关闭触发
 * @param config
 */

var Toast = (function(){

    var instance;  //单例模式

    return function(config) {

        if(instance){
            return instance;
        }

        Observers.call(this);  //继承观察者机制

        this._initFlag = false;  //是否初始化
        this.isShow = false;     //是否显示

        /*公开方法：显示Toast*/
        /* 可选配置项
         * @param title：显示标题
         * @param time：显示时间
         */
        this.show  = function(title,time){
            var me = this;
            if(this.isShow) return;
            this.isShow = true;
            this.config = {
                "title" : "",
                "time" : 3000
            }
            if(!this._initFlag){
                this._init();
                this._initFlag = true;
            }
            this._setTitle(title);
            var _start = this.config.time / 4,
                _duration = this.config.time / 2,
                _end = this.config.time / 4;
            $(".comp_toast").fadeIn(_start,function(){
                setTimeout(function(){
                    $(".comp_toast").fadeOut(_end,function(){
                         me.isShow = false;
                    });
                },_duration);
            });
        };

        /*初始化*/
        this._init = function(){
            this.$toast = $("<div class='comp_toast'></div>");
            $("body").append(this.$toast);
        };

        /*设置Toast标题*/
        this._setTitle = function(title){

            var me = this,
                _width,
                _title;
            _title = Tools.subString(title,25);
            me.$toast.html(_title);

        }

        return instance = this;

    }

})();


module.exports = new Toast();
