package com.example.dodgegame;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    //Code from this program has been used from Beginning Android Games
    //Review SurfaceView, Canvas, continue

    GameSurface gameSurface;
    int ballX = 0;
    int paintBrushY = 0;
    private static int addTerm = 15;
    MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gameSurface = new GameSurface(this);
        setContentView(gameSurface);
        SensorManager manager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor accelerometerSensor = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        manager.registerListener(this, accelerometerSensor, manager.SENSOR_DELAY_NORMAL);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        mediaPlayer = MediaPlayer.create(this, R.raw.music);
        mediaPlayer.start();
    }

    @Override
    protected void onPause(){
        super.onPause();
        gameSurface.pause();
    }

    @Override
    protected void onResume(){
        super.onResume();
        gameSurface.resume();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if(event.values[0] > 4 && ballX >= -470)
            ballX-=15;
        else if(event.values[0] < -4 && ballX <= 300)
            ballX+=15;
        else if(event.values[0] > 0 && ballX >= -470)
            ballX-=5;
        else if(event.values[0] < 0 && ballX <= 300)
            ballX+=5;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    //----------------------------GameSurface Below This Line--------------------------
    public class GameSurface extends SurfaceView implements Runnable{
        Thread gameThread;
        SurfaceHolder holder;
        volatile boolean running = false;
        Bitmap ball;
        Bitmap paintbrush;
        int x = 200;
        Paint paintProperty;
        int screenWidth;
        int screenHeight;
        Rect frameToDraw;
        RectF whereToDraw;
        int soundId;
        SoundPool soundPool;
        int width;
        int height;
        int count = 0;

        public GameSurface(Context context) {
            super(context);

            soundPool = new SoundPool(100, AudioManager.STREAM_MUSIC, 0);
            soundId = soundPool.load(MainActivity.this, R.raw.splash, 1);

            int width = 300;
            int height = 447;

            int brushWidth = 600;
            int brushHeight = 300;

            holder=getHolder();
            ball= BitmapFactory.decodeResource(getResources(),R.drawable.monalisa);
            ball = Bitmap.createScaledBitmap(ball, width, height, false);

            paintbrush = BitmapFactory.decodeResource(getResources(),R.drawable.paintbrush);
            paintbrush = Bitmap.createScaledBitmap(paintbrush, brushWidth, brushHeight, false);

            frameToDraw = new Rect(0, 0, width, height);
            whereToDraw = new RectF(0, 0, width, height);

            Display screenDisplay = getWindowManager().getDefaultDisplay();
            Point sizeOfScreen = new Point();
            screenDisplay.getSize(sizeOfScreen);
            screenWidth=sizeOfScreen.x;
            screenHeight=sizeOfScreen.y;

            paintProperty= new Paint();
            paintProperty.setTextSize(100);

            setOnTouchListener(new OnTouchListener() {
            @Override
                public boolean onTouch(View v, MotionEvent event) {

                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        count++;
                        if (count % 2 == 0)
                            addTerm = 15;
                        else if (count % 2 == 1)
                            addTerm = 45;
                    }
                    return true;
                }
            });
        }

        @Override
        public void run() {

            int randX = (int) (Math.random() * (screenWidth - paintbrush.getWidth()));
            int score = 10;
            boolean collision = false;

            while (running == true && score >= 0){
                if (holder.getSurface().isValid() == false)
                    continue;
                Canvas canvas= holder.lockCanvas();

                width = canvas.getWidth();
                height = canvas.getHeight();

                canvas.drawRGB(114, 163, 145); //255,181,248

                canvas.drawBitmap(ball,(screenWidth) - 600 + ballX,(screenHeight) - 200 - ball.getHeight(),null);

                Paint paint = new Paint();
                paint.setColor(Color.BLACK);
                paint.setTextSize(100);
                canvas.drawText("Lives: " + score, 20, 100, paint);

                canvas.drawBitmap(paintbrush, randX, (screenHeight) - 2000 - paintbrush.getHeight() + paintBrushY, null);

                PaintBrushSpeed();

                if (paintBrushY + paintbrush.getHeight() >= (screenHeight) - 50 - ball.getHeight() && (randX >= ballX + 100 && randX <= ballX + ball.getWidth() + 200))
                {
                    if(!collision) {
                        collision = true;
                        score--;
                        soundPool.play(soundId, 45, 45, 0, 0, 1);
                        ball = BitmapFactory.decodeResource(getResources(), R.drawable.patricklisa);
                        ball = Bitmap.createScaledBitmap(ball, 300, 447, false);
                    }
                }

                if(paintBrushY > 2000) {
                    paintBrushY = 0;
                    randX = (int) (Math.random() * (screenWidth - paintbrush.getWidth()));

                    if (collision)
                    {
                        collision = false;
                        ball = BitmapFactory.decodeResource(getResources(), R.drawable.monalisa);
                        ball = Bitmap.createScaledBitmap(ball, 300, 447, false);
                        soundId = soundPool.load(MainActivity.this, R.raw.splash, 1);
                    }
                }

                if(score <= 0)
                {
                    Paint paint2 = new Paint();
                    paint2.setColor(Color.RED);
                    paint2.setTextSize(150);
                    canvas.drawText("GAME OVER!", 100, 600, paint2);
                    running = false;
                }
                holder.unlockCanvasAndPost(canvas);
            }
        }

        public void PaintBrushSpeed() {
            paintBrushY += addTerm;
        }

        public void resume(){
            running=true;
            gameThread=new Thread(this);
            gameThread.start();
        }

        public void pause() {
            running = false;
            while (true) {
                try {
                    gameThread.join();
                } catch (InterruptedException e) {
                }
            }
        }
    }//GameSurface
}//Activity