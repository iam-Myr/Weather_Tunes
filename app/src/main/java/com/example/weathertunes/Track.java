package com.example.weathertunes;

import android.os.Parcel;
import android.os.Parcelable;

public class Track implements Parcelable {

    private int id;
    private String name;
    private int duration;
    private int artist_id;
    private String artist_name;
    private String album_name;
    private int album_id;
    private String image;
    private String audio_url;

    public Track(int id, String name, int duration, int artist_id, String artist_name, String album_name, int album_id, String album_image, String audio_url) {
        this.id = id;
        this.name = name;
        this.duration = duration;
        this.artist_id = artist_id;
        this.artist_name = artist_name;
        this.album_name = album_name;
        this.album_id = album_id;
        this.image = album_image;
        this.audio_url = audio_url;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getDuration() {
        return duration;
    }

    public int getArtist_id() {
        return artist_id;
    }

    public String getArtist_name() {
        return artist_name;
    }

    public String getAlbum_name() {
        return album_name;
    }

    public int getAlbum_id() {
        return album_id;
    }

    public String getImage() {
        return image;
    }

    public String getAudio_url() {
        return audio_url;
    }

    public String toString(){
        return name +" - "+ audio_url;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeInt(duration);
        dest.writeInt(artist_id);
        dest.writeString(artist_name);
        dest.writeString(album_name);
        dest.writeInt(album_id);
        dest.writeString(image);
        dest.writeString(audio_url);
    }

    public static final Parcelable.Creator<Track> CREATOR = new Parcelable.Creator<Track>() {
        public Track createFromParcel(Parcel in) {
            return new Track(in);
        }

        public Track[] newArray(int size) {
            return new Track[size];
        }
    };

    // example constructor that takes a Parcel and gives you an object populated with it's values
    private Track(Parcel in) {
        id = in.readInt();
        name = in.readString();
        duration = in.readInt();
        artist_id = in.readInt();
        artist_name = in.readString();
        album_name = in.readString();
        album_id = in.readInt();
        image = in.readString();
        audio_url = in.readString();
    }
}

