package com.example.assemblepicture.API;

import com.google.gson.annotations.SerializedName;

public class LevelsResponse {

    @SerializedName("levelsCount")
    private int levelsCount;

    public int getLevels() {
        return levelsCount;
    }

    private void setLevelsCount(int levelsCount) {
        this.levelsCount = levelsCount;
    }
}
