define(function (require, exports) {
    var O = {};

    //数据加入模板|str[string](模板字符串), obj[object](数据对象)
    O.formatTemplate = function (str, obj) {
        for (var i in obj) {
            if (i.indexOf("dataSrc") > -1) {
                str = str.replace(eval('/data-src=\"{%=' + i + '%}\"/g'), "src=\"" + obj[i] + "\"");
            } else {
                str = str.replace(eval('/{%=' + i + '%}/g'), obj[i]);
            }
        }

        return str;
    }

    //jsonp数据转json
    O.getJSONPData = function (str) {
        return JSON.parse(str.substring(5, str.length - 1));
    }

    //判断数据是否为空值
    O.isNothing = function(t){
        return t === '' || t === null || t === undefined;
    }
    //截取字符串长度
    O.cutString = function (str, len) {
        var
            strlen = 0,
            s = '';

        str = this.encodeString(str);

        if (str.length * 2 <= len) {
            s = str;
        } else {
            for (var i = 0; i < str.length; i++) {
                s = s + str.charAt(i);
                if (str.charCodeAt(i) > 128) {
                    strlen = strlen + 2;
                    if (strlen >= len) {
                        s = s.substring(0, s.length - 1) + '...';
                        break;
                    }
                } else {
                    strlen = strlen + 1;
                    if (strlen >= len) {
                        s = s.substring(0, s.length - 2) + '...';
                        break;
                    }
                }
            }
        }

        return s;
    }

    //防止脚本注入
    O.rejectScript = function(s){
        if(Object.prototype.toString.call(s) === '[object String]'){
            return s.replace(/</g, "&lt;").replace(/>/g, "&gt;");
        }else{
            return '';
        }
    }

    //字符串编码
    O.encodeString = function (str) {
        if (str === null || str === "" || !str) {
            return "";
        }
        str = str.replace(/</g, "&lt;");
        str = str.replace(/>/g, "&gt;");
        str = str.replace(/\'/g, "&#39;");
        str = str.replace(/\"/g, "&quot;");
        str = str.replace(/[\r\n]/g, '');
        return str;
    }

    //替换表情
    O.replaceEmotionFace = function (str) {
        str = this.encodeString(str);
        str = str.replace(/\[em_([0-9]*)\]/g, '<img src="' + seajs.resolve("jquery_emotionFace").match(/[\:\/\w\.]+\//) + 'imgs/$1.png" border="0" >');
        return str;
    }

    O.getFlatternDistance = function (lat1, lng1, lat2, lng2) {
        if(this.isNothing(lat1)||this.isNothing(lat2)||this.isNothing(lng1)||this.isNothing(lng2)){
            return '';
        }
        lat1 = lat1 * 1;
        lat2 = lat2 * 1;
        lng1 = lng1 * 1;
        lng2 = lng2 * 1;
        var EARTH_RADIUS = 6378.1370;    //单位kM
        var PI = Math.PI;

        function getRad(d) {
            return d * PI / 180.0;
        }

        var f = getRad((lat1 + lat2) / 2);
        var g = getRad((lat1 - lat2) / 2);
        var l = getRad((lng1 - lng2) / 2);

        var sg = Math.sin(g);
        var sl = Math.sin(l);
        var sf = Math.sin(f);

        var s, c, w, r, d, h1, h2, result;
        var a = EARTH_RADIUS;
        var fl = 1 / 298.257;

        sg = sg * sg;
        sl = sl * sl;
        sf = sf * sf;

        s = sg * (1 - sl) + (1 - sf) * sl;
        c = (1 - sg) * (1 - sl) + sf * sl;

        w = Math.atan(Math.sqrt(s / c));
        r = Math.sqrt(s * c) / w;
        d = 2 * w * a;
        h1 = (3 * r - 1) / 2 / c;
        h2 = (3 * r + 1) / 2 / s;
        result = Math.round((d * (1 + fl * (h1 * sf * (1 - sg) - h2 * (1 - sf) * sg))) * 1000) / 1000;

        return this.mapDistance(result, 'km');
    }
    O.mapDistance = function (d, t) {
        if (t === 'km') {
            if (d < 1) {
                return Math.round(d * 1000) + 'm';
            } else if (d > 99) {
                return Math.round(d) + 'km';
            } else {
                return d.toFixed(1) + 'km';
            }
        } else {
            return d
        }

    }
    //时间比较处理|start[string](开始时间), end[string](结束时间)
    O.comparisonTime = function (start, end) {
        var
            getTimeArray = function (str) {
                var
                    a0 = str.split(' '),
                    a1 = a0[0];

                if (a1.indexOf('-') > -1) {
                    a1 = a1.split('-');
                } else {
                    a1 = a1.split('/');
                }

                return {'a0': a1, 'a1': a0[1]}
            },
            startA = getTimeArray(start),
            endA = getTimeArray(end),
            c = 0;

        start = startA.a0[1] + '-' + startA.a0[2] + '-' + startA.a0[0] + ' ' + startA.a1;
        end = endA.a0[1] + '-' + endA.a0[2] + '-' + endA.a0[0] + ' ' + endA.a1;
        //c = (Date.parse(start) - Date.parse(end)) / 3600 / 1000;
        c = (new Date(start) - new Date(end)) / 3600 / 1000;

        return c > 0 ? true : false;
    };

    //获取到当前时间的间隔
    O.getGapTime = function (timeStr) {
        var
            str = "",
            nowD = new Date(),
            time = new Date(timeStr),
            dayLine = new Date(nowD.getFullYear() + '-' + (nowD.getMonth() + 1) + '-' + nowD.getDate() + " 00:00:00");
        timeGap = parseInt((new Date() - time) / 1000 / 60),
            getTimeStr = function () {
                var
                    str = " ",
                    h = time.getHours() + '',
                    m = time.getMinutes() + '';

                if (h.length === 1) {
                    h = '0' + h;
                }

                if (m.length === 1) {
                    m = '0' + m;
                }

                str = str + h + ":" + m;
                return str;
            },
            getDayStr = function () {
                var
                    str = "",
                    m = (time.getMonth() + 1) + "",
                    d = time.getDate() + "";

                if (m.length === 1) {
                    m = "0" + m;
                }

                if (d.length === 1) {
                    d = "0" + d;
                }

                str = str + m + "-" + d;
                return str;
            };

        if (timeGap <= 1) {
            str = "刚刚"
        } else if (timeGap > 1 && timeGap <= 60) {
            timeGap === 0 ? timeGap = 1 : timeGap = timeGap;
            str = timeGap + "分钟前";
        } else if (parseInt(timeGap / 60) <= 24 && dayLine - time <= 0) {
            str = parseInt(timeGap / 60) + "小时前";
        } else if (dayLine - time > 0 && (dayLine - time) / 1000 / 60 / 60 < 24) {
            str = "昨天" + getTimeStr();
        } else {
            if (nowD.getFullYear() === time.getFullYear()) {
                str = getDayStr() + getTimeStr();
            } else {
                str = time.getFullYear() + "-" + getDayStr();
            }
        }

        return str;
    }

    //智能机浏览器版本信息:
    O.getBrowserVision=function(){
        var u = navigator.userAgent;
        return {//移动终端浏览器版本信息
            trident: u.indexOf('Trident') > -1, //IE内核
            presto: u.indexOf('Presto') > -1, //opera内核
            webKit: u.indexOf('AppleWebKit') > -1, //苹果、谷歌内核
            gecko: u.indexOf('Gecko') > -1 && u.indexOf('KHTML') == -1, //火狐内核
            mobile: !!u.match(/AppleWebKit.*Mobile.*/)||!!u.match(/AppleWebKit/), //是否为移动终端
            ios: !!u.match(/\(i[^;]+;( U;)? CPU.+Mac OS X/), //ios终端
            android: u.indexOf('Android') > -1 || u.indexOf('Linux') > -1, //android终端或者uc浏览器
            iPhone: u.indexOf('iPhone') > -1 || u.indexOf('Mac') > -1, //是否为iPhone或者QQHD浏览器
            iPad: u.indexOf('iPad') > -1, //是否iPad
            webApp: u.indexOf('Safari') == -1 //是否web应该程序，没有头部与底部
        };
    }
    //获取统计数据，超过万数据带万单位
    O.getCountData = function (num) {
        if ((num / 10000) < 1) {
            return num;
        } else {
            return (num / 10000).toFixed(1) + "万";
        }
    }
    //获取对象类型
    O.getObjectType = function (p) {
        var type = Object.prototype.toString.call(p);
        return type.substring(8, type.length - 1).toLowerCase();
    }
    O.clone = function (obj) {
        var o = obj.constructor === Array ? [] : {};
        for (var i in obj) {
            if (obj.hasOwnProperty(i)) {
                o[i] = typeof obj[i] === "object" ? this.clone(obj[i]) : obj[i];
            }
        }
        return o;
    };
    //获取单个url参数
    O.getUrlParam = function(name,url) {
        var r = new RegExp("(\\?|#|&)"+name+"=(.*?)(#|&|$)");
        var m = (url || location.href).match(r);
        return decodeURIComponent(m ? m[2] : '');
    };
    //滑动分页
    O.slider = function (opts) {
        var F = function (opts) {
            this.outer = opts.dom;
            this.list = opts.list;
            this.callback = opts.callback || function () {
            };

            this.init();
            this.renderDOM();
            this.bindDOM();
            if (opts.enLoop) {
                var loopTimer = opts.loopTimer || 5000;
                this.setLoop(loopTimer);
            }
        };

        F.prototype.setLoop = function (timer) {
            var
                that = this,
                fun = function () {
                    if (!that.onTouch) {
                        that.move('+1');
                    }
                };

            setInterval(fun, timer);
        }

        F.prototype.init = function () {
            this.outer.innerHTML = "";
            this.outerW = this.outer.offsetWidth;
            this.idx = 0;
        }

        F.prototype.renderDOM = function () {
            try {
                this.outer.innerHTML = '';
            } catch (e) {
            }

            for (var i = 0; i < this.list.length; i++) {
                var childElement = document.createElement('ul');
                childElement.style.position = 'absolute';
                childElement.style.width = this.outerW + 'px';
                childElement.style.webkitTransform = 'translate3d(' + this.outerW * i + 'px, 0, 0)';
                childElement.innerHTML = this.list[i];
                this.outer.appendChild(childElement);
            }
        }

        F.prototype.bindDOM = function () {
            var
                that = this,
                startHandler = function (e) {
                    that.startTime = new Date() * 1;
                    that.startX = e.touches[0].pageX;
                    that.offsetX = 0;
                    that.onTouch = true;
                },
                moveHandler = function (e) {
                    e.preventDefault();
                    that.offsetX = e.touches[0].pageX - that.startX;

                    var
                        uls = that.outer.getElementsByTagName('ul'),
                        len = uls.length,
                        prevNum, nextNum;

                    prevNum = that.idx - 1;
                    nextNum = that.idx + 1;

                    if (that.idx == len - 1) {
                        nextNum = 0;
                    }

                    if (that.idx == 0) {
                        prevNum = len - 1;
                    }

                    if (!(len <= 3 && that.offsetX < 0)) {
                        uls[prevNum] && (uls[prevNum].style.webkitTransform = 'translate3d(' + (that.offsetX - that.outerW) + 'px, 0, 0)');
                        uls[prevNum] && (uls[prevNum].style.webkitTransition = 'none');
                    }

                    uls[that.idx] && (uls[that.idx].style.webkitTransform = 'translate3d(' + that.offsetX + 'px, 0, 0)');
                    uls[that.idx] && (uls[that.idx].style.webkitTransition = 'none');

                    if (!(len <= 3 && that.offsetX > 0)) {
                        uls[nextNum] && (uls[nextNum].style.webkitTransform = 'translate3d(' + (that.outerW + that.offsetX) + 'px, 0, 0)');
                        uls[nextNum] && (uls[nextNum].style.webkitTransition = 'none');
                    }
                },
                endHandler = function () {
                    var
                        boundary = that.outerW / 6,
                        endTime = new Date() * 1,
                        uls = that.outer.getElementsByTagName('ul');

                    if (endTime - that.startTime > 800) {
                        if (that.offsetX >= boundary) {
                            that.move('-1', true);
                        } else if (that.offsetX < -boundary) {
                            that.move('+1', true);
                        } else {
                            that.move('0', true);
                        }
                    } else {
                        if (that.offsetX > 50) {
                            that.move('-1', true);
                        } else if (that.offsetX < -50) {
                            that.move('+1', true);
                        } else {
                            that.move('0', true);
                        }
                    }

                    that.onTouch = false;
                };

            this.onTouch = false;
            this.outer.addEventListener('touchstart', startHandler);
            this.outer.addEventListener('touchmove', moveHandler);
            this.outer.addEventListener('touchend', endHandler);

            this.removeEvent = function () {
                that.outer.removeEventListener('touchstart', startHandler);
                that.outer.removeEventListener('touchmove', moveHandler);
                that.outer.removeEventListener('touchend', endHandler);
            }
        }

        F.prototype.move = function (n, haveMove) {
            var
                idx = this.idx,
                cidx,
                uls = this.outer.getElementsByTagName('ul'),
                len = uls.length,
                outerW = this.outerW,
                prevNum, nextNum;

            if (typeof n === 'number') {
                cidx = idx;
            } else if (typeof n === 'string') {
                cidx = idx + n * 1;
            }

            prevNum = cidx - 1;
            nextNum = cidx + 1;
            if (cidx > len - 1) {
                prevNum = len - 1;
                cidx = 0;
                nextNum = cidx + 1;
            } else if (cidx == len - 1) {
                prevNum = cidx - 1;
                nextNum = 0;
            } else if (cidx < 0) {
                prevNum = len - 2;
                cidx = len - 1;
                nextNum = 0;
            } else if (cidx == 0) {
                prevNum = len - 1;
                nextNum = cidx + 1;
            }

            this.idx = cidx;
            this.callback(this.idx);

            for (var i = 0; i < len; i++) {
                uls[i].style.webkitTransition = 'none';
                if (i == prevNum || i == cidx || i == nextNum) {
                    uls[i].style.display = 'block';
                } else {
                    uls[i].style.display = 'none';
                }
            }

            var moveFun = function () {
                if (!(len <= 3 && n * 1 < 0)) {
                    uls[prevNum] && (uls[prevNum].style.webkitTransition = '-webkit-transform 0.2s ease-out');
                    uls[prevNum] && (uls[prevNum].style.webkitTransform = 'translate3d(-' + outerW + 'px, 0, 0)');
                }

                uls[cidx].style.webkitTransition = '-webkit-transform 0.2s ease-out';
                uls[cidx].style.webkitTransform = 'translate3d(0, 0, 0)';

                if (!(len <= 3 && n * 1 > 0)) {
                    uls[nextNum] && (uls[nextNum].style.webkitTransition = '-webkit-transform 0.2s ease-out');
                    uls[nextNum] && (uls[nextNum].style.webkitTransform = 'translate3d(' + outerW + 'px, 0, 0)');
                }
            }

            if (len <= 3 && !haveMove) {
                uls[cidx].style.webkitTransform = 'translate3d(' + outerW + 'px, 0, 0)';
                setTimeout(moveFun, 0);
            } else {
                moveFun();
            }
        }

        return new F(opts);
    }

    //滑动条
    O.slideBar = function (opts) {
        var F = function (opts) {
            var
                dom0 = opts.dom0,
                dom1 = opts.dom1,
                dom0W = opts.dom0W,
                dom1W = opts.dom1W,
                maxW = dom0W - dom1W,
                startX = 0,
                startPointX = 0,
                startTime = 0,
                offsetX = 0,
                lastOffsetX = 0,
                startHandler = function (e) {
                    offsetX = 0;
                    startPointX = e.touches[0].pageX;
                    startX = e.touches[0].pageX - lastOffsetX;
                    startTime = new Date() * 1;
                    dom1.style.webkitTransition = '';
                },
                moveHandler = function (e) {
                    e.preventDefault();

                    offsetX = e.touches[0].pageX - startX;
                    offsetX > 0 ? offsetX = 0 : offsetX = offsetX;
                    offsetX < maxW ? offsetX = maxW : offsetX = offsetX;
                    dom1.style.webkitTransform = 'translate3d(' + offsetX + 'px, 0, 0)';

                    lastOffsetX = offsetX;
                },
                endHandler = function (e) {
                    var
                        nowX = e.changedTouches[0].pageX,
                        moveX = startPointX - nowX,
                        timeGap = new Date() * 1 - startTime,
                        speed = Math.abs(moveX) / timeGap;

                    if (timeGap < 300 && Math.abs(moveX) > 70) {
                        dom1.style.webkitTransition = '-webkit-transform 0.2s ease-out';
                        if (moveX >= 0) {
                            lastOffsetX = maxW;
                        } else {
                            lastOffsetX = 0;
                        }
                    } else if (timeGap < 300 && Math.abs(moveX) > 20 && Math.abs(moveX) <= 70) {
                        if (moveX >= 0) {
                            lastOffsetX = Math.floor((maxW + lastOffsetX) / 2);
                        } else {
                            lastOffsetX = Math.floor((0 + lastOffsetX) / 2);
                        }
                        dom1.style.webkitTransition = '-webkit-transform ' + Math.floor(Math.abs(lastOffsetX) / speed) + 'ms ease-out';
                    }
                    dom1.style.webkitTransform = 'translate3d(' + lastOffsetX + 'px, 0, 0)';
                };

            dom0.addEventListener('touchstart', startHandler);
            dom0.addEventListener('touchmove', moveHandler);
            dom0.addEventListener('touchend', endHandler);
        }

        return new F(opts);
    }

    //序列化字符串反向处理
    O.param = function (str) {
        var
            obj = {},
            list = null;

        if (str) {
            list = str.split('&');
            $.each(list, function (index, item) {
                var a = item.split('=');
                obj[a[0]] = a[1];
            });
        }

        return obj;
    }

    //获取超链接参数
    O.getParam = (function () {
        var hash = window.location.href.split('?')[1];
        return O.param(hash);
    }())

    //存储处理
    O.storage = (function () {
        var
            enLocalStorage = window.localStorage ? true : false,
            enCookie = navigator.cookieEnabled ? true : false,
            f = {};

        //初始化存储
        f.init = function (keys, val) {
            var
                getKey = function (key) {
                    if (key === '' || key === null || typeof(key) === 'undefined') {
                        key = val;
                    }

                    return key;
                };

            for (var i = 0; i < keys.length; i++) {
                if (enLocalStorage) {
                    window.localStorage[keys[i]] = getKey(window.localStorage[keys[i]]);
                } else if (enCookie) {
                    Zepto.fn.cookie(keys[i], getKey(Zepto.fn.cookie(keys[i])), {'expires': 30});
                }
            }
        }

        //获取存储
        f.get = function (key) {
            var value = '0';

            if (enLocalStorage) {
                value = window.localStorage[key];
            } else if (enCookie) {
                value = Zepto.fn.cookie(key);
            }

            if(typeof(value) == undefined && value == '' || value == null) {
                value = '[]';
            }

            return value;
        }

        //设置存储
        f.set = function (key, value) {
            if (enLocalStorage) {
                window.localStorage[key] = value;
            } else if (enCookie) {
                Zepto.fn.cookie(key, value, {'expires': 30});
            }
        }

        return f;
    }());

    return O;
});