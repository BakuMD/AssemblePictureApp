package com.example.assemblepicture.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.assemblepicture.R;

public class LevelsActivity extends AppCompatActivity {

    private static final String BASE_URL = "https://assemblepictureapp.web.app/";
    private int levelsCount;
    private int currentLevel;
    private Button level_btn;
    private String theme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_levels);

        Intent intent = getIntent();
        theme = intent.getStringExtra("theme");
        levelsCount = intent.getIntExtra("levelsCount", 1);

        
        SharedPreferences prefs = getSharedPreferences("GamePrefs", MODE_PRIVATE);
        currentLevel = prefs.getInt("currentLevel_" + theme, 1);

        if (!prefs.contains("currentLevel_" + theme)) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("currentLevel_" + theme, 1);
            editor.apply();
        }

        GridLayout levels_grid_layout = findViewById(R.id.levels_grid_layout);
        LayoutInflater inflater = LayoutInflater.from(this);
        int marginT = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics());
        int marginL = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics());

        Button btn_back = findViewById(R.id.back_button);

        for (int i = 1; i <= levelsCount; i++) {
            if (i < currentLevel) {
                level_btn = (Button) inflater.inflate(R.layout.button_for_pass_lvl, null);
                level_btn.setText("" + i);
            } else if (i == currentLevel) {
                level_btn = (Button) inflater.inflate(R.layout.button_for_cur_lvl, null);
                level_btn.setText("" + i);
            } else {
                level_btn = (Button) inflater.inflate(R.layout.button_for_close_lvl, null);
                level_btn.setText("" + i);
            }
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.setMargins(marginL, marginT, marginL, marginT);
            params.width = GridLayout.LayoutParams.WRAP_CONTENT;
            params.height = GridLayout.LayoutParams.WRAP_CONTENT;

            int index = i;
            level_btn.setOnClickListener(view -> {
                if (index > currentLevel) {
                    Toast.makeText(LevelsActivity.this, "Уровень недоступен, пройдите предыдущие", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent gameIntent = new Intent(LevelsActivity.this, GameActivity.class);
                gameIntent.putExtra("isPass", index < currentLevel);
                gameIntent.putExtra("currentLevel", index);
                gameIntent.putExtra("levelsCount", levelsCount);
                gameIntent.putExtra("theme", theme);
                startActivity(gameIntent);
                finish();
            });

            level_btn.setLayoutParams(params);
            levels_grid_layout.addView(level_btn);
        }

        btn_back.setOnClickListener(view -> finish());
    }

    public static void updateLevelProgress(String theme, int level, SharedPreferences prefs) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("currentLevel_" + theme, level);
        editor.apply();
    }
}
