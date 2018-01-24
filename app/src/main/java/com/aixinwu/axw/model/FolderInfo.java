package com.aixinwu.axw.model;

import java.util.List;

/**
 * Created by lionel on 2017/11/17.
 */

public class FolderInfo {

    public String name;
    public String path;
    public List<PhotoInfo> photoInfoList;

    public FolderInfo() {
    }

    public FolderInfo(String name, String path) {
        this.name = name;
        this.path = path;
    }

    public FolderInfo(String name, String path, List<PhotoInfo> photoInfoList) {
        this.name = name;
        this.path = path;
        this.photoInfoList = photoInfoList;
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

    public List<PhotoInfo> getPhotoInfoList() {
        return photoInfoList;
    }

    public void setPhotoInfoList(List<PhotoInfo> photoInfoList) {
        this.photoInfoList = photoInfoList;
    }


}
