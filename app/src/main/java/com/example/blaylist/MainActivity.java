package com.example.blaylist;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    /** Home screen: song list, taps, opening the playing screen. */
    private static final String TAG = "Blaylist.MainScreen";

    private SongsViewModel view_model;
    private AudioService audio_service;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate" + (savedInstanceState == null ? "" : " (recreated)"));
        WindowCompat.enableEdgeToEdge(getWindow());
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        view_model = new ViewModelProvider(this).get(SongsViewModel.class);
        audio_service = AudioService.getInstance();
        audio_service.init(this);

        RecyclerView songs_list = findViewById(R.id.songs_list);
        SongAdapter adapter = new SongAdapter(this::openSong);
        songs_list.setAdapter(adapter);

        // redraw the list every time the songs change (they arrive later, from a background thread)
        // after the app was killed on the playing screen, reopen it (not when Android restores the screens itself)
        boolean reopen_playing = savedInstanceState == null && audio_service.wasOnPlayingScreen();
        view_model.getSongs().observe(this, songs -> {
            if (songs == null) {
                Log.e(TAG, "songs observer got null - showing the empty state");
                showSongCount(0);
                return;
            }
            Log.d(TAG, "songs observer: showing " + songs.size() + " songs");
            adapter.setSongs(songs);
            showSongCount(songs.size());
            songs_list.scheduleLayoutAnimation();
            if (reopen_playing && !songs.isEmpty()) {
                Log.d(TAG, "reopening the playing screen from last time");
                startActivity(new Intent(this, PlayingActivity.class)
                        .putExtra(PlayingActivity.EXTRA_SONG_INDEX, audio_service.getCurrentSongIndex()));
            }
        });

        view_model.loadSongs();
        playHeaderAnimation();
    }

    @Override
    protected void onStop() {
        super.onStop();
        audio_service.save();
    }

    private void openSong(int index) {
        Log.d(TAG, "song #" + index + " tapped, opening the playing screen");
        audio_service.changeSong(index);
        Intent intent = new Intent(this, PlayingActivity.class);
        intent.putExtra(PlayingActivity.EXTRA_SONG_INDEX, index);
        startActivity(intent);
        if (Build.VERSION.SDK_INT < 34) {
            overridePendingTransition(R.anim.slide_up_in, R.anim.hold);
        }
    }

    private void showSongCount(int count) {
        TextView song_count = findViewById(R.id.song_count);
        View empty_state = findViewById(R.id.empty_state);
        if (count == 0) {
            song_count.setText(R.string.song_count_zero);
            empty_state.setVisibility(View.VISIBLE);
        } else {
            song_count.setText(getResources().getQuantityString(R.plurals.song_count, count, count));
            empty_state.setVisibility(View.GONE);
        }
    }

    private void playHeaderAnimation() {
        // the logo rises like the sun, the title follows
        View logo = findViewById(R.id.logo);
        logo.setScaleX(0.6f);
        logo.setScaleY(0.6f);
        logo.setAlpha(0f);
        logo.setRotation(-30f);
        logo.animate().scaleX(1f).scaleY(1f).alpha(1f).rotation(0f)
                .setDuration(700).setStartDelay(100).start();

        int[] ids = {R.id.app_title, R.id.app_subtitle, R.id.divider, R.id.song_count};
        for (int i = 0; i < ids.length; i++) {
            View v = findViewById(ids[i]);
            v.setAlpha(0f);
            v.setTranslationY(24f);
            v.animate().alpha(1f).translationY(0f)
                    .setDuration(500).setStartDelay(250 + i * 90L).start();
        }
    }
}
