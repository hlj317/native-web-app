/**
 * Created by yuanjianping on 2015/4/8.
 */
importScripts('underscore.js');
var
    scaleOfCluster = 0.15,//聚合临界比例
//coordsClustered = []//聚合点数组
    coordsDetail = {},//视频点详细信息
    wholeCoords = [],//所有视频点
    zoomBeenOverlay = {},//已经被渲染的区域
    deltaZoomLon,//经度方向的步长
    deltaZoomLat,//纬度方向的步长
    limitLon,//当前缩放级别下的聚合临界长度
    limitLat,
    clone = function(obj){
        var o = obj.constructor === Array ? [] : {};
        for(var i in obj){
            if(obj.hasOwnProperty(i)){
                o[i] = typeof obj[i] === "object" ? clone(obj[i]) : obj[i];
            }
        }
        return o;
    },
    changeWholeCoords = function(t){
        wholeCoords = wholeCoords[type];
    },
//扩张聚合区域,并返回新聚合的点
    expandClusterZoom = function(nowArea){
        var area,inViewCoords,resultCoords=[];
        while(nowArea.eLon>zoomBeenOverlay.eLon){
            zoomBeenOverlay.eLon += deltaZoomLon;
            area = {
                eLon : zoomBeenOverlay.eLon,
                wLon : zoomBeenOverlay.eLon-deltaZoomLon,
                nLat : zoomBeenOverlay.nLat,
                sLat : zoomBeenOverlay.sLat
            };

            inViewCoords = findCoords(area, wholeCoords);
            resultCoords = resultCoords.concat(clusterCoords(clone(inViewCoords)));
        }
        while(nowArea.wLon<zoomBeenOverlay.wLon){
            zoomBeenOverlay.wLon -= deltaZoomLon;
            area = {
                eLon : zoomBeenOverlay.wLon+deltaZoomLon,
                wLon : zoomBeenOverlay.wLon,
                nLat : zoomBeenOverlay.nLat,
                sLat : zoomBeenOverlay.sLat
            };

            inViewCoords = findCoords(area, wholeCoords);
            resultCoords = resultCoords.concat(clusterCoords(clone(inViewCoords)));
        }
        while(nowArea.nLat>zoomBeenOverlay.nLat){
            zoomBeenOverlay.nLat += deltaZoomLat;
            area = {
                eLon : zoomBeenOverlay.eLon,
                wLon : zoomBeenOverlay.wLon,
                nLat : zoomBeenOverlay.nLat,
                sLat : zoomBeenOverlay.nLat-deltaZoomLat
            };

            inViewCoords = findCoords(area, wholeCoords);
            resultCoords = resultCoords.concat(clusterCoords(clone(inViewCoords)));
        }
        while(nowArea.sLat<zoomBeenOverlay.sLat){
            zoomBeenOverlay.sLat -= deltaZoomLat;
            area = {
                eLon : zoomBeenOverlay.eLon,
                wLon : zoomBeenOverlay.wLon,
                nLat : zoomBeenOverlay.sLat+deltaZoomLat,
                sLat : zoomBeenOverlay.sLat
            };

            inViewCoords = findCoords(area, wholeCoords);
            resultCoords = resultCoords.concat(clusterCoords(clone(inViewCoords)));
        }
        return resultCoords;
    },

//找出区域内的坐标点
    findCoords = function (area, coords) {
        return _.filter(coords, function (o) {
            return o.lon >= area.wLon && o.lon <= area.eLon && o.lat >= area.sLat && o.lat <= area.nLat;
        });
    },

//聚合区域中的坐标
    clusterCoords = function (d) {
        var i, j,
            coords = d;
        for(i=0;i<coords.length-1;i++){
            for(j=1;j<coords.length;j++){
                //合并相近的点
                if((Math.abs(coords[0].lon-coords[j].lon) <= limitLon)
                    && (Math.abs(coords[0].lat-coords[j].lat) <= limitLat)){
                    var len1 = coords[0].ids.length;
                    var len2 = coords[j].ids.length;
                    coords[0].lon = (len1*coords[0].lon+len2*coords[j].lon)/(len1+len2);
                    coords[0].lat = (len1*coords[0].lat+len2*coords[j].lat)/(len1+len2);
                    coords[0].ids = coords[0].ids.concat(coords[j].ids);
                    coords.splice(j,1);
                    j--;
                }
            }
            //第一个聚合完成后和放到末尾
            var temp = coords[0];
            coords.splice(0,1);
            coords.push(temp);
        }

        //插入属性
        for(i=0;i<coords.length;i++){
            var num = coords[i].ids.length;
            coords[i].num = num;
            //if(num === 1){
            coords[i].detail = coordsDetail[coords[i].ids[0]];
            //}
        }
        return coords;
    };

self.onmessage = function(e){
    var i, list, vArea,
        _data = e.data;
    if(_data.method === 'pushClusterCoords'){//插入块级区域坐标点集合
        list = _data.data;
        wholeCoords = [];
        for(i=0;i< list.length;i++){
            wholeCoords.push({
                ids : [list[i].videoId],
                lon : list[i].longitude*1,
                lat : list[i].latitude*1
            });
            coordsDetail[list[i].videoId] = list[i];
        }
        self.postMessage({
            method : 'pushClusterCoords'
        })

    }else if(_data.method === 'firstClusterCoords'){//聚合当前视窗及附近区域坐标点
        vArea = _data.data.area;
        zoomBeenOverlay = vArea;
        deltaZoomLon = vArea.eLon - vArea.wLon;
        deltaZoomLat = vArea.nLat - vArea.sLat;
        limitLon = scaleOfCluster*deltaZoomLon;
        limitLat = scaleOfCluster*deltaZoomLat;
        var inViewCoords = findCoords(vArea, wholeCoords);

        var resultCoords = clusterCoords(clone(inViewCoords));
        self.postMessage({
            method : 'firstClusterCoords',
            data : resultCoords
        });
    }else if(_data.method === 'moveClusterCoords'){//聚合当前视窗及附近区域坐标点
        vArea = _data.data.area;
        self.postMessage({
            method : 'moveClusterCoords',
            data : expandClusterZoom(vArea)
        });
    }else if(_data.method === 'changeTypeCoords'){
        p = _data.data;
        changeWholeCoords(p);
    }
};
