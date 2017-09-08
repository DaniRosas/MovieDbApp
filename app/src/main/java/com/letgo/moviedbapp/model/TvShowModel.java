package com.letgo.moviedbapp.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Comparator;

/**
 * Created by DaniRosas on 6/9/17.
 */

public class TvShowModel implements Comparator<TvShowModel>, Parcelable {

    private int id;
    private String title, releaseDate, posterPath, character, departmentAndJob, mediaType;


    public TvShowModel() {
    }

    protected TvShowModel(Parcel in) {
        id = in.readInt();
        title = in.readString();
        releaseDate = in.readString();
        posterPath = in.readString();
        character = in.readString();
        departmentAndJob = in.readString();
        mediaType = in.readString();
    }

    public static final Creator<TvShowModel> CREATOR = new Creator<TvShowModel>() {
        @Override
        public TvShowModel createFromParcel(Parcel in) {
            return new TvShowModel(in);
        }

        @Override
        public TvShowModel[] newArray(int size) {
            return new TvShowModel[size];
        }
    };

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }

    public String getCharacter() {
        return character;
    }

    public void setCharacter(String character) {
        this.character = character;
    }

    public String getDepartmentAndJob() {
        return departmentAndJob;
    }

    public void setDepartmentAndJob(String departmentAndJob) {
        this.departmentAndJob = departmentAndJob;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(title);
        parcel.writeString(releaseDate);
        parcel.writeString(posterPath);
        parcel.writeString(character);
        parcel.writeString(departmentAndJob);
        parcel.writeString(mediaType);
    }

    @Override
    public int compare(TvShowModel tvShow1, TvShowModel tvShow2) {
        String year1, year2;
        int compareYear1, compareYear2;

        try {
            year1 = tvShow1.getReleaseDate();
        } catch (java.lang.NullPointerException e) {
            year1 = "0";
        }

        try {
            year2 = tvShow2.getReleaseDate();
        } catch (java.lang.NullPointerException e) {
            year2 = "0";
        }


        try {
            compareYear1 = Integer.parseInt(year1);
        } catch (java.lang.NumberFormatException e) {
            compareYear1 = 0;
        }


        try {
            compareYear2 = Integer.parseInt(year2);
        } catch (java.lang.NumberFormatException e) {
            compareYear2 = 0;
        }

        if (compareYear1 == compareYear2)
            return 0;

        if (compareYear1 < compareYear2)
            return 1;
        else return -1;
    }
}
