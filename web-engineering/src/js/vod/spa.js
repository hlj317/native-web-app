var b = window.b = {};//base
var slice = [].slice;

b.Class = function (supClass, childAttr) {
    //若是传了第一个类，便继承之；否则实现新类
    if (typeof supClass === 'object') {
        childAttr = supClass;
        supClass = function () {
        };
    }

    //定义我们创建的类
    var newClass = function () {
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
            childInit.apply(scope, args.concat(slice.call(arguments)));
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
};

var Bird = new b.Class({
    //作为自身属性将被调用，里面必须采用this.XX的方式书写
    _propertys_: function () {
        this.name = '鸟类';
        this.age = 0;
    },
    //一定会被执行的初始化方法
    init: function () {
        alert('一定会执行');
    },
    //原型方法
    breathe: function () {
        alert('我能呼吸');
    }
});

var Chicken = new b.Class(Bird, {
    _propertys_: function () {
        this.sex = '公鸡';
    },
    //一定会被执行的初始化方法
    init: function () {
        alert('我是一只鸡');
    },
    //原型方法
    howl: function () {
        alert('我能打鸣');
    }
});

b.AbstractView = b.Class({
    //基本view应该具有的属性
    _propertys_: function () {
        this.id = (new Date()).getTime(); //唯一pageID
        this.rootBox = $('body'); //视图容器
        this.root = $('<div/>'); //视图的根元素，可进行设置
        this.header = null;
        this.footer = null;
        this.template = '';//可能的模板
        this.isCreated = false;//是否创建完毕
        this.status = b.AbstractView.STATE_NOTCREATE;//当前状态
    },
    init: function () {
    },
    //定义将要用到的事件，其中元素选取都会以root为标准，所以使用内部提供函数吧
    events: {
        'selector,eventType': 'func'
    },
    //默认属性
    attrs: {
    },
    //获取视图元素
    find: function (selector) {
        return this.root.find(selector);
    },
    //创建dom
    create: function (opts) {
        if(!this.isCreated && this.status != b.AbstractView.STATE_ONCREATE) {
            var attr = opts && opts.attr;
            var html = this.createHtml();
            this.initRoot(attr);//初始化root
            this.hide();
            this.rootBox.append(this.root);
            this.root.html(html);
            this.trigger('onCreate');//触发正在创建事件，其实这里都创建完了
            this.status = b.AbstractView.STATE_ONCREATE;
            this.isCreated = true;
            this.bindEvent();
        }
    },
    //呈现/渲染视图
    show: function (callback) {
        if(this.status == b.AbstractView.STATE_ONSHOW) {
            return;
        }
        this.create();
        this.root.show();
        this.trigger('onShow');
        this.status = b.AbstractView.STATE_ONSHOW
        callback && (typeof callback == 'function') && callback.call(this);
        this.trigger('onLoad');
    },
    //隐藏dom
    hide: function (callback) {
        if(!this.root || this.status == b.AbstractView.STATE_ONHIDE) {
            return;
        }
        this.root.hide();
        this.trigger('onHide');
        this.status = b.AbstractView.STATE_ONHIDE;
        callback && (typeof callback == 'function') && callback();
    },
    //事件绑定
    bindEvent: function () {
        var events = this.events;
        for(var k in events) {
            var sec_type = k.replace(/\s/i, '').split(',');
            var func = events[k];
            if(sec_type &&sec_type.length == 2 && typeof func == 'function') {
                var selector = sec_type[0];
                var type = sec_type[1];
                var scope = this;
                this.find(selector).on(type, function () {
                    func.call(scope, $(this));
                })
            }
        }
    },
    //此处可以配合模板与相关参数组成html
    //解析模板也放到此处
    createHtml: function () {
        throw new Error('请重新定义createHtml方法');
    },
    initRoot: function () {
        var attr = this.attrs;
        if(!attr) {
            return;
        }
        for(var k in attr) {
            if(k == 'className') {
                this.root.attr('class', attr[k]);
            }else {
                this.root.attr(k, attr[k]);
            }
        }
        this.root.attr('id', this.id);
    },
    //触发事件
    trigger: function (k, args) {
        var event = this[k];
        args = args || [];
        if(event && typeof event == 'function') {
            event.apply(this, args)
        }
    },
    setRootBox: function (dom) {
        this.rootBox = dom;
    },
    setAttr: function (k, v) {
        this.root.attr(k, v);
    },
    getAttr: function (k) {
        return this.root.attr(k);
    },
    setCss: function (k, v) {
        this.root.css(k, v);
    },
    getCss: function (k) {
        return this.root.css(k);
    },
    //dom创建后执行
    onCreate: function () {

    },
    //dom创建后数据加载时执行，用于加载后执行我们的逻辑
    onLoad: function () {

    },
    //dom创建后，未显示
    onShow: function () {

    },
    //dom隐藏前
    onHide: function () {

    }
});

//组件状态,未创建
b.AbstractView.STATE_NOTCREATE = 'notCreate';
//组件状态,已创建但未显示
b.AbstractView.STATE_ONCREATE = 'onCreate';
//组件状态,已显示
b.AbstractView.STATE_ONSHOW = 'onShow';
//组件状态,已隐藏
b.AbstractView.STATE_ONHIDE = 'onHide';