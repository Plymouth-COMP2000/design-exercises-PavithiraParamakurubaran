package com.pavithira.dinevista.api;

import com.pavithira.dinevista.models.User;
import java.util.Map;
import retrofit2.Call;
import retrofit2.http.*;

public interface ApiService {

    // CREATE USER
    @POST("create_user/{student_id}")
    Call<Map<String, String>> createUser(
            @Path("student_id") String studentId,
            @Body User user
    );

    // READ ALL USERS
    @GET("read_all_users/{student_id}")
    Call<Map<String, Object>> getAllUsers(
            @Path("student_id") String studentId
    );

    // READ SPECIFIC USER
    @GET("read_user/{student_id}/{user_id}")
    Call<Map<String, Object>> getUser(
            @Path("student_id") String studentId,
            @Path("user_id") String userId
    );

    // UPDATE USER
    @PUT("update_user/{student_id}/{user_id}")
    Call<Map<String, String>> updateUser(
            @Path("student_id") String studentId,
            @Path("user_id") String userId,
            @Body User user
    );

    // DELETE USER
    @DELETE("delete_user/{student_id}/{user_id}")
    Call<Map<String, String>> deleteUser(
            @Path("student_id") String studentId,
            @Path("user_id") String userId
    );
}
