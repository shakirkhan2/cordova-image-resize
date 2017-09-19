var ImgResizer = function() {};

ImgResizer.prototype.resize = function(options, successCallback, errorCallback) {
  cordova.exec(function(uri) {
    successCallback(uri);
  }, 
  function(e) {
    errorCallback(e);
  }, 'ImgResizer', 'resize', [options]);
};

var ImgResizer = new ImgResizer();
module.exports = ImgResizer;