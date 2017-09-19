# Image Resizer for Cordova

### Usage
    var ImgResizer = (function() {
      var resizingWidth = 800,
          resizingHeight = 600,
          quality = 90;

      function resize(imageURI, folderName, fileName) {
        var deferred = $.Deferred(),
            options = {
              uri: imageURI,
              folderName: folderName,
              fileName: fileName,
              quality: quality,
              width: resizingWidth,
              height: resizingHeight
            };

        window.ImgResizer.resize(options,
          function(image) {
            deferred.resolve(image);
          }, function() {
            alert('Errors in resizing!');
            deferred.reject();
        });

        return deferred.promise();
      }

      return {
        resize: resize
      }
    }());