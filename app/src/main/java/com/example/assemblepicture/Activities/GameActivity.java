package com.example.assemblepicture.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.assemblepicture.API.LevelApiService;
import com.example.assemblepicture.API.LevelConfig;
import com.example.assemblepicture.MainActivity;
import com.example.assemblepicture.R;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class GameActivity extends AppCompatActivity {

    private static final String BASE_URL = "https://assemblepictureapp.web.app/";
    private int levelsCount;
    private int currentLevel;
    private String theme;
    private GridLayout gridLayout;
    private RelativeLayout rootLayout;
    private ProgressBar progressBar;
    private TextView level_text;
    private int levelNumber;
    private String levelPath;
    private int rowDifficulty;    // формат: 1x, 2x или 3x, где x - размер сетки
    private int difficultyType;   // 1, 2 или 3
    private int gridSize;         // 3, 4 или 5
    private ArrayList<Integer> rotations = new ArrayList<>();
    private ArrayList<Integer> imageTags = new ArrayList<>();
    private ImageView firstSelected = null;
    private static final String TAG = "GameActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        Intent intent = getIntent();
        theme = intent.getStringExtra("theme");
        levelsCount = intent.getIntExtra("levelsCount", 1);
        currentLevel = intent.getIntExtra("currentLevel", 1);
        levelNumber = currentLevel;

        gridLayout = findViewById(R.id.grid_layout);
        rootLayout = findViewById(R.id.root_layout);
        progressBar = findViewById(R.id.progress_bar);
        level_text = findViewById(R.id.level_text);

        levelPath = "level" + levelNumber + "/";

        if (intent.getBooleanExtra("isPass", false)) {
            loadPassLevelData();
        } else {
            loadLevelData();
        }

        ImageButton imageButton = findViewById(R.id.btn_menu_back);
        imageButton.setOnClickListener(view -> {
            Intent levelsIntent = new Intent(GameActivity.this, LevelsActivity.class);
            levelsIntent.putExtra("theme", theme);
            levelsIntent.putExtra("levelsCount", levelsCount);
            startActivity(levelsIntent);
            finish();
        });
    }

    private void loadLevelData() {
        showLoading(true);
        level_text.setText("Уровень " + levelNumber);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL + theme + "/levels/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        LevelApiService apiService = retrofit.create(LevelApiService.class);
        Call<LevelConfig> call = apiService.getLevelConfig(levelPath + "difficulty.json");
        call.enqueue(new Callback<LevelConfig>() {
            @Override
            public void onResponse(Call<LevelConfig> call, Response<LevelConfig> response) {
                if (response.isSuccessful() && response.body() != null) {
                    rowDifficulty = response.body().getDifficulty();
                    difficultyType = rowDifficulty / 10; // 1, 2 или 3
                    gridSize = rowDifficulty % 10;       // 3, 4 или 5

                    loadBackground();

                    if (difficultyType == 1) {
                        loadImages(gridSize);         // просто поворот по тапу
                    } else {
                        loadShuffleLevel(gridSize);  // тасовка + возможно поворот
                    }
                } else {
                    showError();
                }
            }

            @Override
            public void onFailure(Call<LevelConfig> call, Throwable t) {
                showError();
            }
        });
    }

    private void loadBackground() {
        new Thread(() -> {
            try {
                URL url = new URL(BASE_URL + theme + "/levels/" + levelPath + "background.jpg");
                InputStream in = url.openStream();
                Bitmap bitmap = BitmapFactory.decodeStream(in);
                runOnUiThread(() -> rootLayout.setBackground(new android.graphics.drawable.BitmapDrawable(getResources(), bitmap)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void loadImages(int gridSize) {
        gridLayout.removeAllViews();
        gridLayout.setRowCount(gridSize);
        gridLayout.setColumnCount(gridSize);
        rotations.clear();

        int count = gridSize * gridSize;

        for (int i = 1; i <= count; i++) {
            ImageView imageView = new ImageView(this);
            int imageSize = getResources().getDisplayMetrics().widthPixels / (gridSize + 1);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = imageSize;
            params.height = imageSize;
            imageView.setLayoutParams(params);
            imageView.setBackgroundResource(R.drawable.grid_item_border);

            int randomRotation = new int[]{0, 90, 180, 270}[new Random().nextInt(4)];
            imageView.setRotation(randomRotation);
            rotations.add(randomRotation);

            Glide.with(this)
                    .load(BASE_URL + theme + "/levels/" + levelPath + "img" + i + ".jpg")
                    .into(imageView);

            imageView.setOnClickListener(v -> {
                int currentRotation = (int) imageView.getRotation();
                int newRotation = (currentRotation + 90) % 360;
                imageView.setRotation(newRotation);
                int pos = gridLayout.indexOfChild(imageView);
                rotations.set(pos, newRotation);
                checkIfRotatedCompleted();
            });

            gridLayout.addView(imageView);
        }

        showLoading(false);
    }

    private void loadShuffleLevel(int gridSize) {
        gridLayout.removeAllViews();
        gridLayout.setRowCount(gridSize);
        gridLayout.setColumnCount(gridSize);

        int count = gridSize * gridSize;
        ArrayList<Integer> indices = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            indices.add(i);
        }
        Collections.shuffle(indices);

        imageTags.clear();
        rotations.clear();
        firstSelected = null;

        for (int i = 0; i < count; i++) {
            int imageNum = indices.get(i);
            ImageView imageView = new ImageView(this);
            int imageSize = getResources().getDisplayMetrics().widthPixels / (gridSize + 1);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = imageSize;
            params.height = imageSize;
            imageView.setLayoutParams(params);
            imageView.setBackgroundResource(R.drawable.grid_item_border);

            Glide.with(this)
                    .load(BASE_URL + theme + "/levels/" + levelPath + "img" + imageNum + ".jpg")
                    .into(imageView);

            imageView.setTag(imageNum);
            imageTags.add(imageNum);

            int randomRotation = 0;
            if (difficultyType == 3) {
                randomRotation = new int[]{0, 90, 180, 270}[new Random().nextInt(4)];
                imageView.setRotation(randomRotation);
            }
            rotations.add(randomRotation);

            imageView.setOnClickListener(v -> {
                if (difficultyType == 1 || difficultyType == 3) {
                    int currentRotation = (int) imageView.getRotation();
                    int newRotation = (currentRotation + 90) % 360;
                    imageView.setRotation(newRotation);
                    int pos = gridLayout.indexOfChild(imageView);
                    rotations.set(pos, newRotation);
                    checkIfShuffleCompleted();
                }
            });

            imageView.setOnLongClickListener(v -> {
                if (firstSelected == null) {
                    firstSelected = imageView;
                    imageView.setAlpha(0.5f);
                } else if (firstSelected == imageView) {
                    firstSelected.setAlpha(1f);
                    firstSelected = null;
                } else {
                    swapImages(firstSelected, imageView);
                    firstSelected.setAlpha(1f);
                    firstSelected = null;
                    checkIfShuffleCompleted();
                }
                return true;
            });

            gridLayout.addView(imageView);
        }

        showLoading(false);
    }

    private void swapImages(ImageView image1, ImageView image2) {
        int index1 = gridLayout.indexOfChild(image1);
        int index2 = gridLayout.indexOfChild(image2);

        if (index1 == -1 || index2 == -1) return;

        int tag1 = (int) image1.getTag();
        float rot1 = image1.getRotation();
        int tag2 = (int) image2.getTag();
        float rot2 = image2.getRotation();

        Glide.with(this)
                .load(BASE_URL + theme + "/levels/" + levelPath + "img" + tag2 + ".jpg")
                .into(image1);
        Glide.with(this)
                .load(BASE_URL + theme + "/levels/" + levelPath + "img" + tag1 + ".jpg")
                .into(image2);

        image1.setTag(tag2);
        image2.setTag(tag1);
        image1.setRotation(rot2);
        image2.setRotation(rot1);

        imageTags.set(index1, tag2);
        imageTags.set(index2, tag1);
        rotations.set(index1, (int) rot2);
        rotations.set(index2, (int) rot1);

        Log.d(TAG, "swapImages: swapped positions " + index1 + " and " + index2);
    }

    private void checkIfRotatedCompleted() {
        for (int angle : rotations) {
            if (angle != 0) return;
        }
        onLevelCompleted();
    }

    private void checkIfShuffleCompleted() {
        for (int i = 0; i < imageTags.size(); i++) {
            if (imageTags.get(i) != i + 1) return;
            if (difficultyType == 3 && rotations.get(i) != 0) return;
        }
        onLevelCompleted();
    }

    private void onLevelCompleted() {
        new Handler().postDelayed(() -> showWinDialog(levelNumber), 1000);
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        gridLayout.setVisibility(isLoading ? View.INVISIBLE : View.VISIBLE);
    }

    private void showError() {
        showLoading(false);
        Toast.makeText(this, "Ошибка загрузки данных", Toast.LENGTH_SHORT).show();
    }

    private void showWinDialog(int level) {
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.win_dialog, null);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        Button btn_follow = dialogView.findViewById(R.id.btn_follow);
        Button btn_home = dialogView.findViewById(R.id.btn_home);
        TextView textView = dialogView.findViewById(R.id.text_result);

        if (level >= levelsCount) {
            btn_follow.setVisibility(View.GONE);
            textView.setText("Поздравляю, вы прошли " + level + " уровень, пока это последний:(");
        } else {
            textView.setText("Поздравляю, вы прошли " + level + " уровень!");
        }

        btn_follow.setOnClickListener(view -> {
            levelNumber++;
            SharedPreferences prefs = getSharedPreferences("GamePrefs", MODE_PRIVATE);
            LevelsActivity.updateLevelProgress(theme, levelNumber, prefs);
            levelPath = "level" + levelNumber + "/";
            loadLevelData();
            dialog.dismiss();
        });

        btn_home.setOnClickListener(view -> {
            levelNumber++;
            SharedPreferences prefs = getSharedPreferences("GamePrefs", MODE_PRIVATE);
            LevelsActivity.updateLevelProgress(theme, levelNumber, prefs);
            Intent mainIntent = new Intent(GameActivity.this, MainActivity.class);
            startActivity(mainIntent);
            finish();
            dialog.dismiss();
        });
    }

    private void loadPassImages(int gridSize) {
        gridLayout.removeAllViews();
        gridLayout.setRowCount(gridSize);
        gridLayout.setColumnCount(gridSize);
        rotations.clear();

        int count = gridSize * gridSize;

        for (int i = 1; i <= count; i++) {
            ImageView imageView = new ImageView(this);
            int imageSize = getResources().getDisplayMetrics().widthPixels / (gridSize + 1);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = imageSize;
            params.height = imageSize;
            imageView.setLayoutParams(params);
            imageView.setBackgroundResource(R.drawable.grid_item_border);

            Glide.with(this)
                    .load(BASE_URL + theme + "/levels/" + levelPath + "img" + i + ".jpg")
                    .into(imageView);

            gridLayout.addView(imageView);
        }

        showLoading(false);
    }

    private void loadPassLevelData() {
        showLoading(true);
        level_text.setText("Уровень " + levelNumber);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL + theme + "/levels/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        LevelApiService apiService = retrofit.create(LevelApiService.class);
        Call<LevelConfig> call = apiService.getLevelConfig(levelPath + "difficulty.json");
        call.enqueue(new Callback<LevelConfig>() {
            @Override
            public void onResponse(Call<LevelConfig> call, Response<LevelConfig> response) {
                if (response.isSuccessful() && response.body() != null) {
                    rowDifficulty = response.body().getDifficulty();
                    difficultyType = rowDifficulty / 10; // 1, 2 или 3
                    gridSize = rowDifficulty % 10;       // 3, 4 или 5
                    loadBackground();
                    loadPassImages(gridSize);
                } else {
                    showError();
                }
            }

            @Override
            public void onFailure(Call<LevelConfig> call, Throwable t) {
                showError();
            }
        });
    }
}