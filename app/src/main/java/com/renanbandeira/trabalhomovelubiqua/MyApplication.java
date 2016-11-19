package com.renanbandeira.trabalhomovelubiqua;

import android.app.Application;
import com.karumi.dexter.Dexter;

public class MyApplication extends Application {

  @Override public void onCreate() {
    super.onCreate();
    Dexter.initialize(getApplicationContext());
  }
}
