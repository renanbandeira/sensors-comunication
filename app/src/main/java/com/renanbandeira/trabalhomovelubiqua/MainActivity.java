package com.renanbandeira.trabalhomovelubiqua;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.renanbandeira.trabalhomovelubiqua.firebase.Postman;
import com.renanbandeira.trabalhomovelubiqua.model.Connection;
import com.renanbandeira.trabalhomovelubiqua.model.ConnectionStatus;
import com.renanbandeira.trabalhomovelubiqua.model.Device;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements ValueEventListener {
  private DatabaseReference mDatabase;
  ProgressDialog progressDialog;
  boolean deviceExists = false;
  ConnectionStatus status = ConnectionStatus.IDLE;
  String myId;
  private Device connectedDevice;
  String connectionID;
  private Device me;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    setTitle("Trabalho Final");
    mDatabase = FirebaseDatabase.getInstance().getReference();
    progressDialog = new ProgressDialog(this);
    progressDialog.setTitle("Por favor, aguarde");
    progressDialog.setCancelable(false);

    findViewById(R.id.connect_as_client).setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        connectToServer();
      }
    });

    findViewById(R.id.connect_as_server).setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        beAvailable();
      }
    });
    if (!hasMyID()) {
      myId = UUID.randomUUID().toString();
      storeID();
    } else {
      myId = getMyID();
    }

    mDatabase.child("devices").child(myId).addValueEventListener(this);
  }

  @Override protected void onDestroy() {
    mDatabase.child(myId).removeEventListener(this);
    super.onDestroy();
  }

  public void connectToServer() {
    status = ConnectionStatus.INIT_CLIENT;
    signUp();
    if (deviceExists) {
      initConnectionAsClient();
    }
  }

  private void signUp() {
    if (!deviceExists) {
      final Dialog dialog = new Dialog(this);
      dialog.setContentView(R.layout.new_device);
      dialog.setTitle("Cadastrar novo dispositivo");

      dialog.findViewById(R.id.create).setOnClickListener(new View.OnClickListener() {
        @Override public void onClick(View v) {
          String name =
              ((EditText) dialog.findViewById(R.id.device_name)).getText().toString().trim();
          me = new Device(name, myId);
          progressDialog.setMessage("Cadastrando...");
          progressDialog.show();
          mDatabase.child("devices").child(myId).setValue(me);
        }
      });
      dialog.show();
    }
  }

  public void beAvailable() {
    status = ConnectionStatus.INIT_SERVER;
    signUp();
    if (deviceExists) {
      initConnectionAsServer();
    }
  }

  private void initConnectionAsServer() {
    status = ConnectionStatus.WAITING_CLIENT;
    progressDialog.setMessage("Aguardando o outro dispositivo conectar");
    if (!progressDialog.isShowing()) {
      progressDialog.show();
    }
  }

  @Override public void onDataChange(DataSnapshot dataSnapshot) {
    deviceExists = dataSnapshot.exists();
    if (deviceExists) {
      if (me == null)
        me = new Device(dataSnapshot.child("name").getValue().toString(), myId);
      switch (status) {
        case INIT_CLIENT:
          dismissProgressDialog();
          initConnectionAsClient();
          break;
        case WAITING_SERVER:
          if (dataSnapshot.child("connection").exists()) {
            Toast.makeText(this, "Conectado!!", Toast.LENGTH_LONG).show();
            gotoConnectedActivity(ClientActivity.class);
          }
          break;
        case WAITING_CLIENT:
          if (dataSnapshot.child("connection").exists()) {
            String name = dataSnapshot.child("connection")
                .child("client")
                .child("name")
                .getValue()
                .toString();
            String id =
                dataSnapshot.child("connection").child("client").child("id").getValue().toString();
            connectedDevice = new Device(name, id);
            connectionID =
                dataSnapshot.child("connection").child("connectionID").getValue().toString();
            Connection conn = new Connection(connectedDevice, me, connectionID);
            connectedDevice = conn.client;
            Postman.connectResponse(conn);
            Toast.makeText(MainActivity.this, "Conectado com " + connectedDevice.name,
                Toast.LENGTH_LONG).show();
            gotoConnectedActivity(ServerAcivity.class);
          }
          break;
        case INIT_SERVER:
          status = ConnectionStatus.WAITING_CLIENT;
          initConnectionAsServer();
          break;
      }
    }
  }

  private void initConnectionAsClient() {
    mDatabase.child("devices").addListenerForSingleValueEvent(new ValueEventListener() {
      @Override public void onDataChange(DataSnapshot dataSnapshot) {
        Iterator<DataSnapshot> it = dataSnapshot.getChildren().iterator();
        final List<Device> deviceList = new ArrayList<>();
        List<String> itemsList = new ArrayList<>();
        while (it.hasNext()) {
          DataSnapshot item = it.next();
          if (item.child("id").getValue().equals(myId)) continue;
          if (!item.child("connection").exists()) {
            Device device = new Device(item.child("name").getValue().toString(),
                item.child("id").getValue().toString());
            deviceList.add(device);
            itemsList.add(device.name);
          }
        }
        String[] items = itemsList.toArray(new String[itemsList.size()]);
        AlertDialog dialog = new AlertDialog.Builder(MainActivity.this).setItems(items,
            new DialogInterface.OnClickListener() {
              @Override public void onClick(DialogInterface dialog, int which) {
                connectedDevice = deviceList.get(which);
                progressDialog.setMessage("Conectando com " + connectedDevice);
                progressDialog.show();
                status = ConnectionStatus.WAITING_SERVER;
                connectionID = Postman.connect(me, connectedDevice);
              }
            }).setTitle("Dispositivos disponíveis").create();
        dialog.show();
      }

      @Override public void onCancelled(DatabaseError databaseError) {
        MainActivity.this.onCancelled(databaseError);
      }
    });
  }

  @Override public void onCancelled(DatabaseError databaseError) {
    dismissProgressDialog();
    status = ConnectionStatus.IDLE;
    Toast.makeText(this, databaseError.getMessage(), Toast.LENGTH_LONG).show();
    Postman.disconnect(myId);
  }

  private void dismissProgressDialog() {
    if (progressDialog.isShowing()) {
      progressDialog.dismiss();
    }
  }

  private void gotoConnectedActivity(Class mClass) {
    dismissProgressDialog();
    Intent intent = new Intent(this, mClass);
    intent.putExtra("me", me);
    intent.putExtra("connectedDevice", connectedDevice);
    intent.putExtra("connectionId", connectionID);
    startActivity(intent);
  }

  private String getMyID(){
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    return prefs.getString("device", null);
  }

  private boolean hasMyID() {
    return getMyID() != null;
  }

  private void storeID() {
    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
    editor.putString("device", myId);
    editor.apply();
  }
}
