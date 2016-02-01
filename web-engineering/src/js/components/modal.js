'use strict';

require('../../css/components/modal.css');
var Observers = require('base').Observers;

/*对话框组件*/
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

var Modal=(function(){

    var instance;  //单例模式

    return function(config) {

        if(instance){
            return instance;
        }

        Observers.call(this);  //继承观察者机制

        this._initFlag = false;  //是否初始化

        /*对话框配置项*/
        /*
         * 可选配置项
         * @param title
         * @param content
         */
        this.config = (function(){

            var _config = {
                title : "标题",
                content : "请填上内容"
            };

            for(var i in config){
                if(config.hasOwnProperty(i)){
                    _config[i] = config[i];
                }
            }

            return _config;

        })();

        /*公开方法：显示对话框*/
        this.show  = function(){

            if(!this._initFlag){
                this._init();
                this._initFlag = true;
            }else{
                this.$modal.removeClass("hide");
                this.$mask.removeClass("hide");
            }
            this.trigger("show",this.config);
        };

        /*公开方法：隐藏对话框*/
        this.hide  = function(){
            this.$modal.addClass("hide");
            this.$mask.addClass("hide");
            this.trigger("hide",this.config);
        };

        /*对话框模板*/
        this._Tpl = ["<div class='title'><%=data.title%></div>",
                "<div class='content'><%=data.content%></div>",
                "<div class='button'>确定</div>"].join("");

        /*初始化*/
        this._init = function(){
            var me = this,
                obj = {"data":this.config},
                _html;
            this.$modal = $("<div class='comp_modal'></duv>");
            this.$mask = $("<div class='comp_mask'></duv>");
            $("body").append(this.$modal);
            $("body").append(this.$mask);
            _html = _.template(this._Tpl)(obj);
            $(".comp_modal").html(_html);
            this.$sure = $(".button");
            this.$sure.on("click",function(){
                me.hide();
            });
        };
        return instance = this;
    }

})();

module.exports = Modal;
