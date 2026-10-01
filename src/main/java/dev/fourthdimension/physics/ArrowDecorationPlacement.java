package dev.fourthdimension.physics;

import org.joml.Matrix4f;

/** Adapt separately installed decoration to the native main/upstop wheel rows. */
public final class ArrowDecorationPlacement {
  private ArrowDecorationPlacement() {}

  public static final float TIE_SCALE_X = .85f;
  public static final float VERTICAL_OFFSET = -.6f;

  public static Matrix4f spine(Matrix4f frame, float step) {
    return frame.translate(0, VERTICAL_OFFSET, 0).scale(1, 1, step);
  }

  public static Matrix4f tie(Matrix4f frame) {
    // Scale about the provider's separate X=.5/Z=.25 origin, then place the tie.
    return frame
        .translate(0, VERTICAL_OFFSET, 0)
        .scale(TIE_SCALE_X, 1, 1)
        .translate(-.5f, 0, -.25f);
  }
}
