package com.renanbandeira.trabalhomovelubiqua.firebase;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class MyFirebaseInstanceIdService extends FirebaseInstanceIdService {

  @Override public void onTokenRefresh() {
    super.onTokenRefresh();

    //TODO store this
  }

  private void setFirebaseToken(String token) {
    SharedPreferences.Editor
        editor = getApplicationContext().getSharedPreferences("firebase", MODE_PRIVATE).edit();
    editor.putString("token", token);
    editor.apply();
  }

  public static String getFirebaseToken(Context context) {
    SharedPreferences
        prefs = context.getSharedPreferences("firebase", MODE_PRIVATE);
    return prefs.getString("token", "");
  }
}
