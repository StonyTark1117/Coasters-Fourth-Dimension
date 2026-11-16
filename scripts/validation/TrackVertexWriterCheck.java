import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.fourthdimension.client.TrackVertexWriter;
import java.util.Random;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

/** Runs against the pinned Minecraft BufferBuilder, without a game or GL context. */
public final class TrackVertexWriterCheck {
  private record Quad(Vec3 a, Vec3 b, Vec3 c, Vec3 d, Vec3 normal, int color,
                      float u0, float u1, float v0, float v1, int light) {}

  public static void main(String[] args) {
    var random = new Random(904_917);
    var quads = new Quad[1024];
    for (int i = 0; i < quads.length; i++) {
      var normal = i % 13 == 0 ? Vec3.ZERO : point(random).normalize();
      quads[i] = new Quad(point(random), point(random), point(random), point(random), normal,
          random.nextInt(), random.nextFloat(), random.nextFloat(), random.nextFloat(),
          random.nextFloat(), (random.nextInt(16) * 16 << 16) | (random.nextInt(16) * 16));
    }
    int cases = 0;
    long attributes = 0;
    for (var format : new VertexFormat[] {DefaultVertexFormat.BLOCK, DefaultVertexFormat.NEW_ENTITY}) {
      for (int transform = 0; transform < 5; transform++) {
        var stack = new PoseStack();
        if (transform > 0) {
          stack.translate(-123.25, 40.5, -160.125);
          stack.mulPose(new Quaternionf().rotationXYZ(.3f, -.7f, 1.2f));
        }
        if (transform == 2) stack.scale(.5f, .5f, .5f);
        if (transform == 3) stack.scale(.75f, 1.5f, .3f);
        if (transform == 4) stack.scale(-.75f, 1.5f, .3f);
        for (float alpha : new float[] {1, .4f, .35f}) {
          for (boolean fallback : new boolean[] {false, true}) {
            try (var oldBytes = new ByteBufferBuilder(64);
                 var newBytes = new ByteBufferBuilder(64)) {
              var original = new BufferBuilder(oldBytes, VertexFormat.Mode.QUADS, format);
              var optimized = new BufferBuilder(newBytes, VertexFormat.Mode.QUADS, format);
              VertexConsumer target = fallback ? new Forwarding(optimized) : optimized;
              var writer = new TrackVertexWriter(target, stack.last());
              for (int i = 0; i < quads.length; i++) {
                var q = quads[i];
                oldQuad(original, stack.last(), q, alpha);
                // Exercise ordinary→bulk→ordinary transitions in one native batch too.
                if (i == 0 || i == quads.length - 1) oldQuad(target, stack.last(), q, alpha);
                else writer.quad(q.a, q.b, q.c, q.d, q.normal, q.color,
                    q.u0, q.u1, q.v0, q.v1, q.light, alpha);
              }
              try (var oldMesh = original.buildOrThrow(); var newMesh = optimized.buildOrThrow()) {
                if (!oldMesh.drawState().equals(newMesh.drawState()))
                  throw new AssertionError("Draw state changed");
                var a = oldMesh.vertexBuffer();
                var b = newMesh.vertexBuffer();
                if (a.remaining() != b.remaining()) throw new AssertionError("Vertex count changed");
                int stride = format.getVertexSize();
                for (int offset = 0; offset < a.remaining(); offset++) {
                  // Both native formats leave the final padding byte uninitialized.
                  // Every position/color/UV/overlay/light/normal byte is compared.
                  if (offset % stride == stride - 1) continue;
                  if (a.get(offset) != b.get(offset))
                    throw new AssertionError("Attribute mismatch at byte " + offset
                        + ", stride=" + stride + ", transform=" + transform
                        + ", alpha=" + alpha + ", fallback=" + fallback);
                  attributes++;
                }
              }
            }
            cases++;
          }
        }
      }
    }
    System.out.println("PASS: " + cases + " native buffer cases; " + attributes
        + " identical attribute bytes; identity/rotation/uniform/nonuniform/reflected poses,"
        + " opaque/invalid/ghost alpha, both formats and default-consumer fallback.");
  }

  private static Vec3 point(Random random) {
    return new Vec3(random.nextDouble() * 2048 - 1024,
        random.nextDouble() * 384 - 64, random.nextDouble() * 2048 - 1024);
  }

  private static void oldQuad(VertexConsumer out, PoseStack.Pose pose, Quad q, float alpha) {
    oldVertex(out, pose, q, q.a, q.u0, q.v0, alpha);
    oldVertex(out, pose, q, q.b, q.u1, q.v0, alpha);
    oldVertex(out, pose, q, q.c, q.u1, q.v1, alpha);
    oldVertex(out, pose, q, q.d, q.u0, q.v1, alpha);
  }

  private static void oldVertex(VertexConsumer out, PoseStack.Pose pose, Quad q, Vec3 p,
                                float u, float v, float alpha) {
    out.addVertex(pose.pose(), (float) p.x, (float) p.y, (float) p.z)
        .setColor((q.color >> 16) & 255, (q.color >> 8) & 255, q.color & 255, (int) (alpha * 255))
        .setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(q.light)
        .setNormal(pose, (float) q.normal.x, (float) q.normal.y, (float) q.normal.z);
  }

  private record Forwarding(VertexConsumer delegate) implements VertexConsumer {
    public VertexConsumer addVertex(float x, float y, float z) { delegate.addVertex(x, y, z); return this; }
    public VertexConsumer setColor(int r, int g, int b, int a) { delegate.setColor(r, g, b, a); return this; }
    public VertexConsumer setUv(float u, float v) { delegate.setUv(u, v); return this; }
    public VertexConsumer setUv1(int u, int v) { delegate.setUv1(u, v); return this; }
    public VertexConsumer setUv2(int u, int v) { delegate.setUv2(u, v); return this; }
    public VertexConsumer setNormal(float x, float y, float z) { delegate.setNormal(x, y, z); return this; }
  }
}
