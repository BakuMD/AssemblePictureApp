package com.example.assemblepicture.API;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface LevelApiService {
    @GET("{file}")
    Call<LevelConfig> getLevelConfig(@Path("file") String file);
}
