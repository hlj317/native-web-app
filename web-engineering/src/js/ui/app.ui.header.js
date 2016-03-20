'use strict';

var UIHeader = require('UIHeader'),
    template = require('T_APPUIHeader');

var APPUIHeader = _.inherit(UIHeader, {

    propertys: function ($super) {
        $super();
        this.template = template;
    },

    hide: function ($super) {
        $super();
        //      this.wrapper.hide();
    },

    show: function ($super) {
        $super();
    }

});

module.exports = APPUIHeader;
