import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:huaweilocation/huaweilocation.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _message = 'Unknown';
  Huaweilocation _huaweilocation;

  @override
  void initState() {
    super.initState();
    initHuaweiLocation();
  }

  void initHuaweiLocation() async {

    _huaweilocation = new Huaweilocation();
    _huaweilocation.updateLocation().listen((huaweilatlng) {
      setState(() {
        switch(huaweilatlng.code){
          case Huaweilocation.LOCATION_SUCCESS:
            _message = "Latitud: " + huaweilatlng.latitude.toString() + " Longitude" + huaweilatlng.longitude.toString();
            break;

          case Huaweilocation.LOCATION_ERROR:
            _message = huaweilatlng.message;
            break;

          case Huaweilocation.LOCATION_REQUEST_UPDATE_SUCCESS:
            _message = huaweilatlng.message;
            break;

          case Huaweilocation.LOCATION_REQUEST_UPDATE_ERROR:
            _message = huaweilatlng.message;
            break;

          case Huaweilocation.LOCATION_REMOVE_UPDATE:
            _message = huaweilatlng.message;
            break;

          case Huaweilocation.LOCATION_REMOVE_UPDATE_ERROR:
            _message = huaweilatlng.message;
            break;
            
        }
      });
    });

    _huaweilocation.requestLocationUpdates();
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: Text('$_message\n'),
        ),
      ),
    );
  }
}
