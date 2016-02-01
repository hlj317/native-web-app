(function () {
    var ua = navigator.userAgent.toLowerCase(),
        check = function (r) {
            return r.test(ua);
        },
        checkIE = function () {
            if ((navigator.userAgent.search("MSIE") >= 0) || !!window.ActiveXObject || "ActiveXObject" in window) {
                return true;
            } else {
                return false;
            }
        },
        docMode = document.documentMode,
        isStrict = document.compatMode == "CSS1Compat",
        isOpera = check(/opera/),
        isChrome = check(/\bchrome\b/),
        isWebKit = check(/webkit/),
        isSafari = !isChrome && check(/safari/),
        isSafari2 = isSafari && check(/applewebkit\/4/),
        isSafari3 = isSafari && check(/version\/3/),
        isSafari4 = isSafari && check(/version\/4/),
        isIE = !isOpera && (checkIE()),
        isIE7 = isIE && ( check(/msie 7/) || docMode == 7 ),
        isIE8 = isIE && ( check(/msie 8/) && docMode != 7 ),
        isIE9 = isIE && check(/msie 9/),
        isIE10 = isIE && check(/msie 10/),
        isIE11 = isIE && ua.indexOf('trident') > -1 && ua.indexOf('rv:11') > -1,
        isIE6 = isIE && !isIE7 && !isIE8 && !isIE9 && !isIE10 && !isIE11,
        isFirefox = check(/firefox/),
        isGecko = !isWebKit && check(/gecko/),
        isGecko2 = isGecko && check(/rv:1\.8/),
        isGecko3 = isGecko && check(/rv:1\.9/),
        isBorderBox = isIE && !isStrict,
        isWindows = check(/windows|win32/),
        isMac = check(/macintosh|mac os x/),
        isAir = check(/adobeair/),
        isLinux = check(/linux/),
        isSecure = /^https/i.test(window.location.protocol),

        eventSplitter = /\s+/,
        slice = Array.prototype.slice;

    var util =  {
        phoneReg :/(1[3-9][0-9])\d{4}(\d{4})/,
        log: function () {
            window.console && console.log(str);
        },
        login:function(){
            document.location.href = document.location.href.match(/(\w+\:\/\/[\w\d\:\-.]+)\//)[1] + '/auth?host=' + document.location.host + '&returnUrl='+encodeURIComponent(location.href)+'&r=' + Math.random();
        },
        browser : {
            isOpera: isOpera,
            isWebKit: isWebKit,
            isChrome: isChrome,
            isSafari: isSafari,
            isSafari2: isSafari2,
            isSafari3: isSafari3,
            isSafari4: isSafari4,
            isIE: isIE,
            isFirefox: isFirefox,
            isIE6: isIE6,
            isIE7: isIE7,
            isIE8: isIE8,
            isIE9: isIE9,
            isIE10: isIE10,
            isIE11: isIE11,
            isGecko: isGecko,
            isGecko2: isGecko2,
            isGecko3: isGecko3,
            isBorderBox: isBorderBox,
            isWindows: isWindows,
            isMac: isMac,
            isAir: isAir,
            isLinux: isLinux,
            isSecure: isSecure
        },
        inherit: function (subClass,superClass) {
            function F() {}
            F.prototype = superClass.prototype;
            subClass.prototype = new F();
            subClass.prototype.constructor = subClass;
            subClass.superClass = superClass;
        },
        //万位以上数字转换
        numTrans: function(int){
            var f = 0;
            if( int > 9999 ){
                f =(Math.round(int/10000*10)/10).toFixed(1)+'万'
            }else{
                f = int;
            }
            return f;
        },
        //字符转译--XSS攻击
        encode: function(str) {
            if (!str) {
                return '';
            }
            return str.replace(/\>/g, '&gt;').replace(/\</g, '&lt;');
        },
        Log : function(str) {
            window.console && console.log(str);
        },
        /**
         * 字符替换成表情
         * @param str
         * @returns {*}
         */
        replace_em: function (str) {
            str = str.replace(/\</g, '&lt;');
            str = str.replace(/\>/g, '&gt;');
            str = str.replace(/\n/g, '<br/>');
            str = str.replace(/\[em_([0-9]*)\]/g, '<img src="' + seajs.resolve('jquery_emotionFace').match(/[\:\/\w\.]+\//) + 'img/$1.png" width="24" border="0" />');
            return str;
        },
        /**
         * 限制字符串长度
         * @param str 字符串
         * @param tarNum 要限制的长度(英文字符：1，中文字符：2)
         * @param moreStr 替换被截掉的字符串，默认" ..."
         * @returns {*}
         */
        sliceStr: function (str, tarNum, moreStr) {
            var calNum = 0;
            moreStr = moreStr || ' ...';
            for (var i = 0, l = str.length; i < l; i++) {
                if (str.charCodeAt(i) > 255) calNum = calNum + 2;
                else calNum++;
                if (i + 1 < l && calNum >= tarNum) return str.substring(0, i) + moreStr;
                else if (calNum >= tarNum) return str.substring(0, i);
            }
            return str;
        },
        //调用形式  format(new Date(),'yyyy-MM-dd')
        // Date实例方法（原型方法）
        format : function (time,fmt) {
            var o = {
                "M+": time.getMonth() + 1, // 月份
                "d+": time.getDate(), // 日
                "h+": time.getHours(), // 小时
                "m+": time.getMinutes(), // 分
                "s+": time.getSeconds(), // 秒
                "q+": Math.floor((time.getMonth() + 3) / 3), // 季度
                "S": time.getMilliseconds()
                // 毫秒
            };
            if (/(y+)/.test(fmt)) fmt = fmt.replace(RegExp.$1, (time.getFullYear() + "").substr(4 - RegExp.$1.length));
            for (var k in o)
                if (new RegExp("(" + k + ")").test(fmt)) fmt = fmt
                    .replace(RegExp.$1, (RegExp.$1.length == 1) ? (o[k]) : (("00" + o[k]).substr(("" + o[k]).length)));
            return fmt;
        },
        formatTime : function (time) {
            return time.replace(/(n?)(.{5})/, function(a, b, c) {
                return b ? c + '(第二天)' : a;
            });

        },
        formatWeek : function (day) {
            day = day.split ? day.split(',') : day;
            var exist = {
                '0,1,2,3,4,5,6' : '每天',
                '0,1,2,3,4' : '工作日',
                '5,6' : '周末'
            };
            return exist[day] || (function() {
                    var days = [];
                    day.map(function(v) {
                        days.push(['周一', '周二', '周三', '周四', '周五', '周六', '周日'][v]);
                    });
                    return days.join('&nbsp;');
                })();
        },
        /**
         * 随机从数组中取出几个元素
         */
        randomSome: (function () {
            function loopOne(data, outList, outNum) {
                if (!outNum || !data.length) {
                    try {
                        outList.sort(function (item1, item2) {
                            if (item1._randomSortIndex > item2._randomSortIndex) return true;
                        });
                    } catch (e) {
                        // win7 ie8下莫名的 缺少数字 报错
                    }
                    return;
                }
                outList.push(data.splice(Math.floor(Math.random() * data.length), 1)[0]);

                data.length && arguments.callee.call(null, data, outList, --outNum);
            }

            return function (inList, outNum) {
                var rtnList = [],
                    copyList = inList.slice();
                copyList.forEach(function (item, i) {
                    item._randomSortIndex = i;
                });

                inList.length && loopOne(inList, rtnList, outNum);
                copyList.forEach(function (item) {
                    delete item._randomSortIndex
                });
                return rtnList || [];
            }
        })(),
        Event : {
            on: function (events, callback, context) {
                var calls,
                    event,
                    node,
                    tail,
                    list;
                if (!callback) {
                    return this;
                }
                events = events.split(eventSplitter);
                calls = this._callbacks || (this._callbacks = {});

                while (event = events.shift()) {
                    list = calls[event];
                    node = list ? list.tail : {};
                    node.next = tail = {};
                    node.context = context;
                    node.callback = callback;
                    calls[event] = {tail: tail, next: list ? list.next : node};
                }
                return this;
            },

            fire: function (events) {
                var event, node, calls, tail, args, all, rest, ret;
                if (!(calls = this._callbacks)) return this;
                all = calls.all;
                events = events.split(eventSplitter);
                rest = slice.call(arguments, 1);

                while (event = events.shift()) {
                    if (node = calls[event]) {
                        tail = node.tail;
                        while ((node = node.next) !== tail && (ret = node.callback.apply(node.context || this, rest)) !== false) {
                        }
                    }
                    if (node = all) {
                        tail = node.tail;
                        args = [event].concat(rest);
                        while ((node = node.next) !== tail) {
                            node.callback.apply(node.context || this, args);
                        }
                    }
                }

                return ret;
            },

            off: function (events) {
                var event, node, calls, tail;
                if (!(calls = this._callbacks)) return this;
                events = events.split(eventSplitter);
                while (event = events.shift()) {
                    if (node = calls[event]) {
                        tail = node.tail;
                        node.next = tail;
                    }
                }
            },

            offAll: function () {
                this._callbacks = {};
            }

        },
        eventHelp : {
            delegateEventSplitter: /^(\S+)\s*(.*)$/,
            delegateEvents: function () {
                var events = this.events;
                for (var key in events) {
                    var method = events[key];
                    if (!this.isFunction(method)) method = this[events[key]];
                    if (!method) continue;

                    var match = key.match(this.delegateEventSplitter);
                    var eventName = match[1], selector = match[2];

                    method = this.proxy(method, this);

                    if (selector === "") {
                        this.$el.on(eventName, method);
                    } else {
                        this.$el.on(eventName, selector, method);
                    }

                }
                return this;
            },

            isFunction: function (obj) {
                return typeof obj === "function";
            },

            //保持上下文一致
            proxy: function (func, thisObject) {
                return( function () {
                    return func.apply(thisObject, arguments);
                } );
            }
        }
    };
    if (typeof define == 'function') {
        define( function(){ return util; } );
    }else if (typeof module != 'undefined' && module.exports) {
        module.exports = util;
    }
    return util;

}());