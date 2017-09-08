package com.letgo.moviedbapp.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by DaniRosas on 6/9/17.
 */

public class SimilarModel implements Parcelable {
    private int id;
    private String posterPath, title, releaseDate, mediaType;

    public SimilarModel() {
    }

    public SimilarModel(Parcel in) {
        id = in.readInt();
        posterPath = in.readString();
        title = in.readString();
        releaseDate = in.readString();
        mediaType = in.readString();
    }

    public static final Creator<SimilarModel> CREATOR = new Creator<SimilarModel>() {
        @Override
        public SimilarModel createFromParcel(Parcel in) {
            return new SimilarModel(in);
        }

        @Override
        public SimilarModel[] newArray(int size) {
            return new SimilarModel[size];
        }
    };

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
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
        parcel.writeString(posterPath);
        parcel.writeString(title);
        parcel.writeString(releaseDate);
        parcel.writeString(mediaType);
    }
}
