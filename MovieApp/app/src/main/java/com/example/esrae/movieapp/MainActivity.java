package com.example.esrae.movieapp;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    MovieAdapter MovieAdpt;
    GridView gwt;
    List<Movie> Lst;
    int i=0;
    DatabaseHelper myDb;

    String pop="popular";
    String top="top_rated";
    String sel="popular";
    android.app.Fragment first ;
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.most) {
            sel=pop;
            if(isOnline())
            {
                Asy movieTask = new Asy();
                movieTask.execute();
            }
            gwt= (GridView) findViewById(R.id.grid);
            Lst =new ArrayList<>();

            MovieAdpt = new MovieAdapter(Lst,this);
            gwt.setAdapter(MovieAdpt);
            return true;
        }
        else
        if(id == R.id.top){
            sel=top;
            if(isOnline())
            {Asy movieTask = new Asy();

                movieTask.execute();}
            gwt= (GridView) findViewById(R.id.grid);
            Lst =new ArrayList<>();

            MovieAdpt = new MovieAdapter(Lst,this);
            gwt.setAdapter(MovieAdpt);
            return true;
        }
        else
        {
            myDb = new DatabaseHelper(this);
            gwt= (GridView) findViewById(R.id.grid);
            if(istab(MainActivity.this)) {
                frag1.Lst = getData();
                MovieAdpt = new MovieAdapter(frag1.Lst, this);
                gwt.setAdapter(MovieAdpt);
            }
            else {
                Lst = getData();
                MovieAdpt = new MovieAdapter(Lst, this);
                gwt.setAdapter(MovieAdpt);
            }
            return true;
        }
    }
    public ArrayList<Movie> getData(){
        ArrayList<Movie> lst = new ArrayList<>(); ;
        Cursor res = myDb.getAllData();
        while (res.moveToNext()) {
            Movie film =new Movie(
                    res.getString(1),
                    res.getString(3),
                    res.getString(5),
                    res.getString(4),
                    res.getString(2),
                    res.getString(0)
            );
            lst.add(film);
        }
        return lst;
    }

    public void onStart() {
        super.onStart();

        if(isOnline())
        {
            if(istab(MainActivity.this)){
                first=getFragmentManager().findFragmentById(R.id.fg);
            }
            else {
                myDb = new DatabaseHelper(this);
                Lst = new ArrayList<>();
                gwt = (GridView) findViewById(R.id.grid);
                Asy movieTask = new Asy();
                movieTask.execute();
                MovieAdpt = new MovieAdapter(Lst, this);
                gwt.setAdapter(MovieAdpt);
                gwt.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View v,
                                            int position, long id) {
                        Intent intent = new Intent(MainActivity.this, Main2Activity.class);
                        intent.putExtra("Movie", (Serializable) Lst.get(position));
                        startActivity(intent);
                    }
                });
            }

        }
    }
    public static boolean istab(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }


    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }


    class Asy extends AsyncTask<String,Void,List<Movie>> {

        @Override

        protected List<Movie> doInBackground(String... params) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

// Will contain the raw JSON response as a string.
            String Movieapp = null;

            String appId = "e2a4d00b2cd04f026c7700c807660bde";
            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are available at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast

                final String Movie_url = "http://api.themoviedb.org/3/movie/"+sel+"?api_key=";

                Uri builtUri = Uri.parse(Movie_url+appId).buildUpon().build();

                URL url = new URL(builtUri.toString());

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();

                if (inputStream == null) {
                    // Nothing to do.
                    Movieapp = null;
                    Log.e("esraa","ERROR HAHAHAHAHA");
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
                    Movieapp = null;
                }
                Movieapp = buffer.toString();
            } catch (IOException e) {
                Log.e("ForecastFragment", "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attempting
                // to parse it.
                Movieapp = null;
            } finally{
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("ForecastFragment", "Error closing stream", e);
                    }
                }
            }
            try {
                return getMovieDataFromJson(Movieapp);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }


        /**
         * Take the String representing the complete forecast in JSON Format and
         * pull out the data we need to construct the Strings needed for the wireframes.
         *
         * Fortunately parsing is easy:  constructor takes the JSON string and converts it
         * into an Object hierarchy for us.
         */
        private List<Movie> getMovieDataFromJson(String Title)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String Otitle="original_title";
            final String Oview="overview";
            final String Vaver="vote_average";
            final String Ppath="poster_path";
            final String Rdata="release_date";
            final String id="id";

            final String Olanguage="original_language";
            final String pop="popularity";
            final String Vcount="vote_count";

            JSONObject jobject = new JSONObject(Title);
            JSONArray MovieArray = jobject.getJSONArray("results");

            List<Movie> resultStrs = new ArrayList<>();

            for(int i = 0; i < MovieArray.length(); i++) {

                JSONObject Movies = MovieArray.getJSONObject(i);
                Movie film =new Movie(
                        Movies.getString(Otitle),
                        Movies.getString(Vaver),
                        Movies.getString(Ppath),
                        Movies.getString(Rdata),
                        Movies.getString(Oview),
                        Movies.getString(id)
                        );
                resultStrs.add(film);
            }
//Lst=resultStrs;
            return resultStrs;
        }

        @Override
        protected void onPostExecute(List<Movie> strings) {
            if (strings != null){
                if(istab(MainActivity.this))
                    frag1.Lst.clear();
                else
                    Lst.clear();
            }
            if(istab(MainActivity.this))
                frag1.Lst = strings;
            else
                Lst=strings;
            gwt.setAdapter(new MovieAdapter(strings, MainActivity.this));
        }
    }





}
