package com.example.weathertunes;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
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
    private AdapterCallback mAdapterCallback;

    public FavouritesAdapter(Context context, ArrayList<Track> objects, AdapterCallback callback) {
        super(context, 0, objects);
        this.favourites = objects;
        this.context = context;
        this.mAdapterCallback = callback;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View rowView = convertView;

        Track track = favourites.get(position);

        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        rowView = inflater.inflate(R.layout.list_item_favourite, null);


        Button delBtn = (Button) rowView.findViewById(R.id.delBtn);
        delBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("MYR", "DEL WAS PRESSED");
                final View fview = view;
                new AlertDialog.Builder(context)
                        .setTitle("Confirmation")
                        .setMessage("Are you sure you want to delete?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                mAdapterCallback.onDelFromDBCallback(fview, favourites);
                            }})
                        .setNegativeButton(android.R.string.no, null).show();
            }
        });

        TextView nameView = rowView.findViewById(R.id.nameTxt);
        TextView artistView = rowView.findViewById(R.id.artistTxt);
        TextView albumView = rowView.findViewById(R.id.albumTxt);

        nameView.setText(track.getName());
        artistView.setText("Artist: " + track.getArtist_name());
        albumView.setText("Album: " + track.getAlbum_name());

        return  rowView;

    }

    public interface AdapterCallback {
        void onDelFromDBCallback(View v, ArrayList favs);
    }




}