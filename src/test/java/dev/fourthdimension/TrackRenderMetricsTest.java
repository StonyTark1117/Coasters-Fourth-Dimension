package dev.fourthdimension;

import dev.fourthdimension.client.TrackRenderMetrics;
import org.junit.jupiter.api.*;

class TrackRenderMetricsTest {
  @Test
  void aggregatesAndResetsClientRenderSamples() {
    TrackRenderMetrics.reset();
    TrackRenderMetrics.meshBuilt(12);
    TrackRenderMetrics.meshBuilt(8);
    TrackRenderMetrics.rendered(30, 2, 40, 6);
    TrackRenderMetrics.rendered(10, 1, 20, 0);
    TrackRenderMetrics.rendered(5, 0, 10, 0);
    TrackRenderMetrics.rendered(5, 2, 0, 0);
    var snapshot = TrackRenderMetrics.snapshot();
    Assertions.assertEquals(2, snapshot.meshBuilds());
    Assertions.assertEquals(10, snapshot.averageMeshBuildNanos());
    Assertions.assertEquals(4, snapshot.renderStages());
    Assertions.assertEquals(12, snapshot.averageRenderStageNanos());
    Assertions.assertEquals(2, snapshot.geometryStages());
    Assertions.assertEquals(5, snapshot.inRangeRails());
    Assertions.assertEquals(70, snapshot.inRangeQuads());
    Assertions.assertEquals(6, snapshot.inRangeDecorationPieces());
    TrackRenderMetrics.reset();
    Assertions.assertEquals(0, TrackRenderMetrics.snapshot().renderStages());
  }
}
