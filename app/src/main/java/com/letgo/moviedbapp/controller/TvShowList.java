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
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.letgo.moviedbapp.MainActivity;
import com.letgo.moviedbapp.R;
import com.letgo.moviedbapp.adapter.TvShowAdapter;
import com.letgo.moviedbapp.entities.MovieDB;
import com.letgo.moviedbapp.helper.Scrollable;
import com.letgo.moviedbapp.model.TvShowModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * A simple {@link Fragment} subclass.
 */
public class TvShowList extends Fragment implements AdapterView.OnItemClickListener{


    private MainActivity activity;
    private ProgressBar spinner;
    private boolean phone;
    private float scale;
    private int minThreshold;
    private View view;
    private Bundle save;
    private AbsListView listView;
    private HttpURLConnection conn;
    private int totalPages;
    private ArrayList<TvShowModel> tvShowList;
    private TvShowAdapter tvShowAdapter;
    private int checkLoadMore;
    private EndlessScrollListener endlessScrollListener;
    private String currentList;
    private boolean isLoading;
    private Toast toastLoadingMore;
    private int backState;
    private String title;
    private TvShowDetails tvShowDetails;
    private int lastVisitedTV;
    private DateFormat dateFormat;

    public TvShowList() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_tv_show_list, container, false);


        activity = ((MainActivity) getActivity());
        spinner = (ProgressBar) view.findViewById(R.id.progressBar);
        phone = getResources().getBoolean(R.bool.portrait_only);
        scale = getResources().getDisplayMetrics().density;
        toastLoadingMore = Toast.makeText(activity, R.string.loadingMore, Toast.LENGTH_SHORT);



        if (phone)
            minThreshold = (int) (-49 * scale);
        else
            minThreshold = (int) (-42 * scale);

        return view;
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
        if (savedInstanceState != null)
            save = savedInstanceState.getBundle("save");
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        listView = view.findViewById(R.id.listTvShowsTopRated);
        currentList = "top_rated";

        dateFormat = android.text.format.DateFormat.getDateFormat(this.getActivity().getApplicationContext());


        if (listView != null) {

            getActivity().setTitle(getTitle());
            listView.setOnItemClickListener(this);

            updateList();


        }
    }

    /**
     * Fired when list is empty and we should update it.
     */
    public void updateList() {
        if (listView != null) {
            tvShowList = new ArrayList<>();
            tvShowAdapter = new TvShowAdapter(getActivity().getApplicationContext(),R.layout.row,tvShowList);
            listView.setAdapter(tvShowAdapter);
            endlessScrollListener = new EndlessScrollListener();
            listView.setOnScrollListener(endlessScrollListener);
            checkLoadMore = 0;
            final JSONAsyncTask request = new JSONAsyncTask();
            new Thread(new Runnable() {
                public void run() {
                    try {
                        if (!isLoading)
                            request.execute(MovieDB.url + getCurrentList() + "?&api_key=" + MovieDB.key).get(10000, TimeUnit.MILLISECONDS);
                    } catch (TimeoutException | ExecutionException | InterruptedException e) {
                        request.cancel(true);
                        toastLoadingMore.cancel();
                        if (spinner != null)
                            activity.hideView(spinner);
                        // we abort the http request, else it will cause problems and slow connection later
                        if (conn != null)
                            conn.disconnect();
                        isLoading = false;
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(getActivity(), getResources().getString(R.string.timeout), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                }
            }).start();
        }
    }

    public String getCurrentList() {
        return currentList;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

        activity.resetTvDetailsBundle();
        activity.showBackNavigation(true);
        if (tvShowDetails != null) {
            // Old movie details retrieve info and re-init component else crash
            tvShowDetails.onSaveInstanceState(new Bundle());
            Bundle bundle = new Bundle();
            bundle.putInt("id", tvShowList.get(position).getId());
            tvShowDetails = new TvShowDetails();

            tvShowDetails.setArguments(bundle);
        } else tvShowDetails = new TvShowDetails();

        lastVisitedTV = tvShowList.get(position).getId();
        tvShowDetails.setTitle(tvShowList.get(position).getTitle());

        activity.setTitle(tvShowList.get(position).getTitle());

        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        Bundle bundle = new Bundle();
        bundle.putInt("id", tvShowList.get(position).getId());
        tvShowDetails.setArguments(bundle);
        transaction.replace(R.id.frame_container, tvShowDetails);
        // add the current transaction to the back stack:
        transaction.addToBackStack("TVList");
        transaction.commit();
    }


    public class EndlessScrollListener implements AbsListView.OnScrollListener {

        private int currentPage = 1;
        private boolean loading = false;
        private int oldCount = 0;

        public EndlessScrollListener() {
        }


        @Override
        public void onScroll(AbsListView view, int firstVisibleItem,
                             int visibleItemCount, int totalItemCount) {

            if (oldCount != totalItemCount && firstVisibleItem + visibleItemCount >= totalItemCount) {
                loading = true;
                oldCount = totalItemCount;
            }
            if (loading) {
                if (currentPage != totalPages) {
                    currentPage++;
                    checkLoadMore = 1;
                    loading = false;
                    final JSONAsyncTask request = new JSONAsyncTask();
                    new Thread(new Runnable() {
                        public void run() {
                            try {
                                request.execute(MovieDB.url + getCurrentList() + "?&api_key=" + MovieDB.key + "&page=" + currentPage).get(10000, TimeUnit.MILLISECONDS);
                            } catch (TimeoutException | ExecutionException | InterruptedException e) {
                                request.cancel(true);
                                // we abort the http request, else it will cause problems and slow connection later
                                if (conn != null)
                                    conn.disconnect();
                                toastLoadingMore.cancel();
                                currentPage--;
                                loading = true;
                                if (getActivity() != null) {
                                    getActivity().runOnUiThread(new Runnable() {
                                        public void run() {
                                            Toast.makeText(getActivity(), getResources().getString(R.string.timeout), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }
                        }
                    }).start();
                } else {
                    if (totalPages != 1) {
                        Toast.makeText(getActivity(), R.string.nomoreresults, Toast.LENGTH_SHORT).show();
                    }
                    loading = false;

                }
            }


        }


        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
        }

        public int getCurrentPage() {
            return currentPage;
        }

        public void setCurrentPage(int currentPage) {
            this.currentPage = currentPage;
        }

        public int getOldCount() {
            return oldCount;
        }

        public void setOldCount(int oldCount) {
            this.oldCount = oldCount;
        }

        public boolean getLoading() {
            return loading;
        }

        public void setLoading(boolean loading) {
            this.loading = loading;
        }
    }



    /**
     * This class handles the connection to our backend server.
     * If the connection is successful we set our list data.
     */
    class JSONAsyncTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (checkLoadMore == 0) {
                activity.showView(spinner);
                isLoading = true;
            } else {
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        toastLoadingMore.show();
                    }
                });
            }

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

                    JSONObject movieData = new JSONObject(sb.toString());
                    totalPages = movieData.getInt("total_pages");
                    JSONArray movieArray = movieData.getJSONArray("results");

                    // is added checks if we are still on the same view, if we don't do this check the program will crash
                    if (isAdded()) {
                        for (int i = 0; i < movieArray.length(); i++) {
                            JSONObject object = movieArray.getJSONObject(i);

                            TvShowModel tvShowModel = new TvShowModel();
                            tvShowModel.setId(object.getInt("id"));
                            tvShowModel.setTitle(object.getString("name"));
                            if (!object.getString("first_air_date").equals("null") && !object.getString("first_air_date").isEmpty()) {
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy", Locale.ENGLISH);

                                Date date = null;
                                try {
                                    date = sdf.parse(object.getString("first_air_date"));
                                } catch (java.text.ParseException e) {
                                    e.printStackTrace();
                                }
                                String formattedDate = dateFormat.format(date);
                                String releaseDate = formattedDate ;
                                tvShowModel.setReleaseDate(releaseDate);
                            }


                            if (!object.getString("poster_path").equals("null") && !object.getString("poster_path").isEmpty())
                                tvShowModel.setPosterPath(MovieDB.imageUrl + getResources().getString(R.string.imageSize) + object.getString("poster_path"));


                            tvShowList.add(tvShowModel);
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

        @Override
        protected void onPostExecute(Boolean result) {
            // is added checks if we are still on the same view, if we don't do this check the program will cra
            if (isAdded()) {
                if (checkLoadMore == 0) {
                    activity.hideView(spinner);
                    isLoading = false;
                }

                if (!result) {
                    Toast.makeText(getActivity(), R.string.noConnection, Toast.LENGTH_LONG).show();
                    backState = 0;
                } else {
                    tvShowAdapter.notifyDataSetChanged();
                    getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            toastLoadingMore.cancel();
                        }
                    });
                    final View toolbarView = activity.findViewById(R.id.toolbar);
                    listView.post(new Runnable() {
                        @Override
                        public void run() {
                            if (toolbarView.getTranslationY() == -toolbarView.getHeight() && ((Scrollable) listView).getCurrentScrollY() < minThreshold) {
                                if (phone)
                                    listView.smoothScrollBy((int) (56 * scale), 0);
                                else
                                    listView.smoothScrollBy((int) (59 * scale), 0);
                            }
                        }
                    });
                    backState = 1;
                    save = null;
                }
            }
        }

    }
}
