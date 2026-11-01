package com.example.blaylist;

public class Song {
    private String name;            // the song's name
    private String author;          // the song's author
    private int duration;           // the song's duration
    private int image;              // TODO: what type? is needed? maybe the path?
    private String path_to_song;    // path to the song file

    public Song(String path_to_song) {
        this.path_to_song = path_to_song;
        // TODO: implement all the rest
    }

    public String getPath_to_song() {
        return path_to_song;
    }

    public void setPath_to_song(String path_to_song) {
        this.path_to_song = path_to_song;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
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
