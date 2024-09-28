package vn.edu.usth.weather;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class WeatherActivity extends AppCompatActivity {
    private MediaPlayer mediaPlayer;
    private static final String TAG = "WeatherActivity";
    private static final String SERVER_RESPONSE = "server_response";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_weather);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ViewPager pager = findViewById(R.id.pager);
        HomeFragmentPagerAdapter pagerAdapter = new HomeFragmentPagerAdapter(getSupportFragmentManager());
        pager.setAdapter(pagerAdapter);
        pager.setOffscreenPageLimit(3);

        TabLayout tabLayout = findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(pager);

        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);

        setSupportActionBar(toolbar);

        toolbar.showOverflowMenu();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Permission Denied");
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    1);
        } else {
            extractMp3FromAssetsToCard();

            playMp3FromSdCard();
        }


        Log.i(TAG, "App Created");
    }

    private void extractMp3FromAssetsToCard() {
        InputStream inputStream = null;
        FileOutputStream fileOutputStream = null;

        try {
            // Open MP3 file from raw resources
            inputStream = getResources().openRawResource(R.raw.pastlives);

            File sdCard = Environment.getExternalStorageDirectory();

            Log.i("SD external path", sdCard.getAbsolutePath());

            File dir = new File(sdCard.getAbsolutePath() + "/Music");

            // Checking if the path is right or not
            Log.i("SD Card Path", dir.getAbsolutePath());
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File outFile = new File("dir", "pastlives.mp3");

            fileOutputStream = new FileOutputStream(outFile);

            byte[] buffer = new byte[4096];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                fileOutputStream.write(buffer, 0, length);
            }

            fileOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("File error", e.getMessage());
        } finally {
            try {
                if (inputStream != null) inputStream.close();
                if (fileOutputStream != null) fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("Close error", e.getMessage());
            }
        }
    }

    private void playMp3FromSdCard() {
        File sdCard = Environment.getExternalStorageDirectory();
        File audioFile = new File(sdCard.getAbsolutePath() + "/Music/pastlives.mp3");

        if (audioFile.exists()) {
            mediaPlayer = new MediaPlayer();
            try {
                mediaPlayer.setDataSource(audioFile.getAbsolutePath());

                mediaPlayer.prepare();

                mediaPlayer.start();

                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        mediaPlayer.release();
                        mediaPlayer = null;
                    }
                });

            } catch (IOException e) {
                e.printStackTrace();
                Log.e("Play Error", e.getMessage());
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.out_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        int duration = Toast.LENGTH_LONG;

        if (id == R.id.refresh_toolbar) {
            Handler handler = new Handler(Looper.getMainLooper()) {
              @Override
              public void handleMessage(Message msg) {
                  String content = msg.getData().getString(SERVER_RESPONSE);
                  Toast.makeText(getBaseContext(), content, duration).show();
              }
            };
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    Bundle bundle = new Bundle();
                    bundle.putString(SERVER_RESPONSE, getString(R.string.fetch_success));

                    Message msg = new Message();
                    msg.setData(bundle);
                    handler.sendMessage(msg);
                }
            });
            t.start();
            Toast.makeText(getBaseContext(), R.string.refresh_message, Toast.LENGTH_LONG).show();
            return true;
        } else if (id == R.id.setting_toolbar) {
            Intent prefActivityIntent = new Intent(this, PrefActivity.class);
            startActivity(prefActivityIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();

        Log.i(TAG, "App Start");
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.i(TAG, "App Resume");
    }

    @Override
    public void onPause() {
        super.onPause();

        Log.i(TAG, "App Pause");
    }

    @Override
    public void onStop() {
        super.onStop();

        Log.i(TAG, "App Stop");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }

        Log.i(TAG, "App Destroy");
    }
}