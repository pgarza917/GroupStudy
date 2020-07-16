package com.example.studygroup.models;

import com.parse.ParseClassName;
import com.parse.ParseUser;

@ParseClassName("User")
public class User extends ParseUser {

    public static final String KEY_BIO = "bio";
    public static final String KEY_PROFILE_IMAGE = "profileImage";


}
