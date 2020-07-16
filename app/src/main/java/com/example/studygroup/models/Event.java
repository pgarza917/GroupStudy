package com.example.studygroup.models;

import com.parse.Parse;
import com.parse.ParseClassName;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;

import org.parceler.Parcel;

import java.util.Date;

@Parcel(analyze={Event.class})
@ParseClassName("Event")
public class Event extends ParseObject {
    public static final String KEY_TITLE = "title";
    public static final String KEY_DESCRIPTION = "description";
    public static final String KEY_TIME = "time";
    public static final String KEY_CREATED_AT = "createdAt";
    public static final String KEY_LOCATION = "location";
    public static final String KEY_OWNERS = "owners";
    public static final String KEY_LOCATION_NAME = "locationName";

    public String getTitle() {
        return getString(KEY_TITLE);
    }

    public void setTitle(String title) {
        put(KEY_TITLE, title);
    }

    public String getDescription() {
        return getString(KEY_DESCRIPTION);
    }

    public void setDescription(String description) {
        put(KEY_DESCRIPTION, description);
    }

    public Date getTime() {
        return (Date) get(KEY_TIME);
    }

    public void setTime(Date time) {
        put(KEY_TIME, time);
    }

    public ParseGeoPoint getLocation() {
        return getParseGeoPoint(KEY_LOCATION);
    }

    public void setLocation(ParseGeoPoint location) {
        put(KEY_LOCATION, location);
    }

    public String getLocationName() {
        return getString(KEY_LOCATION_NAME);
    }

    public void setLocationName(String locationName) {
        put(KEY_LOCATION_NAME, locationName);
    }
}
