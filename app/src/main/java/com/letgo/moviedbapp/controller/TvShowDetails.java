package com.letgo.moviedbapp.controller;


import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.net.ParseException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.letgo.moviedbapp.MainActivity;
import com.letgo.moviedbapp.R;
import com.letgo.moviedbapp.adapter.SimilarAdapter;
import com.letgo.moviedbapp.entities.MovieDB;
import com.letgo.moviedbapp.model.SimilarModel;
import com.letgo.moviedbapp.view.ObservableParallaxScrollView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * A simple {@link Fragment} subclass.
 */
public class TvShowDetails extends Fragment implements AdapterView.OnItemClickListener{

    private View rootView;
    private MainActivity activity;
    private ImageView backDropPath;
    private int backDropCheck;
    private TextView titleTextview;
    private ImageView posterPath;
    private TextView statusText;
    private TextView typeText;
    private TextView episodeRuntime;
    private TextView numberOfEpisodesText;
    private TextView numberOfSeasonsText;
    private TextView firstAirDateText;
    private TextView lastAirDateText;
    private TextView genres;
    private TextView countries;
    private TextView companies;
    private RatingBar ratingBar;
    private TextView voteCount;
    private ObservableParallaxScrollView scrollView;
    private GridView tvDetailsSimilarGrid;
    private ArrayList<SimilarModel> similarList;
    private View similarHolder;
    private TvShowDetails tvDetails;
    private HttpURLConnection conn;
    private String homeIconUrl, title;
    private int homeIconCheck;
    private float scale;
    private boolean phone;
    private Bundle save;
    private int currentId;
    private int timeOut;
    //private ProgressBar spinner;
    private JSONAsyncTask request;
    private TextView overview;

    public TvShowDetails() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        rootView = inflater.inflate(R.layout.fragment_tv_show_details, container, false);
        activity = ((MainActivity) getActivity());
        backDropPath = (ImageView) rootView.findViewById(R.id.backDropPath);
        titleTextview = (TextView) rootView.findViewById(R.id.title);
        posterPath = (ImageView) rootView.findViewById(R.id.posterPath);
        statusText = (TextView) rootView.findViewById(R.id.status);
        typeText = (TextView) rootView.findViewById(R.id.type);
        episodeRuntime = (TextView) rootView.findViewById(R.id.episodeRuntime);
        numberOfEpisodesText = (TextView) rootView.findViewById(R.id.numberOfEpisodes);
        numberOfSeasonsText = (TextView) rootView.findViewById(R.id.numberOfSeasons);
        firstAirDateText = (TextView) rootView.findViewById(R.id.firstAirDate);
        lastAirDateText = (TextView) rootView.findViewById(R.id.lastAirDate);
        genres = (TextView) rootView.findViewById(R.id.genres);
        countries = (TextView) rootView.findViewById(R.id.countries);
        companies = (TextView) rootView.findViewById(R.id.companies);
        ratingBar = (RatingBar) rootView.findViewById(R.id.ratingBar);
        voteCount = (TextView) rootView.findViewById(R.id.voteCount);
        overview = rootView.findViewById(R.id.overView);
        phone = getResources().getBoolean(R.bool.portrait_only);
        scale = getResources().getDisplayMetrics().density;

        scrollView = (ObservableParallaxScrollView) rootView.findViewById(R.id.tvdetailsinfo);

        tvDetailsSimilarGrid = (GridView) rootView.findViewById(R.id.tvDetailsSimilarGrid);
        similarHolder = rootView.findViewById(R.id.similarHolder);



        return rootView;
    }



    /**
     * Called to do initial creation of a fragment.
     * This is called after onAttach(Activity) and before onCreateView(LayoutInflater, ViewGroup, Bundle).
     *
     * @param savedInstanceState If the fragment is being re-created from a previous saved state, this is the state.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (activity.getTvDetailsBundle().size() > 0 && activity.getRestoreMovieDetailsState()) {
            save = activity.getTvDetailsBundle().get(activity.getTvDetailsBundle().size() - 1);
            activity.removeTvDetailsBundle(activity.getTvDetailsBundle().size() - 1);
           
            activity.setRestoreMovieDetailsState(false);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (save != null) {
            setTitle(save.getString("title"));
            currentId = save.getInt("currentId");
            timeOut = save.getInt("timeOut");
            if (timeOut == 0) {
                //spinner.setVisibility(View.GONE);
                onOrientationChange(save);
            }
        }


        if (currentId != this.getArguments().getInt("id") || this.timeOut == 1) {
            currentId = this.getArguments().getInt("id");
            //spinner.setVisibility(View.VISIBLE);

            request = new JSONAsyncTask();
            new Thread(new Runnable() {
                public void run() {
                    try {
                        request.execute(MovieDB.url + currentId + "?append_to_response=images,credits,similar&api_key=" + MovieDB.key).get(10000, TimeUnit.MILLISECONDS);
                    } catch (TimeoutException | ExecutionException | InterruptedException | CancellationException e) {
                        request.cancel(true);
                        // we abort the http request, else it will cause problems and slow connection later
                        if (conn != null)
                            conn.disconnect();
                       /* if (spinner != null)
                            activity.hideView(spinner);
*/                        if (getActivity() != null && !(e instanceof CancellationException)) {
                            getActivity().runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(getActivity(), getResources().getString(R.string.timeout), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                        setTimeOut(1);
                    }
                }
            }).start();
        }
        activity.setTitle(getTitle());
        activity.setTvDetailsFragment(this);
        if (activity.getSaveInTVDetailsSimFragment()) {
            activity.setSaveInTVDetailsSimFragment(false);
            activity.setTvDetailsSimFragment(this);
        }
    }

    public TextView getOverview() {
        return overview;
    }


    class JSONAsyncTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);
                conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000 /* milliseconds */);
                conn.setConnectTimeout(10000 /* milliseconds */);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.connect();

                int status = conn.getResponseCode();

                if (status == 200) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line).append("\n");
                    }
                    br.close();

                    JSONObject jsonData = new JSONObject(sb.toString());


                    if (isAdded()) {
                        // Backdrop path
                        if (!jsonData.getString("backdrop_path").equals("null") && !jsonData.getString("backdrop_path").isEmpty()) {
                            activity.setBackDropImage(getBackDropPath(), jsonData.getString("backdrop_path"));
                            activity.setImageTag(getBackDropPath(), jsonData.getString("backdrop_path"));
                        } else if (!jsonData.getString("poster_path").equals("null") && !jsonData.getString("poster_path").isEmpty()) {
                            activity.setBackDropImage(getBackDropPath(), jsonData.getString("poster_path"));
                            activity.setImageTag(getBackDropPath(), jsonData.getString("poster_path"));
                        } else
                            setBackDropCheck(1);

                        // Title
                        activity.setText(getTitleTextView(), jsonData.getString("name"));


                        // Poster path
                        if (!jsonData.getString("poster_path").equals("null") && !jsonData.getString("poster_path").isEmpty()) {
                            activity.setImage(getPosterPath(), jsonData.getString("poster_path"));
                            activity.setImageTag(getPosterPath(), jsonData.getString("poster_path"));
                        }


                        // Status
                        if (!jsonData.getString("status").equals("null") && !jsonData.getString("status").isEmpty())
                            activity.setText(getStatusText(), jsonData.getString("status"));
                        else
                            activity.hideTextView(getStatusText());

                        // Type
                        if (!jsonData.getString("type").equals("null") && !jsonData.getString("type").isEmpty())
                            activity.setText(getTypeText(), getResources().getString(R.string.type) + " " + jsonData.getString("type"));
                        else
                            activity.hideTextView(getTypeText());


                        // First air date
                        if (!jsonData.getString("first_air_date").equals("null") && !jsonData.getString("first_air_date").isEmpty()) {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                            try {
                                Date date = sdf.parse(jsonData.getString("first_air_date"));
                                String formattedDate = activity.getDateFormat().format(date);
                                activity.setText(getFirstAirDateText(), getResources().getString(R.string.firstAirDate) + " " + formattedDate);
                            } catch (java.text.ParseException e) {
                                activity.hideTextView(getFirstAirDateText());
                            }
                        } else
                            activity.hideTextView(getFirstAirDateText());


                        // Homepage icon
                        if (!jsonData.getString("homepage").isEmpty() && !jsonData.getString("homepage").equals("null")) {
                            homeIconUrl = jsonData.getString("homepage");
                            homeIconCheck = 0;
                        } else {
                            homeIconCheck = 1;
                        }

                        // Genres
                        JSONArray genresArray = jsonData.getJSONArray("genres");
                        String genresData = "";
                        for (int i = 0; i < genresArray.length(); i++) {
                            if (i + 1 == genresArray.length())
                                genresData += genresArray.getJSONObject(i).get("name");
                            else
                                genresData += genresArray.getJSONObject(i).get("name") + ", ";
                        }

                        if (genresData.isEmpty())
                            activity.hideTextView(getGenres());
                        else {
                            activity.setText(getGenres(), genresData);
                        }

                        // Origin countries
                        JSONArray countriesArray = jsonData.getJSONArray("origin_country");
                        String countriesData = "";
                        for (int i = 0; i < countriesArray.length(); i++) {
                            if (i + 1 == countriesArray.length())
                                countriesData += countriesArray.getString(i);
                            else
                                countriesData += countriesArray.getString(i) + "\n";
                        }

                        if (countriesData.isEmpty())
                            activity.hideTextView(getCountries());
                        else {
                            activity.setText(getCountries(), countriesData);
                        }

                        // Production companies
                        JSONArray companiesArray = jsonData.getJSONArray("production_companies");
                        String companiesData = "";
                        for (int i = 0; i < companiesArray.length(); i++) {
                            if (i + 1 == companiesArray.length())
                                companiesData += companiesArray.getJSONObject(i).get("name");
                            else
                                companiesData += companiesArray.getJSONObject(i).get("name") + "\n";
                        }

                        if (companiesData.isEmpty())
                            activity.hideTextView(getCompanies());
                        else {
                            activity.setText(getCompanies(), companiesData);
                            // if countries is empty we need to set the margin on companies
                            if (countriesData.isEmpty()) {
                                activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) getCompanies().getLayoutParams();
                                        lp.setMargins(0, (int) (28 * scale), 0, 0);
                                    }
                                });
                            }
                        }


                        // Rating
                        if (Float.parseFloat(jsonData.getString("vote_average")) == 0.0f) {
                            activity.hideRatingBar(getRatingBar());
                            activity.hideTextView(getVoteCount());
                        } else {
                            activity.setRatingBarValue(getRatingBar(), (Float.parseFloat(jsonData.getString("vote_average")) / 2));
                            activity.setText(getVoteCount(), jsonData.getString("vote_count") + " " + getString(R.string.voteCount));
                        }





                        //Overview
                        final String overview = jsonData.getString("overview");

                        if (!overview.equals("null") && !overview.isEmpty())
                            activity.setText(getOverview(), overview);
                        else
                            activity.setText(getOverview(), getResources().getString(R.string.noOverview));



                        // Similar
                        JSONObject similarObj = jsonData.getJSONObject("similar");
                        JSONArray similarArray = similarObj.getJSONArray("results");
                        int similarLen = similarArray.length();
                        if (similarLen > 6)
                            similarLen = 6;

                        if (similarLen == 0)
                            activity.hideView(getSimilarHolder());
                        else {
                            final ArrayList<SimilarModel> similarList = new ArrayList<>();

                            for (int i = 0; i < similarLen; i++) {
                                JSONObject object = similarArray.getJSONObject(i);

                                SimilarModel similarModel = new SimilarModel();
                                similarModel.setId(object.getInt("id"));
                                similarModel.setTitle(object.getString("name"));
                                if (!object.getString("poster_path").equals("null") && !object.getString("poster_path").isEmpty())
                                    similarModel.setPosterPath(MovieDB.imageUrl + getResources().getString(R.string.imageSize) + object.getString("poster_path"));
                                if (!object.getString("first_air_date").equals("null") && !object.getString("first_air_date").isEmpty())
                                    similarModel.setReleaseDate(object.getString("first_air_date"));

                                similarList.add(similarModel);
                            }

                            activity.runOnUiThread(new Runnable() {
                                public void run() {
                                    if (isAdded())
                                        setSimilarList(similarList);
                                }
                            });
                        }


                        return true;
                    }
                }


            } catch (ParseException | IOException | JSONException e) {
                if (conn != null)
                    conn.disconnect();
            } finally {
                if (conn != null)
                    conn.disconnect();
            }
            return false;
        }
    }

    
    

    public ImageView getBackDropPath() {
        return backDropPath;
    }

    public int getBackDropCheck() {
        return backDropCheck;
    }

    public void setBackDropCheck(int backDropCheck) {
        this.backDropCheck = backDropCheck;
    }

    public TextView getTitleTextView() {
        return titleTextview;
    }

    public ImageView getPosterPath() {
        return posterPath;
    }

    public TextView getStatusText() {
        return statusText;
    }

    public TextView getTypeText() {
        return typeText;
    }

    public TextView getEpisodeRuntime() {
        return episodeRuntime;
    }

    public TextView getNumberOfEpisodesText() {
        return numberOfEpisodesText;
    }

    public TextView getNumberOfSeasonsText() {
        return numberOfSeasonsText;
    }

    public TextView getFirstAirDateText() {
        return firstAirDateText;
    }

    public TextView getLastAirDateText() {
        return lastAirDateText;
    }

    public TextView getGenres() {
        return genres;
    }

    public TextView getCountries() {
        return countries;
    }

    public TextView getCompanies() {
        return companies;
    }

    public RatingBar getRatingBar() {
        return ratingBar;
    }

    public TextView getVoteCount() {
        return voteCount;
    }

    public View getRootView() {
        return rootView;
    }

    /**
     * Fired when are restoring from backState or orientation has changed.
     *
     * @param outState our bundle with saved state. Our parent fragment handles the saving.
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Used to avoid bug where we add item in the back stack
        // and if we change orientation twice the item from the back stack has null values
        if (save != null && save.getInt("timeOut") == 1)
            save = null;
        save = null;

        Bundle send = new Bundle();
        send.putInt("currentId", currentId);
        if (request != null && request.getStatus() == AsyncTask.Status.RUNNING) {
            timeOut = 1;
            request.cancel(true);
        }
        send.putInt("timeOut", timeOut);
        send.putString("title", title);
        if (timeOut == 0) {
            // HomePage
            send.putInt("homeIconCheck", homeIconCheck);
            if (homeIconCheck == 0)
                send.putString("homepage", homeIconUrl);



        }

        // TV details info begins here
            // Backdrop path
            send.putInt("backDropCheck", getBackDropCheck());
            if (getBackDropCheck() == 0 && getBackDropPath().getTag() != null)
                send.putString("backDropUrl", getBackDropPath().getTag().toString());

            // Poster path url
            if (getPosterPath().getTag() != null)
                send.putString("posterPathURL", getPosterPath().getTag().toString());


            // Rating
            send.putFloat("rating", getRatingBar().getRating());
            send.putString("voteCount", getVoteCount().getText().toString());

            // Title
            send.putString("titleText", getTitleTextView().getText().toString());


            // Status
            send.putString("status", getStatusText().getText().toString());

            // Type
            send.putString("typeText", getTypeText().getText().toString());

            // Episode runtime
            send.putString("episodeRuntime", getEpisodeRuntime().getText().toString());

            // Number of episodes
            send.putString("numberOfEpisodesText", getNumberOfEpisodesText().getText().toString());

            // Number of seasons
            send.putString("numberOfSeasonsText", getNumberOfSeasonsText().getText().toString());

            // First air date
            send.putString("firstAirDateText", getFirstAirDateText().getText().toString());

            // Last air date
            send.putString("lastAirDateText", getLastAirDateText().getText().toString());

            // Genres
            send.putString("genres", getGenres().getText().toString());

            // Production countries
            send.putString("productionCountries", getCountries().getText().toString());

            // Production companies
            send.putString("productionCompanies", getCompanies().getText().toString());

            // Similar list
            if (getSimilarList() != null && getSimilarList().size() > 0)
                send.putParcelableArrayList("similarList", getSimilarList());

        

        save = send;
        activity.addTvDetailsBundle(send);


        }

    /**
     * Fired when are restoring from backState or orientation has changed.
     *
     * @param args our bundle with saved state. Our parent fragment handles the saving.
     */
    @SuppressWarnings("ConstantConditions")
    private void onOrientationChange(Bundle args) {
        // BackDrop path
        backDropCheck = args.getInt("backDropCheck");
        if (backDropCheck == 0) {
            activity.setBackDropImage(backDropPath, args.getString("backDropUrl"));
            backDropPath.setTag(args.getString("backDropUrl"));
        }

        // Release date and title
        activity.setTextFromHtml(titleTextview, args.getString("titleText"));

        // Status
        activity.setText(statusText, args.getString("status"));

        // Type
        if (!args.getString("typeText").isEmpty())
            activity.setText(typeText, args.getString("typeText"));
        else activity.hideView(typeText);

        // Episode runtime
        if (!args.getString("episodeRuntime").isEmpty())
            activity.setText(episodeRuntime, args.getString("episodeRuntime"));
        else activity.hideView(episodeRuntime);

        // Number of episodes
        if (!args.getString("numberOfEpisodesText").isEmpty())
            activity.setText(numberOfEpisodesText, args.getString("numberOfEpisodesText"));
        else activity.hideView(numberOfEpisodesText);

        // Number of seasons
        if (!args.getString("numberOfSeasonsText").isEmpty())
            activity.setText(numberOfSeasonsText, args.getString("numberOfSeasonsText"));
        else activity.hideView(numberOfSeasonsText);

        // First air date
        if (!args.getString("firstAirDateText").isEmpty())
            activity.setText(firstAirDateText, args.getString("firstAirDateText"));
        else activity.hideView(firstAirDateText);

        // Last air date
        if (!args.getString("lastAirDateText").isEmpty())
            activity.setText(lastAirDateText, args.getString("lastAirDateText"));
        else activity.hideView(lastAirDateText);

        // Genres
        if (!args.getString("genres").isEmpty())
            activity.setText(genres, args.getString("genres"));
        else activity.hideView(genres);

        // Production Countries
        if (!args.getString("productionCountries").isEmpty())
            activity.setText(countries, args.getString("productionCountries"));
        else activity.hideView(countries);

        // Production Companies
        if (!args.getString("productionCompanies").isEmpty()) {
            activity.setText(companies, args.getString("productionCompanies"));
            if (args.getString("productionCountries").isEmpty()) {
                ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) companies.getLayoutParams();
                lp.setMargins(0, (int) (28 * getResources().getDisplayMetrics().density), 0, 0);
            }
        } else activity.hideView(companies);


        // Poster path
        if (args.getString("posterPathURL") != null) {
            activity.setImage(posterPath, args.getString("posterPathURL"));
            activity.setImageTag(posterPath, args.getString("posterPathURL"));
        }


        // Rating
        if (args.getString("voteCount").isEmpty()) {
            activity.hideRatingBar(ratingBar);
            activity.hideTextView(voteCount);
        } else {
            ratingBar.setRating(args.getFloat("rating"));
            activity.setText(voteCount, args.getString("voteCount"));
        }

        // Similar list
        similarList = args.getParcelableArrayList("similarList");
        if (similarList != null && similarList.size() > 0)
            setSimilarList(similarList);
        else
            activity.hideView(similarHolder);


    }

    /**
     * Fired when fragment is destroyed.
     */
    public void onDestroyView() {
        super.onDestroyView();
        posterPath.setImageDrawable(null);
        backDropPath.setImageDrawable(null);
        tvDetailsSimilarGrid.setAdapter(null);
    }
    /**
     * We use this key to know if the user has tried to open this movie and the connection failed.
     * So if he tries to load again the same movie we know that the connection has failed and we need to make a new request.
     */
    public void setTimeOut(int timeOut) {
        this.timeOut = timeOut;
    }
    public ObservableParallaxScrollView getScrollView() {
        return scrollView;
    }



    public void setSimilarList(ArrayList<SimilarModel> similarList) {
        this.similarList = similarList;
        SimilarAdapter similarAdapter = new SimilarAdapter(getActivity(), R.layout.similar_row, similarList);
        tvDetailsSimilarGrid.setAdapter(similarAdapter);
        tvDetailsSimilarGrid.setOnItemClickListener(this);

        if (similarList.size() < 4) {
            ViewGroup.LayoutParams lp = tvDetailsSimilarGrid.getLayoutParams();
            lp.height /= 2;
        }
    }

    public ArrayList<SimilarModel> getSimilarList() {
        return similarList;
    }

    public View getSimilarHolder() {
        return similarHolder;
    }


    /**
     * Callback method to be invoked when an item in this AdapterView has been clicked.
     *
     * @param parent   The AdapterView where the click happened.
     * @param view     The view within the AdapterView that was clicked (this will be a view provided by the adapter)
     * @param position The position of the view in the adapter.
     * @param id       The row id of the item that was clicked.
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {

        tvDetails = new TvShowDetails();

        activity.setLastVisitedSimTV(similarList.get(position).getId());
        activity.getTvDetailsFragment().onSaveInstanceState(new Bundle());

        activity.setTvDetailsFragment(null);
        tvDetails.setTitle(similarList.get(position).getTitle());
        FragmentManager manager = getActivity().getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        Bundle bundle = new Bundle();
        bundle.putInt("id", similarList.get(position).getId());
        tvDetails.setArguments(bundle);
        transaction.replace(R.id.frame_container, tvDetails);
        // add the current transaction to the back stack:
        transaction.addToBackStack("similarDetails");
        transaction.commit();


    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle(){
        return this.title;
    }

}
