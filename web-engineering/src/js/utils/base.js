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
        },

        //类继承
        extend : function (supClass, childAttr) {

            //若是传了第一个类，便继承之；否则实现新类
            if (typeof supClass === 'object') {
                childAttr = supClass;
                supClass = function(){};
            }

            //定义我们创建的类
            var newClass = function(){
                this._propertys_();
                this.init.apply(this, arguments);
            };

            newClass.prototype = new supClass();

            var supInit = newClass.prototype.init || function () {
                };
            var childInit = childAttr.init || function () {
                };
            var _supAttr = newClass.prototype._propertys_ || function () {
                };
            var _childAttr = childAttr._propertys_ || function () {
                };

            for (var k in childAttr) {
                //_propertys_中作为私有属性
                childAttr.hasOwnProperty(k) && (newClass.prototype[k] = childAttr[k]);
            }

            //继承的属性有可能重写init方法
            if (arguments.length && arguments[0].prototype && arguments[0].prototype.init === supInit) {
                //重写新建类，初始化方法，传入其继承类的init方法
                newClass.prototype.init = function () {
                    var scope = this;
                    var args = [function () {
                        supInit.apply(scope, arguments);
                    }];
                    childInit.apply(scope, args.concat([].slice.call(arguments)));
                };
            }

            //内部属性赋值
            newClass.prototype._propertys_ = function () {
                _supAttr.call(this);
                _childAttr.call(this);
            };

            //成员属性
            for (var k in supClass) {
                supClass.hasOwnProperty(k) && (newClass[k] = supClass[k]);
            }
            return newClass;
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