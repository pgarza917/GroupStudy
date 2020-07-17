package com.example.studygroup.models;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;

@ParseClassName("File")
public class FileExtended extends ParseObject {
    public static final String KEY_FILE_NAME = "fileName";
    public static final String KEY_FILE_SIZE = "fileSize";
    public static final String KEY_FILE_DATA = "fileData";

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
}
