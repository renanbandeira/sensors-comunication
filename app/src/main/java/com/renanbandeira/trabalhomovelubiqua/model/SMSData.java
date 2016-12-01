package com.renanbandeira.trabalhomovelubiqua.model;

public class SMSData {

  String phone;
  String message;

  public SMSData() {
  }

  public SMSData(String phone, String message) {
    this.phone = phone;
    this.message = message;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }
}
