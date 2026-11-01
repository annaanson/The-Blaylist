package com.example.blaylist;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SongPlaying {
    private boolean is_playing;
    private Song current_song;
    private int secs_from_start;

    public SongPlaying(boolean is_playing, Song current_song, int secs_from_start) {
        this.is_playing = is_playing;
        this.current_song = current_song;
        this.secs_from_start = secs_from_start;
    }

    public boolean isIs_playing() {
        return is_playing;
    }

    public void setIs_playing(boolean is_playing) {
        this.is_playing = is_playing;
    }

    public Song getCurrent_song() {
        return current_song;
    }

    public void setCurrent_song(Song current_song) {
        this.current_song = current_song;
    }

    public int getSecs_from_start() {
        return secs_from_start;
    }

    public void setSecs_from_start(int secs_from_start) {
        this.secs_from_start = secs_from_start;
    }
}
