package com.shadyboshra2012.flutter_install_apk_silently;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.PowerManager;
import android.util.Log;
import androidx.annotation.NonNull;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/**
 * FlutterInstallApkSilentlyPlugin
 */
public class FlutterInstallApkSilentlyPlugin implements FlutterPlugin, MethodCallHandler {
    /// The channel name which it's the bridge between Dart and JAVA
    private static final String CHANNEL_NAME = "shadyboshra2012/flutterinstallapksilently";

    /// Methods name which detect which it called from Flutter.
    private static final String METHOD_INSTALL_APK = "installAPK";
    private static final String METHOD_REBOOT_DEVICE = "rebootDevice";
    private static final String METHOD_UPDATE_TIMEZONE = "updateTimezone";

    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private MethodChannel channel;

    /// Context to hold it for Reboot needs.
    @SuppressLint("StaticFieldLeak")
    private static Context context;

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        channel = new MethodChannel(flutterPluginBinding.getFlutterEngine().getDartExecutor(), CHANNEL_NAME);
        channel.setMethodCallHandler(this);
        context = flutterPluginBinding.getApplicationContext();
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
    public static void registerWith(Registrar registrar) {
        final MethodChannel channel = new MethodChannel(registrar.messenger(), CHANNEL_NAME);
        channel.setMethodCallHandler(new FlutterInstallApkSilentlyPlugin());
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        switch (call.method) {
            case METHOD_INSTALL_APK:
                // Working in background AsyncTask.
                new InstallingTask(result, call).execute();
                break;
            case METHOD_REBOOT_DEVICE:
                try {
                    Process process = Runtime.getRuntime().exec(new String[]{"su", "-c", "am start -a android.intent.action.REBOOT"});
                    process.waitFor();
                    if (process.exitValue() == 0) {
                        result.success(true);
                    } else {
                        String errorDetails = convertStreamToString(process.getErrorStream());
                        result.error("0",  process.exitValue() + " Failed to reboot", errorDetails);
                    }
                    result.success(true);
                } catch (Exception ex) {
                    result.error("0",  ex.getMessage(), ex.getLocalizedMessage());
                }
                break;
            case METHOD_UPDATE_TIMEZONE:
                try {
                    String command = "setprop persist.sys.timezone " + call.argument("tz") + ";";
                    Process process = Runtime.getRuntime().exec(new String[]{"su", "-c", command});
                    process.waitFor();
                    if (process.exitValue() == 0) {
                        result.success(true);
                    } else {
                        String errorDetails = convertStreamToString(process.getErrorStream());
                        result.error("0",  process.exitValue() + " Failed to install", errorDetails);
                    }
                } catch (Exception ex) {
                    result.error("0",  ex.getMessage(), ex.getLocalizedMessage());
                }
                break;
            default:
                result.notImplemented();
                break;
        }
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        channel.setMethodCallHandler(null);
    }

    private String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }
}
