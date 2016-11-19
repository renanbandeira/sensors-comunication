package com.renanbandeira.trabalhomovelubiqua.firebase;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.renanbandeira.trabalhomovelubiqua.model.Connection;
import com.renanbandeira.trabalhomovelubiqua.model.Device;
import java.util.UUID;

/***
 * Responsible of delivering the firebase messages
 */
public class Postman {
  public enum Command {
    LOCATION, BATTERY, WIFI, ACTIVITY, PLAY_SOUND, SEND_SMS
  }

  public static String connect(Device from, Device to) {
    String connectionID = UUID.randomUUID().toString();

    Connection conn = new Connection(from, to, connectionID);
    DatabaseReference mDatabase = getDatabase();
    mDatabase.child("devices").child(to.id).child("connection").setValue(conn);
    mDatabase.child("connections").child(connectionID).setValue(conn);
    return connectionID;
  }

  public static void connectResponse(Connection conn) {
    DatabaseReference mDatabase = getDatabase();
    mDatabase.child("devices").child(conn.client.id).child("connection").setValue(conn);
  }

  public static void disconnect(String from) {
    DatabaseReference mDatabase = getDatabase();
    mDatabase.child("devices").child(from).child("connection").removeValue();
  }

  public static void sendCommand(String connectionId, Command command) {
    DatabaseReference mDatabase = getDatabase();
    mDatabase.child("connections").child(connectionId).child("command").setValue(command);
  }

  public static void sendCommandResponse(String connectionId, String value) {
    DatabaseReference mDatabase = getDatabase();
    mDatabase.child("connections").child(connectionId).child("response").setValue(value);
  }

  private static DatabaseReference getDatabase() {
    return FirebaseDatabase.getInstance().getReference();
  }
}
