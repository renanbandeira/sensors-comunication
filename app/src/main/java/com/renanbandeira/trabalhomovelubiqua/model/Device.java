package com.renanbandeira.trabalhomovelubiqua.model;

import com.google.firebase.database.IgnoreExtraProperties;
import java.io.Serializable;

@IgnoreExtraProperties
public class Device implements Serializable {
  public String name;
  public String id;

  public Device() {
  }

  public Device(String name, String id) {
    this.name = name;
    this.id = id;
  }

  @Override public String toString() {
    return name;
  }
}
