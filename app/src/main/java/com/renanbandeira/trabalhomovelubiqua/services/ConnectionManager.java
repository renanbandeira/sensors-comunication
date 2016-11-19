package com.renanbandeira.trabalhomovelubiqua.services;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import org.greenrobot.eventbus.EventBus;

public class ConnectionManager implements ValueEventListener {
  private DatabaseReference mDatabase;
  private String connectionID;
  private String myID;

  public ConnectionManager(DatabaseReference database, String connectionID, String myID) {
    this.connectionID = connectionID;
    this.myID = myID;
    mDatabase = database;

    mDatabase.child("connections")
        .child(connectionID).addValueEventListener(this);
  }

  @Override public void onDataChange(DataSnapshot dataSnapshot) {
    if (!dataSnapshot.exists()) {
      mDatabase.child("devices").child(myID).child("connection").removeValue();
      EventBus.getDefault().post(new ConnectionEvent());
      mDatabase.child("connections")
          .child(connectionID).removeEventListener(this);
    }
  }

  @Override public void onCancelled(DatabaseError databaseError) {

  }

  public class ConnectionEvent {
  }
}
