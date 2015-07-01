package com.applite.bean;

import java.util.List;
/**
 * Created by yuzhimin on 6/30/15.
 */
public class HomePageDataBean {
    private int app_key;
    private int rubricddisplay;
    private List<SubjectData> subject_data;


    public int getAppKey() {
        return app_key;
    }
    public void setAppKey(int appKey) {
        this.app_key = appKey;
    }

    public int getRubricddisplay() {
        return rubricddisplay;
    }
    public void setRubricddisplay(int mRubricddisplay) {
        this.rubricddisplay = mRubricddisplay;
    }

    public List<SubjectData> getSubjectData() {
        return subject_data;
    }
    public void setSubjectData(List<SubjectData> mSubData) {
        this.subject_data = mSubData;
    }

    @Override
    public String toString()
    {
        return "HomePageDataBean {" + '\'' +
               ", app_key=" + app_key + '\'' +
               ", rubricddisplay=" + rubricddisplay + '\'' +
               ", subject_data=" + subject_data + '\'' +
               "}";
    }
}
