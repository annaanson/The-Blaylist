package com.example.blaylist;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

/**
 * Connects the screens to the songs state. The screens only observe {@link SongPlaying}
 * and redraw themselves whenever it changes; the on...() methods are where the user's
 * actions should be turned into changes of the state.
 */
public class SongsViewModel extends AndroidViewModel {

    // one state shared by both screens
    private final Repository STATE = Repository.getInstance();
    public SongsViewModel(@NonNull Application application){
        super(application);

    }

    public LiveData<SongPlaying> getSongPlaying() {
        return STATE.getCurrentSongPlaying();
    }

    public LiveData<List<Song>> getSongs() {
        return STATE.getSongs();
    }

    public void onSongSelectedUIUpdate(int index) {
        // logic - make songs[index] the current song and play it
        List<Song> songs = getSongs().getValue();
        if(songs == null){
            // error
            return;
        }

        Song selected_song = songs.get(index);
        SongPlaying selected_song_playing = new SongPlaying(true, selected_song, 0);
        STATE.setCurrentSongPlaying(selected_song_playing);
    }

    public void onPlayPauseClickedUIUpdate() {
        // logic - play / pause the current song

        SongPlaying current_song = getSongPlaying().getValue();
        if(current_song == null){
            // error
            return;
        }
        boolean currently_is_playing = current_song.is_playing();
        current_song.setIs_playing(!currently_is_playing);
        STATE.setCurrentSongPlaying(current_song);
    }

    public void loadSongs() {
        // guard: query only once; survives rotation because the ViewModel does
        if (STATE.getSongs().getValue() == null) {
            STATE.loadSongs(getApplication());   // Application IS a Context
        }
    }
}
