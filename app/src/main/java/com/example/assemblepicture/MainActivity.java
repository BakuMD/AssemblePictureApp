package com.example.assemblepicture;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.assemblepicture.API.ApiService;
import com.example.assemblepicture.API.LevelsResponse;
import com.example.assemblepicture.Activities.LevelsActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private Button settings_btn, start_btn;
    private int levelsCount = 0;
    private String theme = "cars";
    private String[] options = {"Машины (выбрано)", "Кухня", "Правители"};
    private static final String BASE_URL = "https://assemblepictureapp.web.app/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Загружаем сохраненную тему
        SharedPreferences prefs = getSharedPreferences("GamePrefs", MODE_PRIVATE);
        theme = prefs.getString("selectedTheme", "cars");
        updateOptions();

        fetchLevelsCount();

        settings_btn = findViewById(R.id.settings_btn);
        start_btn = findViewById(R.id.start_btn);

        settings_btn.setOnClickListener(v -> showDialog());

        start_btn.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, LevelsActivity.class);
            intent.putExtra("theme", theme);
            intent.putExtra("levelsCount", levelsCount);
            startActivity(intent);
        });
    }

    private void updateOptions() {
        options = new String[]{"Машины", "Кухня", "Правители"};
        switch (theme) {
            case "cars":
                options[0] = "Машины (выбрано)";
                break;
            case "cuisine":
                options[1] = "Кухня (выбрано)";
                break;
            case "rulers":
                options[2] = "Правители (выбрано)";
                break;
        }
    }

    private void showDialog() {
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog, null);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        Button btn1 = dialogView.findViewById(R.id.btn_option1);
        Button btn2 = dialogView.findViewById(R.id.btn_option2);
        Button btn3 = dialogView.findViewById(R.id.btn_option3);
        Button cancel = dialogView.findViewById(R.id.cancel_btn);

        btn1.setText(options[0]);
        btn2.setText(options[1]);
        btn3.setText(options[2]);

        btn1.setOnClickListener(view -> {
            theme = "cars";
            saveTheme();
            updateOptions();
            dialog.dismiss();
        });

        btn2.setOnClickListener(view -> {
            theme = "cuisine";
            saveTheme();
            updateOptions();
            dialog.dismiss();
        });

        btn3.setOnClickListener(view -> {
            theme = "rulers";
            saveTheme();
            updateOptions();
            dialog.dismiss();
        });

        cancel.setOnClickListener(view -> dialog.dismiss());
    }

    private void saveTheme() {
        SharedPreferences prefs = getSharedPreferences("GamePrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("selectedTheme", theme);
        editor.apply();
    }

    private void fetchLevelsCount() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        ApiService apiService = retrofit.create(ApiService.class);

        Call<LevelsResponse> call = apiService.getLevels();

        call.enqueue(new Callback<LevelsResponse>() {
            @Override
            public void onResponse(Call<LevelsResponse> call, Response<LevelsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    levelsCount = response.body().getLevels();
                    Log.d("Levels", "Количество уровней: " + levelsCount);
                } else {
                    Log.e("API_ERROR", "Ответ неудачный или пустой");
                }
            }

            @Override
            public void onFailure(Call<LevelsResponse> call, Throwable t) {
                Log.e("API_ERROR", "Ошибка при запросе: " + t.getMessage());
            }
        });
    }
}