package com.renanbandeira.trabalhomovelubiqua;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.renanbandeira.trabalhomovelubiqua.model.Device;
import com.renanbandeira.trabalhomovelubiqua.services.ConnectionManager;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public abstract class ConnectedActivity extends AppCompatActivity {
  protected DatabaseReference mDatabase;
  protected Device connectedDevice;
  protected Device me;
  protected String connectionId;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    me = (Device) getIntent().getExtras().getSerializable("me");
    connectedDevice = (Device) getIntent().getExtras().getSerializable("connectedDevice");
    connectionId = getIntent().getStringExtra("connectionId");
    mDatabase = FirebaseDatabase.getInstance().getReference();
    new ConnectionManager(mDatabase, connectionId, me.id);
    EventBus.getDefault().register(this);
  }

  protected void disconnect() {
    mDatabase.child("connections").child(connectionId).removeValue();
  }

  @Subscribe
  public void onDisconnected(ConnectionManager.ConnectionEvent event) {
    finish();
  }

  @Override protected void onDestroy() {
    EventBus.getDefault().unregister(this);
    super.onDestroy();
  }
}
