package com.example.weathertunes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;


public class WeatherFragment extends Fragment {

    private static final String TAG = "WeatherFragment";

    private String weatherString = "Current Weather";

    public WeatherFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_weather, container, false);

        TextView textWeather = rootView.findViewById(R.id.txtWeather);
        textWeather.setText(weatherString);
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FetchWeatherTask weatherTask = new FetchWeatherTask(this);
        weatherTask.execute();
    }

    public void setWeatherString(String weatherString) {
        TextView textWeather = getActivity().findViewById(R.id.txtWeather);
        textWeather.setText(weatherString);
    }
}