package com.wehi.save;

import qupath.lib.classifiers.object.ObjectClassifier;

import java.io.IOException;

// Taken from Pete BankHead
public interface ClassifierWrapper<T> {
    ObjectClassifier<T> getClassifier() throws IOException;
}

