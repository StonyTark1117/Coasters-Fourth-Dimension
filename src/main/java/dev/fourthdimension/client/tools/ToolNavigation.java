package dev.fourthdimension.client.tools;

import dev.fourthdimension.editor.EditorNetwork;
import dev.fourthdimension.client.SkinAppearanceScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import java.util.function.Consumer;

/** A single header entry keeps the existing editor usable at its minimum window size. */
public final class ToolNavigation extends Screen {
  private final Screen parent;
  private final EditorNetwork.Target target;
  private ToolNavigation(Screen parent, EditorNetwork.Target target) {
    super(Component.literal("Builder and operator tools"));
    this.parent = parent;
    this.target = target;
  }
  public static void add(Screen parent, EditorNetwork.Target target, int x, int y, int width, Consumer<Button> add) {
    add.accept(Button.builder(Component.literal("Tools"), b -> Minecraft.getInstance().setScreen(new ToolNavigation(parent, target)))
        .bounds(x, y, width, 14).build());
  }
  @Override public boolean isPauseScreen() { return false; }
  @Override protected void init() {
    int x = width / 2 - 120, y = height / 2 - 65;
    addRenderableWidget(Button.builder(Component.literal("Setting presets"), b -> minecraft.setScreen(new PresetScreen(target, this))).bounds(x, y, 240, 20).build());
    if (target.vehicle()) {
      addRenderableWidget(Button.builder(Component.literal("Original upholstery colors"), b -> minecraft.setScreen(new PaletteScreen(target, this))).bounds(x, y + 24, 240, 20).build());
      addRenderableWidget(Button.builder(Component.literal("Ride operator panel"), b -> minecraft.setScreen(new OperatorScreen(target, parent))).bounds(x, y + 48, 240, 20).build());
      addRenderableWidget(Button.builder(Component.literal("Optional chassis appearance"), b -> minecraft.setScreen(new SkinAppearanceScreen(target))).bounds(x, y + 72, 240, 20).build());
    }
    addRenderableWidget(Button.builder(Component.literal("Back to editor"), b -> onClose()).bounds(x, y + 104, 240, 20).build());
  }
  @Override public void onClose() { minecraft.setScreen(parent); }
  @Override public void renderBackground(GuiGraphics g, int x, int y, float pt) {}
  @Override public void render(GuiGraphics g, int x, int y, float pt) {
    g.fill(width / 2 - 140, height / 2 - 92, width / 2 + 140, height / 2 + 78, 0xf018202a);
    g.drawCenteredString(font, title, width / 2, height / 2 - 82, 0xffffff);
    super.render(g, x, y, pt);
  }
}
