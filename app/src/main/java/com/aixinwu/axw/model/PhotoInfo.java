package com.aixinwu.axw.model;

/**
 * Created by lionel on 2017/11/17.
 */

public class PhotoInfo {

    public String name;
    public String path;

    public PhotoInfo() {
    }

    public PhotoInfo(String path, String name) {
        this.path = path;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
