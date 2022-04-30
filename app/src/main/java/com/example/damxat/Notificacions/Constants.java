package com.example.damxat.Notificacions;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Constants {
    public static final String SERVER_KEY = "AAAAFI5UQRg:APA91bETIRImdxafcfVXUqPSo1eDPN9Dmi9YhUCurJWS54LRv_UxqaEbnDgpxcERFd9ywMQEnUqvpFalAX52mNixsjdkbmFomENnZ83upKT4kXncWtrmqT7j-8md8k4JfjQYQD-F2G3V";
    public static final String BASE_URL = "https://fcm.googleapis.com/";
    public static final String CONTENT_TYPE = "application/json";
    public static final Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build();
}
