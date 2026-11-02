package com.example.blaylist;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Singleton class
public class Repository {
    private final MutableLiveData<List<Song>> songs = new MutableLiveData<>();
    private final MutableLiveData<SongPlaying> current_song_playing = new MutableLiveData<>();
    private final MutableLiveData<Long> position = new MutableLiveData<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private Repository() {
    }
    private static class Holder {
        private static final Repository INSTANCE = new Repository();
    }
    public static Repository getInstance() {
        return Holder.INSTANCE;
    }

    // getters
    public LiveData<List<Song>> getSongs() { return songs; }
    public LiveData<SongPlaying> getCurrentSongPlaying() { return current_song_playing; }
    public LiveData<Long> getPosition() { return position; }

    // setters
    public void setCurrentSongPlaying(SongPlaying new_song){
        current_song_playing.postValue(new_song);
    }
    public void setPosition(long ms){
        position.postValue(ms);
    }

    public void loadSongs(Context context) {
        Context app = context.getApplicationContext();
        executor.execute(() -> {
            List<Song> result = new ArrayList<>();

            Uri collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

            String[] projection = {
                    MediaStore.Audio.Media._ID,
                    MediaStore.Audio.Media.TITLE,
                    MediaStore.Audio.Media.ARTIST,
                    MediaStore.Audio.Media.DURATION
            };

            // only music, only mp3, only in the Music/ folder
            String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0"
                    + " AND " + MediaStore.Audio.Media.MIME_TYPE + " = ?"
                    + " AND " + MediaStore.Audio.Media.RELATIVE_PATH + " LIKE ?";
            String[] selectionArgs = { "audio/mpeg", "Music/%" };

            String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";

            try (Cursor cursor = app.getContentResolver().query(
                    collection, projection, selection, selectionArgs, sortOrder)) {

                int idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
                int titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
                int artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);
                int durCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION);

                while (cursor.moveToNext()) {
                    long id = cursor.getLong(idCol);
                    Uri contentUri = ContentUris.withAppendedId(collection, id); // cheap!
                    String title = cursor.getString(titleCol);
                    String artist = cursor.getString(artistCol);
                    long duration = cursor.getLong(durCol);

                    result.add(new Song(id, contentUri, title, artist, duration));
                }
            }

            songs.postValue(result); // publish on main thread
            Log.d("Repository", "loaded " + result.size() + " songs");
        });
    }
}
