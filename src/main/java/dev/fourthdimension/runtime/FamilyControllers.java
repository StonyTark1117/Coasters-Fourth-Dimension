package dev.fourthdimension.runtime;

import dev.fourthdimension.FourthDimension;
import dev.fourthdimension.contract.Family;
import dev.ryanhcode.sable.sublevel.SubLevel;
import java.util.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Durable mechanical marker follows assembly, unlike a chassis UUID alone. */
public final class FamilyControllers {
  private FamilyControllers() {}

  public static final DeferredRegister.Blocks BLOCKS =
      DeferredRegister.createBlocks(FourthDimension.ID);
  public static final Map<Family, DeferredBlock<Controller>> CONTROLLERS =
      new EnumMap<>(Family.class);
  private record Resolved(long gameTick, Set<Family> families) {}

  private static final Map<SubLevel, Resolved> RESOLVED = new WeakHashMap<>();

  static {
    for (Family family : Family.values())
      CONTROLLERS.put(
          family, BLOCKS.register(family.id() + "_controller", () -> new Controller(family)));
  }

  public static final class Controller extends Block {
    public final Family family;

    private Controller(Family family) {
      super(BlockBehaviour.Properties.of().strength(3).noOcclusion());
      this.family = family;
    }
  }

  public static Set<Family> find(SubLevel sub) {
    long gameTick = sub.getLevel().getGameTime();
    var cached = RESOLVED.get(sub);
    if (cached != null && cached.gameTick == gameTick) return cached.families;
    var families = scan(sub);
    RESOLVED.put(sub, new Resolved(gameTick, families));
    return families;
  }

  /** Physics may call this more than once per server tick; block mutations are observed next tick. */
  private static Set<Family> scan(SubLevel sub) {
    Set<Family> families = EnumSet.noneOf(Family.class);
    for (var holder : sub.getPlot().getLoadedChunks()) {
      // Block palette scan includes controllers carried by arbitrary assembly/splits.
      var chunk = holder.getChunk();
      for (var section : chunk.getSections())
        if (section != null && section.maybeHas(s -> s.getBlock() instanceof Controller))
          for (int x = 0; x < 16; x++)
            for (int y = 0; y < 16; y++)
              for (int z = 0; z < 16; z++)
                if (section.getBlockState(x, y, z).getBlock() instanceof Controller controller)
                  families.add(controller.family);
    }
    return Set.copyOf(families);
  }
}
