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
    var snapshot = TrackRenderMetrics.snapshot();
    Assertions.assertEquals(2, snapshot.meshBuilds());
    Assertions.assertEquals(10, snapshot.averageMeshBuildNanos());
    Assertions.assertEquals(2, snapshot.renderStages());
    Assertions.assertEquals(20, snapshot.averageRenderStageNanos());
    Assertions.assertEquals(3, snapshot.inRangeRails());
    Assertions.assertEquals(60, snapshot.inRangeQuads());
    Assertions.assertEquals(6, snapshot.inRangeDecorationPieces());
    TrackRenderMetrics.reset();
    Assertions.assertEquals(0, TrackRenderMetrics.snapshot().renderStages());
  }
}
