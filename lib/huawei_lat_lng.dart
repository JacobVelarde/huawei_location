
import 'dart:collection';
import 'dart:convert';


HuaweiLatLng huaweiLatitudLongitudFromJson(LinkedHashMap<dynamic, dynamic> map) => HuaweiLatLng.fromJson(map);

String huaweiLatitudLongitudToJson(HuaweiLatLng data) => json.encode(data.toJson());

class HuaweiLatLng {
  HuaweiLatLng({
    this.latitude,
    this.longitude,
    this.locationAvailability,
    this.code,
    this.message
  });

  String latitude;
  String longitude;
  bool locationAvailability;
  String code;
  String message;

  factory HuaweiLatLng.fromJson(LinkedHashMap<dynamic, dynamic> json) => HuaweiLatLng(
    latitude: json["latitude"] as String,
    longitude: json["longitude"] as String,
    locationAvailability: json.containsKey("locationAvailability") ?
    json["locationAvailability"] == "true" ? true : false : false,
    code: json["code"] as String,
    message: json["message"] as String
  );

  Map<String, dynamic> toJson() => {
    "latitude": latitude,
    "longitude": longitude,
    "locationAvailability" : locationAvailability,
    "code": code,
    "message": message
  };
}