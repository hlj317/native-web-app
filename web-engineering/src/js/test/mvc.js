var Base = require("base").Tools;
var _ = require("underscore");

//继承相关逻辑
(function () {

    // 全局可能用到的变量
    var arr = [];
    var slice = arr.slice;
    /**
     * inherit方法，js的继承，默认为两个参数
     *
     * @param  {function} origin  可选，要继承的类
     * @param  {object}   methods 被创建类的成员，扩展的方法和属性
     * @return {function}         继承之后的子类
     */
    _.inherit = function (origin, methods) {

        // 参数检测，该继承方法，只支持一个参数创建类，或者两个参数继承类
        if (arguments.length === 0 || arguments.length > 2) throw '参数错误';

        var parent = null;

        // 将参数转换为数组
        var properties = slice.call(arguments);

        // 如果第一个参数为类（function），那么就将之取出
        if (typeof properties[0] === 'function')
            parent = properties.shift();
        properties = properties[0];

        // 创建新类用于返回
        function klass() {
            if (_.isFunction(this.initialize))
                this.initialize.apply(this, arguments);
        }

        klass.superclass = parent;

        // 父类的方法不做保留，直接赋给子类
        // parent.subclasses = [];

        if (parent) {
            // 中间过渡类，防止parent的构造函数被执行
            var subclass = function () { };
            subclass.prototype = parent.prototype;
            klass.prototype = new subclass();

            // 父类的方法不做保留，直接赋给子类
            // parent.subclasses.push(klass);
        }

        var ancestor = klass.superclass && klass.superclass.prototype;
        for (var k in properties) {
            var value = properties[k];

            //满足条件就重写
            if (ancestor && typeof value == 'function') {
                var argslist = /^\s*function\s*\(([^\(\)]*?)\)\s*?\{/i.exec(value.toString())[1].replace(/\s/g, '').split(',');
                //只有在第一个参数为$super情况下才需要处理（是否具有重复方法需要用户自己决定）
                if (argslist[0] === '$super' && ancestor[k]) {
                    value = (function (methodName, fn) {
                        return function () {
                            var scope = this;
                            var args = [
                                function () {
                                    return ancestor[methodName].apply(scope, arguments);
                                }
                            ];
                            return fn.apply(this, args.concat(slice.call(arguments)));
                        };
                    })(k, value);
                }
            }

            //此处对对象进行扩展，当前原型链已经存在该对象，便进行扩展
            if (_.isObject(klass.prototype[k]) && _.isObject(value) && (typeof klass.prototype[k] != 'function' && typeof value != 'fuction')) {
                //原型链是共享的，这里处理逻辑要改
                var temp = {};
                _.extend(temp, klass.prototype[k]);
                _.extend(temp, value);
                klass.prototype[k] = temp;
            } else {
                klass.prototype[k] = value;
            }
        }

        //静态属性继承
        //兼容代码，非原型属性也需要进行继承
        for (key in parent) {
            if (parent.hasOwnProperty(key) && key !== 'prototype' && key !== 'superclass')
                klass[key] = parent[key];
        }

        if (!klass.prototype.initialize)
            klass.prototype.initialize = function () { };

        klass.prototype.constructor = klass;

        return klass;
    };

})();

//基类view设计
var AbstractView = _.inherit({
    propertys: function () {
        this.$el = $('#main');
        //事件机制
        this.events = {};
    },
    initialize: function (opts) {
        //这种默认属性
        this.propertys();
    },
    $: function (selector) {
        return this.$el.find(selector);
    },
    show: function () {
        this.$el.show();
        this.bindEvents();
    },
    bindEvents: function () {
        var events = this.events;

        if (!(events || (events = _.result(this, 'events')))) return this;
        this.unBindEvents();

        // 解析event参数的正则
        var delegateEventSplitter = /^(\S+)\s*(.*)$/;
        var key, method, match, eventName, selector;

        // 做简单的字符串数据解析
        for (key in events) {
            method = events[key];
            if (!_.isFunction(method)) method = this[events[key]];
            if (!method) continue;

            match = key.match(delegateEventSplitter);
            eventName = match[1], selector = match[2];
            method = _.bind(method, this);
            eventName += '.delegateUIEvents' + this.id;

            if (selector === '') {
                this.$el.on(eventName, method);
            } else {
                this.$el.on(eventName, selector, method);
            }
        }
        return this;
    },

    unBindEvents: function () {
        this.$el.off('.delegateUIEvents' + this.id);
        return this;
    }

});

//基类Model设计
var AbstractModel = _.inherit({
    initialize: function (opts) {
        this.propertys();
        this.setOption(opts);
    },

    propertys: function () {
        //只取页面展示需要数据
        this.data = {};

        //局部数据改变对应的响应程序，暂定为一个方法
        //可以是一个类的实例，如果是实例必须有render方法
        this.controllers = {};

        //全局初始化数据时候调用的控制器
        this.initController = null;

        this.scope = null;

    },

    addController: function (k, v) {
        if (!k || !v) return;
        this.controllers[k] = v;
    },

    removeController: function (k) {
        if (!k) return;
        delete this.controllers[k];
    },

    setOption: function (opts) {
        for (var k in opts) {
            this[k] = opts[k];
        }
    },

    //首次初始化时，需要矫正数据，比如做服务器适配
    //@override
    handleData: function () { },

    //一般用于首次根据服务器数据源填充数据
    initData: function (data) {
        var k;
        if (!data) return;

        //如果默认数据没有被覆盖可能有误
        for (k in this.data) {
            if (data[k]) this.data[k] = data[k];
        }

        this.handleData();

        if (this.initController && this.get()) {
            this.initController.call(this.scope, this.get());
        }

    },

    //验证data的有效性，如果无效的话，不应该进行以下逻辑，并且应该报警
    //@override
    validateData: function () {
        return true;
    },

    //获取数据前，可以进行格式化
    //@override
    formatData: function (data) {
        return data;
    },

    //获取数据
    get: function () {
        if (!this.validateData()) {
            //需要log
            return {};
        }
        return this.formatData(this.data);
    },

    _update: function (key, data) {
        if (typeof this.controllers[key] === 'function')
            this.controllers[key].call(this.scope, data);
        else if (typeof this.controllers[key].render === 'function')
            this.controllers[key].render.call(this.scope, data);
    },

    //数据跟新后需要做的动作，执行对应的controller改变dom
    //@override
    update: function (key) {
        var data = this.get();
        var k;
        if (!data) return;

        if (this.controllers[key]) {
            this._update(key, data);
            return;
        }

        for (k in this.controllers) {
            this._update(k, data);
        }
    }
});


//博客的model模块应该是完全独立与页面的主流层的，并且可复用
var Model = _.inherit(AbstractModel, {
    propertys: function () {
        this.data = {
            blogs: []
        };
    },
    //新增博客
    add: function (title, type, label) {
        //做数据校验，具体要多严格由业务决定
        if (!title || !type) return null;

        var blog = {};
        blog.id = 'blog_' + _.uniqueId();
        blog.title = title;
        blog.type = type;
        if (label) blog.label = label.split(',');
        else blog.label = [];

        this.data.blogs.push(blog);

        //通知各个控制器变化
        this.update();

        return blog;
    },
    //删除某一博客
    remove: function (id) {
        if (!id) return null;
        var i, len, data;
        for (i = 0, len = this.data.blogs.length; i < len; i++) {
            if (this.data.blogs[i].id === id) {
                data = this.data.blogs.splice(i, 1)
                this.update();
                return data;
            }
        }
        return null;
    },
    //获取所有类型映射表
    getTypeInfo: function () {
        var obj = {};
        var i, len, type;
        for (i = 0, len = this.data.blogs.length; i < len; i++) {
            type = this.data.blogs[i].type;
            if (!obj[type]) obj[type] = 1;
            else obj[type] = obj[type] + 1;
        }
        return obj;
    },
    //获取标签映射表
    getLabelInfo: function () {
        var obj = {}, label;
        var i, len, j, len1, blog, label;
        for (i = 0, len = this.data.blogs.length; i < len; i++) {
            blog = this.data.blogs[i];
            for (j = 0, len1 = blog.label.length; j < len1; j++) {
                label = blog.label[j];
                if (!obj[label]) obj[label] = 1;
                else obj[label] = obj[label] + 1;
            }
        }
        return obj;
    },
    //获取总数
    getNum: function () {
        return this.data.blogs.length;
    }

});

//页面主流程
var View = _.inherit(AbstractView, {
    propertys: function ($super) {
        $super();
        this.$el = $('#main');

        //统合页面所有点击事件
        this.events = {
            'click .js_add': 'blogAddAction',
            'click .js_blog_del': 'blogDeleteAction'
        };

        //实例化model并且注册需要通知的控制器
        //控制器务必做到职责单一
        this.model = new Model({
            scope: this,
            controllers: {
                numController: this.numController,
                typeController: this.typeController,
                labelController: this.labelController,
                blogsController: this.blogsController
            }
        });
    },
    //总博客数
    numController: function () {
        this.$('.js_num').html(this.model.getNum());
    },
    //分类数
    typeController: function () {
        var html = '';
        var tpl = document.getElementById('js_tpl_kv').innerHTML;
        var data = this.model.getTypeInfo();
        html = _.template(tpl)({ objs: data });
        this.$('.js_type_wrapper').html(html);


    },
    //label分类
    labelController: function () {
        //这里的逻辑与type基本一致，但是真实情况不会这样
        var html = '';
        var tpl = document.getElementById('js_tpl_kv').innerHTML;
        var data = this.model.getLabelInfo();
        html = _.template(tpl)({ objs: data });
        this.$('.js_label_wrapper').html(html);

    },
    //列表变化
    blogsController: function () {
        console.log(this.model.get());
        var html = '';
        var tpl = document.getElementById('js_tpl_blogs').innerHTML;
        var data = this.model.get();
        html = _.template(tpl)(data);
        this.$('.js_blogs_wrapper').html(html);
    },
    //添加博客点击事件
    blogAddAction: function () {
        //此处未做基本数据校验，因为校验的工作应该model做，比如字数限制，标签过滤什么的
        //这里只是往model中增加一条数据，事实上这里还应该写if预计判断是否添加成功，略去
        this.model.add(
            this.$('.js_title').val(),
            this.$('.js_type').val(),
            this.$('.js_label').val()
        );

    },
    blogDeleteAction: function (e) {
        var el = $(e.currentTarget);
        this.model.remove(el.attr('data-id'));
    }
});

var view = new View();
view.show();