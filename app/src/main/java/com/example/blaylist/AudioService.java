package com.example.blaylist;

import android.content.Context;

import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AudioService {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private int current_song_index;
    private final Repository STATE = Repository.getInstance();
    private ExoPlayer player;   // lives as long as the app, so music keeps playing between screens

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
        if (player == null) {
            player = new ExoPlayer.Builder(context.getApplicationContext()).build();
            player.addListener(new Player.Listener() {
                @Override
                public void onPlaybackStateChanged(int playbackState) {
                    if (playbackState == Player.STATE_ENDED) {
                        playNext();
                    }
                }
            });
        }
    }

    private List<Song> getSongs() {
        return STATE.getSongs().getValue();   // whatever is loaded right now
    }

    public void on_play_pause_click(){
        SongPlaying current_song = STATE.getCurrentSongPlaying().getValue();
        if (current_song == null){
            // error
            return;
        }
        boolean is_playing = current_song.is_playing();
        long updated_position = player.getCurrentPosition();
        if (is_playing){
            player.pause();
        }
        else{
            player.play();
        }
        STATE.setCurrentSongPlaying(new SongPlaying(!is_playing, current_song.getCurrent_song(), updated_position));

    }

    public void playNext(){
        // need to change the SongPlaying and then play it
        List<Song> songs = this.getSongs();
        if (songs == null){
            //error
            return;
        }
        int next_index = (this.current_song_index + 1) % songs.size();
        this.changeSong(next_index);
    }

    public void playPrev(){
        // need to change the SongPlaying and then play it
        List<Song> songs = this.getSongs();
        if (songs == null){
            //error
            return;
        }
        int prev_index = (this.current_song_index - 1 + songs.size()) % songs.size();
        this.changeSong(prev_index);
    }

    public void changeSong(int new_song_index){
        // we change the song to a new song
        List<Song> songs = this.getSongs();
        if (songs == null){
            //error
            return;
        }
        Song new_song = songs.get(new_song_index);
        SongPlaying new_song_playing = new SongPlaying(true, new_song, 0);

        // first we pause the previous song, then we change UI, then play the new song
        player.pause();
        this.current_song_index = new_song_index;
        STATE.setCurrentSongPlaying(new_song_playing);
        player.setMediaItem(MediaItem.fromUri(new_song.getContentUri()));
        player.prepare();
        player.play();
    }


}
