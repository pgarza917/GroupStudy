package com.example.studygroup.models;

import com.parse.Parse;
import com.parse.ParseClassName;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseUser;

import org.parceler.Parcel;

import java.util.Date;
import java.util.List;

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
    public static final String KEY_FILES = "files";
    public static final String KEY_USERS = "users";
    public static final String KEY_PRIVACY = "privacy";
    public static final String KEY_SUBJECT = "subject";
    public boolean suggestion = false;

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

    public void setFiles(List<FileExtended> files) {
        put(KEY_FILES, files);
    }

    public List<FileExtended> getFiles() {
        return (List<FileExtended>) get(KEY_FILES);
    }

    public void setOwners(List<ParseUser> owners) {
        put(KEY_OWNERS, owners);
    }

    public List<ParseUser> getOwners() {
        return (List<ParseUser>) get(KEY_OWNERS);
    }

    public void setUsers(List<ParseUser> users) {
        put(KEY_USERS, users);
    }

    public List<ParseUser> getUsers() {
        return (List<ParseUser>) get(KEY_USERS);
    }

    public void setPrivacy(int setting) {
        if (setting == 0) {
            put(KEY_PRIVACY, "open");
        } else {
            put(KEY_PRIVACY, "closed");
        }
    }

    public String getPrivacy() { return getString(KEY_PRIVACY); }

    public void setSuggestion(boolean state) {
        suggestion = state;
    }

    public boolean getSuggestion() {
        return suggestion;
    }

}
