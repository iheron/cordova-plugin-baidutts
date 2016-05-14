var exec = require('cordova/exec');
var SERVICE_NAME = 'BaiduTts';
var ACTION_SPEAK = 'speak';
var ACTION_STOP = 'stop';

var baiduTts = function () {
  this.events = {};
};

baiduTts.prototype = {
  init: function (entity) {
    exec(null, null, SERVICE_NAME, ACTION_INIT, [entity]);
  },

  speak: function (options) {
    var text = options.text;
    var volume = options.volume;
    var speed = options.speed;
    var pitch = options.pitch;
    
    var self = this;
    var cb = function (info) {
      if (self.events.hasOwnProperty(info.event)) {
        self.events[info.event]();
      }
    };
    exec(cb, cb, SERVICE_NAME, ACTION_SPEAK, [{text: text, volume: volume, speed: speed, pitch: pitch}]);
  },

  stop: function () {
    exec(null, null, SERVICE_NAME, ACTION_STOP, [null]);
  },
  onStart: function (fn) {
    if (typeof fn === 'function') {
      this.events.onStart = fn;
    }
  },
  onStop: function (fn) {
    if (typeof fn === 'function') {
      this.events.onStop = fn;
    }
  }
};

module.exports = new baiduTts();
