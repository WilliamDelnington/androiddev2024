package vn.edu.usth.weather;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
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