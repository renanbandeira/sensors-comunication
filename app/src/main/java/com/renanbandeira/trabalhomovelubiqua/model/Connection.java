package com.renanbandeira.trabalhomovelubiqua.model;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Connection {
  public Device client;
  public Device server;
  public String connectionID;

  public Connection() {
  }

  public Connection(Device client, Device server, String connectionID) {
    this.client = client;
    this.server = server;
    this.connectionID = connectionID;
  }
}
