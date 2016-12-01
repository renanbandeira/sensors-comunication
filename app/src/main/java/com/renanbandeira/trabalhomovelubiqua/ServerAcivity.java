package com.renanbandeira.trabalhomovelubiqua;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.renanbandeira.trabalhomovelubiqua.firebase.Postman;
import com.renanbandeira.trabalhomovelubiqua.services.CommandResponseManager;
import java.util.ArrayList;

import static com.renanbandeira.trabalhomovelubiqua.firebase.Postman.Command.SEND_SMS;

public class ServerAcivity extends ConnectedActivity implements ValueEventListener {
  ArrayAdapter<String> adapter;
  ListView mLogList;
  CommandResponseManager commandManager;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_server);

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
    commandManager = new CommandResponseManager(this);
    commandManager.registerServices();
    mDatabase.child("connections").child(connectionId).child("command").addValueEventListener(this);
  }

  @Override protected void onStop() {
    commandManager.unregisterServices();
    super.onStop();
  }

  @Override public void onDataChange(DataSnapshot dataSnapshot) {
    if (!dataSnapshot.exists()) return;
    String command = dataSnapshot.getValue().toString();
    if (command.startsWith(String.valueOf(SEND_SMS))) {
      adapter.add("Pedido por: " + SEND_SMS);
    } else {
      adapter.add("Pedido por: " + command);
    }

    String response = commandManager.getCommandResponse(command);
    Postman.sendCommandResponse(connectionId, response);
    adapter.add(response);
  }

  @Override public void onCancelled(DatabaseError databaseError) {
    Log.e("Error", databaseError.getMessage());
  }

  @Override protected void onDestroy() {
    mDatabase.child("connections").child(connectionId).child("command").removeEventListener(this);
    super.onDestroy();
  }
}
