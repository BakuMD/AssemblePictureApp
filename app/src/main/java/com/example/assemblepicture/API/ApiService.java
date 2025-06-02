package com.example.assemblepicture.API;

import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiService {
    @GET("numberoflevels.json")  // замените на актуальный путь
    Call<LevelsResponse> getLevels();
}
