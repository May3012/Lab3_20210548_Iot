package com.example.lab2_20210548;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.activity.result.ActivityResultLauncher;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.Observer;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import com.example.lab2_20210548.Dtos.Task;
import com.example.lab2_20210548.Dtos.User;
import com.example.lab2_20210548.Services.DummyService;
import com.example.lab2_20210548.Workers.TimerWorker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class PomodoroActivity extends AppCompatActivity {
    private TextView fullNameUser, timer, emailUser, descanso;
    private Button startButton, restartButton;
    private CountDownTimer countDownTimer;
    private boolean timerRunning = false;
    private User user;
    private ImageView logoutIcon;

    DummyService dummyService = new Retrofit.Builder()
            .baseUrl("https://dummyjson.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DummyService.class);

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.pomodoro_vista);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        logoutIcon = findViewById(R.id.logoutIcon);

        //  OnClickListener al ícono de logout
        logoutIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Se cancela todos los trabajos en segundo plano
                WorkManager.getInstance(PomodoroActivity.this).cancelAllWork();
                // Ir a la MainActivity y cerramos pomodoro
                Intent intent = new Intent(PomodoroActivity.this, MainActivity.class);
                startActivity(intent);
                finish(); // Finalizar la actividad
            }
        });

        user = (User) getIntent().getSerializableExtra("user");

        // Referenciar las vistas
        fullNameUser = findViewById(R.id.fullName);
        emailUser = findViewById(R.id.email);
        timer = findViewById(R.id.timer);
        descanso = findViewById(R.id.descanso);
        startButton = findViewById(R.id.playButtonPomodoro);
        restartButton = findViewById(R.id.retryButton);
        restartButton.setVisibility(View.GONE); // El botón de reinicio está oculto inicialmente

        // Mostrar información del usuario
        fullNameUser.setText(user.getFirstName() + " " + user.getLastName());
        emailUser.setText(user.getEmail());

        // Establecemos el icono de género
        ImageView genderIcon = findViewById(R.id.icon_gender);
        if (user.getGender().equals("male")) {
            genderIcon.setImageResource(R.drawable.baseline_man_24);
        } else {
            genderIcon.setImageResource(R.drawable.baseline_woman_24);
        }

        // Para empezar el pomodoro
        startButton.setOnClickListener(view -> {
            startButton.setVisibility(View.GONE);
            restartButton.setVisibility(View.VISIBLE);
            //Logica del temporizador
            startCountdown();
        });

        // Para reiniciar Pomodoro
        restartButton.setOnClickListener(view -> {
            startButton.setVisibility(View.VISIBLE);
            restartButton.setVisibility(View.GONE);
            timer.setText("25:00");
            descanso.setText("Descanso: 5:00");
        });
    }

    private void startCountdown() {
        Data dataBuilder = new Data.Builder()
                .putInt("minutos", 25)
                .build();

        WorkRequest workRequest = new OneTimeWorkRequest.Builder(TimerWorker.class)
                .setInputData(dataBuilder)
                .build();

        WorkManager.getInstance(this).enqueue(workRequest);

        // Observando cambios en el Worker
        WorkManager.getInstance(this).getWorkInfoByIdLiveData(workRequest.getId())
                .observe(this, workInfo -> {
                    if (workInfo != null) {
                        if (workInfo.getState() == WorkInfo.State.RUNNING) {
                            Data progress = workInfo.getProgress();
                            int contador = progress.getInt("contador", 0);
                            int minutos = contador / 60;
                            int segundos = contador % 60;
                            String tiempoFormateado = String.format("%02d:%02d", minutos, segundos);
                            timer.setText(tiempoFormateado);
                        } else if (workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                            timer.setText("00:00");
                            descanso.setText("En descanso");
                            startDescanso();
                        }
                    }
                });
    }

    private void startDescanso() {
        Data dataBuilderDescanso = new Data.Builder()
                .putInt("minutos", 5)
                .build();

        WorkRequest workRequestDescanso = new OneTimeWorkRequest.Builder(TimerWorker.class)
                .setInputData(dataBuilderDescanso)
                .build();

        WorkManager.getInstance(this).enqueue(workRequestDescanso);

        // Observando el descanso
        WorkManager.getInstance(this).getWorkInfoByIdLiveData(workRequestDescanso.getId())
                .observe(this, workInfoDescanso -> {
                    if (workInfoDescanso != null) {
                        if (workInfoDescanso.getState() == WorkInfo.State.RUNNING) {
                            Data progressDescanso = workInfoDescanso.getProgress();
                            int contadorDescanso = progressDescanso.getInt("contador", 0);
                            int minutosDescanso = contadorDescanso / 60;
                            int segundosDescanso = contadorDescanso % 60;
                            String tiempoDescansoFormateado = String.format("%02d:%02d", minutosDescanso, segundosDescanso);
                            timer.setText(tiempoDescansoFormateado);
                        } else if (workInfoDescanso.getState() == WorkInfo.State.SUCCEEDED) {
                            descanso.setText("Fin del descanso");
                            timer.setText("00:00");
                            showEndDialog();
                        }
                    }
                });
    }
    private void showEndDialog(){
        // Obtener las tareas del usuario
        dummyService.getTasks(user.getId()).enqueue(new Callback<Task>() {
            @Override
            public void onResponse(Call<Task> call, Response<Task> response) {
                if (response.isSuccessful()) {
                    Task task = response.body();
                    if (task != null && task.getTodos().length == 0) {
                        // Se muestra el dialog si no hay tareas
                        MaterialAlertDialogBuilder dialogFinDescanso = new MaterialAlertDialogBuilder(PomodoroActivity.this);
                        dialogFinDescanso.setTitle("Atención");
                        dialogFinDescanso.setMessage("Terminó el tiempo de descanso. Dale al botón de reinicio para empezar otro ciclo");
                        dialogFinDescanso.setPositiveButton("Entendido", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                                restartButton.setVisibility(View.GONE);
                                startButton.setVisibility(View.VISIBLE);
                            }
                        });
                    } else {
                        // Se muestra TareasActivity si es que hay tareas
                        Intent intent = new Intent(PomodoroActivity.this, TasksActivity.class);
                        intent.putExtra("listaTareas", task.getTodos());
                        intent.putExtra("user", user);
                        launcher.launch(intent);
                    }
                }
            }

            @Override
            public void onFailure(Call<Task> call, Throwable t) {

            }
        });
    }


    // Se muestra`TareasActivity` para gestionar las tareas con ActivityResultLauncher
    ActivityResultLauncher<Intent> launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == RESULT_OK) {
            Intent data = result.getData();
            if (data != null) {
                user = (User) data.getSerializableExtra("usuario");
            }
        }
    });


}