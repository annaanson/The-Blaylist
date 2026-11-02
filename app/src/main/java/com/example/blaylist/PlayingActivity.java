package com.example.blaylist;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import java.util.Locale;

public class PlayingActivity extends AppCompatActivity {

    public static final String EXTRA_SONG_INDEX = "song_index";

    private SongsViewModel view_model;

    private ImageView song_art;
    private TextView song_title;
    private TextView song_author;
    private SeekBar seek_bar;
    private TextView time_elapsed;
    private TextView time_total;
    private ImageButton play_pause_button;
    private ObjectAnimator star_spin;

    private Song shown_song;            // the song currently drawn on screen
    private boolean shown_playing;      // whether the button currently shows "pause"
    private int slide_direction = 1;    // 1 = next song slides in from the right, -1 = from the left
    private boolean user_seeking;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.enableEdgeToEdge(getWindow());
        setContentView(R.layout.activity_playing);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            // the dunes reach under the navigation bar, only the controls are lifted above it
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            View controls = v.findViewById(R.id.play_pause_button);
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) controls.getLayoutParams();
            params.bottomMargin = getResources().getDimensionPixelSize(R.dimen.controls_bottom_margin) + systemBars.bottom;
            controls.setLayoutParams(params);
            return insets;
        });
        if (Build.VERSION.SDK_INT >= 34) {
            overrideActivityTransition(OVERRIDE_TRANSITION_OPEN, R.anim.slide_up_in, R.anim.hold);
            overrideActivityTransition(OVERRIDE_TRANSITION_CLOSE, R.anim.hold, R.anim.slide_down_out);
        }

        view_model = new ViewModelProvider(this).get(SongsViewModel.class);

        song_art = findViewById(R.id.song_art);
        song_title = findViewById(R.id.song_title);
        song_author = findViewById(R.id.song_author);
        seek_bar = findViewById(R.id.seek_bar);
        time_elapsed = findViewById(R.id.time_elapsed);
        time_total = findViewById(R.id.time_total);
        play_pause_button = findViewById(R.id.play_pause_button);
        ImageButton next_button = findViewById(R.id.next_button);
        ImageButton previous_button = findViewById(R.id.previous_button);

        findViewById(R.id.back_button).setOnClickListener(v -> finish());

        play_pause_button.setOnClickListener(v -> {
            pressBounce(v);
            view_model.onPlayPauseClicked();
        });
        next_button.setOnClickListener(v -> {
            nudge(v, 1);
            slide_direction = 1;
            view_model.onNextClicked();
        });
        previous_button.setOnClickListener(v -> {
            nudge(v, -1);
            slide_direction = -1;
            view_model.onPreviousClicked();
        });

        setupSceneryAnimations();

        // until the state says otherwise, show the song that was picked on the home screen
        showSelectedSong(getIntent().getIntExtra(EXTRA_SONG_INDEX, 0));

        view_model.getSongPlaying().observe(this, this::render);
    }

    @Override
    public void finish() {
        super.finish();
        if (Build.VERSION.SDK_INT < 34) {
            overridePendingTransition(R.anim.hold, R.anim.slide_down_out);
        }
    }

    @Override
    protected void onDestroy() {
        star_spin.cancel();
        super.onDestroy();
    }

    /** Draws the screen from the given state. Called every time SongPlaying changes. */
    private void render(SongPlaying state) {
        if (state == null || state.getCurrent_song() == null) {
            return;
        }
        Song song = state.getCurrent_song();
        if (song != shown_song) {
            if (shown_song == null) {
                showSong(song);
            } else {
                slideToSong(song);
            }
        }
        showProgress(state.getSecs_from_start(), song.getDuration());
        showPlaying(state.is_playing());
    }

    private void showSelectedSong(int index) {
        Song[] songs = view_model.getSongs();
        if (index >= 0 && index < songs.length) {
            showSong(songs[index]);
            showProgress(0, songs[index].getDuration());
        } else {
            song_title.setText(R.string.nothing_playing);
            song_author.setVisibility(View.GONE);
        }
        showPlaying(false);
    }

    private void showSong(Song song) {
        shown_song = song;
        song_title.setText(SongAdapter.displayName(song_title, song));
        if (song.getAuthor() != null && !song.getAuthor().isEmpty()) {
            song_author.setText(song.getAuthor());
            song_author.setVisibility(View.VISIBLE);
        } else {
            song_author.setVisibility(View.INVISIBLE);
        }
        if (song.getImage() != null) {
            song_art.setImageBitmap(song.getImage());
        } else {
            song_art.setImageResource(R.drawable.art_placeholder);
        }
    }

    /** The old song slides away and the new one slides in from the other side. */
    private void slideToSong(Song song) {
        View[] views = {song_art, song_title, song_author};
        float distance = getResources().getDisplayMetrics().widthPixels * 0.7f * slide_direction;
        for (int i = 0; i < views.length; i++) {
            View v = views[i];
            boolean last = i == views.length - 1;
            v.animate().cancel();
            v.animate()
                    .translationX(-distance).alpha(0f).rotationY(-12f * slide_direction)
                    .setDuration(180).setStartDelay(i * 30L)
                    .withEndAction(() -> {
                        if (last) {
                            showSong(song);
                        }
                        v.setTranslationX(distance);
                        v.setRotationY(12f * slide_direction);
                        v.animate()
                                .translationX(0f).alpha(1f).rotationY(0f)
                                .setDuration(320).setStartDelay(last ? 0 : 40)
                                .setInterpolator(new OvershootInterpolator(0.8f))
                                .withEndAction(null)
                                .start();
                    })
                    .start();
        }
        // showSong(song) runs after the views went out of sight, so remember the target right away
        shown_song = song;
    }

    private void showProgress(int secs_from_start, int duration) {
        time_total.setText(formatTime(duration));
        seek_bar.setMax(Math.max(duration, 0));
        if (!user_seeking) {
            seek_bar.setProgress(secs_from_start);
            time_elapsed.setText(formatTime(secs_from_start));
        }
    }

    /** Switches the big button between play and pause, morphing one icon into the other. */
    private void showPlaying(boolean playing) {
        if (playing == shown_playing && play_pause_button.getDrawable() != null) {
            return;
        }
        boolean animate = playing != shown_playing;
        shown_playing = playing;
        play_pause_button.setContentDescription(getString(playing ? R.string.cd_pause : R.string.cd_play));

        if (animate) {
            Drawable morph = ContextCompat.getDrawable(this,
                    playing ? R.drawable.avd_play_to_pause : R.drawable.avd_pause_to_play);
            play_pause_button.setImageDrawable(morph);
            if (morph instanceof Animatable) {
                ((Animatable) morph).start();
            }
        } else {
            play_pause_button.setImageResource(playing ? R.drawable.ic_pause : R.drawable.ic_play);
        }

        if (playing) {
            if (star_spin.isPaused()) {
                star_spin.resume();
            } else if (!star_spin.isStarted()) {
                star_spin.start();
            }
        } else if (star_spin.isRunning()) {
            star_spin.pause();
        }
    }

    private void setupSceneryAnimations() {
        // the star behind the play button turns slowly while a song plays
        star_spin = ObjectAnimator.ofFloat(findViewById(R.id.play_star), View.ROTATION, 0f, 360f);
        star_spin.setDuration(16000);
        star_spin.setRepeatCount(ValueAnimator.INFINITE);
        star_spin.setInterpolator(new LinearInterpolator());

        // the stars twinkle
        ObjectAnimator twinkle = ObjectAnimator.ofFloat(findViewById(R.id.stars), View.ALPHA, 1f, 0.55f);
        twinkle.setDuration(2600);
        twinkle.setRepeatMode(ValueAnimator.REVERSE);
        twinkle.setRepeatCount(ValueAnimator.INFINITE);
        twinkle.start();

        // the moon floats gently
        ObjectAnimator float_moon = ObjectAnimator.ofFloat(findViewById(R.id.moon), View.TRANSLATION_Y, 0f, 10f);
        float_moon.setDuration(3400);
        float_moon.setRepeatMode(ValueAnimator.REVERSE);
        float_moon.setRepeatCount(ValueAnimator.INFINITE);
        float_moon.start();
    }

    /** Pushes a button a little in the given direction and lets it spring back. */
    private void nudge(View v, int direction) {
        float distance = 14f * getResources().getDisplayMetrics().density * direction;
        v.animate().cancel();
        v.animate().translationX(distance).setDuration(90).withEndAction(() ->
                v.animate().translationX(0f).setDuration(260)
                        .setInterpolator(new OvershootInterpolator(3f)).start()
        ).start();
    }

    private void pressBounce(View v) {
        v.animate().cancel();
        v.animate().scaleX(0.88f).scaleY(0.88f).setDuration(80).withEndAction(() ->
                v.animate().scaleX(1f).scaleY(1f).setDuration(240)
                        .setInterpolator(new OvershootInterpolator(4f)).start()
        ).start();
    }

    private static String formatTime(int secs) {
        if (secs < 0) {
            secs = 0;
        }
        return String.format(Locale.US, "%d:%02d", secs / 60, secs % 60);
    }
}
