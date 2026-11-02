package com.example.blaylist;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

/**
 * Connects the screens to the songs state. The screens only observe {@link SongPlaying}
 * and redraw themselves whenever it changes; the on...() methods are where the user's
 * actions should be turned into changes of the state.
 */
public class SongsViewModel extends ViewModel {

    // one state shared by both screens
    private static final SongsEntireState STATE = new SongsEntireState();

    public LiveData<SongPlaying> getSongPlaying() {
        return STATE.get_song_playing();
    }

    public Song[] getSongs() {
        return STATE.get_songs_list();
    }

    public void onSongSelectedUIUpdate(int index) {
        // logic - make songs[index] the current song and play it
        Song selected_song = getSongs()[index];
        SongPlaying selected_song_playing = new SongPlaying(true, selected_song, 0);
        STATE.updateSongPlaying(selected_song_playing);
    }

    public void onPlayPauseClickedUIUpdate() {
        // logic - play / pause the current song

        SongPlaying current_song = getSongPlaying().getValue();
        if(current_song == null){
            return;
        }
        boolean currently_is_playing = current_song.is_playing();
        current_song.setIs_playing(!currently_is_playing);
        STATE.updateSongPlaying(current_song);
    }

    public void onNextClickedUIUpdate() {
        // TODO: logic - play the next song
    }

    public void onPreviousClickedUIUpdate() {
        // TODO: logic - play the previous song
    }

    public void onSeekUIUpdate(int secs_from_start) {
        // TODO: logic - jump to secs_from_start in the current song
    }
}
