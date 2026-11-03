package com.example.blaylist;

import android.graphics.Bitmap;
import android.net.Uri;

public class Song {
    private long id;
    private Uri contentUri;
    private String title;
    private final String artist;
    private final long duration;

    public Song(long id, Uri contentUri, String title, String artist, long duration) {
        this.id = id;
        this.contentUri = contentUri;
        this.title = title;
        this.artist = artist;
        this.duration = duration;
    }

    public long getId() { return id; }

    public Uri getContentUri(){
        return contentUri;
    }
    public long getDuration() {
        return duration;
    }

    public String getArtist() {
        return artist;
    }

    public String getTitle() {
        return title;
    }
}
