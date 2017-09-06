package ru.obrazcenter.teplolux;

class WallValues {
    int glassArea;
    float length;
    boolean isOutside;
    int[] thicknesses;
    String[] materials;

    int lNumb; // only for saving

    boolean err;
    class Errors {
        boolean length;
        boolean zeroLength;
        boolean[] zeros /* if width.length == 0 */, materials, thicknesses;
        boolean tooBigGlassArea;
    }
    Errors errors;
}