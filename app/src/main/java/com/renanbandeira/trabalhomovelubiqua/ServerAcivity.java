package com.renanbandeira.trabalhomovelubiqua;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.renanbandeira.trabalhomovelubiqua.firebase.Postman;
import com.renanbandeira.trabalhomovelubiqua.model.Device;
import java.util.ArrayList;

import static com.renanbandeira.trabalhomovelubiqua.firebase.Postman.Command.ACTIVITY;
import static com.renanbandeira.trabalhomovelubiqua.firebase.Postman.Command.BATTERY;
import static com.renanbandeira.trabalhomovelubiqua.firebase.Postman.Command.DISCONNECT;
import static com.renanbandeira.trabalhomovelubiqua.firebase.Postman.Command.LOCATION;
import static com.renanbandeira.trabalhomovelubiqua.firebase.Postman.Command.WIFI;

public class ServerAcivity extends AppCompatActivity implements ValueEventListener {

  private DatabaseReference mDatabase;
  Device connectedDevice;
  Device me;
  String connectionId;
  ArrayAdapter<String> adapter;
  ListView mLogList;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_server);
    me = (Device) getIntent().getExtras().getSerializable("me");
    connectedDevice = (Device) getIntent().getExtras().getSerializable("connectedDevice");
    connectionId = getIntent().getStringExtra("connectionId");

    mLogList = (ListView) findViewById(R.id.log);
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    setTitle("Conectado com: " + connectedDevice.name);

    adapter = new ArrayAdapter<>(this,
        android.R.layout.simple_list_item_1, android.R.id.text1, new ArrayList<String>());
    mLogList.setAdapter(adapter);

    findViewById(R.id.disconnect).setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        disconnect();
      }
    });

    mDatabase = FirebaseDatabase.getInstance().getReference();
    mDatabase.child("connections").child(connectionId).child("command").addValueEventListener(this);
  }

  @Override public void onDataChange(DataSnapshot dataSnapshot) {
    if (!dataSnapshot.exists()) return;
    String command = dataSnapshot.getValue().toString();
    adapter.add("Pedido por: " + command);

    if (command.equals(String.valueOf(DISCONNECT))) {
      disconnect();
      return;
    }
    if (command.equals(String.valueOf(ACTIVITY))) {
      Postman.sendCommandResponse(connectionId, "Correndo");
      adapter.add("Respondido Correndo");
    } else if (command.equals(String.valueOf(BATTERY))) {
      Postman.sendCommandResponse(connectionId, "Bateria boa");
      adapter.add("Respondido Bateria boa");
    } else if (command.equals(String.valueOf(LOCATION))) {
      Postman.sendCommandResponse(connectionId, "EM Casa");
      adapter.add("Respondido Em Casa");
    } else if (command.equals(String.valueOf(WIFI))) {
      Postman.sendCommandResponse(connectionId, "Conectado demais");
      adapter.add("Respondido Conectado demais");
    }
  }

  private void disconnect() {
    Postman.sendCommandDisconnectResponse(connectionId);
    Postman.disconnect(me.id);
    finish();
  }

  @Override public void onCancelled(DatabaseError databaseError) {
    Log.e("Error", databaseError.getMessage());
  }

  @Override protected void onDestroy() {
    mDatabase.child("connections").child(connectionId).child("command").removeEventListener(this);
    super.onDestroy();
  }
}
