package com.wehi;

import org.controlsfx.control.action.Action;
import qupath.lib.gui.ActionTools;
import qupath.lib.gui.QuPathGUI;
import qupath.lib.gui.extensions.QuPathExtension;
import qupath.lib.gui.tools.MenuTools;

public class ManualGatingExtension implements QuPathExtension {
    @Override
    public void installExtension(QuPathGUI quPathGUI) {
        Action manualGatingWindow = ActionTools.createAction(new ManualGatingWindow(quPathGUI), "Manual Gating");

        MenuTools.addMenuItems(
                quPathGUI.getMenu("Extensions>Manual Gating", true),
                manualGatingWindow);

        Action functionalMarkerThresholdWindow = ActionTools.createAction(new FunctionalMarkerThresholdWindow(quPathGUI), "Functional Marker Threshold");
    }

    @Override
    public String getName() {
        return "Manual Gating Extension";
    }

    @Override
    public String getDescription() {
        return "Manual Gating for multiplexed data";
    }
}
