package com.example.weathertunes;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_ACCESS_FINE_LOCATION = 1;
    private static boolean ACCESS_FINE_LOCATION_GRANTED = false;
    public static SQLiteDatabase db;
    private Track track = null;

    public static String ClearTags = "happy + brazil + cute + electric + energy";
    public static String RainTags = "sad + cafe + jazz + funk + ballad + tango + lofi";
    public static String CloudsTags = "rap + sad + storm";
    public static String MistTags = "horror + scary + dark + metal";
    public static String SnowTags = "christmas + bells + winter";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //Get activity items
        setRequestedOrientation(SCREEN_ORIENTATION_PORTRAIT);
        Button songBtn = findViewById(R.id.songBtn);
        Button favouritesBtn = findViewById(R.id.favouritesBtn);
        Button addToFavBtn = findViewById(R.id.addToFavBtn);
        songBtn.setVisibility(View.GONE);
        favouritesBtn.setVisibility(View.GONE);
        addToFavBtn.setVisibility(View.GONE);
        TextView weatherTxt = findViewById(R.id.weatherTxt);

        Location location =  getLocation();

        FetchWeatherTask fetchWeather = new FetchWeatherTask(location.getLongitude(), location.getLatitude());
        String cityAndWeather = null;

        try {
            cityAndWeather = fetchWeather.execute().get();
            Log.d("MYR", "The weather is: " + cityAndWeather);
            weatherTxt.setText(cityAndWeather);

        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        final String weather = cityAndWeather.split(" - ")[1];
        final MediaPlayer mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        //Create or open DB
        db = getBaseContext().openOrCreateDatabase(
                "favourites-db.db",
                Context.MODE_PRIVATE,
                null);

        //db.execSQL("CREATE TABLE IF NOT EXISTS Person(name text not null, age integer not null )");
        db.execSQL("CREATE TABLE IF NOT EXISTS \"Favourites\" (\n" +
                "    \"id\"    INTEGER NOT NULL,\n" +
                "    \"name\"    TEXT,\n" +
                "    \"duration\"    INTEGER,\n" +
                "    \"artist_id\"    NUMERIC,\n" +
                "    \"artist_name\"    BLOB,\n" +
                "    \"album_name\"    INTEGER,\n" +
                "    \"album_id\"    INTEGER,\n" +
                "    \"album_image\"    TEXT,\n" +
                "    \"audio_url\"    TEXT NOT NULL,\n" +
                "    PRIMARY KEY(\"id\")\n" +
                ");");


        //Make buttons visible
        songBtn.setVisibility(View.VISIBLE);
        favouritesBtn.setVisibility(View.VISIBLE);
        addToFavBtn.setVisibility(View.VISIBLE);

        songBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.reset();

                final String tag = generateTag(weather);


                FetchTrackTask fetchTrack = new FetchTrackTask(tag);
                try {
                    track = fetchTrack.execute().get();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                try {
                    mediaPlayer.setDataSource(track.getAudio_url());
                    mediaPlayer.prepare();// might take long! (for buffering, etc)
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mediaPlayer.start();
            }
        });

        addToFavBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (track != null){
                    int id = track.getId();
                    String name = track.getName();
                    int duration = track.getDuration();
                    int artist_id = track.getArtist_id();
                    String artist_name = track.getArtist_name();
                    String album_name = track.getAlbum_name();
                    int album_id = track.getAlbum_id();
                    String album_image = track.getAlbum_image();
                    String audio_url = track.getAudio_url();

                    db.execSQL("INSERT OR IGNORE INTO Favourites(id,name,duration,artist_id,artist_name,album_name,album_id,album_image,audio_url)" +
                            "VALUES ('"+id+"','"+name+"','"+duration+"', '"+artist_id+"','"+artist_name+"','"+album_name+"','"+album_id+"','"+album_image+"','"+audio_url+"')");
                }
            }
        });



        favouritesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,FavouritesActivity.class);
                intent.putExtra("weather", weather);

                startActivity(intent);
            }
        });

    }

    public Location getLocation() {
        int hasReadContactPermission = ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION);
        Log.d("MYR", "HAS Read Location Permissions " + hasReadContactPermission);

        if (hasReadContactPermission == PackageManager.PERMISSION_GRANTED) {
            Log.d("MYR", "Permission Granted");
            ACCESS_FINE_LOCATION_GRANTED = true;
        } else {
            Log.d("MYR", "Requesting permission");
            ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION}, REQUEST_CODE_ACCESS_FINE_LOCATION);
        }

        if (ACCESS_FINE_LOCATION_GRANTED) {
            LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            Location location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (location != null) {
                Log.e("ΜΥR", "Longitude: " + location.getLongitude());
                Log.e("ΜΥR", "Latitude: " + location.getLatitude());
            } else {
                location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }
            return location;
        }
        return null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    Log.d("MYR", "Permission Granted!");
                    ACCESS_FINE_LOCATION_GRANTED = true;
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Log.d("MYR", "Permission refused");
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    public String generateTag(String weather){
        String tag;


        //will be more complicated with randomizer tomorrow!!!!

        if (weather.equals("Clear")) tag = ClearTags;
        else if (weather.equals("Clouds")) tag = CloudsTags;
        else if (weather.equals("Rain")) tag = RainTags;
        else if (weather.equals("Snow")) tag = SnowTags;
        else if (weather.equals("Mist")) tag = MistTags;
        else tag = "cafe";

        return tag;


    }
}
