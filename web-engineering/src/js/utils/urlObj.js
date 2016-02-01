/**
 * url解析，包括请求参数和hash。[url] + ?data=xxx情形处理
 */
define(function () {
    return (function () {

        var initParam = (function () {
                var param = document.location.href.match(/[\d\:\w\/\.]+\?([\w\d\=\&]+)/),
                    obj = {},
                    list;
                if (param) {
                    param = param[1];
                    list = param.split('&');
                    $.each(list, function (index, item) {
                        obj[item.split('=')[0]] = item.split('=')[1];
                    });
                }
                return obj;
            })(),

            initHash = (function () {
                return function(key){
                    var hash = window.location.href.split('#')[1],
                        obj = {},
                        list;
                    if (hash) {
                        list = hash.split('&');
                        $.each(list, function (index, item) {
                            obj[item.split('=')[0]] = item.split('=')[1];
                        });
                    }
                    return obj[key] || '';
                }
            })(),

            curHref = document.location.href.match(/(\w+\:\/\/[\w\d\:\-.]+)\//)[1],
            from;
        from = initParam['data'];

        var urlObj = {
            "test1": "https://test1.ys7.com",
            "pb": "https://pbportal.ys7.com",
            "i": "https://i.ys7.com",
            "test2": "https://test2.ys7.com",
            "test": "https://test.shipin7.com"
        };
        from = from && urlObj[from];
        var staticPre = from ? from : '//' + yundomain;
        return {
            preUrl: from || curHref,
            staticPre: staticPre,
            urlParam: initParam,
            urlHash:initHash,
            fileName:document.location.href.match(/\/([\w\d\.]+)$/)
        }
    })();
});