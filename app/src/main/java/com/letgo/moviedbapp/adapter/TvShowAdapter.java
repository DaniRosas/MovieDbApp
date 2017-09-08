package com.letgo.moviedbapp.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.letgo.moviedbapp.R;
import com.letgo.moviedbapp.model.TvShowModel;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import java.util.ArrayList;

/**
 * Created by DaniRosas on 6/9/17.
 */

public class TvShowAdapter extends ArrayAdapter<TvShowModel> {
    private ArrayList<TvShowModel> moviesList;
    private LayoutInflater vi;
    private int Resource;
    private ViewHolder holder;
    private ImageLoader imageLoader;
    private DisplayImageOptions options;

    public TvShowAdapter(Context context, int resource, ArrayList<TvShowModel> objects) {
        super(context, resource, objects);
        vi = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        Resource = resource;
        moviesList = objects;
        imageLoader = ImageLoader.getInstance();
        options = new DisplayImageOptions.Builder()
                // Bitmaps in RGB_565 consume 2 times less memory than in ARGB_8888. Caching images in memory else
                // flicker while toolbar hiding
                .bitmapConfig(Bitmap.Config.RGB_565)
                .imageScaleType(ImageScaleType.EXACTLY)
                .cacheInMemory(true)
                .showImageOnLoading(R.mipmap.ic_launcher)
                .showImageForEmptyUri(R.mipmap.ic_launcher)
                .showImageOnFail(R.mipmap.ic_launcher)
                .cacheOnDisk(true)
                .build();
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // convert view = design
        View v = convertView;
        if (v == null) {
            holder = new ViewHolder();
            v = vi.inflate(Resource, null);
            holder.title = (TextView) v.findViewById(R.id.title);
            holder.posterPath = (ImageView) v.findViewById(R.id.posterPath);
            holder.character = (TextView) v.findViewById(R.id.character);
            holder.department = (TextView) v.findViewById(R.id.department);
            holder.releaseDate = (TextView) v.findViewById(R.id.releaseDate);
            v.setTag(holder);
        } else {
            holder = (ViewHolder) v.getTag();
        }


        holder.title.setText(moviesList.get(position).getTitle());


        if (moviesList.get(position).getReleaseDate() != null) {
            holder.releaseDate.setText("(" + moviesList.get(position).getReleaseDate() + ")");
            holder.releaseDate.setVisibility(View.VISIBLE);
        } else
            holder.releaseDate.setVisibility(View.GONE);


        if (moviesList.get(position).getCharacter() != null) {
            holder.character.setText(moviesList.get(position).getCharacter());
            holder.character.setVisibility(View.VISIBLE);
        } else
            holder.character.setVisibility(View.GONE);


        if (moviesList.get(position).getDepartmentAndJob() != null) {
            holder.department.setText(moviesList.get(position).getDepartmentAndJob());
            holder.department.setVisibility(View.VISIBLE);
        } else
            holder.department.setVisibility(View.GONE);

        // if getPosterPath returns null imageLoader automatically sets default image
        imageLoader.displayImage(moviesList.get(position).getPosterPath(), holder.posterPath, options);


        return v;

    }

    /**
     * Defines movie list row elements.
     */
    static class ViewHolder {
        public TextView title;
        public ImageView posterPath;
        public TextView character;
        public TextView department;
        public TextView releaseDate;
    }


}
