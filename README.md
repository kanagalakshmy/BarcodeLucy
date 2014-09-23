Cordova Plugin
===============

A Cordova plugin to have a restricted view of camera. It uses zbar for barcode scanning and asynchronously provides the barcode back to javascript.

Calls
------

* startCamera `(x,y,width,height)` 

Starts a camera with above dimensions and tries scanning a barcode. When a barcode is detected, it is provided back to js

* stopCamera 

Stops the camera and removes the preview

* unfreeze

Stops the camera but does not remove the preview. The view is freezed on the screen and it doesn't scan for barcode

