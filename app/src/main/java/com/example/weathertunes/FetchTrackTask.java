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
import java.util.ArrayList;
import java.util.Random;

public class FetchTrackTask extends AsyncTask<String, Void, Track> {

    private String fuzzytags;
    private OnTaskCompleted listener;
    private static final String API_KEY = BuildConfig.TRACK_SUPER_KEY;


    public FetchTrackTask(String fuzzytags, OnTaskCompleted listener) {
        this.fuzzytags = fuzzytags;
        this.listener = listener;
    }

    public interface OnTaskCompleted {
        void onTrackFetchCompleted(Track track);
    }

    private Track getTrackDataFromJson(String trackJsonStr)
            throws JSONException {

        JSONObject trackJson = new JSONObject(trackJsonStr);
        JSONArray trackArray = trackJson.getJSONArray("results");
        JSONObject aTrack = trackArray.getJSONObject(0);

        int id = Integer.parseInt(aTrack.getString("id"));
        String name = aTrack.getString("name");
        int duration = Integer.parseInt(aTrack.getString("duration"));
        int artist_id = Integer.parseInt(aTrack.getString("artist_id"));
        String artist_name = aTrack.getString("artist_name");
        String album_name = aTrack.getString("album_name");
        int album_id = Integer.parseInt(aTrack.getString("album_id"));
        String image = aTrack.getString("image");
        String audio_url = aTrack.getString("audio");

        Track track = new Track(id, name, duration, artist_id, artist_name, album_name, album_id, image, audio_url);

        return track;
    }

    @Override
    protected Track doInBackground(String... params) {

        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String trackJsonStr = null;
        String trackFormat = "json";

        try {
            // Construct the URL for the Jamendo query
            // Possible parameters are avaiable at Jamendo's track API page, at
            // https://developer.jamendo.com/v3.0/tracks
            final String baseUrl = "https://api.jamendo.com/v3.0/tracks/?";
            //"client_id=63258834&format=json&limit=1&fuzzytags=groove+rock";
            final String apiKeyParam = "client_id";
            final String formatParam = "format";
            final String offsetParam = "offset";
            final String limitParam = "limit";
            final String fuzzytagsParam = "fuzzytags";

            Uri builtUri = Uri.parse(baseUrl).buildUpon()
                    .appendQueryParameter(apiKeyParam, API_KEY)
                    .appendQueryParameter(formatParam, "json")
                    .appendQueryParameter(offsetParam, String.valueOf(new Random().nextInt(20)))
                    .appendQueryParameter(limitParam, "1")
                    .appendQueryParameter(fuzzytagsParam, fuzzytags)
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
                // Nothing to do.
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
            trackJsonStr = buffer.toString();
            Log.v("MYR", "Track JSON String: " + trackJsonStr);
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

            return getTrackDataFromJson(trackJsonStr);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Track result) {
        if (result != null) Log.d("MYR", result.toString());
        listener.onTrackFetchCompleted(result);
    }

}
