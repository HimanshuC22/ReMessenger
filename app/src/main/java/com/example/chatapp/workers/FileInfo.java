package com.example.chatapp.workers;

import java.io.File;

public class FileInfo {
    File file;
    Integer size;
    Integer loc;
    Integer format;
    long actual_size;

    public long getActual_size() {
        return actual_size;
    }

    public FileInfo(File file, Integer size, Integer loc, Integer format, long actual_size) {
        this.file = file;
        this.size = size;
        this.loc = loc;
        this.format = format;
        this.actual_size = actual_size;
    }

    public File getFile() {
        return file;
    }

    public Integer getSize() {
        return size;
    }

    public Integer getLoc() {
        return loc;
    }

    public Integer getFormat() {
        return format;
    }

    public String Info() {
        return String.valueOf(this.size) + this.loc + this.format;
    }
}
