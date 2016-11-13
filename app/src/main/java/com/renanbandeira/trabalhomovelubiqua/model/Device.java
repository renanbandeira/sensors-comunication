package com.renanbandeira.trabalhomovelubiqua.model;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Device {
  public String name;
  public String id;
  public boolean available;

  public Device() {
  }

  public Device(String name, String id) {
    this.name = name;
    this.id = id;
    available = true;
  }

  public Device(String name, String id, boolean available) {
    this.name = name;
    this.id = id;
    this.available = available;
  }

  @Override public String toString() {
    return name;
  }
}
