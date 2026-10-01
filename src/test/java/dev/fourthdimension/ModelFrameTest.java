package dev.fourthdimension;

import static org.junit.jupiter.api.Assertions.*;

import dev.fourthdimension.physics.ArrowDecorationPlacement;
import dev.fourthdimension.physics.ModelFrame;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

class ModelFrameTest {
  @Test
  void providerOriginsRemainCenteredAndOffsetsFollowBankedLocalUp() {
    for (int sign : new int[] {-1, 1})
      for (double bank : new double[] {0, Math.PI / 6, Math.PI / 2, Math.PI}) {
        var up = new Vector3d(Math.sin(bank), Math.cos(bank), 0);
        var frame = ModelFrame.at(12, 34, 56, new Vector3d(0, 0, sign), up);
        var tie = ArrowDecorationPlacement.tie(new Matrix4f(frame));
        var spine = ArrowDecorationPlacement.spine(new Matrix4f(frame), 2);
        var expected = frame.transformPosition(new Vector3f(0, -.6f, 0));
        assertEquals(0, expected.distance(tie.transformPosition(new Vector3f(.5f, 0, .25f))), 1e-5);
        assertEquals(0, expected.distance(spine.transformPosition(new Vector3f())), 1e-5);
        assertEquals(.85, tie.transformDirection(new Vector3f(1, 0, 0)).length(), 1e-6);
        assertEquals(2, spine.transformDirection(new Vector3f(0, 0, 1)).length(), 1e-6);
        assertTrue(tie.determinant() > 0);
        assertTrue(spine.determinant() > 0);
      }
  }

  @Test
  void reversedAndBankedFramesKeepExteriorWindingAndTheirUpDirection() {
    for (int sign : new int[] {-1, 1})
      for (double bank : new double[] {0, Math.PI / 2, Math.PI}) {
        var up = new Vector3d(Math.sin(bank), Math.cos(bank), 0);
        var matrix = ModelFrame.at(12, 34, 56, new Vector3d(0, 0, sign * 2), up);
        assertEquals(1, matrix.determinant(), 1e-6);
        assertEquals(12, matrix.transformPosition(new Vector3f()).x, 1e-6);
        var modelUp = matrix.transformDirection(new Vector3f(0, 1, 0));
        assertEquals(up.x, modelUp.x, 1e-6);
        assertEquals(up.y, modelUp.y, 1e-6);
        assertEquals(sign, matrix.transformDirection(new Vector3f(0, 0, 1)).z, 1e-6);
        assertTrue(matrix.scale(1, 1, .5f).determinant() > 0);
      }
  }

  @Test
  void degenerateFramesFailBeforeSuppressingFallbackGeometry() {
    assertThrows(
        IllegalArgumentException.class,
        () -> ModelFrame.at(0, 0, 0, new Vector3d(), new Vector3d(0, 1, 0)));
    assertThrows(
        IllegalArgumentException.class,
        () -> ModelFrame.at(0, 0, 0, new Vector3d(0, 1, 0), new Vector3d(0, 1, 0)));
    assertThrows(
        IllegalArgumentException.class,
        () -> ModelFrame.at(Double.NaN, 0, 0, new Vector3d(0, 0, 1), new Vector3d(0, 1, 0)));
  }
}
