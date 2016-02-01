define(function(require, exports) {
var
_ = require('underscore'),
$YS = require('squareYs'),

//指定host
host = $YS.getParam['host'] ? $YS.getParam['host'] : '',

//来源
from = $YS.getParam['f'] ? $YS.getParam['f'] : '',

//版本
version = (function() {

    var
      version = 0,
      versionStr = $YS.getParam['version'];

    if(versionStr) {
      var versionArray = /(\d+)\.(\d+)\.(\d+)/.exec(versionStr);
      versionArray.splice(0, 1);

      for(var i = 0; i < versionArray.length; i++) {
          version += Number(versionArray[i]) * Math.pow(1000, 2 - i);
      }
    }

  return version;
}()),

//系统类型0未知、1Android、2IOS
osType = (function() {
    var
      osType = 0,
      ua = navigator.userAgent;

    if(ua.indexOf('Android') > -1 || ua.indexOf('Linux') > -1) {
      osType = 1;
    }

    if(!!ua.match(/\(i[^;]+;( U;)? CPU.+Mac OS X/)) {
      osType = 2;
    }

    return osType;
}()),

//链接
  urls = {
      //banner
      'banner': host + '/H5/square/banner',

      //今日精彩
      'special': host + '/H5/square/special',

      //热门频道列表
      'channelHot': host + '/H5/channel/hot',

      //视频列表
      'list': (function() {
          var url = host + '/H5/square/query';

          if(from === 'app') {
              if(version >= 2007000) {
                  url += '?preFlag=true';
              }
          } else {
              url += '?preFlag=true';
          }

          return url;
      }()),

      //直播列表
      'demoCameraList': (function() {
          var liveUrl = host + '/square/demoCameraApplyAction!queryDemoCameraList.action';

          switch(from) {
              case 'app':
                  liveUrl += '?type=200&serverType=60';
                  break;

              case 'alipay':
                  liveUrl += '?type=200';
                  break;

              case 'weixin':
                  liveUrl += '?type=199';
                  break;

              default:
                  liveUrl += '?type=200';
          }

          return liveUrl;
      }()),

      //增加直播视频播放数
      'addViewedCount': host + '/H5/square/view/add',

      //点播列表
      'playlists': host + '/vod/square/playlists',

      //直播播放页
      'livePlayer': 'play?squareid=',

      //点播播放页
      'vodPlayer': 'video?videoId='
  },

//固定命名
names = {
  //直播历史存储名称
  'storageLive': 'ys7_square_live_' + from,

  //搜索历史存储名称
  'storageSearch': 'ys7_square_search_' + from
},

//native接口文件兼容性判断
appBridge = (function() {
  var o = {};

  o['support'] = false,
      o['supportOpenPage'] = false;

  if(window.YsAppBridge && from === 'app' && version >= 2004000) {
      o['support'] = true;
      o['supportOpenPage'] = true;
  }

  if(osType === 2 && version <= 2004001) {
      o['supportOpenPage'] = false;
  }

  return o;
}()),

//app直播服务信息
liveServerInfo = (function() {
  var
      obj,
      ajaxData = {};

  // ajaxData['url'] = urls.demoCameraList;
  // ajaxData['async'] = false;
  // ajaxData['success'] = function(data) {
  //   var data = $YS.getJSONPData(data);
  //   data.serverInfo && (obj = data.serverInfo);
  // };

  // $.ajax(ajaxData);
  return obj;
}()),

//videoItem模板
tplFun_videoItem = (function() {
  var htmlStr = '';

  htmlStr += '<dl id="dl<%=id%>" class="videoItem" data-info="<%=info%>">';
  htmlStr += '<dt>';
  htmlStr += '<span class="<%=markClass%>"><%=markStr%></span>';
  htmlStr += '<img id="<%=imgId%>" class="videoImg" src="<%=videoCoverUrl%>">';
  htmlStr += '</dt>';

  htmlStr += '<dd class="dd0">';

  htmlStr += '<div class="viewed">';
  htmlStr += '<img src="' + staticPath + '/base/imgs/mobile/1_3_7_sicon_eye.png"/>';
  htmlStr += '<%=statPlay%>';
  htmlStr += '</div>';

  htmlStr += '<div class="likeCount">';
  htmlStr += '<img src="' + staticPath + '/base/imgs/mobile/1_3_7_sicon_hand.png"/>';
  htmlStr += '<%=statFavour%>';
  htmlStr += '</div>';

  htmlStr += '</dd>';

  htmlStr += '<dd class="dd1"><%=title%></dd>';
  htmlStr += '</dl>';

  return _.template(htmlStr);
}()),

//获取历史记录
  getHistory = function(key) {
      return JSON.parse(decodeURIComponent($YS.storage.get(key))) || [];
  },

//设置历史记录
  setHistory = function(obj, key, maxLength) {
      var historyObj = JSON.parse(decodeURIComponent($YS.storage.get(key))) || [];

      for(var i = 0; i < historyObj.length; i++) {
          if(historyObj[i].id) {
              if(historyObj[i].id == obj.id) {
                  historyObj.splice(i, 1);
              }
          } else {
              if(JSON.stringify(historyObj[i]) == JSON.stringify(obj)) {
                  historyObj.splice(i, 1);
              }
          }
      }

      if(historyObj.length >= maxLength) {
          historyObj.pop();
      }

      historyObj.unshift(obj);

      $YS.storage.set(key, encodeURIComponent(JSON.stringify(historyObj)));
  },

//获取直播信息
  getVideoData = function(data) {
      var obj = {
          'id': data.id || data.videoId,
          'title': $YS.encodeString((data.cameraName || data.title)),
          'statPlay': $YS.getCountData(data.viewedCount || data.statPlay),
          'statFavour': data.likeCount || data.statfavour,
          'uploadTime': $YS.getGapTime((data.gmtCreate || data.uploadTime)),
          'ownerAvatar': 'http://' + yundomain + (data.avatarPath || data.ownerAvatar)
      };

      if(data.specialVideoType == 1) {
          if(!!data.videoCoverUrl) {
              obj['videoCoverUrl'] = data.videoCoverUrl + "@400w_225h";
          } else {
              obj['videoCoverUrl'] = 'http://' + yundomain + data.md5Serial + '_web.jpeg';
          }
          obj['urlStr'] = getLiveUrl(data);
          obj['videoType'] = 2;
      } else if(data.specialVideoType == 0) {
          obj['videoCoverUrl'] = data.videoCoverUrl + "@240w_135h_80Q";
          obj['urlStr'] = urls.vodPlayer + obj['id'] + (from !== '' ? ('&f=' + from) : '');
          obj['videoType'] = 1;
      }

      return obj;
  },

//获取直播或点播信息（新版）
  getVideoInfo = function(data, currentTime) {
      var
          currentTime = currentTime || 0,
          obj = {
              'id': data.videoId,
              'videoType': data.videoType,
              'title': $YS.encodeString(data.title),
              'statPlay': $YS.getCountData(data.statPlay),
              'statFavour': data.statFavour,
              'uploadTime': $YS.getGapTime(data.uploadTime),
              'ownerAvatar': 'http://' + yundomain + data.ownerAvatar,
              'urlStr': getVideoUrl(data, currentTime),
              'loadTime': new Date().getTime(),
              'prevue': data.prevue,
              'pStartTime': data.startDate,
              'pRemainTime': data.startDate - currentTime,
              'pOverTime': data.pOverTime
          };

      if(obj['videoType'] == 2) {
          if(!!data.videoCoverUrl) {
              obj['videoCoverUrl'] = data.videoCoverUrl + "@400w_225h";
          } else {
              obj['videoCoverUrl'] = 'http://' + yundomain + data.md5Serial + '_web.jpeg';
          }
      } else if(obj['videoType'] == 1) {
          obj['videoCoverUrl'] = data.videoCoverUrl + "@240w_135h_80Q";
      }

      return obj;
  },

//获取直播链接
  getLiveUrl = function(data) {
      var
          urlStr = '',
          getWebUrl = function() {
              urlStr += urls.livePlayer + data.id;
              urlStr += from !== '' ? ('&f=' + from) : '';
          };

      if(data.channel == 9) {
          //运动专区
          getWebUrl();
      } else if(from === 'app') {
          //app中打开
          var
              paramData = {},
              rtsp = '';

          rtsp += 'rtsp://'
          rtsp += liveServerInfo.externalIp + ':';
          rtsp += liveServerInfo.tcpPort + '/';
          rtsp += 'demo://'
          rtsp += data.subSerail + ':';
          rtsp += data.channelNo + ':';
          rtsp += data.streamType + ':';
          rtsp += '1:0:';
          rtsp += data.casIp + ':';
          //rtsp += data.casPort;
          rtsp += '0'

          urlStr += 'shipin7://shipingc?' + rtsp;

          if(version >= 2001000) {
              paramData['subserial'] = data.subSerail;
              paramData['channelno'] = data.channelNo;
              paramData['squareid'] = data.id;
              paramData['soundtype'] = data.isOpenSound;
              paramData['cameraname'] = data.cameraName;
              urlStr += '&' + $.param(paramData) + '&md5Serial=' + data.md5Serial;
          }

      } else {
          //其他入口
          getWebUrl();
      }

      return urlStr;
  },

//获取播放链接（新版）
  getVideoUrl = function(data, currentTime) {
      var urlStr = '';

      if(data.videoType == 1) {
          urlStr += urls.vodPlayer + data.videoId;
          urlStr += from !== '' ? ('&f=' + from) : '';
      } else if(data.videoType == 2) {
          if(from === 'app') {
              //app中打开
              var
                  paramData = {},
                  rtsp = data.videoFileUrl;
              urlStr += 'shipin7://shipingc?' + rtsp;

              if(version >= 2001000) {
                  paramData['subserial'] = data.deviceSubSerail;
                  paramData['channelno'] = data.deviceChannelNo;
                  paramData['squareid'] = data.videoId;
                  paramData['soundtype'] = data.deviceIsOpenSound;
                  paramData['cameraname'] = data.title;
                  urlStr += '&' + $.param(paramData) + '&md5Serial=' + data.md5Serial;
              }

              if(version >= 2007000) {
                  paramData['videoCoverUrl'] = data.videoCoverUrl || '';
                  paramData['prevue'] = data.prevue;
                  paramData['pStartTime'] = data.startDate;
                  paramData['pRemainTime'] = /* data.pRemainTime */ data.startDate - currentTime;
                  urlStr += '&' + $.param(paramData);
              }

          } else {
              //其他入口
              urlStr += urls.livePlayer + data.videoId;
              urlStr += from !== '' ? ('&f=' + from) : '';
          }
      }

      return urlStr;
  },

// //获取直播列表
// getLiveList = function(render, container, callbacks) {
//   var
//   that = this,
//   callbacks = callbacks || {},
//   paramData = {},
//   pageStart = 0,
//   pageSize = 16,
//   nowChannel = null,
//   lastVideoId = 0,
//   getData = function() {
//     var
//     urlStr = '',
//     ajaxChannel = paramData['channel'],
//     encodeData = function(data) {

//       //非当前频道的数据抛弃
//       if(ajaxChannel && ajaxChannel !== nowChannel) {
//         //console.log('数据失效：请求频道[' + ajaxChannel + '],当前频道[' + nowChannel + ']');
//         return;
//       }

//       var
//       data = data.demoCameras,
//       dataLength = data.length,
//       obj = {};

//       //数据重复抛弃
//       if(data[0] && data[0].id === lastVideoId && $(container).html() !== '') {
//         //console.log('数据重复：id[' + data[0].id + ']')
//         return;
//       }

//       for(var i = 0; i < dataLength; i++) {
//         obj['id'] = data[i].id;
//         obj['cameraName'] = $YS.cutString(data[i].cameraName, 12);
//         obj['showCount'] = $YS.getCountData(data[i].viewedCount);

//         if(!!data[i].vodCoverUrl) {
//           obj['imgStr'] = data[i].vodCoverUrl + "@400w_225h";
//         } else {
//           obj['imgStr'] = 'http://' + yundomain + data[i].md5Serial + '_web.jpeg';
//         }

//         obj['urlStr'] = getLiveUrl(data[i]);
//         obj['channel'] = data[i].channel;
//         obj['info'] = encodeURIComponent(JSON.stringify(obj));
//         obj['address'] = $YS.encodeString(data[i].address);

//         render(container, obj);
//       }

//       //保存最后一条数据id
//       if(data[0]) {
//         lastVideoId = data[0].id ? data[0].id : 0;
//       }

//       that.loading = false;
//       $('#loading').hide();

//       //当前频道无内容
//       if(dataLength === 0 && pageStart - 1 === 0) {
//         that.loadAll = true;
//         callbacks.showNoVideo && callbacks.showNoVideo(nowChannel);
//         //console.log('当前频道无内容[' + nowChannel + ']');
//         return;
//       }

//       //全部加载完成
//       if(dataLength < pageSize) {
//         that.loadAll = true;
//         callbacks.showLoadAll && callbacks.showLoadAll();
//         //console.log('加载完成');
//         return;
//       }

//       //频道为最新时最多只加载3页
//       if(nowChannel === '-2' && pageStart >= 3) {
//         that.loadAll = true;
//         callbacks.showLoadAll && callbacks.showLoadAll();
//         //console.log('加载完成');
//         return;
//       }
//     };

//     paramData['pageStart'] = pageStart;
//     paramData['pageSize'] = pageSize;
//     urlStr = urls.demoCameraList + '&' + $.param(paramData);
//     $.getJSON(urlStr + '&jsoncallback=?', encodeData)
//     pageStart++;
//     paramData = {};
//   },
//   renderHistory = function() {
//     var
//     data = getHistory(names.storageLive),
//     dataLength = data.length;

//     for(var i = 0; i < dataLength; i++) {
//       var obj = data[i];
//       obj['info'] = encodeURIComponent(JSON.stringify(obj));
//       render(container, obj, 'no');
//     }

//     //保存最后一条数据id
//     if(data[0]) {
//       lastVideoId = data[0].id ? data[0].id : 0;
//     }

//     that.loading = false;
//     $('#loading').hide();

//     if(dataLength === 0) {
//       that.loadAll = true;
//       callbacks.showNoVideo && callbacks.showNoVideo(nowChannel);
//       //console.log('当前频道无内容[' + nowChannel + ']');
//       return;
//     } else {
//       that.loadAll = true;
//       callbacks.showLoadAll && callbacks.showLoadAll();
//       //console.log('加载完成');
//       return;
//     }
//   };

//   this.reset = function() {
//     this.loading = false;
//     this.loadAll = false;
//     paramData = {};
//     pageStart = 0;

//     $(container).html('');
//     $('#loading').hide();
//     callbacks.hideNoVideo && callbacks.hideNoVideo();
//     callbacks.hideLoadAll && callbacks.hideLoadAll();
//   }

//   this.load = function(channel) {
//     this.loading = true;
//     $('#loading').show();

//     if(isNaN(parseInt(channel))) {
//       switch(channel) {
//         case 'new':
//         paramData['channel'] = nowChannel = '-2';
//         paramData['sortType'] = '2';
//         getData();
//         break;

//         case 'history':
//         paramData['channel'] = nowChannel = channel;
//         renderHistory();
//         break;
//       }

//     } else {
//       paramData['channel'] = nowChannel = channel;
//       getData();
//     }
//   }

//   this.search = function(key) {
//     this.loading = true;
//     $('#loading').show();

//     paramData['cameraName'] = encodeURIComponent(key);
//     paramData['squareClientType'] = 2;
//     getData();
//   }

//   this.reset();

//   return this;
// },

//设置滚动加载
  setScrollLoad = function(callBack) {

      var
          windowH = $(window).height(),
          bindDocument = function() {
              var
                  scrollTop = $('body').scrollTop(),
                  totalHeight = Math.floor($(window).height() + $('body').scrollTop()),
                  triggerHeight = Math.floor($(document).height() - 200);

              if(triggerHeight <= totalHeight) {
                  callBack();
              }

              try {
                  //跳转顶部按钮
                  if(scrollTop >= windowH) {
                      $('#toTopBtn').show();
                  } else {
                      $('#toTopBtn').hide();
                  }
              } catch(e) {}
          },
          bindBtn = function(e) {
              document.body.scrollTop = 0;
              e.stopPropagation();
              e.preventDefault();
          };

      $(document).on('scroll touchmove', bindDocument);
      try {
          $('#toTopBtn').on('touchstart', bindBtn);
      } catch(e) {}
  },

//获取坐标
  getCurrentPosition = function(callback) {
      var
          obj = {},
          getByWeb = function() {
              var
                  isReturn = false,
                  webSuccess = function(position) {
                      var
                          lat = position.coords.latitude,
                          lon = position.coords.longitude;

                      if(isReturn) {
                          return;
                      }

                      obj['success'] = true;
                      obj['lat'] = lat;
                      obj['lon'] = lon;
                      obj['dataType'] = 'w';
                      callback(obj);
                      isReturn = true;
                  },
                  webError = function(e) {
                      if(isReturn) {
                          return;
                      }

                      obj['success'] = false;
                      obj['error'] = e;
                      callback(obj);
                      isReturn = true;
                  };

              if(navigator.geolocation) {
                  var timeOut = function() {

                      if(isReturn) {
                          return;
                      }

                      obj['success'] = false;
                      obj['error'] = '定位请求未响应';
                      callback(obj);

                      isReturn = true;
                  };

                  navigator.geolocation.getCurrentPosition(webSuccess, webError);
                  setTimeout(timeOut, 30000);
              } else {
                  obj['success'] = false;
                  obj['error'] = '不支持web定位';
                  callback(obj);
                  isReturn = true;
              }

          };

      if(appBridge.support) {
          var appCallback = function(obj) {
              var obj = JSON.parse(obj);
              if(obj.data.lat) {
                  obj['success'] = true;
                  obj['lat'] = obj.data.lat;
                  obj['lon'] = obj.data.lon;
                  obj['dataType'] = 'n';
                  callback(obj);
              } else {
                  getByWeb();
              }
          };
          window.YsAppBridge.getLocation(appCallback);
      } else {
          getByWeb();
      }
  },

//获取地址
  getAddress = function(callback) {
      var fun = function(obj) {
          if(obj.success) {
              var
                  urlStr = 'http://api.map.baidu.com/geocoder/v2/?ak=BVZmVCyHzPusTYduzI5i69GY&location=' + obj.lat + ',' + obj.lon + '&output=json&pois=0',
                  ajaxCallback = function(data) {
                      var data = data || {};
                      data['success'] = true;
                      data['dataType'] = obj.dataType;
                      callback(data);
                  };

              $.getJSON(urlStr + '&callback=?', ajaxCallback);
          } else {
              callback(obj);
          }
      }
      getCurrentPosition(fun)
  },

//获取原生播放器数据
  getNativePlayerData = function(str) {
      var
          obj = {},
          tempObj = {},
          array0 = str.split('?'),
          array1 = array0[1].split('&');

      obj['rtsp'] = array1[0];

      array1.shift();
      $.each(array1, function(index, item) {
          var a = item.split('=');
          tempObj[a[0]] = a[1];
      });

      obj["videoType"] = "live";
      obj["subSerial"] = tempObj['subserial'];
      obj["channelNo"] = tempObj['channelno'];
      obj["squareId"] = tempObj['squareid'];
      obj["soundType"] = tempObj['soundtype'];
      obj["cameraName"] = decodeURI(tempObj['cameraname']);
      obj["md5Serial"] = tempObj['md5Serial'];

      return obj;
  },

//打开视频
  openVideo = function(data) {
      var
          dataObj = null;

      if($.isPlainObject(data)) {
          dataObj = data;
      } else {
          dataObj = JSON.parse(decodeURIComponent(data));
      }

      //增加点击数
      if(dataObj.urlStr.indexOf("shipin7://shipingc?") !== -1 && version < 3000000) {
          $.ajax({
              'url': urls.addViewedCount,
              'type': 'POST',
              'data': {'id': dataObj.id}
          });
          //$.getJSON(urls.addViewedCount + '?id=' + dataObj.id + '&r=' + Math.random());
      }

      //设置历史记录
      setHistory(dataObj, names.storageLive, 6);

      if(appBridge.support) {
          if(dataObj.channel == 9 || dataObj.channel == 'vod') {
              if(appBridge.supportOpenPage) {
                  var obj = {};
                  obj['url'] = 'http://' + window.location.host + dataObj.urlStr;
                  obj['type'] = '1';
                  window.YsAppBridge.openPage(obj)
              } else {
                  window.location.href = dataObj.urlStr;
              }
          } else {
              var
                  obj = {},
                  tempObj = {},
                  array0 = dataObj.urlStr.split('?'),
                  array1 = array0[1].split('&'),
                  getObj = function(index, item) {
                      var a = item.split('=');
                      tempObj[a[0]] = a[1];
                  };

              obj['rtsp'] = array1[0];
              array1.shift();
              $.each(array1, getObj);
              obj["videoType"] = "live";
              obj["subSerial"] = tempObj['subserial'];
              obj["channelNo"] = tempObj['channelno'];
              obj["squareId"] = tempObj['squareid'];
              obj["soundType"] = tempObj['soundtype'];
              obj["cameraName"] = decodeURI(tempObj['cameraname']);
              obj["md5Serial"] = tempObj['md5Serial'];

              window.YsAppBridge.openVideo(obj);
          }
      } else {
          window.location.href = dataObj.urlStr;
      }
  },

//打开播放器(新版打开视频)
  openPlayer = function(data) {
      var dataObj;

      if($.isPlainObject(data)) {
          dataObj = data;
      } else {
          //转换失败直接跳出
          try{
              if(data.indexOf('{') == -1) {
                  dataObj = JSON.parse(decodeURIComponent(data));
              } else {
                  dataObj = JSON.parse(data);
              }
          } catch(e) {
              console.log('数据错误');
              return;
          }
      }

      var
          gapTime = new Date().getTime() - (dataObj.loadTime * 1),
          remainTime = dataObj.pRemainTime - gapTime;

      //增加点击数
      if(dataObj.urlStr.indexOf("shipin7://shipingc?") !== -1 && version < 3000000) {
          $.ajax({
              'url': urls.addViewedCount,
              'type': 'POST',
              'data': {'id': dataObj.id}
          });
          //$.getJSON(urls.addViewedCount + '?id=' + dataObj.id + '&r=' + Math.random());
      }

      //设置历史记录
      setHistory(dataObj, names.storageLive, 6);

      //如果时间小于0修改预告为直播
      if(remainTime <= 0 && dataObj.prevue == true) {
          try {
              window.chgVideoStatus(dataObj.id);
          } catch(e) {}
      }

      if(appBridge.support) {
          if(dataObj.videoType == 1) {
              //点播
              if(appBridge.supportOpenPage) {

                  var obj = {},
                      _location = window.location.href,
                      _host = window.location.host;

                  if(/^https*:\/\//.test(dataObj.urlStr)){
                      obj['url'] = dataObj.urlStr;
                  }else{
                      obj['url'] = _location.match(/^https*:\/\/.+\//)[0] + dataObj.urlStr;
                  }
                  obj['type'] = "1";

                  window.YsAppBridge.openPage(obj)

              } else {
                  window.location.href = dataObj.urlStr;
              }
          } else if(dataObj.videoType == 2) {
              //直播
              var
                  obj = {},
                  tempObj = {},
                  array0 = dataObj.urlStr.split('?'),
                  array1 = array0[1].split('&'),
                  getObj = function(index, item) {
                      var a = item.split('=');
                      tempObj[a[0]] = a[1];
                  };

              obj['rtsp'] = array1[0];
              array1.shift();
              $.each(array1, getObj);
              obj["videoType"] = "live";
              obj["subSerial"] = tempObj['subserial'];
              obj["channelNo"] = tempObj['channelno'];
              obj["squareId"] = tempObj['squareid'];
              obj["soundType"] = tempObj['soundtype'];
              obj["cameraName"] = decodeURI(tempObj['cameraname']);
              obj["md5Serial"] = tempObj['md5Serial'];

              if(tempObj["videoCoverUrl"] || tempObj["videoCoverUrl"] == "") {
                  obj["videoCoverUrl"] = decodeURIComponent(tempObj["videoCoverUrl"]);
              }

              if(tempObj['prevue'] && tempObj['prevue'] === 'true') {
                  //obj["prevue"] = tempObj['prevue'];

                  obj["prevue"] = (function() {
                      if(remainTime <= 0) {
                          return 'false';
                      } else {
                          return tempObj['prevue'];
                      }
                  }());

                  obj["pStartTime"] = tempObj['pStartTime'];
                  obj["pRemainTime"] = remainTime + '';
              }

              window.YsAppBridge.openVideo(obj);
          }
      } else {
          window.location.href = dataObj.urlStr;
      }
  },

//打开超链接
  openUrl = function(urlStr) {
      if(appBridge.support && appBridge.supportOpenPage) {
          var obj = {},
              _location = window.location.href,
              _host = window.location.host;
          if(/^https*:\/\//.test(urlStr)){
              obj['url'] = urlStr;
          }else{
              obj['url'] = _location.match(/^https*:\/\/.+\//)[0] + urlStr;
          }
          obj['type'] = '1';
          window.YsAppBridge.openPage(obj);
      } else {
          window.location.href = urlStr;
      }
  },

//添加CSS规则
  addCssRule = function(rule) {
      var
          cssRules = document.styleSheets[0].cssRules;
      if(!cssRules) {
          index = 1;
      } else {
          index = cssRules.length;
      }
      document.styleSheets[0].insertRule(rule, index);
  },

//渲染直播列表
  renderLiveList = function(container, data) {
      var data = data || {};
      data.imgId = 'liveImg' + data['id'];

      var info = tplFun_videoItem(data);

      $(container).append(info);

      //视频封面淡入
      $('#' + data.imgId).on('load', function(){
          $(this).css('opacity', '1');
      });
  };

$(document).ready(function() {
    //初始化直播与搜索历史记录
    $YS.storage.init([names.storageLive, names.storageSearch], encodeURIComponent('[]'));
});

exports.host = host;
exports.from = from;
exports.version = version;
exports.osType = osType;
exports.urls = urls;
exports.names = names;
exports.appBridge = appBridge;
exports.liveServerInfo = liveServerInfo;
exports.tplFun_videoItem = tplFun_videoItem;
exports.getHistory = getHistory;
exports.setHistory = setHistory;
exports.getVideoData = getVideoData;
exports.getVideoInfo = getVideoInfo;
exports.getLiveUrl = getLiveUrl;
exports.getVideoUrl = getVideoUrl;
//exports.getLiveList = getLiveList;
exports.setScrollLoad = setScrollLoad;
exports.getCurrentPosition = getCurrentPosition;
exports.getAddress = getAddress;
exports.openVideo = openVideo;
exports.openPlayer = openPlayer;
exports.openUrl = openUrl;
exports.addCssRule = addCssRule;
exports.renderLiveList = renderLiveList;
});