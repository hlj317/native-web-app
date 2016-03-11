define(['underscore'], function (_) {
    'use strict';

    return _.inherit({

        _propertys: function () {
            this.APP = this.APP || window.APP;
            var i = 0, len = 0, k;
            if (this.APP && this.APP.interface) {
                for (i = 0, len = this.APP.interface.length; i < len; i++) {
                    k = this.APP.interface[i];
                    if (k == 'showPageView') continue;

                    if (_.isFunction(this.APP[k])) {
                        this[k] = $.proxy(this.APP[k], this.APP);
                    }
                    else this[k] = this.APP[k];
                }
            }

            this.header = this.APP.header;
        },

        //显示具体层级的视图
        showPageView: function (name, _viewdata, id) {
            this.APP.curViewIns = this;
            this.APP.showPageView(name, _viewdata, id)
        },
        propertys: function () {
            //这里设置UI的根节点所处包裹层
            this.wrapper = $('#main');
            this.id = _.uniqueId('page-view-');
            this.classname = '';

            this.viewId = null;
            this.refer = null;

            //模板字符串，各个组件不同，现在加入预编译机制
            this.template = '';
            //事件机制
            this.events = {};

            //自定义事件
            //此处需要注意mask 绑定事件前后问题，考虑scroll.radio插件类型的mask应用，考虑组件通信
            this.eventArr = {};

            //初始状态为实例化
            this.status = 'init';

            this._propertys();
        },

        getViewModel: function () {
            //假如有datamodel的话，便直接返回，不然便重写，这里基本为了兼容
            if (_.isObject(this.datamodel)) return this.datamodel;
            return {};
        },

        //子类事件绑定若想保留父级的，应该使用该方法
        addEvents: function (events) {
            if (_.isObject(events)) _.extend(this.events, events);
        },

        //订阅事件的回调函数
        on: function (type, fn, insert) {
            if (!this.eventArr[type]) this.eventArr[type] = [];

            //头部插入
            if (insert) {
                this.eventArr[type].splice(0, 0, fn);
            } else {
                this.eventArr[type].push(fn);
            }
        },

        //取消事件的回调函数
        off: function (type, fn) {
            if (!this.eventArr[type]) return;
            if (fn) {
                this.eventArr[type] = _.without(this.eventArr[type], fn);
            } else {
                this.eventArr[type] = [];
            }
        },

        //定义类的自定义事件
        trigger: function (type) {
            var _slice = Array.prototype.slice;
            var args = _slice.call(arguments, 1);
            var events = this.eventArr;
            var results = [], i, l;

            if (events[type]) {
                for (i = 0, l = events[type].length; i < l; i++) {
                    results[results.length] = events[type][i].apply(this, args);
                }
            }
            return results;
        },

        //创建根节点
        createRoot: function (html) {

            //如果存在style节点，并且style节点不存在的时候需要处理
            if (this.style && !$('#page_' + this.viewId)[0]) {
                $('head').append($('<style id="page_' + this.viewId + '" class="page-style">' + this.style + '</style>'))
            }

            //如果具有fake节点，需要移除
            $('#fake-page').remove();

            //UI的根节点
            this.$el = $('<div class="cm-view page-' + this.viewId + ' ' + this.classname + '" style="display: none; " id="' + this.id + '">' + html + '</div>');
            if (this.wrapper.find('.cm-view')[0]) {
                this.wrapper.append(this.$el);
            } else {
                this.wrapper.html('').append(this.$el);
            }

        },

        //是否是默认事件
        _isAddEvent: function (key) {
            if (key == 'onCreate' || key == 'onPreShow' || key == 'onShow' || key == 'onRefresh' || key == 'onHide')
                return true;
            return false;
        },

        //根据参数重置当前属性
        setOption: function (options) {

            for (var k in options) {

                if (k == 'events') {
                    _.extend(this[k], options[k]);
                    continue;
                } else if (this._isAddEvent(k)) {
                    this.on(k, options[k])
                    continue;
                } else if(k == "html"){
                    var _html = options[k]();
                    this[k] = _html;
                    continue;
                }
                this[k] = options[k];
            }

        },

        //组件初始化
        initialize: function (opts) {
            //设置默认属性
            this.propertys();
            //根据参数重置属性
            this.setOption(opts);
            //检测不合理属性，修正为正确数据
            this.resetPropery();
            //为当前类添加事件
            this.addEvent();
            this.create();

            this.initElement();

            //window.sss = this;

        },

        //选择器
        $: function (selector) {
            return this.$el.find(selector);
        },

        //提供属性重置功能，对属性做检查
        resetPropery: function () { },

        //各事件注册点，用于被继承override
        addEvent: function () {
        },

        //渲染模板，定义组件生命周期的相关事件
        create: function () {
            this.trigger('onPreCreate');   //自定义事件：onPreCreate
            //如果没有传入模板，说明html结构已经存在
            this.createRoot(this.render());
            this.status = 'create';
            this.trigger('onCreate');
        },

        //实例化需要用到到dom元素
        initElement: function () { },

        //渲染模板，创建html片段
        render: function (callback) {
            var data = this.getViewModel() || {};
            var html = this.template;
            if (!this.template) return '';
            //引入预编译机制，比如webpack
            if (_.isFunction(this.template)) {
                html = this.template(data);
            } else {
                html = _.template(this.template)(data);
            }
            typeof callback == 'function' && callback.call(this);
            return html;
        },

        /**
         * @description 组件刷新方法，首次显示会将ui对象实际由内存插入包裹层
         * @method refresh
         * @param {Boolean} needRecreate 组件是否重新创建，生命周期状态为create
         */
        refresh: function (needRecreate) {
            this.resetPropery();
            if (needRecreate) {
                this.create();
            } else {
                this.$el.html(this.render());   //render可以传入回调函数，渲染之后的业务逻辑
            }
            this.initElement();   //初始化DOM
            if (this.status != 'hide') this.show();
            this.trigger('onRefresh');
        },

        /**
        * @description 组件显示方法，首次显示会将ui对象实际由内存插入包裹层
        * @method show
        */
        show: function () {
            this.trigger('onPreShow');
            //      //如果包含就不要乱搞了
            //      if (!$.contains(this.wrapper[0], this.$el[0])) {
            //        //如果需要清空容器的话便清空
            //        if (this.needEmptyWrapper) this.wrapper.html('');
            //        this.wrapper.append(this.$el);
            //      }

            this.$el.show();
            this.status = 'show';

            this.bindEvents();

            this.initHeader();
            this.trigger('onShow');
        },

        initHeader: function (){},

        //组件隐藏方法
        hide: function () {
            if (!this.$el || this.status !== 'show') return;

            this.trigger('onPreHide');
            this.$el.hide();

            this.status = 'hide';
            this.unBindEvents();
            this.trigger('onHide');
        },

        //组件销毁方法
        destroy: function () {
            this.status = 'destroy';
            this.unBindEvents();
            this.$root.remove();
            this.trigger('onDestroy');
            delete this;
        },

        //初始化事件
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
        },

        //获取当前url中的key值
        getParam: function (key) {
            return _.getUrlParam(window.location.href, key)
        },

        //渲染模板
        renderTpl: function (tpl, data) {
            if (!_.isFunction(tpl)) tpl = _.template(tpl);
            return tpl(data);
        }


    });

});
