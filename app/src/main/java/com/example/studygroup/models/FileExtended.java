package com.example.studygroup.models;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;

import org.parceler.Parcel;

import java.util.Date;


@Parcel(analyze={FileExtended.class})
@ParseClassName("File")
public class FileExtended extends ParseObject {
    public static final String KEY_FILE_NAME = "fileName";
    public static final String KEY_FILE_SIZE = "fileSize";
    public static final String KEY_FILE_DATA = "fileData";
    public static final String KEY_MODIFIED = "lastModified";

    public String getFileName() {
        return getString(KEY_FILE_NAME);
    }

    public void setFileName(String fileName) {
        put(KEY_FILE_NAME, fileName);
    }

    public long getFileSize() {
        return getLong(KEY_FILE_SIZE);
    }

    public void setFileSize(long fileSize) {
        put(KEY_FILE_SIZE, fileSize);
    }

    public ParseFile getFile() {
        return getParseFile(KEY_FILE_DATA);
    }

    public void setFile(ParseFile file) {
        put(KEY_FILE_DATA, file);
    }

    public Date getLastModified() {
        return getDate(KEY_MODIFIED);
    }

    public void setLastModified(Date modified) {
        put(KEY_MODIFIED, modified);
    }
}
