package com.example.vectorsense;


import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class RetroClient {
    private static final String BASE_URL = "https://androidapi.vectorsense.com.pk/";

    private static Retrofit retrofit;

    public static Retrofit getClient() {
        if (retrofit == null) {
            // Create OkHttpClient with logging interceptor
            OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
            //time out settings
            httpClient.connectTimeout(120, TimeUnit.SECONDS); // Adjust connection timeout
            httpClient.readTimeout(120, TimeUnit.SECONDS); // Adjust read timeout
            httpClient.writeTimeout(120, TimeUnit.SECONDS);
            // Add logging interceptor
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY); // Set log level as per your requirement
            httpClient.addInterceptor(logging);

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(httpClient.build()) // Set custom OkHttpClient
                    .build();
        }
        return retrofit;
    }
}

