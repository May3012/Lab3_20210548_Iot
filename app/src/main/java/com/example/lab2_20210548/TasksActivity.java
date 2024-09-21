package com.example.lab2_20210548;

import android.content.Intent;
import android.os.Bundle;

import com.example.lab2_20210548.Dtos.Tareas;
import com.example.lab2_20210548.Dtos.User;
import com.example.lab2_20210548.Services.DummyService;
import com.google.android.material.snackbar.Snackbar;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.work.WorkManager;

import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
/*
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.lab2_20210548.databinding.ActivityTasksBinding;*/

public class TasksActivity extends AppCompatActivity {
    private Spinner spinnerTareas;
    private TextView emailUser, fullNameUser, textoTareasDe;
    private Button cambiarEstado;
    private Tareas[] listaTareas;
    private ArrayAdapter<String> adapter;
    private ImageView logoutIcon, backIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tasks);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // Recuperamos el Intent con los datos del usuario y las tareas
        Intent intent = getIntent();
        User user = (User) intent.getSerializableExtra("user");
        listaTareas = (Tareas[]) intent.getSerializableExtra("listaTareas");

        logoutIcon = findViewById(R.id.logoutIcon);
        backIcon = findViewById(R.id.iconBack);
        // OnClickListener al ícono de logout
        logoutIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Se cancela todos los trabajos en segundo plano
                WorkManager.getInstance(TasksActivity.this).cancelAllWork();
                // Ir a la MainActivity y cerrar pomodoro
                Intent intent = new Intent(TasksActivity.this, MainActivity.class);
                startActivity(intent);
                finish(); // Finalizar la actividad
            }
        });
        // Para regresar
        backIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Se cancela todos los trabajos en segundo plano
                WorkManager.getInstance(TasksActivity.this).cancelAllWork();
                // Ir a PomodoroActivity
                Intent intent = new Intent(TasksActivity.this, PomodoroActivity.class);
                startActivity(intent);
                finish();
            }
        });


        // Referencias a las vistas
        fullNameUser = findViewById(R.id.fullName);
        emailUser = findViewById(R.id.email);
        textoTareasDe = findViewById(R.id.texto_tareasde);
        spinnerTareas = findViewById(R.id.spinner_tareas);
        cambiarEstado = findViewById(R.id.cambiar_estado_tarea);

        // Los datos del usuario
        fullNameUser.setText(user.getFirstName() + " " + user.getLastName());
        emailUser.setText(user.getEmail());
        textoTareasDe.setText("VER TAREAS DE " + user.getFirstName() + " :");

        // Establecer el icono de género
        ImageView genderIcon = findViewById(R.id.icon_gender);
        if (user.getGender().equals("male")) {
            genderIcon.setImageResource(R.drawable.baseline_man_24);
        } else {
            genderIcon.setImageResource(R.drawable.baseline_woman_24);
        }

        // Llenado del Spinner con las tareas
        llenarSpinner();

        // Configuramos el botón para cambiar el estado de la tarea seleccionada
        cambiarEstado.setOnClickListener(v -> cambiar_estado_tarea());
    }

    //  Spinner con las tareas del usuario
    private void llenarSpinner() {
        List<String> lista = new ArrayList<>();
        for (Tareas tarea : listaTareas) {
            // completado
            if (tarea.isCompleted()) {
                lista.add(tarea.getTodo() + " - Completado");
            } else {
                //no completado
                lista.add(tarea.getTodo() + " - No completado");
            }
        }

        // Se crea un ArrayAdapter y lo seteamos en el Spinner
        adapter = new ArrayAdapter<>(this, R.layout.spinner_item, lista);
        spinnerTareas.setAdapter(adapter);
    }

    // Para cambiar el estado de la tarea seleccionada
    private void cambiar_estado_tarea() {
        int selectedPosition = spinnerTareas.getSelectedItemPosition();
        Tareas selectedTask = listaTareas[selectedPosition];
        boolean newStatus = !selectedTask.isCompleted();  // Cambiar el estado

        // Se crea Retrofit para hacer una consulta PUT para cambiar el estado
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://dummyjson.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        DummyService dummyService = retrofit.create(DummyService.class);

        dummyService.cambiarEstado(selectedTask.getId(), newStatus).enqueue(new Callback<Tareas>() {
            @Override
            public void onResponse(Call<Tareas> call, Response<Tareas> response) {
                if (response.isSuccessful()) {
                    Tareas updatedTask = response.body();
                    Toast.makeText(TasksActivity.this, "Estado cambiado: " + (updatedTask.isCompleted() ? "Completada" : "No completada"), Toast.LENGTH_SHORT).show();

                    // Actualizamos la tarea
                    listaTareas[selectedPosition].setCompleted(newStatus);
                    llenarSpinner();  // Actualizamos el Spinner
                } else {
                    Toast.makeText(TasksActivity.this, "Error al cambiar el estado", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Tareas> call, Throwable t) {
                Toast.makeText(TasksActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}