var express = require('express');
var router = express.Router();
var socketApi = require('../socketApi')
var io = socketApi.io;
/* GET home page. */
router.get('/', function(req, res, next) {
  res.render('index');
});

router.post('/',function(req,res,next){
	io.emit('invoke','');
	res.send('done');
});
module.exports = router;
