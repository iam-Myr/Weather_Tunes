package com.example.weathertunes;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;

import static com.example.weathertunes.MainActivity.mediaPlayer;

public class FavouritesActivity extends AppCompatActivity implements FavouritesAdapter.AdapterCallback {

    private SQLiteDatabase db;
    private FavouritesAdapter favAdapter;
    private ArrayList<Track> favourites;
    private ListView favList;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourites);

        db = getBaseContext().openOrCreateDatabase(
                "favourites-db.db",
                Context.MODE_PRIVATE,
                null);

        intent = getIntent();

        TextView favouritesTxt = findViewById(R.id.favouritesTxt);
        //String weather = intent.getStringExtra("weather");
        //weatherTxt.setText("Current weather: " + weather);

        favList = findViewById(R.id.favsList);
        favList.setClickable(true);
        favList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Track track = favourites.get(position);
                Log.d("MYR", "Song is: " + track.toString());
                Intent request = new Intent(FavouritesActivity.this,MainActivity.class);
                request.putExtra("url",track.getAudio_url());
                setResult(RESULT_OK, request ); // set your result
                finish(); // return back to MainActivity

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        this.favourites = getArrayFromDB(this.db);
        loadDataFromArrayL(favourites);
        Log.d("MYR", "I got: "+intent.getStringExtra("album_img"));
    }

    public void loadDataFromArrayL(ArrayList<Track> list){

        favAdapter = new FavouritesAdapter(this,list, this);


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

    @Override
    public void onDelFromDBCallback(View v, ArrayList favourites) {

        View parentRow = (View) v.getParent();
        if (favAdapter.getCount() != 0) {//list isn't empty

            //find position of button pressed
            ListView listView = (ListView) parentRow.getParent();
            final int position = listView.getPositionForView(parentRow);
            Track track = (Track)favourites.get(position);
            String trackID = String.valueOf(track.getId());

            //delete from DB and ArrayList
            db.delete("Favourites", "id" + "=" + trackID, null);
            this.favourites.remove(position);

            //Update UI - Note: Could have read DB again to update UI
            loadDataFromArrayL(this.favourites);
        }
    }
}
