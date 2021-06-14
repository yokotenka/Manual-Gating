package com.wehi.observer;

public interface Observer {
    void update(String str1, String str2, String str3, String str4);
    void updateXThreshold(double threshold);
    void updateYThreshold(double threshold);
}
