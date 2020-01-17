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
import java.util.Random;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;


public class MainActivity extends AppCompatActivity implements FetchWeatherTask.OnTaskCompleted, FetchTrackTask.OnTaskCompleted {

    private static final int REQUEST_CODE_ACCESS_FINE_LOCATION = 1;
    private static boolean ACCESS_FINE_LOCATION_GRANTED = false;
    private static boolean WEATHER_FETCHED = false;
    private static boolean MEDIAPLAYER_STARTED = false;
    private static String[] weather;
    private static Track currentTrack;
    private Intent intent;
    private static SQLiteDatabase db;
    private static MediaPlayer mediaPlayer;

    public static String[] ClearTags = {"happy", "brazil", "cute", "electric", "energy"};
    public static String[] RainTags = {"sad", "cafe", "jazz", "funk", "ballad", "tango", "lofi"};
    public static String[] CloudsTags = {"rap", "sad", "storm"};
    public static String[] MistTags = {"horror", "scary", "dark", "metal"};
    public static String[] SnowTags = {"christmas", "bells", "winter"};
    public static String[] FogTags = {"scary"};

    private TextView weatherTxt;
    private Button pauseBtn;
    private Button skipBtn;
    private Button favouritesBtn;
    private Button addToFavBtn;
    private TextView playingTxt;
    private ImageView songImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d("Main Activity", "started successfully");

        //Get activity items
        setRequestedOrientation(SCREEN_ORIENTATION_PORTRAIT);
        skipBtn = findViewById(R.id.skipBtn);
        skipBtn.setEnabled(false);
        favouritesBtn = findViewById(R.id.favouritesBtn);
        addToFavBtn = findViewById(R.id.addToFavBtn);
        addToFavBtn.setEnabled(false);
        pauseBtn = findViewById(R.id.pauseBtn);
        pauseBtn.setEnabled(false);
        weatherTxt = findViewById(R.id.weatherTxt);
        playingTxt =  findViewById(R.id.playingTxt);
        songImg = findViewById(R.id.songImg);

        intent = new Intent(MainActivity.this,FavouritesActivity.class);

        songImg.setImageResource(getResources().getIdentifier("drawable/loading", null, this.getPackageName()));

        //Get location
        Location location =  getLocation();
        double longitude;
        double latitude;
        if(location == null){
            latitude = 34.3666; //33.74900;
            longitude = 25.9507;//84.38798;
            Toast.makeText(getApplicationContext(), "Couldn't find location! You are now in Atlanta", Toast.LENGTH_SHORT).show();
        }
        else {
            longitude = location.getLongitude();
            latitude = location.getLatitude();
        }

        //Create Media player
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        //Get weather
        FetchWeatherTask fetchWeather = new FetchWeatherTask(longitude, latitude, this );
        fetchWeather.execute();


        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if(WEATHER_FETCHED) {
                    mediaPlayer.reset();
                    fetchTrack(weather[1]);
                }
            }
        });

        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer player) {
                playingTxt.setText("Now Playing: " + currentTrack.getName());
                Picasso.with(getApplicationContext()).load(currentTrack.getAlbum_image()).into(songImg);
                pauseBtn.setText("PAUSE");
                player.start();
                MEDIAPLAYER_STARTED = true;
                addToFavBtn.setEnabled(true);
                pauseBtn.setEnabled(true);
                skipBtn.setEnabled(true);
            }


        });

        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Toast.makeText(getApplicationContext(), "An error occured", Toast.LENGTH_SHORT).show();
                mediaPlayer.reset();
                return false;
            }
        });

        //Create or open DB
        db = getBaseContext().openOrCreateDatabase(
                "favourites-db.db",
                Context.MODE_PRIVATE,
                null);

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


        skipBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                songImg.setImageResource(getResources().getIdentifier("drawable/loading", null, "com.example.weathertunes"));
                if(WEATHER_FETCHED) {
                    mediaPlayer.reset();
                    fetchTrack(weather[1]);
                }
            }
        });

        addToFavBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentTrack != null){
                    int id = currentTrack.getId();
                    String name = currentTrack.getName();
                    int duration = currentTrack.getDuration();
                    int artist_id = currentTrack.getArtist_id();
                    String artist_name = currentTrack.getArtist_name();
                    String album_name = currentTrack.getAlbum_name();
                    int album_id = currentTrack.getAlbum_id();
                    String album_image = currentTrack.getAlbum_image();
                    String audio_url = currentTrack.getAudio_url();

                    db.execSQL("INSERT OR IGNORE INTO Favourites(id,name,duration,artist_id,artist_name,album_name,album_id,album_image,audio_url)" +
                            "VALUES ('"+id+"','"+name+"','"+duration+"', '"+artist_id+"','"+artist_name+"','"+album_name+"','"+album_id+"','"+album_image+"','"+audio_url+"')");
                    Toast.makeText(getApplicationContext(), "Added to favourites!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        favouritesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //intent.putExtra("weather", weather[1]); attempts to read from null array
                intent.putExtra("playerStarted",MEDIAPLAYER_STARTED);

                startActivity(intent);
            }
        });

        pauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mediaPlayer.isPlaying()){
                    mediaPlayer.pause();
                    pauseBtn.setText("PLAY");
                } else {
                    pauseBtn.setText("PAUSE");
                    mediaPlayer.start();
                }
            }
        });

       /* loopCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mediaPlayer.isPlaying()){
                    if (loopCheck.isChecked())
                        mediaPlayer.setLooping(true);
                    else
                        mediaPlayer.setLooping(false);
                }
            }
        });

        */
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaPlayer.release();
        mediaPlayer = null;
    }

    @Override
    public void onWeatherFetchCompleted(String[] result) {
        if(result != null) {
            weather = result;
            weatherTxt.setText(weather[0] + " - " + weather[1]);
            WEATHER_FETCHED = true;
        }
        else {
            Toast.makeText(getApplicationContext(), "Couldn't fetch weather", Toast.LENGTH_SHORT).show();
        }

        if(WEATHER_FETCHED) {
            fetchTrack(weather[1]);
        }
    }

    @Override
    public void onTrackFetchCompleted(Track result) {
        currentTrack = result;

        try {
            if (currentTrack != null) {
                mediaPlayer.setDataSource(currentTrack.getAudio_url());
                mediaPlayer.prepareAsync();
            }
            else Toast.makeText(getApplicationContext(), "Couldn't fetch song", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    public void fetchTrack(String weather){
        pauseBtn.setEnabled(false);
        skipBtn.setEnabled(false);
        addToFavBtn.setEnabled(false);
        String tag = generateTag(weather);

        FetchTrackTask fetchTrack = new FetchTrackTask(tag, this);
        fetchTrack.execute();
    }

    public String generateTag(String weather){
        Random rand = new Random();

        switch (weather) {
            case "Clear":
                return ClearTags[rand.nextInt(ClearTags.length)];
            case "Clouds":
                return CloudsTags[rand.nextInt(CloudsTags.length)];
            case "Rain":
                return RainTags[rand.nextInt(RainTags.length)];
            case "Snow":
                return SnowTags[rand.nextInt(SnowTags.length)];
            case "Mist":
                return MistTags[rand.nextInt(MistTags.length)];
            case "Fog":
                return FogTags[rand.nextInt(FogTags.length)];
            default:
                return "random";

        }
    }

}