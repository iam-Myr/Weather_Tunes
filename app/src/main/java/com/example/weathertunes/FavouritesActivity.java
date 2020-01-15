package com.example.weathertunes;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

public class FavouritesActivity extends AppCompatActivity {

    SQLiteDatabase db = MainActivity.db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourites);

        Intent intent = getIntent();

        TextView weatherTxt = findViewById(R.id.weatherTxt2);

        String weather = intent.getStringExtra("weather");

        weatherTxt.setText(weather);

    }

    @Override
    protected void onStart() {
        super.onStart();

        readDB();
    }

    private void readDB(){
        Cursor cursor = db.rawQuery("select * from Favourites",
                null);

        StringBuilder sb = new StringBuilder();

        while(cursor.moveToNext()){
            int id = cursor.getInt(0);
            String name = cursor.getString(1);

            String resultRow = "ROW = ID: "+id+" name: "+name;
            sb.append(resultRow).append("\n");
            Log.d("MYR", resultRow);
        }
        cursor.close();

        EditText displayDB = findViewById(R.id.displayDBTxt);

        displayDB.setText(sb.toString());
    }
}
