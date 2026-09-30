package dev.fourthdimension.client;

import dev.fourthdimension.editor.EditorNetwork;
import dev.fourthdimension.runtime.*;
import java.util.stream.Collectors;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.network.PacketDistributor;

public final class RotationEditorScreen extends Screen {
  private static String copiedProfile = "";
  private final EditorNetwork.Target target;
  private EditBox value;
  private long revision;
  private String status = "";
  private boolean free;
  private boolean magnetic;
  private int previewLoading;

  public RotationEditorScreen(EditorNetwork.Target target) {
    super(Component.literal("Fourth Dimension"));
    this.target = target;
  }

  @Override
  public boolean isPauseScreen() {
    return false;
  }

  @Override
  protected void init() {
    var data = EligibilityHooks.data(minecraft.level);
    if (data == null) {
      onClose();
      return;
    }
    int left = width / 2 - 170, top = height / 2 - 90;
    value =
        new EditBox(
            font, left, top + 50, 340, 22, Component.literal("Profile or free-spin tuning"));
    value.setMaxLength(16384);
    if (target.vehicle()) {
      var v = data.vehicles.get(target.id());
      if (v == null) {
        onClose();
        return;
      }
      revision = v.revision;
      free = !v.family.driven();
      magnetic = v.family == dev.fourthdimension.contract.Family.FREE_SPIN;
      status =
          v.family.id()
              + " | "
              + (v.boardingReady ? "Ready" : "Aligning / moving")
              + " | "
              + (v.restraints ? "Restraints closed" : "Restraints open");
      value.setValue(v.damping + "," + v.ballast);
      value.setTooltip(
          net.minecraft.client.gui.components.Tooltip.create(
              Component.literal(
                  "Damping,uniform ballast kg. Current group ballast: "
                      + v.carriers.stream()
                          .map(
                              c ->
                                  String.format(
                                      java.util.Locale.ROOT, "%.0f", v.ballast + c.ballast))
                          .collect(Collectors.joining(" / "))
                      + ". Applying uniform tuning resets per-group ballast.")));
      value.setEditable(free);
      addButton("Capture", left, top + 82, 10);
      addButton("Close restraints", left + 115, top + 82, 11);
      addButton("Open restraints", left + 230, top + 82, 12);
      addButton("Dispatch", left, top + 108, 13);
      addButton("Retain", left + 115, top + 108, 14);
      addButton("Release retention", left + 230, top + 108, 15);
      if (free) addButton("Apply tuning", left, top + 134, 16);
      if (magnetic) {
        addButton("Empty ballast", left, top + 186, 17);
        addButton("Balanced ballast", left + 115, top + 186, 18);
        addButton("Left-front load", left + 230, top + 186, 19);
      }
    } else {
      var rail = data.rails.stream().filter(r -> r.id.equals(target.id())).findFirst().orElse(null);
      if (rail == null) {
        onClose();
        return;
      }
      revision = rail.revision;
      free = !rail.family.driven();
      magnetic = rail.family == dev.fourthdimension.contract.Family.FREE_SPIN;
      status =
          rail.variant
              + " | revision "
              + revision
              + " | "
              + (rail.valid ? "Bound" : "Rebind required");
      value.setValue(
          rail.profile.keys().stream()
              .map(
                  k ->
                      k.distance()
                          + ":"
                          + Math.toDegrees(k.angle())
                          + ":"
                          + Math.toDegrees(k.slope()))
              .collect(Collectors.joining(";")));
      if (magnetic) {
        value.setValue(dev.fourthdimension.magnetic.MagneticData.format(rail.magnets));
        value.setTooltip(
            net.minecraft.client.gui.components.Tooltip.create(
                Component.literal(
                    "One span per semicolon: start:end:induce or damp:direction +/-1:strength"
                        + " 0..20:falloff 0..0.5:target sum. Left-front=1, left-rear=2,"
                        + " right-front=4, right-rear=8, all=15. Distances start at the section"
                        + " first anchor. Requires equipment items; removing spans refunds"
                        + " them.")));
      }
      value.setEditable(!free || magnetic);
      if (magnetic) addButton("Apply magnets", left, top + 82, 4);
      if (!free) {
        addButton("Apply profile", left, top + 82, 0);
        addButton("Mirror angles", left + 115, top + 82, 2);
        addButton("Reverse", left + 230, top + 82, 3);
        addRenderableWidget(
            Button.builder(Component.literal("Copy"), b -> copiedProfile = value.getValue())
                .bounds(left, top + 108, 110, 20)
                .build());
        addRenderableWidget(
            Button.builder(
                    Component.literal("Paste"),
                    b -> {
                      if (!copiedProfile.isEmpty()) value.setValue(copiedProfile);
                    })
                .bounds(left + 115, top + 108, 110, 20)
                .build());
      }
      addButton("Rebind geometry", left + 230, top + 108, 1);
    }
    if (free)
      addRenderableWidget(
          Button.builder(
                  Component.literal("Preview: empty"),
                  b -> {
                    previewLoading = (previewLoading + 1) % 3;
                    b.setMessage(
                        Component.literal(
                            new String[] {
                                  "Preview: empty", "Preview: balanced", "Preview: front load"
                                }
                                [previewLoading]));
                  })
              .bounds(left + 230, top + 134, 110, 20)
              .build());
    addRenderableWidget(
        Button.builder(
                Component.literal("Preview seats"),
                b -> {
                  try {
                    var snapshot = EligibilityHooks.data(minecraft.level);
                    var rail =
                        snapshot.rails.stream()
                            .filter(
                                r ->
                                    target.vehicle()
                                        ? r.matches(
                                            snapshot.vehicles.get(target.id()).boundFrom,
                                            snapshot.vehicles.get(target.id()).boundTo)
                                        : r.id.equals(target.id()))
                            .findFirst()
                            .orElseThrow();
                    RidePreview.select(
                        rail,
                        rail.family.driven() && !target.vehicle()
                            ? EditorNetwork.parse(value.getValue())
                            : rail.profile,
                        previewLoading,
                        target.vehicle() ? snapshot.vehicles.get(target.id()).damping : .8,
                        magnetic && !target.vehicle()
                            ? dev.fourthdimension.magnetic.MagneticData.parse(value.getValue())
                            : rail.magnets);
                    onClose();
                  } catch (RuntimeException failure) {
                    status = "Preview: " + failure.getMessage();
                  }
                })
            .bounds(left + 115, top + 134, 110, 20)
            .build());
    addRenderableWidget(value);
    addRenderableWidget(
        Button.builder(Component.literal("Done"), b -> onClose())
            .bounds(left + 230, top + 160, 110, 20)
            .build());
  }

  private void addButton(String label, int x, int y, int action) {
    addRenderableWidget(
        Button.builder(
                Component.literal(label),
                b -> {
                  PacketDistributor.sendToServer(
                      new EditorNetwork.Edit(target.id(), revision, action, value.getValue()));
                  onClose();
                })
            .bounds(x, y, 110, 20)
            .build());
  }

  @Override
  public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {}

  @Override
  public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    int left = width / 2 - 180, top = height / 2 - 100;
    graphics.fill(
        left, top, left + 360, top + (target.vehicle() && magnetic ? 232 : 200), 0xee171d25);
    graphics.drawString(font, title, left + 10, top + 10, 0xffffff);
    graphics.drawString(font, status, left + 10, top + 26, 0xf3d179);
    String hint =
        target.vehicle()
            ? (free
                ? "Damping, ballast kg per carrier (80 kg per rider)"
                : "Capture, close restraints, then dispatch")
            : (free
                ? magnetic
                    ? "start:end:induce/damp:direction:strength:falloff:mask"
                    : "Free-spin motion is tuned on the car"
                : "distance:degrees:slope; ...  (angles remain unwrapped)");
    graphics.drawString(font, hint, left + 10, top + 42, 0xc2d2dd);
    super.render(graphics, mouseX, mouseY, partialTick);
  }

  @EventBusSubscriber(
      modid = "fourthdimension",
      value = Dist.CLIENT,
      bus = EventBusSubscriber.Bus.MOD)
  public static final class Registration {
    @SubscribeEvent
    public static void setup(FMLClientSetupEvent event) {
      event.enqueueWork(
          () ->
              EditorNetwork.openClient =
                  target ->
                      net.minecraft.client.Minecraft.getInstance()
                          .setScreen(new RotationEditorScreen(target)));
    }
  }
}
