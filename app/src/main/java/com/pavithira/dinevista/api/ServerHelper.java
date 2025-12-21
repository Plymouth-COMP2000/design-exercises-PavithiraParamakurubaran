package com.pavithira.dinevista.api;

import android.util.Log;
import com.pavithira.dinevista.models.User;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ServerHelper {

    private static final String TAG = "ServerHelper";
    private static ApiService apiService = ApiClient.getRetrofit().create(ApiService.class);

    public interface ServerCallback {
        void onSuccess(Map<?, ?> response);
        void onFailure(String errorMessage);
    }

    public static void createUser(String studentId, User user, ServerCallback callback) {
        Call<Map<String, String>> call = apiService.createUser(studentId, user);

        call.enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "CreateUser Success: " + response.body());
                    callback.onSuccess(response.body());
                } else {
                    Log.e(TAG, "CreateUser Failed: " + response.code());
                    callback.onFailure("Server Error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                Log.e(TAG, "CreateUser Failed", t);
                callback.onFailure("Network Error: " + t.getMessage());
            }
        });
    }
}
