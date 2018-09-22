var socket_io = require('socket.io');
var io = socket_io();
var socketApi = {};

socketApi.io = io;
io.on('connection',function(socket){
	console.log('A user is connected');
	socket.on('change',function(msg){
		io.emit('change',msg);
		if(msg=="come_back") io.emit('invoke',"");
	});
});
module.exports = socketApi;