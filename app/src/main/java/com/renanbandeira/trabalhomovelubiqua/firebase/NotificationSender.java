package com.renanbandeira.trabalhomovelubiqua.firebase;

import android.util.Log;
import android.widget.Toast;
import java.io.IOException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.google.android.gms.internal.zzs.TAG;

public class NotificationSender {
  /*
  public static final String FCM_MESSAGE_URL = "https://fcm.googleapis.com/fcm/send";
  OkHttpClient mClient = new OkHttpClient();
  public void sendMessage(final JSONArray recipients, final String title, final String body, final String icon, final String message) {

    new AsyncTask<String, String, String>() {
      @Override
      protected String doInBackground(String... params) {
        try {
          JSONObject root = new JSONObject();
          JSONObject notification = new JSONObject();
          notification.put("body", body);
          notification.put("title", title);
          notification.put("icon", icon);

          JSONObject data = new JSONObject();
          data.put("message", message);
          root.put("notification", notification);
          root.put("data", data);
          root.put("registration_ids", recipients);

          String result = postToFCM(root.toString());
          Log.d(TAG, "Result: " + result);
          return result;
        } catch (Exception ex) {
          ex.printStackTrace();
        }
        return null;
      }

      @Override
      protected void onPostExecute(String result) {
        try {
          JSONObject resultJson = new JSONObject(result);
          int success, failure;
          success = resultJson.getInt("success");
          failure = resultJson.getInt("failure");
          Toast.makeText(getCurrentActivity(), "Message Success: " + success + "Message Failed: " + failure, Toast.LENGTH_LONG).show();
        } catch (JSONException e) {
          e.printStackTrace();
          Toast.makeText(getCurrentActivity(), "Message Failed, Unknown error occurred.", Toast.LENGTH_LONG).show();
        }
      }
    }.execute();
  }

  String postToFCM(String bodyString) throws IOException {
    RequestBody body = RequestBody.create(JSON, bodyString);
    Request request = new Request.Builder()
        .url(FCM_MESSAGE_URL)
        .post(body)
        .addHeader("Authorization", "key=" + SERVER_KEY)
        .build();
    Response response = mClient.newCall(request).execute();
    return response.body().string();
  }

  */
}
