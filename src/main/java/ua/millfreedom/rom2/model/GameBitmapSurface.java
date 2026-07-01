package ua.millfreedom.rom2.model;

import ua.millfreedom.rom2.model.color.RGB16;

public record GameBitmapSurface(
        int width,
        int height,
        RGB16[] pixels   // RGB16*
) {}

