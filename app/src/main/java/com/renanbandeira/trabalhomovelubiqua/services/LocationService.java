package com.renanbandeira.trabalhomovelubiqua.services;

import android.content.Context;
import android.location.Location;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import org.greenrobot.eventbus.EventBus;

public class LocationService implements LocationListener {
  Context context;
  LocationRequest mLocationRequest;

  public LocationService(Context context) {
    mLocationRequest = new LocationRequest();
    mLocationRequest.setInterval(10000);
    mLocationRequest.setFastestInterval(5000);
    mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
  }

  protected LocationRequest getLocationRequest() {
    return mLocationRequest;
  }

  @Override public void onLocationChanged(Location location) {
    String msg = "Latitude: " + location.getLatitude() + "\n Longitude: " + location.getLongitude();
    EventBus.getDefault().post(new LocationEvent(msg));
  }

  public class LocationEvent {
    public final String message;

    public LocationEvent(String message) {
      this.message = message;
    }
  }
}
