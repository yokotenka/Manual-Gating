package com.wehi;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import qupath.lib.images.servers.ImageServer;
import qupath.lib.objects.PathObject;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class ChannelInformationExtraction {

    /**
     * Method to extract the markers
     */
    public static ObservableList<String> extractMarkers(ImageServer<BufferedImage> imageServer){
        ObservableList<String> markers = FXCollections.observableArrayList();
        for (int i=0; i < imageServer.nChannels(); i++){
            markers.add(imageServer.getChannel(i).getName());
        }
        return markers;
    }

    /**
     * Method to extract the measurement names
     */
    public static ObservableList<String> extractMarkerMeasurements(Collection<PathObject> cells, ImageServer<BufferedImage> imageServer) {

        // Do something for when no cell detected
        if (cells == null) {
            return null;
        }
        // Gets a cell
        PathObject cell = (PathObject) cells.toArray()[0];
        String markerName = imageServer.getChannel(0).getName();
        List<String> measurementList = cell.getMeasurementList().getMeasurementNames();

        // Potentially could be a source of error #################################################
        return FXCollections.observableList(measurementList.stream()
                .parallel()
                .filter(it -> it.contains(markerName + ":"))
                .map(it -> it.substring(markerName.length() + 2))
                .collect(Collectors.toList()));
    }
}
