'use strict';

var AbstractView = require('AbstractView'),
    layoutHtml = require('./tpl.layout.ejs');

var indexPage = _.inherit(AbstractView, {
    propertys: function ($super) {
        $super();
        this.template = layoutHtml;
        this.events = {
            'click .js_clickme': 'clickAction'
        };
    },

    clickAction: function () {
        this.showMessage('显示消息');
    },

    initHeader: function (name) {
        var title = '多Webview容器';
        this.header.set({
            view: this,
            title: title,
            back: function () {
                console.log('回退');
            }
        });
    }
});

module.exports = indexPage;
