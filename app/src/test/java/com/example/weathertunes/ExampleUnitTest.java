package com.example.weathertunes;

import android.net.Uri;
import android.util.Log;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    protected String doInBackground(String... params) {

        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String trackJsonStr = null;
        String trackFormat = "json";
        String units = "metric";

        http://api.jamendo.com/v3.0/tracks/?client_id=63258834&format=json&limit=1&fuzzytags=groove+rock

        try {
            // Construct the URL for the Jamendo query
            // Possible parameters are avaiable at Jamendo's track API page, at
            // https://developer.jamendo.com/v3.0/tracks
            final String baseUrl = "http://api.jamendo.com/v3.0/tracks/?";
            //"client_id=63258834&format=json&limit=1&fuzzytags=groove+rock";
            final String apiKeyParam = "client_id";
            final String formatParam = "format";
            final String limitParam = "limit";
            final String fuzzytagsParam = "fuzzytags";


            Uri builtUri = Uri.parse(baseUrl).buildUpon()
                    .appendQueryParameter(apiKeyParam, "734077") //code for thessaloniki
                    .appendQueryParameter(formatParam, "json")
                    .appendQueryParameter(limitParam, "1")
                    .appendQueryParameter(fuzzytagsParam, "groove+rock")
                    .build();

            URL url = new URL(builtUri.toString());

            Log.v("MYR", "Built URI: " + builtUri.toString());

            // Create the request to OpenWeatherMap, and open the connection

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

        // This will only happen if there was an error getting or parsing the forecast.
        return null;
    }


}
}
