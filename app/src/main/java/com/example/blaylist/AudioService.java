package com.example.blaylist;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Vibrator;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AudioService {
    /** Playback: player setup, play/pause, song changes, player state and errors. */
    private static final String TAG = "Blaylist.AudioService";

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private int current_song_index;
    private final Repository STATE = Repository.getInstance();
    private ExoPlayer player;   // lives as long as the app, so music keeps playing between screens
    private Vibrator vibrator;
    private SharedPreferences prefs;   // survives the app being killed: last song, position, screen

    private AudioService(){
        this.current_song_index = 0;
    }

    private static class Holder {
        private static final AudioService INSTANCE = new AudioService();
    }
    public static AudioService getInstance() {
        return AudioService.Holder.INSTANCE;
    }

    /** Call once (MainActivity.onCreate) before using the player. */
    public void init(Context context) {
        if (player != null) {
            Log.d(TAG, "init: player already exists, keeping it (music continues)");
            return;
        }
        vibrator = context.getSystemService(Vibrator.class);
        prefs = context.getApplicationContext().getSharedPreferences("playback", Context.MODE_PRIVATE);
        STATE.getSongs().observeForever(songs -> restore());   // restore as soon as the songs are loaded
        if (vibrator == null || !vibrator.hasVibrator()) {
            Log.d(TAG, "init: this device has no vibrator, song changes won't buzz");
        }
        player = new ExoPlayer.Builder(context.getApplicationContext()).build();
        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                Log.d(TAG, "player state -> " + stateName(playbackState));
                if (playbackState == Player.STATE_ENDED) {
                    Log.d(TAG, "song #" + current_song_index + " finished, auto-advancing to the next one");
                    playNext();
                }
            }

            @Override
            public void onPlayerError(@NonNull PlaybackException error) {
                Log.e(TAG, "player failed on song #" + current_song_index + ": " + error.getErrorCodeName(), error);
            }
        });
        Log.d(TAG, "init: ExoPlayer created");
    }

    private static String stateName(int state) {
        switch (state) {
            case Player.STATE_IDLE: return "IDLE";
            case Player.STATE_BUFFERING: return "BUFFERING";
            case Player.STATE_READY: return "READY";
            case Player.STATE_ENDED: return "ENDED";
            default: return "UNKNOWN(" + state + ")";
        }
    }

    public int getCurrentSongIndex() { return current_song_index; }

    /** Saves the current song and position. Called on every song change and when a screen stops. */
    public void save() {
        List<Song> songs = getSongs();
        if (player == null || player.getMediaItemCount() == 0 || songs == null || current_song_index >= songs.size()) {
            return;   // nothing chosen yet
        }
        Song song = songs.get(current_song_index);
        prefs.edit().putLong("song_id", song.getId()).putLong("position_ms", player.getCurrentPosition()).apply();
        Log.d(TAG, "save: '" + song.getTitle() + "' at " + player.getCurrentPosition() + " ms");
    }

    public void setOnPlayingScreen(boolean on_playing_screen) {
        prefs.edit().putBoolean("on_playing_screen", on_playing_screen).apply();
    }

    public boolean wasOnPlayingScreen() {
        return prefs.getBoolean("on_playing_screen", false);
    }

    /** After the app was killed: load the saved song at the saved position, paused. */
    private void restore() {
        List<Song> songs = getSongs();
        long id = prefs.getLong("song_id", -1);
        if (player.getMediaItemCount() > 0 || songs == null || id == -1) {
            return;   // already playing something (app wasn't killed), or nothing saved
        }
        for (int i = 0; i < songs.size(); i++) {
            if (songs.get(i).getId() == id) {
                long position = prefs.getLong("position_ms", 0);
                current_song_index = i;
                player.setMediaItem(MediaItem.fromUri(songs.get(i).getContentUri()), position);
                player.prepare();
                STATE.setCurrentSongPlaying(new SongPlaying(false, songs.get(i), position));
                Log.d(TAG, "restore: '" + songs.get(i).getTitle() + "' (#" + i + ") at " + position + " ms, paused");
                return;
            }
        }
        Log.e(TAG, "restore: saved song id " + id + " is no longer on the device, starting fresh");
    }

    private List<Song> getSongs() {
        return STATE.getSongs().getValue();   // whatever is loaded right now
    }

    public void on_play_pause_click(){
        SongPlaying current_song = STATE.getCurrentSongPlaying().getValue();
        if (current_song == null){
            Log.e(TAG, "play/pause clicked but no song is selected (current SongPlaying is null)");
            return;
        }
        if (player == null) {
            Log.e(TAG, "play/pause clicked before init() - player is null");
            return;
        }
        boolean is_playing = current_song.is_playing();
        long updated_position = player.getCurrentPosition();
        if (is_playing){
            Log.d(TAG, "pause '" + current_song.getCurrent_song().getTitle() + "' at " + updated_position + " ms");
            player.pause();
        }
        else{
            Log.d(TAG, "resume '" + current_song.getCurrent_song().getTitle() + "' from " + updated_position + " ms");
            player.play();
        }
        STATE.setCurrentSongPlaying(new SongPlaying(!is_playing, current_song.getCurrent_song(), updated_position));

    }

    public void playNext(){
        // need to change the SongPlaying and then play it
        List<Song> songs = this.getSongs();
        if (songs == null || songs.isEmpty()){
            Log.e(TAG, "next: no songs loaded (" + (songs == null ? "list is null" : "list is empty") + ")");
            return;
        }
        int next_index = (this.current_song_index + 1) % songs.size();
        Log.d(TAG, "next: #" + current_song_index + " -> #" + next_index);
        this.changeSong(next_index);
    }

    public void playPrev(){
        // need to change the SongPlaying and then play it
        List<Song> songs = this.getSongs();
        if (songs == null || songs.isEmpty()){
            Log.e(TAG, "previous: no songs loaded (" + (songs == null ? "list is null" : "list is empty") + ")");
            return;
        }
        int prev_index = (this.current_song_index - 1 + songs.size()) % songs.size();
        Log.d(TAG, "previous: #" + current_song_index + " -> #" + prev_index);
        this.changeSong(prev_index);
    }

    public void changeSong(int new_song_index){
        // we change the song to a new song
        List<Song> songs = this.getSongs();
        if (songs == null){
            Log.e(TAG, "changeSong(" + new_song_index + "): songs not loaded yet (list is null)");
            return;
        }
        if (new_song_index < 0 || new_song_index >= songs.size()) {
            Log.e(TAG, "changeSong(" + new_song_index + "): index out of range, there are " + songs.size() + " songs");
            return;
        }
        if (player == null) {
            Log.e(TAG, "changeSong(" + new_song_index + "): called before init() - player is null");
            return;
        }
        Song new_song = songs.get(new_song_index);
        SongPlaying new_song_playing = new SongPlaying(true, new_song, 0);
        Log.d(TAG, "changeSong: #" + new_song_index + " '" + new_song.getTitle() + "' by " + new_song.getArtist()
                + " (" + new_song.getContentUri() + ")");

        // first we pause the previous song, then we change UI, then play the new song
        player.pause();
        this.current_song_index = new_song_index;
        STATE.setCurrentSongPlaying(new_song_playing);
        player.setMediaItem(MediaItem.fromUri(new_song.getContentUri()));
        player.prepare();
        player.play();
        save();
        if (vibrator != null) {
            vibrator.vibrate(100);   // short buzz on every song change
        }
    }


}
