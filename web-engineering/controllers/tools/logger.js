var log4js = require( 'log4js' ),
	config = {

        'appenders':[
            {'type': 'console','category':'console'},
            {
                'type': 'dateFile',
                'filename':'../vod-gulp/logs/',
                'pattern':'yyyyMMdd.log',
                'absolute':true,
                'alwaysIncludePattern':true,
                'category':'logInfo'
            }
        ],
        'levels':{'logInfo':'DEBUG'}

    };

log4js.configure( config );

module.exports = log4js.getLogger('logInfo');
