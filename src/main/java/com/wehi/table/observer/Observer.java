package com.wehi.table.observer;

public interface Observer {
    void update(String str1, String str2, String str3, String str4);
    void update(String str1, String str2);
    void updateXThreshold(double threshold);
    void updateYThreshold(double threshold);
}
