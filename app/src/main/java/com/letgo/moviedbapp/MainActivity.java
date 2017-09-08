package com.letgo.moviedbapp;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.letgo.moviedbapp.controller.TvShowDetails;
import com.letgo.moviedbapp.controller.TvShowList;
import com.letgo.moviedbapp.entities.MovieDB;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.utils.StorageUtils;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {


    // used to store app title
    private CharSequence mTitle;

    //used to know if it's a phone or tablet
    private boolean phone;
    private ArrayList<Bundle> tvDetailsBundle;
    private int oldPos;
    private ImageLoader imageLoader;
    private DisplayImageOptions optionsWithFade, optionsWithoutFade;
    private DisplayImageOptions backdropOptionsWithoutFade;
    private DateFormat dateFormat;
    private DisplayImageOptions backdropOptionsWithFade;
    private boolean restoreMovieDetailsAdapterState;
    private boolean restoreMovieDetailsState;
    private int lastVisitedSimMovie;
    private int lastVisitedSimTV;
    private boolean saveInMovieDetailsSimFragment;
    private TvShowDetails movieDetailsSimFragment;
    private TvShowDetails tvDetailsSimFragment;
    private TvShowDetails tvDetailsFragment;
    private boolean saveInTvShowDetailsSimFragment;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTitle  = getTitle();

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle("Tv Shows");

            setSupportActionBar(toolbar);

            toolbar.bringToFront();
        }

        phone = getResources().getBoolean(R.bool.portrait_only);

        // Check orientation and lock to portrait if we are on phone
            if (phone) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }

            // on first time display view for first nav item
            displayView(1);


            // Universal Loader options and configuration.
            DisplayImageOptions options = new DisplayImageOptions.Builder()
                    // Bitmaps in RGB_565 consume 2 times less memory than in ARGB_8888.
                    .bitmapConfig(Bitmap.Config.RGB_565)
                    .imageScaleType(ImageScaleType.EXACTLY)
                    .cacheInMemory(false)
                    .showImageOnLoading(R.mipmap.ic_launcher)
                    .showImageForEmptyUri(R.mipmap.ic_launcher)
                    .showImageOnFail(R.mipmap.ic_launcher)
                    .cacheOnDisk(true)
                    .build();
            Context context = this;
            File cacheDir = StorageUtils.getCacheDirectory(context);
            // Create global configuration and initialize ImageLoader with this config
            ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                    .diskCache(new UnlimitedDiscCache(cacheDir)) // default
                    .defaultDisplayImageOptions(options)
                    .build();
            ImageLoader.getInstance().init(config);


        // Get reference for the imageLoader
        imageLoader = ImageLoader.getInstance();

        // Options used for the backdrop image in movie and tv details and gallery
        optionsWithFade = new DisplayImageOptions.Builder()
                // Bitmaps in RGB_565 consume 2 times less memory than in ARGB_8888.
                .bitmapConfig(Bitmap.Config.RGB_565)
                .displayer(new FadeInBitmapDisplayer(500))
                .imageScaleType(ImageScaleType.EXACTLY)
                .cacheInMemory(false)
                .showImageOnLoading(Color.BLACK)
                .showImageForEmptyUri(Color.BLACK)
                .showImageOnFail(Color.BLACK)
                .cacheOnDisk(true)
                .build();
        optionsWithoutFade = new DisplayImageOptions.Builder()
                // Bitmaps in RGB_565 consume 2 times less memory than in ARGB_8888.
                .bitmapConfig(Bitmap.Config.RGB_565)
                .imageScaleType(ImageScaleType.EXACTLY)
                .cacheInMemory(false)
                .showImageOnLoading(Color.BLACK)
                .showImageForEmptyUri(Color.BLACK)
                .showImageOnFail(Color.BLACK)
                .cacheOnDisk(true)
                .build();

        // Options used for the backdrop image in movie and tv details and gallery

        backdropOptionsWithoutFade = new DisplayImageOptions.Builder()
                // Bitmaps in RGB_565 consume 2 times less memory than in ARGB_8888.
                .bitmapConfig(Bitmap.Config.RGB_565)
                .imageScaleType(ImageScaleType.EXACTLY)
                .cacheInMemory(false)
                .showImageOnLoading(R.mipmap.ic_launcher)
                .showImageForEmptyUri(R.mipmap.ic_launcher)
                .showImageOnFail(R.mipmap.ic_launcher)
                .cacheOnDisk(true)
                .build();

        dateFormat = android.text.format.DateFormat.getDateFormat(this);

    }




    private void displayView(int position) {
        if (position != 0) {
            // Clear history of the back stack if any
            FragmentManager fm = getFragmentManager();
            fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            // update the main content by replacing fragments
            Fragment fragment = null;

            resetTvDetailsBundle();


            fragment = new TvShowList();


            fm.beginTransaction()
                    .replace(R.id.frame_container, fragment)
                    .commit();


        }
    }

      /* This method is used in MovieDetails, CastDetails and TVDetails.
            *
            * @param layout the layout which we hide.
            */
    public void hideLayout(final ViewGroup layout) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                layout.setVisibility(View.GONE);
            }
        });
    }

    /**
     * This method is used in MovieDetails, CastDetails and TVDetails.
     *
     * @param view the view which we hide.
     */
    public void hideView(final View view) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                view.setVisibility(View.GONE);
            }
        });
    }

    /**
     * This method is used in MovieDetails, CastDetails and TVDetails.
     *
     * @param view the view which we show.
     */
    public void showView(final View view) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                view.setVisibility(View.VISIBLE);
            }
        });
    }

    /**
     * This method is used in MovieDetails, CastDetails and TVDetails.
     *
     * @param textView the TextView which we hide.
     */
    public void hideTextView(final TextView textView) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView.setVisibility(View.GONE);
            }
        });
    }

    /**
     * This method is used in MovieDetails, CastDetails and TVDetails.
     *
     * @param ratingBar the RatingBar which we hide.
     */
    public void hideRatingBar(final RatingBar ratingBar) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ratingBar.setVisibility(View.GONE);
            }
        });
    }

    /**
     * This method is used in MovieDetails, CastDetails and TVDetails.
     * Makes a view invisible.
     *
     * @param view the View which we make invisible.
     */
    public void invisibleView(final View view) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                view.setVisibility(View.INVISIBLE);
            }
        });
    }


    /**
     * Method to reset TV Details ArrayList (back navigation)
     */
    public void resetTvDetailsBundle() {
        tvDetailsBundle = new ArrayList<>();
    }

    /**
     * This method is used in MovieDetails, CastDetails and TVDetails.
     * runOnUiThread() is called because we can't update it from async task.
     *
     * @param ratingBar the ratingBar we set value.
     * @param value     the value we will set on the ratingBar.
     */
    public void setRatingBarValue(final RatingBar ratingBar, final float value) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ratingBar.setRating(value);
            }
        });
    }

    public DateFormat getDateFormat() {
        return dateFormat;
    }


    /**
     * This method is used in TVShowDetails.
     * We update the text value of a TextView, from runOnUiThread() because we can't update it from async task.
     *
     * @param text  the TextView to update.
     * @param value the new text value.
     */
    public void setText(final TextView text, final String value) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                text.setText(value);
            }
        });
    }

    /**
     * This method is used in MovieDetails, CastDetails and TVDetails.
     * We update the text value of a TextView, from runOnUiThread() because we can't update it from async task.
     *
     * @param text  the TextView to update.
     * @param value the new text value.
     */
    public void setTextFromHtml(final TextView text, final String value) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                text.setText(Html.fromHtml(value));
            }
        });
    }

    /**
     * This method is used in MovieDetails, CastDetails and TVDetails.
     * We use our imageLoader to display image on the given image view.
     * runOnUiThread() is called because we can't update it from async task.
     *
     * @param img the ImageView we display image on.
     * @param url the url to set tag.
     *            R.string.backDropImgSize defines the size of our backDrop images.
     *            If we load the image for first time we show fade effect, else no effect.
     */
    public void setBackDropImage(final ImageView img, final String url) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (imageLoader.getDiskCache().get(MovieDB.imageUrl + getResources().getString(R.string.backDropImgSize) + url).exists())
                    imageLoader.displayImage(MovieDB.imageUrl + getResources().getString(R.string.backDropImgSize) + url, img, backdropOptionsWithoutFade);
                else
                    imageLoader.displayImage(MovieDB.imageUrl + getResources().getString(R.string.backDropImgSize) + url, img, backdropOptionsWithFade);
            }
        });
    }

    /**
     * This method is used in TVShowDetails.
     * We use our imageLoader to display image on the given image view.
     * runOnUiThread() is called because we can't update it from async task.
     *
     * @param img the ImageView we display image on.
     * @param url the url from which we display the image.
     *            R.string.imageSize defines the size of our images.
     */
    public void setImage(final ImageView img, final String url) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                imageLoader.displayImage(MovieDB.imageUrl + getResources().getString(R.string.imageSize) + url, img);
            }
        });
    }

    /**
     * This method is used in TVShowDetails.
     * We set url tag on the imageView. So when we tap later on it we known which url to load.
     * runOnUiThread() is called because we can't update it from async task.
     *
     * @param img the ImageView we display image on.
     * @param url the url to set tag.
     *            R.string.imageSize defines the size of our images.
     */
    public void setImageTag(final ImageView img, final String url) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                img.setTag(url);
            }
        });
    }

    /**
     * Set this to true if we should restore our Movie Details savedState when we press back button.
     */
    public void setRestoreMovieDetailsState(boolean restoreMovieDetailsState) {
        this.restoreMovieDetailsState = restoreMovieDetailsState;
    }

    /**
     * true if we should restore our Movie Details savedState when we press back button.
     */
    public boolean getRestoreMovieDetailsState() {
        return restoreMovieDetailsState;
    }

    /**
     * Set this to true if we should restore our Movie Details Adapter savedState when we press back.
     */
    public void setRestoreMovieDetailsAdapterState(boolean restoreMovieDetailsAdapterState) {
        this.restoreMovieDetailsAdapterState = restoreMovieDetailsAdapterState;
    }

    /**
     * true if we should restore our Movie Details Adapter savedState when we press back button.
     */
    public boolean getRestoreMovieDetailsAdapterState() {
        return restoreMovieDetailsAdapterState;
    }

    public int getLastVisitedSimMovie() {
        return lastVisitedSimMovie;
    }

    public void setLastVisitedSimMovie(int lastVisitedSimMovie) {
        this.lastVisitedSimMovie = lastVisitedSimMovie;
    }

    public int getLastVisitedSimTV() {
        return lastVisitedSimTV;
    }

    public void setLastVisitedSimTV(int lastVisitedSimTV) {
        this.lastVisitedSimTV = lastVisitedSimTV;
    }

    public boolean getSaveInMovieDetailsSimFragment() {
        return saveInMovieDetailsSimFragment;
    }

    public void setSaveInMovieDetailsSimFragment(boolean saveInMovieDetailsSimFragment) {
        this.saveInMovieDetailsSimFragment = saveInMovieDetailsSimFragment;
    }

    public TvShowDetails getMovieDetailsSimFragment() {
        return movieDetailsSimFragment;
    }

    public void setMovieDetailsSimFragment(TvShowDetails movieDetailsSimFragment) {
        this.movieDetailsSimFragment = movieDetailsSimFragment;
    }
    public TvShowDetails getTvDetailsSimFragment() {
        return tvDetailsSimFragment;
    }

    public void setTvDetailsSimFragment(TvShowDetails tvDetailsSimFragment) {
        this.tvDetailsSimFragment = tvDetailsSimFragment;
    }

    /**
     * Method which returns our TVDetails Fragment.
     */
    public TvShowDetails getTvDetailsFragment() {
        return tvDetailsFragment;
    }

    /**
     * Method which sets our TVDetails Fragment.
     */
    public void setTvDetailsFragment(TvShowDetails tvDetailsFragment) {
        this.tvDetailsFragment = tvDetailsFragment;
    }

    /**
     * Method which gets TV Details savedState from our ArrayList.
     * We use it for our back navigation.
     */
    public ArrayList<Bundle> getTvDetailsBundle() {
        return tvDetailsBundle;
    }

    public void setSaveInTVDetailsSimFragment(boolean saveInTVDetailsSimFragment) {
        this.saveInTvShowDetailsSimFragment = saveInTVDetailsSimFragment;
    }


    /**
     * Method which adds TV Details savedState to our ArrayList.
     * We use it for our back navigation.
     */
    public void addTvDetailsBundle(Bundle tvDetailsBundle) {
        this.tvDetailsBundle.add(tvDetailsBundle);
    }

    /**
     * Method which removes TV Details savedState to our ArrayList.
     * We use it for our back navigation.
     */
    public void removeTvDetailsBundle(int pos) {
        tvDetailsBundle.remove(pos);
    }

    public boolean getSaveInTVDetailsSimFragment() {
        return saveInTvShowDetailsSimFragment;
    }

    public void showBackNavigation(boolean enabled){
        if(enabled) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }else{
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setDisplayShowHomeEnabled(false);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {

        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(false);

        displayView(1);

        return super.onSupportNavigateUp();

    }
}


