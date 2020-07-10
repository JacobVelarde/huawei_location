

import 'dart:async';

import 'package:flutter/services.dart';

import 'huawei_lat_lng.dart';

class Huaweilocation {

  static const String LOCATION_SUCCESS = "200";
  static const String LOCATION_ERROR = "500";
  static const String LOCATION_REQUEST_UPDATE_SUCCESS = "201";
  static const String LOCATION_REQUEST_UPDATE_ERROR = "501";
  static const String LOCATION_REMOVE_UPDATE = "202";
  static const String LOCATION_REMOVE_UPDATE_ERROR = "502";

  static const MethodChannel _channelLocation = const MethodChannel('com.jacob.huaweilocation/location');
  //static const String _methodUpdateLocation = "locationResult", _methodLocationAvailability = "locationAvailability";
  StreamController _updateController;

  Huaweilocation(){
    _updateController = StreamController<HuaweiLatLng>();
    _callback();
  }
  
  Stream<HuaweiLatLng> updateLocation(){
    return _updateController.stream;
  }

  _callback(){
    _setMethodCallHandler((call) {
      _updateController.sink.add(huaweiLatitudLongitudFromJson(call.arguments));
    });
  }

  _setMethodCallHandler(Future<dynamic> handler(MethodCall call)){
    _channelLocation.setMethodCallHandler(handler);
  }

  requestLocationUpdates() async{
    try{
      _channelLocation.invokeListMethod("requestLocationUpdates");
    } on PlatformException catch (e) {
      print(e.message);
    }
  }

  removeLocationUpdates(){
    try{
      _channelLocation.invokeListMethod("removeLocationUpdates");
    } on PlatformException catch (e) {
      print(e.message);
    }
  }

  dispose(){
    _updateController.sink.close();
  }

}
