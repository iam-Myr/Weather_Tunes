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
import android.widget.ImageView;
import android.widget.TextView;


import androidx.annotation.NonNull;

import com.squareup.picasso.Picasso;

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

        ViewHolder viewHolder;

        if(convertView == null){
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item_favourite, null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder)convertView.getTag();
        }






        viewHolder.delBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("MYR", "DEL WAS PRESSED");
                final View fview = view;
                new AlertDialog.Builder(context, R.style.AlertDialogCustom)
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

        Track track = favourites.get(position);

        viewHolder.nameTxt.setText(track.getName());
        viewHolder.artistTxt.setText("Artist: " + track.getArtist_name());
        viewHolder.albumTxt.setText("Album: " + track.getAlbum_name());
        Picasso.with(context).load(track.getImage()).into(viewHolder.albumImg);

        return  convertView;
    }

    private class ViewHolder {
        final TextView nameTxt;
        final TextView artistTxt;
        final TextView albumTxt;
        final ImageView albumImg;
        final Button delBtn;

        ViewHolder(View view){
            nameTxt = view.findViewById(R.id.nameTxt);
            artistTxt = view.findViewById(R.id.artistTxt);
            albumTxt = view.findViewById(R.id.albumTxt);
            albumImg = view.findViewById(R.id.albumImg);
            delBtn = view.findViewById(R.id.delBtn);
        }
    }

    public interface AdapterCallback {
        void onDelFromDBCallback(View v, ArrayList favs);
    }


}