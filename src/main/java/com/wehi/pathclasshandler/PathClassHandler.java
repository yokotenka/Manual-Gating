package com.wehi.pathclasshandler;

/**
 * A class for applying changes to the PathClass of cells in the image
 */
public class PathClassHandler {

    public static PathClassHandler pathClassHandler;

    public static PathClassHandler getInstance(){
        if (pathClassHandler ==null){
            pathClassHandler = new PathClassHandler();
        }
        return pathClassHandler;
    }


}
