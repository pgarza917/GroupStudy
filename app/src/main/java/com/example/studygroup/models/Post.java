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
    public static final String KEY_EVENT = "event";
    public static final String KEY_GROUP = "group";

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

    public Event getEvent() {
        return (Event) getParseObject(KEY_EVENT);
    }

    public void setEvent(Event event) {
        put(KEY_EVENT, event);
    }

    public Group getGroup() {
        return (Group) getParseObject(KEY_GROUP);
    }

    public void setGroup(Group group) {
        put(KEY_GROUP, group);
    }
}
