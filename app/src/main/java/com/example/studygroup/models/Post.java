package com.example.studygroup.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

import org.parceler.Parcel;

import java.util.List;

@Parcel(analyze={Post.class})
@ParseClassName("Post")
public class Post extends ParseObject {
    public static final String KEY_TEXT = "text";
    public static final String KEY_FILES = "files";
    public static final String KEY_CREATOR = "creator";
    public static final String KEY_EDITED = "editedEvent";

    public String getText() {
        return getString(KEY_TEXT);
    }

    public void setText(String text) {
        put(KEY_TEXT, text);
    }

    public List<FileExtended> getFiles() {
        return getList(KEY_FILES);
    }

    public void setFiles(List<FileExtended> files) {
        put(KEY_FILES, files);
    }

    public ParseUser getCreator() {
        return getParseUser(KEY_CREATOR);
    }

    public void setCreator(ParseUser user) {
        put(KEY_CREATOR, user);
    }

    public boolean getEdited() {
        return getBoolean(KEY_EDITED);
    }

    public void setEdited(boolean edited) {
        put(KEY_EDITED, edited);
    }
}
