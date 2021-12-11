package com.example.fluppygamenew;

import static com.example.fluppygamenew.MainActivity.GAME_MODE;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.util.Timer;
import java.util.TimerTask;

import com.google.gson.Gson;

public class inGameActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private final int MAX = 4, MIN = 0;
    private static int DELAY = 700;
    private Timer timer;
    private MyDB myDB;
    private int clock = 0;
    private int lifeCount = 2;
    private int shipIndex = 2;
    private TextView scoreBar;
    private ImageView[] ships = new ImageView[5];
    private ImageView leftBtn, rightBtn;
    private ImageView[][] asteroidsMat = new ImageView[9][5];
    private ImageView[] hearts = new ImageView[3];
    private ImageView[] explosions = new ImageView[5];
    private ImageView[][] coinsMat = new ImageView[9][5];
    private MediaPlayer explosionSound;
    private SensorManager sensorManager;
    private Sensor sensor;
    private String gameMode = "";
    private SensorEventListener accSensorEventListener;
    public enum DirectionAction { LEFT,RIGHT }
    private LocationManager locationManager;
    private Location location;
    private int score;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_in_game);
        hideSystemUI();
        initViews();

        String fromJSON = MSPv3.getInstance(this).getStringSP("MY_DB", "");
        myDB = new Gson().fromJson(fromJSON, MyDB.class);
        if (myDB == null)
            myDB = new MyDB();

        if (getIntent() != null) {
            Intent intent = getIntent();

            gameMode = intent.getStringExtra(GAME_MODE);
            if (gameMode.equals("Sensor")) {
                initSensor();
                accSensorEventListener = new SensorEventListener() {
                    @Override
                    public void onSensorChanged(SensorEvent event) {
                        float x = event.values[0];
                        if (x <= -0.5) {
                            DirectionAction action = DirectionAction.LEFT;
                            moveCarBySensors(action);
                        } else if (x >= 0.5) {
                            DirectionAction action = DirectionAction.RIGHT;
                            moveCarBySensors(action);
                        }
                    }

                    @Override
                    public void onAccuracyChanged(Sensor sensor, int accuracy) {
                    }
                };
                leftBtn.setVisibility(View.INVISIBLE);
                rightBtn.setVisibility(View.INVISIBLE);

            } else {  // Btns mode
                rightBtn.setOnClickListener(v -> {
                    if (shipIndex < 4) {
                        ships[shipIndex].setVisibility(View.GONE);
                        shipIndex++;
                        ships[shipIndex].setVisibility(View.VISIBLE);
                        checkHit();
                    }
                });

                leftBtn.setOnClickListener(v -> {
                    if (shipIndex > 0) {
                        ships[shipIndex].setVisibility(View.GONE);
                        shipIndex--;
                        ships[shipIndex].setVisibility(View.VISIBLE);
                        checkHit();
                    }
                });
            }
        }
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

    }

    @Override
    protected void onStart() {
        super.onStart();
        startTicker();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(gameMode.equals("Sensors"))
            sensorManager.unregisterListener(accSensorEventListener);
    }

    protected void onResume() {
        super.onResume();
        if(gameMode.equals("Sensors"))
            sensorManager.registerListener(accSensorEventListener, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        hideSystemUI();
    }

    public boolean isSensorExists(int sensorType) {
        return (sensorManager.getDefaultSensor(sensorType) != null);
    }

    private void startTicker() {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Log.d("timeTick", "Tick: " + clock + " On Thread: " + Thread.currentThread().getName());
                runOnUiThread(() -> {
                    Log.d("timeTick", "Tick: " + clock + " On Thread: " + Thread.currentThread().getName());
                    if (lifeCount < 0)
                        finishGame();
                    else
                    updateAsteroid();
                });
            }
        }, 0, DELAY);
    }

    private void finishGame() {
        timer.cancel();
        Record record = new Record();
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 101);

        }
         location =  locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (myDB.getRecords().size() == 0) {
            record.setScore(score).setLat(location.getLatitude()).setLon(location.getLongitude());
            myDB.getRecords().add(record);
        }
        else if (myDB.getRecords().size() <= 10) {
            record.setScore(score).setLat(location.getLatitude()).setLon(location.getLongitude());
            myDB.getRecords().add(record);
        } else if (myDB.getRecords().get(myDB.getRecords().size() - 1).getScore() < score) {
            record.setScore(score).setLat(location.getLatitude()).setLon(location.getLongitude());
            myDB.getRecords().set(myDB.getRecords().size() - 1, record);
        }
        myDB.sortRecords();

        Intent intent = new Intent(this, RecordAndMapActivity.class);
        Bundle bundle = new Bundle();
        String json = new Gson().toJson(myDB);
        bundle.putString("myDB", json);
        intent.putExtra("myDB", bundle);
        MSPv3.getInstance(this).putStringSP("MY_DB", json);
        finish();
        startActivity(intent);
    }

    private void sortMyDB(MyDB myDB) {
        Record tempRecord;
        for(int i = myDB.getRecords().size() - 1; i >= 0; i--)
            if(i >= 1 && myDB.getRecords().get(i).getScore() > myDB.getRecords().get(i - 1).getScore()) {
                tempRecord = myDB.getRecords().get(i - 1);
                myDB.getRecords().set(i - 1, myDB.getRecords().get(i));
                myDB.getRecords().set(i, tempRecord);
            }
    }

    private void moveCarBySensors(DirectionAction action) {
        if (action == DirectionAction.LEFT) {
            if (shipIndex < 4) {
                ships[shipIndex].setVisibility(View.INVISIBLE);
                shipIndex++;
                ships[shipIndex].setVisibility(View.VISIBLE);
                checkHit();
            }
        } else if (action == DirectionAction.RIGHT) {
            if (shipIndex > 0) {
                ships[shipIndex].setVisibility(View.INVISIBLE);
                shipIndex--;
                ships[shipIndex].setVisibility(View.VISIBLE);
                checkHit();
            }
        }
    }


    private void updateAsteroid() {
        clock++;
        score = score + 1;
        scoreBar.setText(String.valueOf(score));

            hideExplosions();
            for (int i = 0; i < 5; i++) {
                if (asteroidsMat[8][i].getVisibility() == View.VISIBLE) {
                    asteroidsMat[8][i].setVisibility(View.GONE);
                }

                if (coinsMat[8][i].getVisibility() == View.VISIBLE) {
                    coinsMat[8][i].setVisibility(View.GONE);
                }

                for (int j = 8; j >= 0; j--) {
                    if (asteroidsMat[j][i].getVisibility() == View.VISIBLE) {
                        asteroidsMat[j][i].setVisibility(View.GONE);
                        asteroidsMat[j + 1][i].setVisibility(View.VISIBLE);
                    }
                }
                for (int j = 8; j >= 0; j--) {
                    if (coinsMat[j][i].getVisibility() == View.VISIBLE) {
                        coinsMat[j][i].setVisibility(View.GONE);
                        coinsMat[j + 1][i].setVisibility(View.VISIBLE);
                    }
                }
            }


        if (clock % 2 == 0)
            newAstroidsOnTheBord();

        if (clock % 4 == 0)
            newCoinsOnTheBord();

       checkHit();

    }



    private void newAstroidsOnTheBord() {
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
            case 3:
                asteroidsMat[0][3].setVisibility(View.VISIBLE);
                break;
            case 4:
                asteroidsMat[0][4].setVisibility(View.VISIBLE);
                break;
        }
    }

    private void newCoinsOnTheBord() {
        int randomNumer = randomAsteroids();
        switch (randomNumer) {
            case 0:
                coinsMat[0][0].setVisibility(View.VISIBLE);
                break;
            case 1:
                coinsMat[0][1].setVisibility(View.VISIBLE);
                break;
            case 2:
                coinsMat[0][2].setVisibility(View.VISIBLE);
                break;
            case 3:
                coinsMat[0][3].setVisibility(View.VISIBLE);
                break;
            case 4:
                coinsMat[0][4].setVisibility(View.VISIBLE);
                break;
        }
    }

    private void checkHit() {
        if (lifeCount < 0) {
            lifeCount = 2;
        }
        if (asteroidsMat[8][shipIndex].getVisibility() == View.VISIBLE
                && ships[shipIndex].getVisibility() == View.VISIBLE) {
            asteroidsMat[8][shipIndex].setVisibility(View.GONE);
            ships[shipIndex].setVisibility(View.GONE);
            explosions[shipIndex].setVisibility(View.VISIBLE);
            toast("Boom");
            vibrate();
            explosionSound.start();
            hearts[lifeCount--].setVisibility(View.INVISIBLE);
        }
        if (coinsMat[8][shipIndex].getVisibility() == View.VISIBLE
                && ships[shipIndex].getVisibility() == View.VISIBLE) {
            coinsMat[8][shipIndex].setVisibility(View.GONE);
            score += 100;
            toast("nice1");
            vibrate();

        }
    }

    private void initSensor() {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
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
        explosions[3].setVisibility(View.GONE);
        explosions[4].setVisibility(View.GONE);
        ships[shipIndex].setVisibility(View.VISIBLE);

    }

    private int randomAsteroids() {
        return (int) (Math.random()*(MAX+1-MIN)) + MIN;
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

    private void initViews() {
        explosionSound = MediaPlayer.create(this,R.raw.aaa);
        Log.d(TAG, "anteroidViews: Started");
        ships[0] = findViewById(R.id.main_IMG_leftShip);
        ships[1] = findViewById(R.id.main_IMG_leftToCenterShip);
        ships[2] = findViewById(R.id.main_IMG_ship);
        ships[3] = findViewById(R.id.main_IMG_rightToCenterShip);
        ships[4] = findViewById(R.id.main_IMG_rightShip);

        scoreBar = findViewById(R.id.textView_Score);

        rightBtn = findViewById(R.id.rightClick_Btn);
        leftBtn = findViewById(R.id.leftClick_Btn);

        asteroidsMat[0][0] = findViewById(R.id.asteroid_left1);
        asteroidsMat[1][0] = findViewById(R.id.asteroid_left2);
        asteroidsMat[2][0] = findViewById(R.id.asteroid_left3);
        asteroidsMat[3][0] = findViewById(R.id.asteroid_left4);
        asteroidsMat[4][0] = findViewById(R.id.asteroid_left5);
        asteroidsMat[5][0] = findViewById(R.id.asteroid_left6);
        asteroidsMat[6][0] = findViewById(R.id.asteroid_left7);
        asteroidsMat[7][0] = findViewById(R.id.asteroid_left8);
        asteroidsMat[8][0] = findViewById(R.id.asteroid_left9);

        asteroidsMat[0][1] = findViewById(R.id.asteroid_leftToCenter1);
        asteroidsMat[1][1] = findViewById(R.id.asteroid_leftToCenter2);
        asteroidsMat[2][1] = findViewById(R.id.asteroid_leftToCenter3);
        asteroidsMat[3][1] = findViewById(R.id.asteroid_leftToCenter4);
        asteroidsMat[4][1] = findViewById(R.id.asteroid_leftToCenter5);
        asteroidsMat[5][1] = findViewById(R.id.asteroid_leftToCenter6);
        asteroidsMat[6][1] = findViewById(R.id.asteroid_leftToCenter7);
        asteroidsMat[7][1] = findViewById(R.id.asteroid_leftToCenter8);
        asteroidsMat[8][1] = findViewById(R.id.asteroid_leftToCenter9);

        asteroidsMat[0][2] = findViewById(R.id.asteroid_center1);
        asteroidsMat[1][2] = findViewById(R.id.asteroid_center2);
        asteroidsMat[2][2] = findViewById(R.id.asteroid_center3);
        asteroidsMat[3][2] = findViewById(R.id.asteroid_center4);
        asteroidsMat[4][2] = findViewById(R.id.asteroid_center5);
        asteroidsMat[5][2] = findViewById(R.id.asteroid_center6);
        asteroidsMat[6][2] = findViewById(R.id.asteroid_center7);
        asteroidsMat[7][2] = findViewById(R.id.asteroid_center8);
        asteroidsMat[8][2] = findViewById(R.id.asteroid_center9);

        asteroidsMat[0][3] = findViewById(R.id.asteroid_rightToCenter1);
        asteroidsMat[1][3] = findViewById(R.id.asteroid_rightToCenter2);
        asteroidsMat[2][3] = findViewById(R.id.asteroid_rightToCenter3);
        asteroidsMat[3][3] = findViewById(R.id.asteroid_rightToCenter4);
        asteroidsMat[4][3] = findViewById(R.id.asteroid_rightToCenter5);
        asteroidsMat[5][3] = findViewById(R.id.asteroid_rightToCenter6);
        asteroidsMat[6][3] = findViewById(R.id.asteroid_rightToCenter7);
        asteroidsMat[7][3] = findViewById(R.id.asteroid_rightToCenter8);
        asteroidsMat[8][3] = findViewById(R.id.asteroid_rightToCenter9);

        asteroidsMat[0][4] = findViewById(R.id.asteroid_right1);
        asteroidsMat[1][4] = findViewById(R.id.asteroid_right2);
        asteroidsMat[2][4] = findViewById(R.id.asteroid_right3);
        asteroidsMat[3][4] = findViewById(R.id.asteroid_right4);
        asteroidsMat[4][4] = findViewById(R.id.asteroid_right5);
        asteroidsMat[5][4] = findViewById(R.id.asteroid_right6);
        asteroidsMat[6][4] = findViewById(R.id.asteroid_right7);
        asteroidsMat[7][4] = findViewById(R.id.asteroid_right8);
        asteroidsMat[8][4] = findViewById(R.id.asteroid_right9);

        hearts[0] = findViewById(R.id.rightHeart);
        hearts[1] = findViewById(R.id.middletHeart);
        hearts[2] = findViewById(R.id.leftHeart);

        coinsMat[0][0] = findViewById(R.id.coin_left1);
        coinsMat[1][0] = findViewById(R.id.coin_left2);
        coinsMat[2][0] = findViewById(R.id.coin_left3);
        coinsMat[3][0] = findViewById(R.id.coin_left4);
        coinsMat[4][0] = findViewById(R.id.coin_left5);
        coinsMat[5][0] = findViewById(R.id.coin_left6);
        coinsMat[6][0] = findViewById(R.id.coin_left7);
        coinsMat[7][0] = findViewById(R.id.coin_left8);
        coinsMat[8][0] = findViewById(R.id.coin_left9);

        coinsMat[0][1] = findViewById(R.id.coin_leftToCenter1);
        coinsMat[1][1] = findViewById(R.id.coin_leftToCenter2);
        coinsMat[2][1] = findViewById(R.id.coin_leftToCenter3);
        coinsMat[3][1] = findViewById(R.id.coin_leftToCenter4);
        coinsMat[4][1] = findViewById(R.id.coin_leftToCenter5);
        coinsMat[5][1] = findViewById(R.id.coin_leftToCenter6);
        coinsMat[6][1] = findViewById(R.id.coin_leftToCenter7);
        coinsMat[7][1] = findViewById(R.id.coin_leftToCenter8);
        coinsMat[8][1] = findViewById(R.id.coin_leftToCenter9);

        coinsMat[0][2] = findViewById(R.id.coin_center1);
        coinsMat[1][2] = findViewById(R.id.coin_center2);
        coinsMat[2][2] = findViewById(R.id.coin_center3);
        coinsMat[3][2] = findViewById(R.id.coin_center4);
        coinsMat[4][2] = findViewById(R.id.coin_center5);
        coinsMat[5][2] = findViewById(R.id.coin_center6);
        coinsMat[6][2] = findViewById(R.id.coin_center7);
        coinsMat[7][2] = findViewById(R.id.coin_center8);
        coinsMat[8][2] = findViewById(R.id.coin_center9);

        coinsMat[0][3] = findViewById(R.id.coin_rightToCenter1);
        coinsMat[1][3] = findViewById(R.id.coin_rightToCenter2);
        coinsMat[2][3] = findViewById(R.id.coin_rightToCenter3);
        coinsMat[3][3] = findViewById(R.id.coin_rightToCenter4);
        coinsMat[4][3] = findViewById(R.id.coin_rightToCenter5);
        coinsMat[5][3] = findViewById(R.id.coin_rightToCenter6);
        coinsMat[6][3] = findViewById(R.id.coin_rightToCenter7);
        coinsMat[7][3] = findViewById(R.id.coin_rightToCenter8);
        coinsMat[8][3] = findViewById(R.id.coin_rightToCenter9);

        coinsMat[0][4] = findViewById(R.id.coin_right1);
        coinsMat[1][4] = findViewById(R.id.coin_right2);
        coinsMat[2][4] = findViewById(R.id.coin_right3);
        coinsMat[3][4] = findViewById(R.id.coin_right4);
        coinsMat[4][4] = findViewById(R.id.coin_right5);
        coinsMat[5][4] = findViewById(R.id.coin_right6);
        coinsMat[6][4] = findViewById(R.id.coin_right7);
        coinsMat[7][4] = findViewById(R.id.coin_right8);
        coinsMat[8][4] = findViewById(R.id.coin_right9);

        explosions[0] = findViewById(R.id.leftExplosion);
        explosions[1] = findViewById(R.id.leftToCenterExplosion);
        explosions[2] = findViewById(R.id.centerExplosion);
        explosions[3] = findViewById(R.id.rightToCenterExplosion);
        explosions[4] = findViewById(R.id.rightExplosion);

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