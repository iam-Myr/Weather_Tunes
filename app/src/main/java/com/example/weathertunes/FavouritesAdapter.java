package com.example.weathertunes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;


import androidx.annotation.NonNull;

import java.util.ArrayList;

public class FavouritesAdapter extends ArrayAdapter<Track> {

    Context context;
    ArrayList<Track> favourites;


    public FavouritesAdapter(Context context, ArrayList<Track> objects) {
        super(context, 0, objects);
        this.favourites = objects;
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

       View rowView = convertView;

        Track track = favourites.get(position);

        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        rowView = inflater.inflate(R.layout.list_item_favourite, null);


        TextView nameView = rowView.findViewById(R.id.nameTxt);
        TextView artistView = rowView.findViewById(R.id.artistTxt);
        TextView albumView = rowView.findViewById(R.id.albumTxt);

        nameView.setText(track.getName());
        artistView.setText("Artist: " + track.getArtist_name());
        albumView.setText("Album: " + track.getAlbum_name());

        return  rowView;

    }

}