package com.example.studygroup.models;


import com.parse.ParseClassName;
import com.parse.ParseObject;

import org.parceler.Parcel;

@Parcel(analyze={Subject.class})
@ParseClassName("Subject")
public class Subject extends ParseObject {

    public static final String KEY_SUBJECT_NAME = "subjectName";
    public boolean editMode;

    public String getSubjectName() {
        return getString(KEY_SUBJECT_NAME);
    }

    public boolean isEditMode() {
        return editMode;
    }

    public void setEditMode(boolean editMode) {
        this.editMode = editMode;
    }
}
