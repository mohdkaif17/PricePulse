package com.example.pricepulse.data.remote;

import android.content.Context;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.TimeUnit;

public class ApiClient {
    private static ApiClient instance;
    private final PricePulseApiService apiService;
    private SessionManager sessionManager;

    private ApiClient(SessionManager sessionManager) {
        this.sessionManager = sessionManager;

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .addInterceptor(chain -> {
                    Request original = chain.request();
                    Request.Builder builder = original.newBuilder()
                            .header("Accept", "application/json")
                            .header("Content-Type", "application/json");

                    if (this.sessionManager != null && this.sessionManager.isLoggedIn()) {
                        builder.header("Authorization", "Bearer " + this.sessionManager.getAuthToken());
                    }

                    return chain.proceed(builder.build());
                })
                .addInterceptor(logging)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ApiConfig.BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        this.apiService = retrofit.create(PricePulseApiService.class);
    }

    public static synchronized ApiClient getInstance(SessionManager sessionManager) {
        if (instance == null || (sessionManager != null && instance.sessionManager == null)) {
            instance = new ApiClient(sessionManager);
        } else if (sessionManager != null) {
            instance.sessionManager = sessionManager;
        }
        return instance;
    }

    public static synchronized PricePulseApiService getApiService() {
        if (instance == null) {
            instance = new ApiClient(null);
        }
        return instance.apiService;
    }

    public static synchronized PricePulseApiService getApiService(Context context) {
        if (instance == null) {
            instance = new ApiClient(context != null ? new SessionManager(context) : null);
        } else if (context != null && instance.sessionManager == null) {
            instance.sessionManager = new SessionManager(context);
        }
        return instance.apiService;
    }

    public static boolean isAdmin() {
        if (instance != null && instance.sessionManager != null) {
            return instance.sessionManager.isAdmin();
        }
        return false;
    }

    public PricePulseApiService getApiServiceInstance() {
        return apiService;
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }
}
