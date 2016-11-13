package com.renanbandeira.trabalhomovelubiqua;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
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
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;
import com.renanbandeira.trabalhomovelubiqua.model.ConnectionStatus;
import com.renanbandeira.trabalhomovelubiqua.model.Device;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements ValueEventListener {
  private DatabaseReference mDatabase;
  ProgressDialog progressDialog;
  boolean deviceExists = false;
  ConnectionStatus status = ConnectionStatus.IDLE;
  String firebaseId;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    firebaseId = FirebaseInstanceId.getInstance().getToken();
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
    if (firebaseId != null) {
      mDatabase.child("devices").child(firebaseId).addValueEventListener(this);
    }
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    mDatabase.child(firebaseId).removeEventListener(this);
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
          String firebaseId = FirebaseInstanceId.getInstance().getToken();
          String name = ((EditText) dialog.findViewById(R.id.device_name)).getText().toString().trim();
          Device device = new Device(name, firebaseId);
          progressDialog.setMessage("Cadastrando...");
          progressDialog.show();
          mDatabase.child("devices").child(firebaseId).setValue(device);

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
    if (deviceExists){
      switch (status) {
        case INIT_CLIENT:
          dismissProgressDialog();
          initConnectionAsClient();
          break;
        case WAITING_SERVER:
          if (dataSnapshot.child("connection").exists()) {
            Toast.makeText(this, "Conectado!!", Toast.LENGTH_LONG).show();
            //TODO go to other activity
          }
          break;
        case WAITING_CLIENT:
          if (dataSnapshot.child("connection").exists()) {
            String deviceId = dataSnapshot.child("connection").getValue().toString();
            mDatabase.child("devices").child(deviceId).child("connection").setValue(firebaseId);
            mDatabase.child("devices").child(deviceId).addListenerForSingleValueEvent(new ValueEventListener() {
              @Override public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue().toString();
                Toast.makeText(MainActivity.this, "Conectado com" + name, Toast.LENGTH_LONG).show();
              }

              @Override public void onCancelled(DatabaseError databaseError) {
                MainActivity.this.onCancelled(databaseError);
              }
            });

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
        while(it.hasNext()) {
          DataSnapshot item = it.next();
          if(item.child("id").getValue().equals(firebaseId)) continue;
          if ((Boolean) item.child("available").getValue()) {
            Device device = new Device(item.child("name").getValue().toString(),
                item.child("id").getValue().toString(),
                item.child("available").getValue().equals("true"));
            deviceList.add(device);
            itemsList.add(device.name);
          }
        }
        String[] items = itemsList.toArray(new String[itemsList.size()]);
        AlertDialog dialog = new AlertDialog.Builder(MainActivity.this).setItems(items,
            new DialogInterface.OnClickListener() {
          @Override public void onClick(DialogInterface dialog, int which) {
            Device device = deviceList.get(which);
            progressDialog.setMessage("Conectando com " + device);
            progressDialog.show();
            status = ConnectionStatus.WAITING_SERVER;
            mDatabase.child("devices").child(device.id).child("connection").setValue(firebaseId);
          }
        })
            .setTitle("Dispositivos disponíveis")
            .create();
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
    //TODO remove connection in both sides on database
  }

  private void dismissProgressDialog() {
    if (progressDialog.isShowing()) {
      progressDialog.dismiss();
    }
  }
}
