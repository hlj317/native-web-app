
define(['UIHeader', 'T_APPUIHeader','underscore'], function (UIHeader, template,_) {

    return _.inherit(UIHeader, {

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
});
