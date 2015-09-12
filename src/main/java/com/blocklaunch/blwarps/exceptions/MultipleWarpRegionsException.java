package com.blocklaunch.blwarps.exceptions;

import com.flowpowered.math.vector.Vector3d;

public class MultipleWarpRegionsException extends Exception {

    private static final long serialVersionUID = 1L;

    public MultipleWarpRegionsException(Vector3d location) {
        super("Multiple warp regions were found at: " + location.toString());
    }
}
