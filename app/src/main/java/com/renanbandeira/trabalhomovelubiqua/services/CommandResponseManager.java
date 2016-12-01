package com.renanbandeira.trabalhomovelubiqua.services;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.renanbandeira.trabalhomovelubiqua.R;
import com.renanbandeira.trabalhomovelubiqua.model.SMSData;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import static com.renanbandeira.trabalhomovelubiqua.firebase.Postman.Command.ACTIVITY;
import static com.renanbandeira.trabalhomovelubiqua.firebase.Postman.Command.BATTERY;
import static com.renanbandeira.trabalhomovelubiqua.firebase.Postman.Command.LOCATION;
import static com.renanbandeira.trabalhomovelubiqua.firebase.Postman.Command.PLAY_SOUND;
import static com.renanbandeira.trabalhomovelubiqua.firebase.Postman.Command.SEND_SMS;
import static com.renanbandeira.trabalhomovelubiqua.firebase.Postman.Command.WIFI;

public class CommandResponseManager
    implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
    MediaPlayer.OnCompletionListener {
  private Context context;
  private String lastActivityRecognized = "Desconhecido";
  private String lastLocation = "Desconhecido";
  GoogleApiClient mGoogleApiClient;
  LocationService locationService;

  public CommandResponseManager(Context context) {
    this.context = context;
    locationService = new LocationService(context);
    mGoogleApiClient = new GoogleApiClient.Builder(context).addConnectionCallbacks(this)
        .addOnConnectionFailedListener(this)
        .addApi(LocationServices.API)
        .addApi(ActivityRecognition.API)
        .build();
  }

  public void registerServices() {
    mGoogleApiClient.connect();
    EventBus.getDefault().register(this);
  }

  public void unregisterServices() {
    if (mGoogleApiClient.isConnected()) {
      try {
        mGoogleApiClient.disconnect();
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, locationService);
      } catch (IllegalStateException e) {

      }
    }
    EventBus.getDefault().unregister(this);
  }

  public String getCommandResponse(String command) {
    if (command.equals(String.valueOf(ACTIVITY))) {
      return lastActivityRecognized;
    } else if (command.equals(String.valueOf(BATTERY))) {
      return getBatteryLevel(context);
    } else if (command.equals(String.valueOf(LOCATION))) {
      return lastLocation;
    } else if (command.equals(String.valueOf(WIFI))) {
      return getWifiStats();
    } else if (command.equals(String.valueOf(PLAY_SOUND))) {
      playSound();
      return "Áudio tocado";
    } else if (command.startsWith(String.valueOf(SEND_SMS))) {
      String params = command.replace(String.valueOf(SEND_SMS), "");
      SMSData data = new Gson().fromJson(params, SMSData.class);
      sendSMS(data.getPhone(), data.getMessage());
      return "Enviado SMS";
    }
    return "Comando inválido";
  }

  private String getBatteryLevel(Context context) {
    Intent batteryIntent =
        context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    if (batteryIntent == null) return "Nível desconhecido";
    int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
    int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

    // Error checking that probably isn't needed but I added just in case.
    if (level == -1 || scale == -1) {
      return "Nível de bateria: " + 50.0f;
    }

    return "Nível de bateria: " + ((float) level / (float) scale) * 100.0f;
  }

  private String getWifiStats() {
    ConnectivityManager connManager =
        (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
    if (networkInfo == null || !networkInfo.isConnected()) return "Sem conexão";

    final WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    final WifiInfo connectionInfo = wifiManager.getConnectionInfo();

    return "SSID: "
        + connectionInfo.getSSID()
        + "\nVelocidade: "
        + connectionInfo.getLinkSpeed()
        + "Mbps\nForça do sinal: "
        + WifiManager.calculateSignalLevel(connectionInfo.getRssi(), 100)
        + "%";
  }

  @Subscribe
  public void onActivityRecognized(ActivityRecognitionService.ActivityRecognitionEvent event) {
    lastActivityRecognized = event.message;
  }

  @Subscribe public void onLocationUpdated(LocationService.LocationEvent event) {
    lastLocation = event.message;
  }

  @Override public void onConnected(@Nullable Bundle bundle) {
    Dexter.checkPermission(new PermissionListener() {
      @Override public void onPermissionGranted(PermissionGrantedResponse response) {
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
            locationService.getLocationRequest(), locationService);
      }

      @Override public void onPermissionDenied(PermissionDeniedResponse response) {
        lastLocation = "Não permitido pelo usuário";
      }

      @Override public void onPermissionRationaleShouldBeShown(PermissionRequest permission,
          PermissionToken token) {
        token.continuePermissionRequest();
      }
    }, Manifest.permission.ACCESS_FINE_LOCATION);

    Intent intent = new Intent(context, ActivityRecognitionService.class);
    PendingIntent pendingIntent =
        PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(mGoogleApiClient, 3000,
        pendingIntent);
  }

  @Override public void onConnectionSuspended(int i) {
    Toast.makeText(context, "Erro de conexao com o Play Services", Toast.LENGTH_SHORT).show();
  }

  @Override public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    Log.e("Falha com Play Services", connectionResult.getErrorMessage());
    Toast.makeText(context, "Falha de conexao com o Play Services", Toast.LENGTH_SHORT).show();
  }

  private void playSound(){
    MediaPlayer mediaPlayer = MediaPlayer.create(context, R.raw.audio);
    mediaPlayer.start();
    mediaPlayer.setOnCompletionListener(this);
  }

  @Override public void onCompletion(MediaPlayer mp) {
    mp.release();
  }

  private void sendSMS(final String phoneNo, final String msg) {
    Dexter.checkPermission(new PermissionListener() {
      @Override public void onPermissionGranted(PermissionGrantedResponse response) {
        try {
          SmsManager smsManager = SmsManager.getDefault();
          smsManager.sendTextMessage(phoneNo, null, msg, null, null);
        } catch (Exception ex) {
          ex.printStackTrace();
        }
      }

      @Override public void onPermissionDenied(PermissionDeniedResponse response) {
        Toast.makeText(context, "Permissão para enviar SMS negada!", Toast.LENGTH_LONG).show();
      }

      @Override public void onPermissionRationaleShouldBeShown(PermissionRequest permission,
          PermissionToken token) {
        token.continuePermissionRequest();
      }
    }, Manifest.permission.SEND_SMS);

  }
}
