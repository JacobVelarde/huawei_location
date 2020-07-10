package com.jacob.huaweilocation

import android.app.Activity
import android.content.Context
import android.content.IntentSender
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.annotation.NonNull;
import com.huawei.hms.common.ApiException
import com.huawei.hms.common.ResolvableApiException
import com.huawei.hms.location.*

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry.Registrar

/** HuaweilocationPlugin */
public class HuaweilocationPlugin: FlutterPlugin, MethodCallHandler, ActivityAware {
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private lateinit var channel : MethodChannel
  private lateinit var context : Context
  private lateinit var activity: Activity

  override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    channel = MethodChannel(flutterPluginBinding.getFlutterEngine().getDartExecutor(), "com.jacob.huaweilocation/location")
    channel.setMethodCallHandler(this);
  }

  // This static function is optional and equivalent to onAttachedToEngine. It supports the old
  // pre-Flutter-1.12 Android projects. You are encouraged to continue supporting
  // plugin registration via this function while apps migrate to use the new Android APIs
  // post-flutter-1.12 via https://flutter.dev/go/android-project-migration.
  //
  // It is encouraged to share logic between onAttachedToEngine and registerWith to keep
  // them functionally equivalent. Only one of onAttachedToEngine or registerWith will be called
  // depending on the user's project. onAttachedToEngine or registerWith must both be defined
  // in the same class.
  companion object {
    @JvmStatic
    fun registerWith(registrar: Registrar) {
      val channel = MethodChannel(registrar.messenger(), "com.jacob.huaweilocation/location")
      channel.setMethodCallHandler(HuaweilocationPlugin())
    }
  }

  private lateinit var fusedLocationProviderClient : FusedLocationProviderClient
  private lateinit var settingsClient: SettingsClient
  private lateinit var locationRequest: LocationRequest
  private lateinit var mLocationCallback: LocationCallback

  override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
    if (call.method == "requestLocationUpdates") {
      initLocation()
      requestLocationUpdates()
    } else if (call.method == "removeLocationUpdates") {
      removeLocationUpdates()
    }else{
      result.notImplemented()
    }
  }

  override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
  }


  fun initLocation(){
    fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity)
    settingsClient = LocationServices.getSettingsClient(activity)

    locationRequest = LocationRequest()
    locationRequest.interval = 10000
    locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

    listenLocation()

  }

  private fun listenLocation(){
    mLocationCallback = object : LocationCallback() {
      override fun onLocationResult(locationResult: LocationResult) {
        if (locationResult != null) {
          val locations: List<Location> = locationResult.locations
          if (!locations.isEmpty()) {
            for (location in locations) {
              val coordenadas = hashMapOf<String, String>()
              coordenadas["latitude"] = location.latitude.toString()
              coordenadas["longitude"] = location.longitude.toString()
              coordenadas["locationAvailability"] = "true"
              coordenadas["code"] = "200"
              coordenadas["message"] = "location available"
              channel.invokeMethod("locationResult", coordenadas)
            }
          }
        }
      }

      override fun onLocationAvailability(locationAvailability: LocationAvailability) {
        if (locationAvailability != null) {
          val flag = locationAvailability.isLocationAvailable
          val coordenadas = hashMapOf<String, String>()
          coordenadas["latitude"] = "0"
          coordenadas["longitude"] = "0"
          coordenadas["locationAvailability"] = flag.toString()
          coordenadas["code"] = "500"
          coordenadas["message"] = "location not available"
          channel.invokeMethod("locationAvailability", coordenadas)
        }
      }
    }
  }

  private fun requestLocationUpdates() {
    val coordenadas = hashMapOf<String, String>()
    coordenadas["latitude"] = "0"
    coordenadas["longitude"] = "0"
    try {
      val builder = LocationSettingsRequest.Builder()
      builder.addLocationRequest(locationRequest)
      val locationSettingsRequest = builder.build()
      // check devices settings before request location updates.
      settingsClient.checkLocationSettings(locationSettingsRequest)
              .addOnSuccessListener {
                //request location updates
                fusedLocationProviderClient.requestLocationUpdates(locationRequest, mLocationCallback, Looper.getMainLooper())
                        .addOnSuccessListener {
                          coordenadas["code"] = "201"
                          coordenadas["message"] = "request location updates success"
                          channel.invokeMethod("requestLocationUpdate", coordenadas)
                        }
                        .addOnFailureListener { e ->
                          print("requestLocationUpdates error")
                          coordenadas["code"] = "501"
                          coordenadas["message"] = e.localizedMessage
                          channel.invokeMethod("requestLocationUpdate", coordenadas)
                        }
              }
              .addOnFailureListener { e ->
                val statusCode = (e as ApiException).statusCode
                when (statusCode) {
                  10803 ->{
                    coordenadas["code"] = "501"
                    coordenadas["message"] = e.message.toString()
                    channel.invokeMethod("requestLocationUpdate", coordenadas)
                  }
                  LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> try {
                    val rae = e as ResolvableApiException
                    rae.startResolutionForResult(activity, 0)
                    coordenadas["code"] = "501"
                    coordenadas["message"] = rae.localizedMessage
                    channel.invokeMethod("requestLocationUpdate", coordenadas)
                  } catch (sie: IntentSender.SendIntentException) {
                    coordenadas["code"] = "501"
                    coordenadas["message"] = sie.localizedMessage
                    channel.invokeMethod("requestLocationUpdate", coordenadas)
                  }
                }
              }
    } catch (e: Exception) {
      print("requestLocationUpdates RESOLUTION_REQUIRED")
      coordenadas["code"] = "501"
      coordenadas["message"] = e.localizedMessage
      channel.invokeMethod("requestLocationUpdate", coordenadas)
    }
  }

  private fun removeLocationUpdates() {
    val coordenadas = hashMapOf<String, String>()
    coordenadas["latitude"] = "0"
    coordenadas["longitude"] = "0"
    try {
      fusedLocationProviderClient.removeLocationUpdates(mLocationCallback)
              .addOnSuccessListener {
                print("removeLocationUpdates success")
                coordenadas["code"] = "202"
                coordenadas["message"] = "success"
                channel.invokeMethod("removeLocationUpdates", coordenadas)
              }
              .addOnFailureListener { e ->
                print("removeLocationUpdates error")
                coordenadas["code"] = "502"
                coordenadas["message"] = e.localizedMessage
                channel.invokeMethod("removeLocationUpdates", coordenadas)
              }
    } catch (e: java.lang.Exception) {
      print("removeLocationUpdates error")
      coordenadas["code"] = "502"
      coordenadas["message"] = e.localizedMessage
      channel.invokeMethod("removeLocationUpdates", coordenadas)
    }
  }

  override fun onDetachedFromActivity() {
    TODO("Not yet implemented")
  }

  override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
    TODO("Not yet implemented")
  }

  override fun onAttachedToActivity(binding: ActivityPluginBinding) {
    activity = binding.activity
  }

  override fun onDetachedFromActivityForConfigChanges() {
    TODO("Not yet implemented")
  }
}
