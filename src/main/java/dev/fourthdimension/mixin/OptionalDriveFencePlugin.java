package dev.fourthdimension.mixin;

import java.util.*;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import org.spongepowered.asm.mixin.extensibility.*;

/**
 * Optional addons merge private callbacks into native classes. Their generated names are unstable;
 * identify the pinned call sites instead, after their priority-1000 mixins have been applied.
 * No optional or Minecraft classes are loaded during transformation.
 */
public final class OptionalDriveFencePlugin implements IMixinConfigPlugin {
  private static final String ARGS="(Ldev/ryanhcode/sable/sublevel/ServerSubLevel;Lnet/minecraft/server/level/ServerLevel;Ldev/ryanhcode/sable/sublevel/system/SubLevelPhysicsSystem;Ldev/silvergold/simulatedcoasters/track/graph/CoasterPathTrackFrame$GraphHit;Ldev/silvergold/simulatedcoasters/track/graph/CoasterPathTrackFrame$TrackBasis;D";
  public void onLoad(String p) {}
  public String getRefMapperConfig(){return null;}
  public boolean shouldApplyMixin(String target,String mixin){return true;}
  public void acceptTargets(Set<String> own,Set<String> other){}
  public List<String> getMixins(){return null;}
  public void preApply(String name,ClassNode node,String mixin,IMixinInfo info){}
  public void postApply(String name,ClassNode node,String mixin,IMixinInfo info){
    if(!mixin.endsWith(".OptionalDriveFenceMixin"))return;
    for(var method:node.methods){
      if((method.access&Opcodes.ACC_STATIC)==0||!method.desc.startsWith(ARGS))continue;
      String hook=null;
      for(var instruction:method.instructions.toArray())if(instruction instanceof MethodInsnNode call){
        if(call.owner.equals("net/antopfr/coastersmagnetized/magnet/MagnetBoost")&&call.name.equals("accelerationFor"))hook="fenceMagnetized";
        if(call.owner.equals("dev/notzyvex/coasters_extras/net/RideTelemetry")&&call.name.equals("report"))hook="ctnb";
      }
      if(hook==null)continue;
      var code=new InsnList();var ordinary=new LabelNode();
      for(int i=0;i<5;i++)code.add(new VarInsnNode(Opcodes.ALOAD,i));
      code.add(new VarInsnNode(Opcodes.DLOAD,5));
      code.add(new MethodInsnNode(Opcodes.INVOKESTATIC,"dev/fourthdimension/integration/OptionalDrive",hook,ARGS+")Z",false));
      code.add(new JumpInsnNode(Opcodes.IFEQ,ordinary));code.add(new InsnNode(Opcodes.RETURN));code.add(ordinary);
      method.instructions.insert(code);method.maxStack=Math.max(method.maxStack,7);
      System.out.println("Fourth Dimension fenced optional "+hook+" force callback in "+name);
    }
  }
}
