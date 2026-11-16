package dev.fourthdimension.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/** Frame-local writer for the unchanged track quad attributes. */
public final class TrackVertexWriter {
  private final VertexConsumer buffer;
  private final PoseStack.Pose pose;
  private final Vector3f position = new Vector3f();
  private final Vector3f normal = new Vector3f();

  public TrackVertexWriter(VertexConsumer buffer, PoseStack.Pose pose) {
    this.buffer = buffer;
    this.pose = pose;
  }

  public void quad(Vec3 a, Vec3 b, Vec3 c, Vec3 d, Vec3 sourceNormal, int color,
                   float u0, float u1, float v0, float v1, int light, float alpha) {
    // All four vertices share this normal. Use Pose's normal transform, including
    // its nonuniform-scale normalization, exactly as VertexConsumer.setNormal.
    pose.transformNormal((float) sourceNormal.x, (float) sourceNormal.y,
        (float) sourceNormal.z, normal);
    int argb = ((int) (alpha * 255) << 24) | (color & 0xffffff);
    vertex(a, argb, u0, v0, light);
    vertex(b, argb, u1, v0, light);
    vertex(c, argb, u1, v1, light);
    vertex(d, argb, u0, v1, light);
  }

  private void vertex(Vec3 point, int argb, float u, float v, int light) {
    pose.pose().transformPosition((float) point.x, (float) point.y, (float) point.z, position);
    // BufferBuilder has a native full-format path for this overload. Other
    // consumers retain the interface's ordinary attribute-by-attribute fallback.
    buffer.addVertex(position.x, position.y, position.z, argb, u, v,
        OverlayTexture.NO_OVERLAY, light, normal.x, normal.y, normal.z);
  }
}
