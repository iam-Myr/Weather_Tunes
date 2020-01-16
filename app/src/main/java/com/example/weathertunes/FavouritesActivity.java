package com.example.weathertunes;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class FavouritesActivity extends AppCompatActivity {

    SQLiteDatabase db = MainActivity.db;
    FavouritesAdapter favAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourites);

        loadDataFromDB();

        Intent intent = getIntent();

        TextView weatherTxt = findViewById(R.id.weatherTxt2);
        String weather = intent.getStringExtra("weather");
        weatherTxt.setText("Current weather: " + weather);

        getArrayFromDB(db);

        /* DB delete code
                if (favAdapter.getCount() != 0) {//list isn't empty
                    View parentRow = (View) v.getParent();
                    ListView listView = (ListView) parentRow.getParent();
                    final int position = listView.getPositionForView(parentRow);
                    Track track = (Track)favourites.get(position);
                    String trackID = String.valueOf(track.getId());
                    db.delete("Favourites", "id = ?", new String[] {trackID});
               }
            */
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadDataFromDB();
    }

    public void loadDataFromDB(){
        ArrayList favourites = getArrayFromDB(this.db);
        favAdapter = new FavouritesAdapter(this,favourites);

        ListView favList = findViewById(R.id.favsList);
        favList.setAdapter(favAdapter);
        favAdapter.notifyDataSetChanged();
    }

    public ArrayList<Track> getArrayFromDB(SQLiteDatabase db){
        Cursor cursor = db.rawQuery("select * from Favourites",
                null);

        ArrayList<Track> favourites = new ArrayList<>();

        while(cursor.moveToNext()){
            int id = cursor.getInt(0);
            String name = cursor.getString(1);
            int duration = cursor.getInt(2);
            int artist_id = cursor.getInt(3);
            String artist_name = cursor.getString(4);
            String album_name = cursor.getString(5);
            int album_id = cursor.getInt(6);
            String album_image = cursor.getString(7);
            String audio_url = cursor.getString(8);

            Track track = new Track(id, name, duration, artist_id, artist_name, album_name, album_id, album_image, audio_url);
            favourites.add(track);
        }
        cursor.close();

        return favourites;
    }
}
