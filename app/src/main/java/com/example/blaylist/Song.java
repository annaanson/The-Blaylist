package com.example.blaylist;

import android.graphics.Bitmap;

public class Song {
    private String name;            // the song's name
    private String author;          // the song's author
    private int duration;           // the song's duration
    private Bitmap image;           // the song's embedded picture, null if it has none
    private String path_to_song;    // path to the song file

    private int id;

    public Song(String path_to_song) {
        this.path_to_song = path_to_song;
        // TODO: implement all the rest
    }

    public String getPath_to_song() {
        return path_to_song;
    }

    public int getDuration() {
        return duration;
    }

    public Bitmap getImage() {
        return image;
    }

    public String getAuthor() {
        return author;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Song{" +
                "name='" + name + '\'' +
                ", author='" + author + '\'' +
                ", duration=" + duration +
                ", path_to_song='" + path_to_song + '\'' +
                '}';
    }
}
