var http	= require( 'http' ),
	qs		= require( 'querystring' ),
	domain	= require( 'domain' ),
	crypto	= require( 'crypto' ),
    config = require( '../../config' ),
    request = require('koa-request');

// var Domain = domain.create();

// Domain.on( 'error', function( e ){
// 	logger.error( e );
// });

// function exist( req, res, method ){
// 	if( req.method == method ){
// 		return true;
// 	} else {
// 		res.redirect( '/404.ejs' );
// 		return false;
// 	}
// }

// function getClientIp( req ){
//     return req.headers['x-forwarded-for'] ||
//     req.connection.remoteAddress ||
//     req.socket.remoteAddress ||
//     req.connection.socket.remoteAddress;
// };

// var Cookie = {

// 	get: function( req, name ){
// 		var cookie = qs.parse( req.headers.cookie, ';', '=' );
// 		return name ? cookie[ name ] : cookie;
// 	}

// }

// function createSID( req ){
// 	var sha1 = crypto.createHash( 'sha1' ),
// 		ip = getClientIp( req );
// 	sha1.update( ip );
// 	return sha1.digest( 'hex' );
// }

// function checkSID( req ){
// 	var _cookie = Cookie.get( req, 'JSESSIONID' );
// 	return createSID( req ) == _cookie;
// }

function *redirect( req, res , path, method, httpObj){

    /*请求主体传值方法*/
    var options = {
        method: method || "POST",
        data: (req.method == "GET" ) ? httpObj.query : httpObj.body,
        url : config.serverPath + path
    };

    options.form = options.data;
    options.headers = req.headers;
    var ret = yield request(options);
    var response = ret.body;
    if(/^jsonp\d*\(.+\)$/.test(response)){
        return response;
        //response = response.replace(/^jsonp\d*\((.+)\)$/,function($1,$2){return $2});
    }
    return JSON.parse(response);

    /*请求头传值方法*/
    //var options = {
    //    path: path,
    //    method: method || "POST",
    //    data: (req.method == "GET" ) ? httpObj.query : httpObj.body,
    //    host: _config.routeHost,
    //    port: _config.routePort,
    //    headers: {'User-Agent':'request'}
    //};
    //if( options.method == 'GET' ){
    //    options.path += '?' + qs.stringify( httpObj.query );
    //} else {
    //    req.data = options.data;
    //    options.path += '?' + qs.stringify( req.data );
    //}
    //options.uri = "http://" + _config.routeHost + ":" + _config.routePort + options.path;
    //options.headers = req.headers;
    //var ret = yield request(options);
    //return JSON.parse(ret.body);

}

module.exports = {
    redirect: redirect
}
