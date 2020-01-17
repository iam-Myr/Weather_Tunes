package com.example.weathertunes;

import android.net.Uri;
import android.os.AsyncTask;
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

public class FetchWeatherTask extends AsyncTask<String, Void, String[]> {

    private String longitude;
    private String latitude;
    private OnTaskCompleted listener;

    public FetchWeatherTask(double longitude, double latitude, OnTaskCompleted listener) {
        this.longitude = String.valueOf(longitude);
        this.latitude = String.valueOf(latitude);
        this.listener = listener;
    }

    public interface OnTaskCompleted {
        void onWeatherFetchCompleted(String [] result);
    }

    private String[] getWeatherDataFromJson(String weatherJsonStr)
            throws JSONException {

        String[] weather = new String[2];

        JSONObject weatherJson = new JSONObject(weatherJsonStr);
        String city = weatherJson.getString("name");
        weather[0] = city;

        JSONArray articlesJson = weatherJson.getJSONArray("weather");
        JSONObject weatherDescJson = articlesJson.getJSONObject(0);
        String mainWeather = weatherDescJson.getString("main");
        weather[1] = mainWeather;

        return weather;
    }

    @Override
    protected String[] doInBackground(String... params) {

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
            Log.v("MYR", "Forecast JSON String: " + forecastJsonStr);
        } catch (IOException e) {
            Log.e("MYR", "Error ", e);
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
                    Log.e("MYR", "Error closing stream", e);
                }
            }
        }
        try {
            return getWeatherDataFromJson(forecastJsonStr);
        } catch (JSONException e) {
            Log.e("MYR", e.getMessage(), e);
            e.printStackTrace();
        }

        // This will only happen if there was an error getting or parsing the forecast.
        return null;
    }

    @Override
    protected void onPostExecute(String[] result) {
        if (result != null) Log.d("MYR", "The weather is: " + result[0] + " - " + result[1]);
        listener.onWeatherFetchCompleted(result);
    }
}