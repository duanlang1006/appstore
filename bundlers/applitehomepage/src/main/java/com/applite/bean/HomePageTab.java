package com.applite.bean;

/**
 * Created by zhimin.yu on 6/3/15.
 */

public class HomePageTab {
    private int id;
    private String s_name;
    private String s_key;

    @Override
    public String toString() {
        return "HomePageTab{" +
                "id=" + id +
                ", s_key='" + s_key + '\'' +
                ", s_name='" + s_name + '\'' +
                '}';
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    public String getKey() {
        return s_key;
    }

    public void setKey(String s_key) {
        this.s_key = s_key;
    }

    public String getName() {
        return s_name;
    }

    public void setName(String s_name) {
        this.s_name = s_name;
    }
}
