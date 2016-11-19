package com.renanbandeira.trabalhomovelubiqua;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.renanbandeira.trabalhomovelubiqua.firebase.Postman;

public class ClientActivity extends ConnectedActivity
    implements ValueEventListener, AdapterView.OnItemClickListener {

  Postman.Command[] commands = new Postman.Command[] {
      Postman.Command.ACTIVITY, Postman.Command.BATTERY, Postman.Command.LOCATION,
      Postman.Command.WIFI
  };

  ListView mServicesList;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_server);



    mServicesList = (ListView) findViewById(R.id.log);
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    setTitle("Conectado com: " + connectedDevice.name);

    ArrayAdapter<String> adapter =
        new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, android.R.id.text1,
            getResources().getStringArray(R.array.commands));
    mServicesList.setClickable(true);
    mServicesList.setOnItemClickListener(this);
    mServicesList.setAdapter(adapter);

    findViewById(R.id.disconnect).setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        disconnect();
      }
    });

    mDatabase.child("connections")
        .child(connectionId)
        .child("response")
        .addValueEventListener(this);
  }

  @Override public void onDataChange(DataSnapshot dataSnapshot) {
    if (!dataSnapshot.exists()) return;

    if(dataSnapshot.getValue() != null) {
      Toast.makeText(this, "Resultado: " + dataSnapshot.getValue().toString(), Toast.LENGTH_LONG).show();
    }
  }

  @Override public void onCancelled(DatabaseError databaseError) {
    Log.e("Error", databaseError.getMessage());
  }

  @Override protected void onDestroy() {
    mDatabase.child("connections").child(connectionId).child("command").removeEventListener(this);
    super.onDestroy();
  }

  @Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    Postman.sendCommand(connectionId, commands[position]);
  }
}
