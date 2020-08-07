package com.example.studygroup.models;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

import org.parceler.Parcel;

import java.util.List;

@Parcel(analyze={Group.class})
@ParseClassName("Group")
public class Group extends ParseObject {
    public static final String KEY_GROUP_NAME = "groupName";
    public static final String KEY_GROUP_USERS = "users";
    public static final String KEY_GROUP_PRIVACY = "privacy";
    public static final String KEY_GROUP_FILES = "files";
    public static final String KEY_GROUP_EVENTS = "events";
    public static final String KEY_NUMBER_USERS = "numberOfUsers";
    public static final String KEY_GROUP_PICTURE = "groupImage";
    public static final String KEY_GROUP_DESCRIPTION = "description";
    public boolean isCurrentUserMember = false;

    public String getGroupName() {
        return getString(KEY_GROUP_NAME);
    }

    public void setGroupName(String name) {
        put(KEY_GROUP_NAME, name);
    }

    public List<ParseUser> getGroupUsers() {
        return getList(KEY_GROUP_USERS);
    }

    public String getGroupPrivacy() {
        return getString(KEY_GROUP_PRIVACY);
    }

    public void setGroupPrivacy(String privacy) {
        put(KEY_GROUP_PRIVACY, privacy);
    }

    public List<FileExtended> getGroupFiles() {
        return getList(KEY_GROUP_FILES);
    }

    public List<Event> getGroupEvents() {
        return getList(KEY_GROUP_EVENTS);
    }

    public int getNumberOfUsers() {
        return getInt(KEY_NUMBER_USERS);
    }

    public void setNumberOfUsers(int numberOfUsers) {
        put(KEY_NUMBER_USERS, numberOfUsers);
    }

    public ParseFile getGroupImage() {
        return getParseFile(KEY_GROUP_PICTURE);
    }

    public void setGroupImage(ParseFile image) {
        put(KEY_GROUP_PICTURE, image);
    }

    public boolean isCurrentUserMember() {
        return isCurrentUserMember;
    }

    public void setCurrentUserMember(boolean currentUserMember) {
        isCurrentUserMember = currentUserMember;
    }

    public String getGroupDescription() {
        return getString(KEY_GROUP_DESCRIPTION);
    }

    public void setGroupDescription(String description) {
        put(KEY_GROUP_DESCRIPTION, description);
    }
}
