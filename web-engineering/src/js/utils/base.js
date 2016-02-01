define(function(require, exports){

    //工具函数
    Tools = {

        //判断中英文字符长度
        getStrLen : function(str){
            var len = 0;
            for (var i = 0; i < str.length; i++) {
                var c = str.charCodeAt(i);
                //单字节加1
                if ((c >= 0x0001 && c <= 0x007e) || (0xff60 <= c && c <= 0xff9f)) {
                    len++;
                }
                else {
                    len += 2;
                }
            }
            return len;
        },

        //截取中英文字符串
        subString : function(str,num,ellipsis){

            var newStr = "",len = 0;

            for (var i = 0; i < str.length && len < num; i++) {
                var c = str.charCodeAt(i);
                //单字节加1
                if ((c >= 0x0001 && c <= 0x007e) || (0xff60 <= c && c <= 0xff9f)) {
                    len++;
                }
                else {
                    len += 2;
                }
                newStr += str[i];
            }

            if(ellipsis) newStr+= "…";

            return newStr;
        }

    }

    //封装观察者模式类，需继承使用
    function Observers(){

        this.eventsList = [];

        this.on = function(key,fn){

            if(!this.eventsList[key]){
                this.eventsList[key] = [];
            }

            this.eventsList[key].push(fn);

        };

        this.trigger = function(){

            var key = Array.prototype.shift.call(arguments),
                fns = this.eventsList[key];

            if((!fns) || fns.length == 0){
                return false;
            }

            for(var i= 0,fn;fn = fns[i++];){
                fn.apply(this,arguments);
            }

        };

        this.un = function(key,fn){

            var fns = this.eventsList[key];

            if((!fns) || fns.length == 0){
                return false;
            }

            if(!fn){
                fns.length = 0;
            }

            for(var i = 0,_fn;_fn = fns[i++];){
                if(_fn === fn){
                    fns.splice(i-1,1);
                }
            }

        }

    }

    exports.Observers = Observers;
    exports.Tools = Tools;

});