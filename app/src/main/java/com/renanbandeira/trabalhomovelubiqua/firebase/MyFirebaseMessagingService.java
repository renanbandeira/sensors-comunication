package com.renanbandeira.trabalhomovelubiqua.firebase;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
  public static String TAG = "FIREBASEEEEE";
  @Override public void onMessageReceived(RemoteMessage remoteMessage) {
    Log.d(TAG, "From: " + remoteMessage.getFrom());
    new Handler(Looper.getMainLooper()).post(new ToastSender("From veio"));
    // Check if message contains a data payload.
    if (remoteMessage.getData().size() > 0) {
      Log.d(TAG, "Message data payload: " + remoteMessage.getData());
      new Handler(Looper.getMainLooper()).post(new ToastSender("Data veio"));
    }

    // Check if message contains a notification payload.
    if (remoteMessage.getNotification() != null) {
      Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
    }
  }

  @Override public void onDeletedMessages() {
    new Handler(Looper.getMainLooper()).post(new ToastSender("Message deleted veio"));
    super.onDeletedMessages();
  }

  @Override public void onSendError(String s, Exception e) {
    new Handler(Looper.getMainLooper()).post(new ToastSender("Message error veio"));
    super.onSendError(s, e);
  }

  @Override public void onMessageSent(String s) {
    new Handler(Looper.getMainLooper()).post(new ToastSender("Message sent veio"));
    super.onMessageSent(s);
  }

  class ToastSender implements Runnable{
    String msg;
    public ToastSender(String msg) {
      this.msg = msg;
    }

    @Override public void run() {
      Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
    }
  }
}
