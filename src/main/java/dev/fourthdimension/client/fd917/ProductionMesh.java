package dev.fourthdimension.client.fd917;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector3d;

/** Small batched world-lit mechanism surfaces. All inputs are world coordinates. */
public final class ProductionMesh {
  private final VertexConsumer out;
  private final PoseStack.Pose transform;
  private final java.util.Map<String, TextureAtlasSprite> sprites = new java.util.HashMap<>();
  public ProductionMesh(PoseStack.Pose transform) {
    this.transform = transform;
    out = Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(Sheets.cutoutBlockSheet());
  }
  /** Conservative world-space bounds; callers still update animation while culled. */
  public static boolean visible(net.neoforged.neoforge.client.event.RenderLevelStageEvent event, Vector3d center, double radius) {
    return event.getFrustum().isVisible(new net.minecraft.world.phys.AABB(center.x-radius,center.y-radius,center.z-radius,
        center.x+radius,center.y+radius,center.z+radius));
  }
  private TextureAtlasSprite sprite(String material) {
    return sprites.computeIfAbsent(material, name -> Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS)
        .apply(ResourceLocation.fromNamespaceAndPath("fourthdimension", "block/fd917/"+name)));
  }
  public void quad(Vector3d a, Vector3d b, Vector3d c, Vector3d d, String material, int light) {
    var n = new Vector3d(b).sub(a).cross(new Vector3d(c).sub(a));
    if (n.lengthSquared() < 1e-16) return;
    n.normalize();
    var texture = sprite(material);
    // Indicators are complete framed symbols, not a repeating surface material.
    // Thin lamp faces must include the colored center instead of only its border.
    boolean indicator = material.startsWith("indicator_");
    float u = indicator ? 1 : (float)Math.min(1, a.distance(b));
    float v = indicator ? 1 : (float)Math.min(1, b.distance(c));
    vertex(a, n, texture.getU(0), texture.getV(0), light);
    vertex(b, n, texture.getU(u), texture.getV(0), light);
    vertex(c, n, texture.getU(u), texture.getV(v), light);
    vertex(d, n, texture.getU(0), texture.getV(v), light);
  }
  private void vertex(Vector3d p, Vector3d n, float u, float v, int light) {
    out.addVertex(transform.pose(), (float)p.x, (float)p.y, (float)p.z).setColor(255,255,255,255)
        .setUv(u,v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light)
        .setNormal(transform,(float)n.x,(float)n.y,(float)n.z);
  }
  public void beam(Vector3d a, Vector3d b, Vector3d up, double width, double height,
                   String material, int light) {
    var direction = new Vector3d(b).sub(a);
    if (direction.lengthSquared() < 1e-12) return;
    direction.normalize();
    var right = new Vector3d(direction).cross(up);
    if (right.lengthSquared() < 1e-8) right.set(direction).cross(1,0,0);
    if (right.lengthSquared() < 1e-8) right.set(direction).cross(0,0,1);
    right.normalize().mul(width/2);
    var vertical = new Vector3d(right).cross(direction).normalize().mul(height/2);
    var rings = new Vector3d[2][4];
    for (int j=0;j<2;j++) for (int i=0;i<4;i++) {
      double x=i==0||i==3?-1:1, y=i<2?-1:1;
      rings[j][i]=new Vector3d(j==0?a:b).fma(x,right).fma(y,vertical);
    }
    // Subdivide long strips at one-block density without emitting internal caps.
    int segments = Math.max(1,(int)Math.ceil(a.distance(b)));
    for(int i=0;i<4;i++) for(int j=0;j<segments;j++) {
      int next=(i+1)%4;
      var p=new Vector3d(rings[0][i]).lerp(rings[1][i],(double)j/segments);
      var q=new Vector3d(rings[0][i]).lerp(rings[1][i],(double)(j+1)/segments);
      var r=new Vector3d(rings[0][next]).lerp(rings[1][next],(double)(j+1)/segments);
      var s=new Vector3d(rings[0][next]).lerp(rings[1][next],(double)j/segments);
      quad(p,q,r,s,material,light);
    }
    quad(rings[0][3],rings[0][2],rings[0][1],rings[0][0],material,light);
    quad(rings[1][0],rings[1][1],rings[1][2],rings[1][3],material,light);
  }
  public void wheel(Vector3d center, Vector3d axle, Vector3d radial, double radius,
                    double width, double spin, int light) {
    wheel(center, axle, radial, radius, width, spin, light, "rubber");
  }
  public void wheel(Vector3d center, Vector3d axle, Vector3d radial, double radius,
                    double width, double spin, int light, String tread) {
    var u = new Vector3d(radial).normalize();
    var v = new Vector3d(axle).cross(u).normalize();
    var half = new Vector3d(axle).normalize().mul(width/2);
    // Tire stays circular: hub spokes carry phase, so contact does not pulsate with faceting.
    for(int i=0;i<16;i++) {
      double a=i*Math.PI/8, b=(i+1)*Math.PI/8;
      var ra=new Vector3d(u).mul(Math.cos(a)*radius).fma(Math.sin(a)*radius,v);
      var rb=new Vector3d(u).mul(Math.cos(b)*radius).fma(Math.sin(b)*radius,v);
      quad(new Vector3d(center).sub(half).add(ra),new Vector3d(center).sub(half).add(rb),
          new Vector3d(center).add(half).add(rb),new Vector3d(center).add(half).add(ra),tread,light);
      for(int side:new int[]{-1,1}) {
        var face=new Vector3d(center).fma(side,half);
        var ia=new Vector3d(ra).mul(.55);
        var ib=new Vector3d(rb).mul(.55);
        if(side<0) { var swap=ra; ra=rb; rb=swap; swap=ia; ia=ib; ib=swap; }
        quad(new Vector3d(face).add(ra),new Vector3d(face).add(rb),new Vector3d(face).add(ib),
            new Vector3d(face).add(ia),tread,light);
        // Degenerate quad is deliberate triangle; three noncollinear first vertices supply its normal.
        quad(face,new Vector3d(face).add(ia),new Vector3d(face).add(ib),face,"machined",light);
        if(side<0) { var swap=ra; ra=rb; rb=swap; }
      }
    }
    for(int side:new int[]{-1,1}) for(int spoke=0;spoke<3;spoke++) {
      double a=spin+spoke*2*Math.PI/3;
      var hub=new Vector3d(center).fma(side*(width/2+.002),axle);
      var tip=new Vector3d(hub).fma(Math.cos(a)*radius*.5,u).fma(Math.sin(a)*radius*.5,v);
      beam(hub,tip,axle,.025,.012,"paint_yellow",light);
    }
  }
  public void ring(Vector3d center, Vector3d axis, Vector3d radial, double inner,
                   double outer, double width, String material, int light) {
    var u=new Vector3d(radial).normalize(); var v=new Vector3d(axis).cross(u).normalize();
    var half=new Vector3d(axis).normalize().mul(width/2);
    for(int i=0;i<32;i++) {
      double a=i*Math.PI/16,b=(i+1)*Math.PI/16;
      var oa=new Vector3d(u).mul(Math.cos(a)*outer).fma(Math.sin(a)*outer,v);
      var ob=new Vector3d(u).mul(Math.cos(b)*outer).fma(Math.sin(b)*outer,v);
      var ia=new Vector3d(oa).mul(inner/outer); var ib=new Vector3d(ob).mul(inner/outer);
      for(int side:new int[]{-1,1}) {
        var face=new Vector3d(center).fma(side,half);
        if(side>0)quad(new Vector3d(face).add(oa),new Vector3d(face).add(ob),new Vector3d(face).add(ib),new Vector3d(face).add(ia),material,light);
        else quad(new Vector3d(face).add(ia),new Vector3d(face).add(ib),new Vector3d(face).add(ob),new Vector3d(face).add(oa),material,light);
      }
      quad(new Vector3d(center).sub(half).add(oa),new Vector3d(center).sub(half).add(ob),new Vector3d(center).add(half).add(ob),new Vector3d(center).add(half).add(oa),material,light);
      quad(new Vector3d(center).add(half).add(ia),new Vector3d(center).add(half).add(ib),new Vector3d(center).sub(half).add(ib),new Vector3d(center).sub(half).add(ia),material,light);
    }
  }

}
