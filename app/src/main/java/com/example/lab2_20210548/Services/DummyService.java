package com.example.lab2_20210548.Services;


import com.example.lab2_20210548.Dtos.Tareas;
import com.example.lab2_20210548.Dtos.Task;
import com.example.lab2_20210548.Dtos.User;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface DummyService {

    // para autenticar el logeo
    @FormUrlEncoded
    @POST("/auth/login")
    Call<User> authenticationUser(@Field("username") String username, @Field("password") String password);

    // para obtener la lista de tareas por usuario
    @GET("/todos/user/{userId}")
    Call<Task> getTasks(@Path("userId") Integer userId);

    // para cambiar de estado
    @FormUrlEncoded
    @PUT("/todos/{todoId}")
    Call<Tareas> cambiarEstado(@Path("todoId") Integer todoId, @Field("completed") boolean completed);


}
