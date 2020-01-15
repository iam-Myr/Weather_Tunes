package com.example.weathertunes;

import android.net.Uri;
import android.os.AsyncTask;
import android.text.format.Time;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class FetchWeatherTask extends AsyncTask<String, Void, String> {

    private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();
    private String longitude;
    private String latitude;

    public FetchWeatherTask(double longitude, double latitude) {
        this.longitude = String.valueOf(longitude);
        this.latitude = String.valueOf(latitude);
    }

    private String getWeatherDataFromJson(String weatherJsonStr)
            throws JSONException {

        JSONObject weatherJson = new JSONObject(weatherJsonStr);
        String city = weatherJson.getString("name");

        JSONArray articlesJson = weatherJson.getJSONArray("weather");
        JSONObject weatherDescJson = articlesJson.getJSONObject(0);
        String mainWeather = weatherDescJson.getString("main");

        String resultString = (city + " - " + mainWeather);

        return resultString;
    }

    @Override
    protected String doInBackground(String... params) {

        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String forecastJsonStr = null;
        String weatherFormat = "json";
        int numDays = 1;
        String units = "metric";

        try {
            // Construct the URL for the OpenWeatherMap query
            // Possible parameters are avaiable at OWM's forecast API page, at
            // http://openweathermap.org/API#forecast
            //MODIFIED FOR CITY OF THESSALONIKI, GREECE
            final String baseUrl = "https://api.openweathermap.org/data/2.5/weather?";
            //"lat=35&lon=139";
            final String latitudeParam = "lat";
            final String longitudeParam = "lon";
            final String apiKeyParam = "APPID";

            Uri builtUri = Uri.parse(baseUrl).buildUpon()
                    .appendQueryParameter(latitudeParam, latitude)
                    .appendQueryParameter(longitudeParam,longitude)
                    .appendQueryParameter(apiKeyParam, "040416fa50c8a477e42af9c9f7b6bf5f")
                    .build();

            URL url = new URL(builtUri.toString());

            Log.v("MYR", "Built URI: " + builtUri.toString());

            // Create the request to OpenWeatherMap, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                Log.e("MYR", "Input was null");
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }
            forecastJsonStr = buffer.toString();
            Log.v(LOG_TAG, "Forecast JSON String: " + forecastJsonStr);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the weather data, there's no point in attemping
            // to parse it.
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }
        try {
            return getWeatherDataFromJson(forecastJsonStr);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }

        // This will only happen if there was an error getting or parsing the forecast.
        return null;
    }

    @Override
    protected void onPostExecute(String result) {

    }
}