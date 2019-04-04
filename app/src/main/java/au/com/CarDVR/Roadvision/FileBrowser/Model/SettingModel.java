package au.com.CarDVR.Roadvision.FileBrowser.Model;

import java.util.ArrayList;

/**
 * User : LiangXiong(LiangXiong.sz@foxmail.com)
 * Date : 2016-11-03
 * Time : 13:44
 * QQ   : 294894105 ZH
 * About:
 */

public class SettingModel {
    String title;
    String mGet;
    String mSet;
    String mSwitch;
    String mDefault;

    public String getmDefault() {
        return mDefault;
    }

    public void setmDefault(String mDefault) {
        this.mDefault = mDefault;
    }

    @Override
    public String toString() {
        return "SettingModel{" +
                "title='" + title + '\'' +
                ", mGet='" + mGet + '\'' +
                ", mSet='" + mSet + '\'' +
                ", mSwitch='" + mSwitch + '\'' +
                ", titles=" + titles +
                '}';
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getmGet() {
        return mGet;
    }

    public void setmGet(String mGet) {
        this.mGet = mGet;
    }

    public String getmSet() {
        return mSet;
    }

    public void setmSet(String mSet) {
        this.mSet = mSet;
    }

    public String getmSwitch() {
        return mSwitch;
    }

    public void setmSwitch(String mSwitch) {
        this.mSwitch = mSwitch;
    }

    public ArrayList<SettingItemModel> getTitles() {
        return titles;
    }

    public void setTitles(ArrayList<SettingItemModel> titles) {
        this.titles = titles;
    }

    ArrayList<SettingItemModel> titles;


}
