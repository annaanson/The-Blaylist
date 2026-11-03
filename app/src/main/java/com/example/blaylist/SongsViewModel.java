package com.example.blaylist;

import android.app.Application;
import android.util.Log;

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
    /** ViewModel: what the screens ask for (load songs, old UI-only state updates). */
    private static final String TAG = "Blaylist.ViewModel";

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
            Log.e(TAG, "onSongSelectedUIUpdate(" + index + "): songs not loaded yet (list is null)");
            return;
        }
        if (index < 0 || index >= songs.size()) {
            Log.e(TAG, "onSongSelectedUIUpdate(" + index + "): index out of range, there are " + songs.size() + " songs");
            return;
        }
        Log.d(TAG, "onSongSelectedUIUpdate: selecting #" + index);

        Song selected_song = songs.get(index);
        SongPlaying selected_song_playing = new SongPlaying(true, selected_song, 0);
        STATE.setCurrentSongPlaying(selected_song_playing);
    }

    public void onPlayPauseClickedUIUpdate() {
        // logic - play / pause the current song

        SongPlaying current_song = getSongPlaying().getValue();
        if(current_song == null){
            Log.e(TAG, "onPlayPauseClickedUIUpdate: no song is selected (current SongPlaying is null)");
            return;
        }
        Log.d(TAG, "onPlayPauseClickedUIUpdate: toggling playing to " + !current_song.is_playing());
        boolean currently_is_playing = current_song.is_playing();
        current_song.setIs_playing(!currently_is_playing);
        STATE.setCurrentSongPlaying(current_song);
    }

    public void loadSongs() {
        // guard: query only once; survives rotation because the ViewModel does
        if (STATE.getSongs().getValue() == null) {
            Log.d(TAG, "loadSongs: no songs yet, asking the repository to load them");
            STATE.loadSongs(getApplication());   // Application IS a Context
        } else {
            Log.d(TAG, "loadSongs: songs already loaded (" + STATE.getSongs().getValue().size() + "), skipping query");
        }
    }
}
