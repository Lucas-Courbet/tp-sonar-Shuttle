package com.simplecity.amp_library.http;

import com.simplecity.amp_library.http.lastfm.LastFmService;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HttpClient {

    public static final String TAG = "HttpClient";

    private static HttpClient sInstance;

    public static final OkHttpClient okHttpClient;

    public static final LastFmService lastFmService;

    public static final String TAG_ARTWORK = "artwork";

    public static synchronized HttpClient getInstance() {
        if (sInstance == null) {
            sInstance = new HttpClient();
        }
        return sInstance;
    }

    private HttpClient() {
        // This method is intentionnaly blank.
    }
}