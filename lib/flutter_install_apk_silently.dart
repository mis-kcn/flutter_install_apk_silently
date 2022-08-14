import 'dart:async';
import 'dart:io';

import 'package:flutter/services.dart';

class FlutterInstallApkSilently {
  /// The channel name which it's the bridge between Dart and JAVA or SWIFT
  static const String _CHANNEL_NAME =
      "shadyboshra2012/flutterinstallapksilently";

  /// Methods name which detect which it called from Flutter.
  static const String _METHOD_INSTALL_APK = "installAPK";
  static const String _METHOD_REBOOT_DEVICE = "rebootDevice";
  static const String _METHOD_UPDATE_TIMEZONE = "updateTimezone";
  static const String _METHOD_FETCH_NETWORK_STATS = "fetchNetworkStats";

  /// Initialize the channel
  static const MethodChannel _channel = const MethodChannel(_CHANNEL_NAME);

  /// Install APK silently without granting permission.
  /// [file] APK file.
  static Future<bool?> installAPK({required File file}) async {
    if (Platform.isAndroid) {
      try {
        final bool? isInstalled = await _channel.invokeMethod(
            _METHOD_INSTALL_APK, <String, String>{'filePath': file.path});
        return isInstalled;
      } on PlatformException catch (e) {
        throw "Installation Error Occurred: Code: ${e.code}. Message: ${e.message}. Details: ${e.details}";
      }
    } else {
      // Return false if not Android.
      return false;
    }
  }

  /// Reboot device
  static Future<bool?> rebootDevice() async {
    if (Platform.isAndroid) {
      try {
        return await _channel.invokeMethod(_METHOD_REBOOT_DEVICE);
      } on PlatformException catch (e) {
        throw "Reboot Device Error Occurred: Code: ${e.code}. Message: ${e.message}. Details: ${e.details}";
      }
    } else {
      // Return false if not Android.
      return false;
    }
  }

  /// Update Timezone
  static Future<bool?> updateTimezone(String tz) async {
    if (Platform.isAndroid) {
      try {
        return await _channel.invokeMethod(
          _METHOD_UPDATE_TIMEZONE,
          <String, String>{'tz': tz},
        );
      } on PlatformException catch (e) {
        throw "Update Timezone Error Occurred: Code: ${e.code}. Message: ${e.message}. Details: ${e.details}";
      } catch (e) {
        throw "Update Timezone Error Occurred: $e";
      }
    } else {
      // Return false if not Android.
      return false;
    }
  }

  /// Fetch network stats
  static Future<Map<String, dynamic>> fetchNetworkStats() async {
    if (Platform.isAndroid) {
      return Map<String, dynamic>.from(await _channel.invokeMethod(
        _METHOD_FETCH_NETWORK_STATS,
      ));
    } else {
      throw UnimplementedError();
    }
  }
}
