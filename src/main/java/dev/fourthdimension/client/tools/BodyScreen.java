package dev.fourthdimension.client.tools;

import com.mojang.blaze3d.platform.Lighting;
import dev.fourthdimension.editor.EditorNetwork;
import dev.fourthdimension.runtime.EligibilityHooks;
import dev.fourthdimension.runtime.FoundationData.Vehicle;
import dev.fourthdimension.tools.VehicleBody;
import dev.fourthdimension.track.TrackItems;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

/** A local body preview becomes persistent only after the guarded server edit succeeds. */
public final class BodyScreen extends Screen {
  private static final int PANEL_WIDTH = 224;
  private static final int APPLY_BODY = 82;
  private final EditorNetwork.Target target;
  private final Screen parent;
  private VehicleBody selected;
  private EditorNetwork.Edit pending;
  private long displayedRevision;
  private boolean previewing;
  private String feedback = "Choose a body to preview. Apply saves it on this car.";

  public BodyScreen(EditorNetwork.Target target, Screen parent) {
    super(Component.literal("Car body"));
    this.target = target;
    this.parent = parent;
  }

  private Vehicle vehicle() {
    var data = EligibilityHooks.data(minecraft.level);
    return data == null ? null : data.vehicles.get(target.id());
  }

  @Override public boolean isPauseScreen() { return false; }

  @Override protected void init() {
    var vehicle = vehicle();
    if (vehicle == null || !VehicleBody.supported(vehicle)) {
      onClose();
      return;
    }
    if (selected == null) selected = VehicleBody.resolve(vehicle);
    if (pending == null) displayedRevision = vehicle.revision;
    int x = width - PANEL_WIDTH + 8, y = 30;
    for (var choice : VehicleBody.choices(vehicle)) {
      addRenderableWidget(Button.builder(Component.literal((choice == selected ? "✓ " : "") + choice.label),
          button -> {
            selected = choice;
            previewing = true;
            BodyModels.preview(target.id(), choice);
            feedback = "Local preview only. Apply saves it on this car.";
            rebuildWidgets();
          }).bounds(x, y, PANEL_WIDTH - 16, 20).build()).active = pending == null;
      y += 24;
    }
    addRenderableWidget(Button.builder(Component.literal("Apply body"), button -> apply())
        .bounds(x, height - 50, PANEL_WIDTH - 16, 20).build()).active = pending == null;
    addRenderableWidget(Button.builder(Component.literal(pending == null ? "Back / cancel preview" : "Back (request sent)"),
        button -> onClose()).bounds(x, height - 26, PANEL_WIDTH - 16, 20).build());
  }

  private void apply() {
    if (pending != null || selected == null) return;
    pending = new EditorNetwork.Edit(target.id(), displayedRevision, APPLY_BODY, selected.id);
    feedback = "Waiting for the server to save this body…";
    rebuildWidgets();
    PacketDistributor.sendToServer(pending);
  }

  public void receive(EditorNetwork.Result result) {
    if (pending == null || !result.id().equals(pending.id())
        || result.action() != pending.action()
        || result.requestedRevision() != pending.revision()) return;
    pending = null;
    feedback = result.message();
    BodyModels.clearPreview();
    previewing = false;
    // The authoritative snapshot precedes the result, including after a stale edit.
    selected = null;
    rebuildWidgets();
  }

  @Override public void tick() {
    if (vehicle() == null) onClose();
  }

  @Override public void removed() { BodyModels.clearPreview(); }

  @Override public void onClose() {
    BodyModels.clearPreview();
    minecraft.setScreen(parent);
  }

  @Override public void renderBackground(GuiGraphics graphics, int x, int y, float partialTick) {}

  @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    int left = width - PANEL_WIDTH;
    graphics.fill(left, 0, width, height, 0xe018202a);
    graphics.drawCenteredString(font, title, left + PANEL_WIDTH / 2, 10, 0xffffff);
    var vehicle = vehicle();
    if (vehicle != null && selected != null) {
      var model = BodyModels.miniature(selected, vehicle);
      boolean unavailable = model == null;
      if (unavailable) model = BodyModels.miniature(VehicleBody.BASE, vehicle);
      if (model != null) renderMiniature(graphics, model, vehicle, left + PANEL_WIDTH / 2, 106);
      graphics.drawCenteredString(font, previewing ? "Local preview" : "Saved body", left + PANEL_WIDTH / 2, 133, 0xc9d5e8);
      String note = unavailable ? "Model unavailable; showing Original."
          : !previewing && !selected.id.equals(vehicle.bodyId)
              ? "Saved body unavailable; showing Original."
              : "Train must be empty, stopped and captured.";
      graphics.fill(0, height - 80, Math.max(0, left - 4), height, 0xe018202a);
      graphics.drawWordWrap(font, Component.literal(note), 8, height - 74, Math.max(80, left - 20), 0xffd185);
      graphics.drawWordWrap(font, Component.literal("Seat colors and optional chassis appearance stay separate."),
          8, height - 38, Math.max(80, left - 20), 0xc9d5e8);
    }
    var feedbackLines = font.split(Component.literal(feedback), PANEL_WIDTH - 16);
    int visibleLines = Math.max(1, (height - 202) / font.lineHeight);
    for (int i = 0; i < Math.min(feedbackLines.size(), visibleLines); i++) {
      FormattedCharSequence line = feedbackLines.get(i);
      if (i == visibleLines - 1 && feedbackLines.size() > visibleLines)
        line = Component.literal("More details… (hover)").getVisualOrderText();
      graphics.drawString(font, line, left + 8, 148 + i * font.lineHeight, 0xffd185);
    }
    super.render(graphics, mouseX, mouseY, partialTick);
    if (feedbackLines.size() > visibleLines && mouseX >= left + 8 && mouseX < width - 8
        && mouseY >= 148 && mouseY < height - 54)
      graphics.renderTooltip(font, font.split(Component.literal(feedback), Math.min(300, width - 24)), mouseX, mouseY);
  }

  private void renderMiniature(GuiGraphics graphics, BakedModel model, Vehicle vehicle, int x, int y) {
    ItemStack stack = VehicleBody.choices(vehicle).contains(VehicleBody.WING_COMPACT)
        ? TrackItems.WING_CAR.get().getDefaultInstance()
        : TrackItems.FIXED_INVERTED_CAR.get().getDefaultInstance();
    graphics.flush();
    graphics.pose().pushPose();
    try {
      graphics.pose().translate(x, y, 150);
      graphics.pose().scale(48, -48, 48);
      if (!model.usesBlockLight()) Lighting.setupForFlatItems();
      minecraft.getItemRenderer().render(stack, ItemDisplayContext.GUI, false, graphics.pose(),
          graphics.bufferSource(), LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, model);
      graphics.flush();
    } finally {
      graphics.pose().popPose();
      Lighting.setupFor3DItems();
    }
  }
}
