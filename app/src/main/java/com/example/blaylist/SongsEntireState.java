package com.example.blaylist;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;


public class SongsEntireState {

    private MutableLiveData<SongPlaying> song_playing;
    private Song[] songs_list;

    public SongsEntireState(){

        // TODO: load all songs from the /Music folder to the Song list
        // TODO: in case there is a prior context, it should be loaded(current song id and its postition)

    }

    public LiveData<SongPlaying> get_song_playing(){
        // return an immutable version of the song currently playing
        return song_playing;
    }

    public void updateSongPlaying(SongPlaying new_song){
        song_playing.postValue(new_song);
    }
}
