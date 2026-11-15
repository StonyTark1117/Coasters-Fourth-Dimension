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
  private boolean recovery;
  private int previewLoading;
  private Button recoveryStartTow;
  private Button recoveryAttach;
  private boolean recoveryStartTowFixtureClickQueued;
  private boolean recoveryAttachFixtureClickQueued;
  private EditorNetwork.Edit pendingEdit;
  private String feedback = "";
  private boolean feedbackError;
  private boolean initialized;

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
    boolean compound=target.vehicle()? data.vehicles.containsKey(target.id())&&dev.fourthdimension.vehicle.NewGenerationFlyingVehicle.is(data.vehicles.get(target.id())) : data.rails.stream().anyMatch(r->r.id.equals(target.id())&&r.variant.equals(dev.fourthdimension.vehicle.NewGenerationFlyingVehicle.VARIANT));
    if(compound){minecraft.setScreen(new NewGenerationFlyingScreen(target));return;}
    int left = width / 2 - 170, top = height / 2 - (target.vehicle() ? 116 : 90);
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
      free = v.family.foundationFixture() && !v.family.driven();
      magnetic = v.family == dev.fourthdimension.contract.Family.FREE_SPIN;
      recovery = v.recoveryCart != null;
      updateVehicleStatus(v);
      value.setValue(dev.fourthdimension.vehicle.RollerBallVehicle.is(v) ? "Roller Ball — passive swing; latch on guided lift"
          : dev.fourthdimension.vehicle.AxisVehicle.is(v) ? "Axis — continuous roll, track FREE / DAMP / DRIVE"
          : dev.fourthdimension.vehicle.FlyingVehicle.is(v) ? "Flying — " + v.flying.phase + " | lock " + v.flying.lock
          : dev.fourthdimension.vehicle.FixedInvertedVehicle.is(v)
          ? "Fixed Inverted — four seats, overhead track, level dock" : dev.fourthdimension.vehicle.WingVehicle.is(v)
          ? "Fixed seats — ordinary Coasters track" : dev.fourthdimension.vehicle.SpinningVehicle.is(v)
              ? "S1 free spin — matching track and a bound dock" : dev.fourthdimension.vehicle.SuspendedVehicle.is(v)
                  ? "Suspended ±45° — overhead track, lift locks, level dock" : v.damping + "," + v.ballast);
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
      if(dev.fourthdimension.vehicle.RollerBallVehicle.is(v))
        value.setTooltip(net.minecraft.client.gui.components.Tooltip.create(Component.literal(
            "Passive carrier motion. The Lift Control owns guided climb and summit release; return sections damp the carrier.")));
      value.setEditable(free);
      addButton(v.family.rotary() && !dev.fourthdimension.vehicle.FlyingVehicle.is(v) ? "Capture" : "Stop", left, top + 82, 10);
      if (dev.fourthdimension.vehicle.FlyingVehicle.is(v)) {
        addButton(v.flying.body ? "Open body" : "Close body", left+115,top+82,v.flying.body?35:34);
        addButton(v.flying.legs ? "Open legs" : "Close legs", left+230,top+82,v.flying.legs?37:36);
        addButton("Set prone",left,top+186,31);
        addButton("Set seated",left+115,top+186,32);
        addButton("Resume posture",left+230,top+186,33);
      } else {
        addButton("Close restraints", left + 115, top + 82, 11);
        addButton("Open restraints", left + 230, top + 82, 12);
      }
      addButton("Dispatch", left, top + 108, 13);
      addButton("Retain", left + 115, top + 108, 14);
      addButton("Release retention", left + 230, top + 108, 15);
      if ("Matching track support lost".equals(v.fault))
        addButton("Repair matching track", left, top + 160, 25);
      else if (v.alignmentRequired)
        addButton("Realign matching track", left, top + 160, 26);
      if (free) addButton("Apply tuning", left, top + 134, 16);
      if (dev.fourthdimension.vehicle.RollerBallVehicle.is(v) || dev.fourthdimension.vehicle.AxisVehicle.is(v) || dev.fourthdimension.vehicle.FlyingVehicle.is(v) || dev.fourthdimension.vehicle.FixedInvertedVehicle.is(v) || dev.fourthdimension.vehicle.SpinningVehicle.is(v) || dev.fourthdimension.vehicle.SuspendedVehicle.is(v))
        addButton("Resume to dock", left, top + 134, 28);
      if (v.recoveryCart == null && v.family.foundationFixture()) {
        recoveryAttach = addButton("Attach recovery", left, top + 212, 20);
        if (v.family == dev.fourthdimension.contract.Family.PAX)
          addButton("Detach shuttle", left + 115, top + 212, 24);
      } else if (v.recoveryCart != null) {
        recoveryStartTow = addButton("Start tow", left, top + 212, 21);
        addButton("Brake tow", left + 115, top + 212, 22);
        addButton("Detach recovery", left + 230, top + 212, 23);
      }
      if (magnetic) {
        addButton("Empty ballast", left, top + 186, 17);
        addButton("Balanced ballast", left + 115, top + 186, 18);
        addButton("Left-front load", left + 230, top + 186, 19);
      }
      if (v.family == dev.fourthdimension.contract.Family.PAX && v.maintenance)
        addRenderableWidget(
            Button.builder(
                    Component.literal("Retire empty car"),
                    b ->
                        minecraft.setScreen(
                            new net.minecraft.client.gui.screens.ConfirmScreen(
                                confirmed -> {
                                  if (confirmed) {
                                    PacketDistributor.sendToServer(
                                        new EditorNetwork.Edit(target.id(), revision, 27, ""));
                                    minecraft.setScreen(null);
                                  } else minecraft.setScreen(this);
                                },
                                Component.literal("Retire this PAX car?"),
                                Component.literal(
                                    "Permanently remove this empty, detached car and its carriers."),
                                Component.literal("Retire car"),
                                Component.literal("Cancel"))))
                .bounds(left + 115, top + 186, 110, 20)
                .build());
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
      if (rail.family == dev.fourthdimension.contract.Family.SPINNING || rail.family == dev.fourthdimension.contract.Family.SUSPENDED)
        addButton(rail.liftLock ? "Lift lock: on" : "Lift lock: off", left, top + 82, 6);
      if (rail.family == dev.fourthdimension.contract.Family.AXIS) {
        addButton("Roll: "+rail.axisMode,left,top+134,7);
        addButton(rail.liftLock?"Lift lock: on":"Lift lock: off",left+230,top+134,6);
      }
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
      addRenderableWidget(
          Button.builder(
                  Component.literal("Track appearance"),
                  b ->
                      minecraft.setScreen(
                          new dev.fourthdimension.client.integration.AppearanceScreen(target.id())))
              .bounds(left, top + 160, 110, 20)
              .build());
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
                                  "Preview: empty",
                                  "Preview: balanced",
                                  magnetic ? "Preview: front load" : "Preview: left load"
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
    if (!initialized && recovery
        && recoveryStartTow != null
        && Boolean.getBoolean("fourthdimension.carrierFixtureRecoveryEditorAction")) {
      recoveryStartTowFixtureClickQueued = true;
    }
    if (!initialized && !recovery
        && recoveryAttach != null
        && Boolean.getBoolean("fourthdimension.carrierFixtureRecoveryEditorAttach")) {
      recoveryAttachFixtureClickQueued = true;
    }
    initialized = true;
    if (pendingEdit != null) setEditingEnabled(false);
  }

  private void clickQueuedRecoveryStartTow() {
    if (!recoveryStartTowFixtureClickQueued
        || !CarrierTransformValidation.recoveryEditorActionReady()) return;
    recoveryStartTowFixtureClickQueued = false;
    minecraft.execute(
        () -> {
          if (minecraft.screen != this) return;
          // Route this opt-in fixture through Screen's normal mouse dispatcher rather than
          // sending an editor packet directly. The button's ordinary callback owns the packet.
          mouseClicked(
              recoveryStartTow.getX() + recoveryStartTow.getWidth() / 2.0,
              recoveryStartTow.getY() + recoveryStartTow.getHeight() / 2.0,
              0);
        });
  }

  private void clickQueuedRecoveryAttach() {
    if (!recoveryAttachFixtureClickQueued) return;
    recoveryAttachFixtureClickQueued = false;
    minecraft.execute(
        () -> {
          if (minecraft.screen != this) return;
          mouseClicked(
              recoveryAttach.getX() + recoveryAttach.getWidth() / 2.0,
              recoveryAttach.getY() + recoveryAttach.getHeight() / 2.0,
              0);
        });
  }

  private Button addButton(String label, int x, int y, int action) {
    var button =
        Button.builder(
                Component.literal(label),
                b -> {
                  if (!recovery
                      && action == 20
                      && Boolean.getBoolean(
                          "fourthdimension.carrierValidationRecoveryEditorAttach"))
                    CarrierTransformValidation.recoveryEditorAttachClicked();
                  if (recovery
                      && action == 21
                      && Boolean.getBoolean(
                          "fourthdimension.carrierValidationRecoveryEditorAction"))
                    CarrierTransformValidation.recoveryEditorStartTowClicked();
                  submit(action);
                })
            .bounds(x, y, 110, 20)
            .build();
    if (recovery && (action == 12 || action == 13 || action == 15)) {
      button.active = false;
      button.setTooltip(
          Tooltip.create(Component.translatable("editor.fourthdimension.recovery.locked")));
    } else if (action >= 20 && action <= 26) {
      button.setTooltip(
          Tooltip.create(
              Component.translatable("editor.fourthdimension.recovery.action_" + action)));
    }
    addRenderableWidget(button);
    return button;
  }

  private void submit(int action) {
    if (pendingEdit != null) return;
    pendingEdit = new EditorNetwork.Edit(target.id(), revision, action, value.getValue());
    feedback = "Waiting for server…";
    feedbackError = false;
    setEditingEnabled(false);
    PacketDistributor.sendToServer(pendingEdit);
  }

  private void setEditingEnabled(boolean enabled) {
    for (var child : children())
      if (child instanceof AbstractWidget widget) widget.active = enabled;
  }

  private void receiveResult(EditorNetwork.Result result) {
    if (pendingEdit == null
        || !pendingEdit.id().equals(result.id())
        || pendingEdit.revision() != result.requestedRevision()
        || pendingEdit.action() != result.action()) return;
    var draft = value.getValue();
    pendingEdit = null;
    feedback = result.message();
    feedbackError = !result.success();
    if (result.success() && result.action() == 13) {
      onClose();
      return;
    }
    // The result follows the current snapshot on the same connection. Rebuilding updates
    // the revision and conditional controls, without reopening or losing an invalid draft.
    rebuildWidgets();
    if (!result.success()) value.setValue(draft);
  }

  private void updateVehicleStatus(FoundationData.Vehicle vehicle) {
    status =
        recovery
            ? Component.translatable(
                    vehicle.stopping
                        ? "editor.fourthdimension.recovery.braked"
                        : "editor.fourthdimension.recovery.towing")
                .getString()
            : (dev.fourthdimension.vehicle.WingVehicle.is(vehicle) ? "Wing" : vehicle.family.id())
                + " | "
                + (vehicle.boardingReady ? "Ready" : "Aligning / moving")
                + " | "
                + (vehicle.restraints ? "Restraints closed" : "Restraints open");
    if (dev.fourthdimension.vehicle.FlyingVehicle.is(vehicle)) {
      status="Flying | body "+(vehicle.flying.body?"closed":"open")+" | legs "+(vehicle.flying.legs?"closed":"open")+" | lock "+vehicle.flying.lock;
      if(value!=null)value.setValue(String.format(java.util.Locale.ROOT,"%s | %.1f° | %s",vehicle.flying.phase,
          Math.toDegrees(-vehicle.carriers.getFirst().angle),vehicle.flying.lock));
    }
    if (dev.fourthdimension.vehicle.RollerBallVehicle.is(vehicle)) {
      var carrier=vehicle.carriers.getFirst();
      String mechanism=carrier.latched||carrier.liftHeld?"latched"
          :vehicle.roller.phase==dev.fourthdimension.vehicle.RollerBallState.Phase.RETURN_DAMP?"damping"
          :vehicle.roller.released?"free":"guided";
      status="Roller Ball | "+vehicle.roller.phase+" | "+mechanism
          +" | restraints "+(vehicle.restraints?"closed":"open");
    }
    if (!vehicle.fault.isEmpty()) status += " | " + vehicle.fault;
  }

  @Override
  public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {}

  @Override
  public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    int left = width / 2 - 180, top = height / 2 - (target.vehicle() ? 126 : 100);
    graphics.fill(left, top, left + 360, top + (target.vehicle() ? 252 : 200), 0xee171d25);
    graphics.drawString(font, title, left + 10, top + 10, 0xffffff);
    if (target.vehicle()) {
      var snapshot = EligibilityHooks.data(minecraft.level);
      var vehicle = snapshot == null ? null : snapshot.vehicles.get(target.id());
      if (vehicle != null) updateVehicleStatus(vehicle);
    }
    if (recovery
        && Boolean.getBoolean("fourthdimension.carrierValidationRecoveryEditor"))
      CarrierTransformValidation.recoveryEditorRendered();
    if (!recovery
        && Boolean.getBoolean("fourthdimension.carrierValidationRecoveryEditorAttach"))
      CarrierTransformValidation.recoveryEditorAttachRendered();
    String visibleStatus =
        font.width(status) <= 340 ? status : font.plainSubstrByWidth(status, 330) + "…";
    graphics.drawString(font, visibleStatus, left + 10, top + 26, 0xf3d179);
    String hint =
        target.vehicle()
            ? (recovery
                ? Component.translatable("editor.fourthdimension.recovery.hint").getString()
                : free
                    ? "Damping, ballast kg per carrier (80 kg per rider)"
                    : "Capture, close restraints, then dispatch")
            : (free
                ? magnetic
                    ? "start:end:induce/damp:direction:strength:falloff:mask"
                    : "Free-spin motion is tuned on the car"
                : "distance:degrees:slope; ...  (angles remain unwrapped)");
    if(target.vehicle()) {
      var snapshot=EligibilityHooks.data(minecraft.level);
      var selected=snapshot==null?null:snapshot.vehicles.get(target.id());
      if(selected!=null&&dev.fourthdimension.vehicle.FlyingVehicle.is(selected))
        hint="Close body + legs; set prone, wait for lock, dispatch. Seated to unload.";
      if(selected!=null&&dev.fourthdimension.vehicle.RollerBallVehicle.is(selected))
        hint="Close restraints and Dispatch; the tagged summit guides release.";
    } else {
      var snapshot=EligibilityHooks.data(minecraft.level);
      var selected=snapshot==null?null:snapshot.rails.stream().filter(r->r.id.equals(target.id())).findFirst().orElse(null);
      if(selected!=null&&selected.family==dev.fourthdimension.contract.Family.ROLLER_BALL)
        hint="Use section tags for lift, swing and return; bind the Lift Control.";
    }
    String instruction = feedback.isEmpty() ? hint : feedback;
    graphics.drawString(
        font, font.plainSubstrByWidth(instruction, 340), left + 10, top + 42,
        feedbackError ? 0xff8888 : 0xc2d2dd);
    super.render(graphics, mouseX, mouseY, partialTick);
    clickQueuedRecoveryAttach();
    clickQueuedRecoveryStartTow();
    if (mouseX >= left + 10 && mouseX < left + 350 && mouseY >= top + 26 && mouseY < top + 36)
      graphics.renderTooltip(
          font, font.split(Component.literal(status), Math.min(300, width - 24)), mouseX, mouseY);
    if (!feedback.isEmpty()
        && mouseX >= left + 10 && mouseX < left + 350
        && mouseY >= top + 42 && mouseY < top + 52)
      graphics.renderTooltip(
          font, font.split(Component.literal(feedback), Math.min(300, width - 24)), mouseX, mouseY);
  }

  @EventBusSubscriber(modid = "fourthdimension", value = Dist.CLIENT)
  public static final class Registration {
    @SubscribeEvent
    public static void setup(FMLClientSetupEvent event) {
      event.enqueueWork(
          () -> {
              EditorNetwork.openClient =
                  target ->
                      net.minecraft.client.Minecraft.getInstance()
                          .setScreen(new RotationEditorScreen(target));
              EditorNetwork.resultClient = result -> {
                var screen = net.minecraft.client.Minecraft.getInstance().screen;
                if (screen instanceof RotationEditorScreen editor) editor.receiveResult(result);
                else if(screen instanceof NewGenerationFlyingScreen editor)editor.receive(result);
              };
          });
    }
  }
}
