package com.example.lab2_20210548.Workers;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class TimerWorker extends Worker {

    public TimerWorker(Context context, WorkerParameters params){
        super(context,params);
    }

    @NonNull
    @Override
    public Result doWork() {

        int minutos = getInputData().getInt("minutos", 25);
        int totalSeconds = minutos * 60;

        try {
            for (int i = totalSeconds; i >= 0; i--) {
                setProgressAsync(new Data.Builder().putInt("contador", i).build());
                // Espera de un segundo
                Thread.sleep(1000);
            }
            return Result.success(new Data.Builder().putString("info", "Worker ended").build());
        } catch (InterruptedException e) {
            Log.e("TimerWorker", "Worker interrupted", e);
            return Result.retry();
        }
    }

}
