package com.assistia.exception;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.annotation.NonNull;
import java.io.PrintWriter;
import java.io.StringWriter;

public class GlobalExceptionHandler implements Thread.UncaughtExceptionHandler{

  private Context context;
  private StringBuilder errorMessageBuilder;


  public GlobalExceptionHandler(Context context){
    this.errorMessageBuilder = new StringBuilder();
    this.context = context;
  }

  @Override
  public void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {

    StringWriter stackTraceWriter = new StringWriter();
    e.printStackTrace(new PrintWriter(stackTraceWriter));
    errorMessageBuilder.append(stackTraceWriter.toString());

    Log.e("com.assistia", errorMessageBuilder.toString(), e);

    new Handler(Looper.getMainLooper()).post(() -> {
      AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this.context)
          .setTitle("An error occurred.")
          .setMessage(e.getMessage())
          .setPositiveButton("Terminate Application",
              (dialog, which) -> dialog.dismiss());
      alertDialogBuilder.show();
      android.os.Process.killProcess(android.os.Process.myPid());
    });

  }
}
