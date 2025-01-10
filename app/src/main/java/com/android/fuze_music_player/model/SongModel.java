package com.android.fuze_music_player.model;

import java.io.Serializable;

public class SongModel implements Serializable {
    private long id;
    private String title;
    private String artist;
    private String album;
    private long duration;
    private String path;

    // Constructor có tham số để khởi tạo đối tượng SongModel
    public SongModel(long id, String title, String artist, String album, long duration, String path) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.duration = duration;
        this.path = path;
    }

    // Constructor không tham số
    public SongModel() {
    }

    // Getter và Setter cho các thuộc tính của SongModel
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}