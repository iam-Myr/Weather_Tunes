package com.example.weathertunes;

public class Track {

    private int id;
    private String name;
    private int duration;
    private int artist_id;
    private String artist_name;
    private String album_name;
    private int album_id;
    private String album_image;
    private String audio_url;

    public Track(int id, String name, int duration, int artist_id, String artist_name, String album_name, int album_id, String album_image, String audio_url) {
        this.id = id;
        this.name = name;
        this.duration = duration;
        this.artist_id = artist_id;
        this.artist_name = artist_name;
        this.album_name = album_name;
        this.album_id = album_id;
        this.album_image = album_image;
        this.audio_url = audio_url;
    }

    public String getAudio_url() {
        return audio_url;
    }

    public String toString(){
        return name +" - "+ audio_url;
    }
}
