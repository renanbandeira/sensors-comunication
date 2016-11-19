package com.renanbandeira.trabalhomovelubiqua.services;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import java.util.List;
import org.greenrobot.eventbus.EventBus;

public class ActivityRecognitionService extends IntentService {


  public ActivityRecognitionService() {
    super("ActivityRecognizedService");
  }

  public ActivityRecognitionService(String name) {
    super(name);
  }

  @Override
  protected void onHandleIntent(Intent intent) {
    if(ActivityRecognitionResult.hasResult(intent)) {
      ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
      String msg = getProbableActivity(result.getMostProbableActivity());
      EventBus.getDefault().post(new ActivityRecognitionEvent(msg));
      //handleDetectedActivities( result.getProbableActivities() );
    }
  }

  private void handleDetectedActivities(List<DetectedActivity> probableActivities) {
    String message = "";
    for( DetectedActivity activity : probableActivities ) {
      message = getProbableActivity(activity) + "\n";
    }
  }

  private String getProbableActivity(DetectedActivity activity) {
    String message = "";
    switch (activity.getType()) {
      case DetectedActivity.IN_VEHICLE: {
        message += "Veículo: "  + activity.getConfidence();
        break;
      }
      case DetectedActivity.ON_BICYCLE: {
        message += "Bike: "  + activity.getConfidence();
        break;
      }
      case DetectedActivity.ON_FOOT: {
        message += "A pé: "  + activity.getConfidence();
        break;
      }
      case DetectedActivity.RUNNING: {
        message += "Correndo: "  + activity.getConfidence();
        break;
      }
      case DetectedActivity.STILL: {
        message += "Parado: "  + activity.getConfidence();
        break;
      }
      case DetectedActivity.TILTING: {
        message += "Inclinando: "  + activity.getConfidence();
        break;
      }
      case DetectedActivity.WALKING: {
        message += "Andando: "  + activity.getConfidence();
        break;
      }
      case DetectedActivity.UNKNOWN: {
        message += "Desconhecido: "  + activity.getConfidence();
        break;
      }
    }
    return message;
  }

  public class ActivityRecognitionEvent {

    public final String message;

    public ActivityRecognitionEvent(String message) {
      this.message = message;
    }
  }
}
