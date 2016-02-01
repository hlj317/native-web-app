var http = require('http'),
    httpProxy = require('http-proxy'),
    proxy = httpProxy.createProxyServer({}),
    server = http.createServer(function(req, res) {
        proxy.web(req, res, {target: 'http://127.0.0.1:8080'});
    });

console.log('服务启动！');
server.listen(3004);
