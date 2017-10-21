package com.nvidia.developer.opengl.utils;

/**
 * Created by mazhen'gui on 2017/10/20.
 */

public class AttribBinder {
    private String name;
    private int location;

    public AttribBinder(){}

    public AttribBinder(String name, int location) {
        this.name = name;
        this.location = location;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getLocation() {
        return location;
    }

    public void setLocation(int location) {
        this.location = location;
    }
}
