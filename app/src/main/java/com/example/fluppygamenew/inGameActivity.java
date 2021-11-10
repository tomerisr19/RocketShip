package com.example.fluppygamenew;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import android.content.Context;
import java.util.Timer;
import java.util.TimerTask;
import java.util.List;

public class inGameActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static int DELAY = 700;
    private Timer timer;
    private int clock = 0;
    private int lifeCount = 2;
    private int shipIndex = 1;
    private ImageView[] ships = new ImageView[3];
    private ImageView leftBtn, rightBtn;
    private ImageView[][] asteroidsMat = new ImageView[6][3];
    private ImageView[] hearts = new ImageView[3];
    private ImageView[] explosions = new ImageView[3];


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_in_game);
        hideSystemUI();
        createAsteroid();

        rightBtn.setOnClickListener(v -> {
            if(shipIndex < 2) {
                ships[shipIndex].setVisibility(View.INVISIBLE);
                shipIndex++;
                ships[shipIndex].setVisibility(View.VISIBLE);
                checkHit();
            }
        });

        leftBtn.setOnClickListener(v -> {
            if(shipIndex > 0) {
                ships[shipIndex].setVisibility(View.INVISIBLE);
                shipIndex--;
                ships[shipIndex].setVisibility(View.VISIBLE);
                checkHit();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        startTicker();
    }

    private void startTicker() {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Log.d("timeTick", "Tick: " + clock + " On Thread: " + Thread.currentThread().getName());
                runOnUiThread(() -> {
                    Log.d("timeTick", "Tick: " + clock + " On Thread: " + Thread.currentThread().getName());
                    updateAsteroid();
                });
            }
        }, 0, DELAY);
    }


    private void updateAsteroid() {
        clock++;
        hideExplosions();
        if (clock % 2 == 0) {
            int randomNumer = randomAsteroids();
            switch (randomNumer) {
                case 0:
                    asteroidsMat[0][0].setVisibility(View.VISIBLE);
                    break;
                case 1:
                    asteroidsMat[0][1].setVisibility(View.VISIBLE);
                    break;
                case 2:
                    asteroidsMat[0][2].setVisibility(View.VISIBLE);
                    break;
            }

        }
        for (int i = 0; i < 3; i++) {
            if (asteroidsMat[5][i].getVisibility() == View.VISIBLE) {
                asteroidsMat[5][i].setVisibility(View.GONE);
            }

            for (int j = 4; j >= 0; j--) {
                if (asteroidsMat[j][i].getVisibility() == View.VISIBLE) {
                    asteroidsMat[j][i].setVisibility(View.INVISIBLE);
                    asteroidsMat[j+1][i].setVisibility(View.VISIBLE);
                }
            }
        }
       checkHit();


    }

    private void checkHit() {
        if (lifeCount < 0) {
            lifeCount = 2;
        }
        if (asteroidsMat[5][0].getVisibility() == View.VISIBLE
                && ships[0].getVisibility() == View.VISIBLE) {
            asteroidsMat[5][0].setVisibility(View.GONE);
            ships[0].setVisibility(View.GONE);
            explosions[0].setVisibility(View.VISIBLE);
            toast("Boom");
            vibrate();
            hearts[lifeCount--].setVisibility(View.INVISIBLE);

        } else if (asteroidsMat[5][1].getVisibility() == View.VISIBLE
                && ships[1].getVisibility() == View.VISIBLE) {
            asteroidsMat[5][1].setVisibility(View.GONE);
            ships[1].setVisibility(View.GONE);
            explosions[1].setVisibility(View.VISIBLE);
            toast("Boom");
            vibrate();
            hearts[lifeCount--].setVisibility(View.INVISIBLE);

        } else if (asteroidsMat[5][2].getVisibility() == View.VISIBLE
                && ships[2].getVisibility() == View.VISIBLE) {
            asteroidsMat[5][2].setVisibility(View.GONE);
            ships[2].setVisibility(View.GONE);
            explosions[2].setVisibility(View.VISIBLE);
            toast("Boom");
            vibrate();
            hearts[lifeCount--].setVisibility(View.INVISIBLE);
        }

    }

    private void vibrate() {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            v.vibrate(500);
        }
    }

    private void hideExplosions() {
        explosions[0].setVisibility(View.GONE);
        explosions[1].setVisibility(View.GONE);
        explosions[2].setVisibility(View.GONE);
        ships[shipIndex].setVisibility(View.VISIBLE);

    }

    private int randomAsteroids() {

        return (int) (Math.random()*(2+1-0)) + 0;
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopTicker();
    }

    private void toast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show();
    }

    private void stopTicker() {
        timer.cancel();
    }


    private void createAsteroid() {

        Log.d(TAG, "anteroidViews: Started");
        ships[0] = findViewById(R.id.main_IMG_leftShip);
        ships[1] = findViewById(R.id.main_IMG_ship);
        ships[2] = findViewById(R.id.main_IMG_rightShip);

        rightBtn = findViewById(R.id.rightClick_Btn);
        leftBtn = findViewById(R.id.leftClick_Btn);

        asteroidsMat[0][0] = findViewById(R.id.asteroid_left1);
        asteroidsMat[1][0] = findViewById(R.id.asteroid_left2);
        asteroidsMat[2][0] = findViewById(R.id.asteroid_left3);
        asteroidsMat[3][0] = findViewById(R.id.asteroid_left4);
        asteroidsMat[4][0] = findViewById(R.id.asteroid_left5);
        asteroidsMat[5][0] = findViewById(R.id.asteroid_left6);
        asteroidsMat[0][1] = findViewById(R.id.asteroid_center1);
        asteroidsMat[1][1] = findViewById(R.id.asteroid_center2);
        asteroidsMat[2][1] = findViewById(R.id.asteroid_center3);
        asteroidsMat[3][1] = findViewById(R.id.asteroid_center4);
        asteroidsMat[4][1] = findViewById(R.id.asteroid_center5);
        asteroidsMat[5][1] = findViewById(R.id.asteroid_center6);
        asteroidsMat[0][2] = findViewById(R.id.asteroid_right1);
        asteroidsMat[1][2] = findViewById(R.id.asteroid_right2);
        asteroidsMat[2][2] = findViewById(R.id.asteroid_right3);
        asteroidsMat[3][2] = findViewById(R.id.asteroid_right4);
        asteroidsMat[4][2] = findViewById(R.id.asteroid_right5);
        asteroidsMat[5][2] = findViewById(R.id.asteroid_right6);

        hearts[0] = findViewById(R.id.rightHeart);
        hearts[1] = findViewById(R.id.middletHeart);
        hearts[2] = findViewById(R.id.leftHeart);

        explosions[0] = findViewById(R.id.leftExplosion);
        explosions[1] = findViewById(R.id.centerExplosion);
        explosions[2] = findViewById(R.id.rightExplosion);

    }

    public void hideSystemUI() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                      //  | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                     //   | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }


}