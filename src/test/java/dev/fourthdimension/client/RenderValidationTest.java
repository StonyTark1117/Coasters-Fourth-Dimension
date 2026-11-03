package dev.fourthdimension.client;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class RenderValidationTest {
  @Test
  void acceptsRequiredGeometryAfterEmptyLoadingStages() {
    TrackRenderMetrics.reset();
    TrackRenderMetrics.meshBuilt(1);
    TrackRenderMetrics.rendered(1, 2, 8, 0);
    TrackRenderMetrics.rendered(1, 0, 8, 0);
    TrackRenderMetrics.rendered(1, 2, 0, 0);
    var sample = TrackRenderMetrics.snapshot();

    Assertions.assertEquals(3, sample.renderStages());
    Assertions.assertEquals(1, sample.geometryStages());
    Assertions.assertTrue(RenderValidation.hasRequiredOriginalGeometry(sample, 1));
    Assertions.assertFalse(RenderValidation.hasRequiredOriginalGeometry(sample, 2));
  }

  @Test
  void acceptsMultipleOriginalGeometryStages() {
    TrackRenderMetrics.reset();
    TrackRenderMetrics.meshBuilt(1);
    TrackRenderMetrics.rendered(1, 1, 4, 0);
    TrackRenderMetrics.rendered(1, 2, 8, 0);
    var sample = TrackRenderMetrics.snapshot();

    Assertions.assertTrue(RenderValidation.hasRequiredOriginalGeometry(sample, 2));
  }
}
