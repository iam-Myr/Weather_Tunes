package com.example.weathertunes;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

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
        final Button songBtn = findViewById(R.id.songBtn);
        Button favouritesBtn = findViewById(R.id.favouritesBtn);
        Button addToFavBtn = findViewById(R.id.addToFavBtn);
        songBtn.setVisibility(View.GONE);
        favouritesBtn.setVisibility(View.GONE);
        addToFavBtn.setVisibility(View.GONE);
        TextView weatherTxt = findViewById(R.id.weatherTxt);
        final TextView playingTxt = findViewById(R.id.playingTxt);
        final ImageView songImg = findViewById(R.id.songImg);


        Location location =  getLocation();
        double longitude;
        double latitude;
        if(location == null){
            Log.d("MYR", "IM IN NULL?");
            latitude = 39.3666; //33.74900;
            longitude = 22.9507;//84.38798;
            Toast.makeText(getApplicationContext(), "Couldn't find location! You are now in Atlanta", Toast.LENGTH_SHORT).show();

        }
        else {
           longitude = location.getLongitude();
           latitude = location.getLatitude();
        }

        FetchWeatherTask fetchWeather = new FetchWeatherTask(longitude, latitude );
        String cityAndWeather = null;

        try {
            cityAndWeather = fetchWeather.execute().get();
            if (cityAndWeather == null) cityAndWeather = "Atlanta - Clouds";
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
                playTrack(mediaPlayer, weather);
                playingTxt.setText("Now Playing: " + track.getName());
                Picasso.with(getApplicationContext()).load(track.getAlbum_image()).into(songImg);
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
        int hasReadLocationPermission = ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION);
        Log.d("MYR", "HAS Read Location Permissions " + hasReadLocationPermission);

        if (hasReadLocationPermission == PackageManager.PERMISSION_GRANTED) {
            Log.d("MYR", "Permission Granted");
            ACCESS_FINE_LOCATION_GRANTED = true;
        } else {
            Log.d("MYR", "Requesting permission");
            ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION}, REQUEST_CODE_ACCESS_FINE_LOCATION);
        }

        if (ACCESS_FINE_LOCATION_GRANTED) {
            LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if(lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                return lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }
            else {
                Toast.makeText(getApplicationContext(), "GPS must be enabled!", Toast.LENGTH_SHORT).show();
            }
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

    public void playTrack(MediaPlayer player, String weather){
        player.reset();

        String tag = generateTag(weather);

        FetchTrackTask fetchTrack = new FetchTrackTask(tag);
        try {
            track = fetchTrack.execute().get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            if (track != null) {
                player.setDataSource(track.getAudio_url());

                player.prepare();// might take long! (for buffering, etc)
                player.start();

            }
            else Toast.makeText(getApplicationContext(), "Couldn't fetch song", Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            e.printStackTrace();
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