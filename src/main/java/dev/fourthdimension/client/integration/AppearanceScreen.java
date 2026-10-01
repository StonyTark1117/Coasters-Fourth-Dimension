package dev.fourthdimension.client.integration;

import dev.fourthdimension.editor.EditorNetwork;
import dev.fourthdimension.integration.TrackAppearance;
import dev.fourthdimension.runtime.EligibilityHooks;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

/** A section appearance editor; ride mechanics and rotation editing remain separate. */
public final class AppearanceScreen extends Screen {
  private final UUID railId;
  private long revision;
  private List<String> choices;
  private int choice;
  private EditBox color;
  private String savedStatus = "";

  public AppearanceScreen(UUID railId) {
    super(Component.literal("Track appearance"));
    this.railId = railId;
  }

  @Override
  public boolean isPauseScreen() {
    return false;
  }

  private String name(String id) {
    return id.equals(TrackAppearance.ORIGINAL)
        ? "Original"
        : id.endsWith("arrow_4d_dbl_spine")
            ? "Track Styles: Arrow double spine"
            : id.endsWith("arrow_4d")
                ? "Track Styles: Arrow spine"
                : "Unavailable saved appearance";
  }

  @Override
  protected void init() {
    var data = EligibilityHooks.data(minecraft.level);
    var rail =
        data == null
            ? null
            : data.rails.stream().filter(r -> r.id.equals(railId)).findFirst().orElse(null);
    if (rail == null) {
      onClose();
      return;
    }
    revision = rail.revision;
    choices = new ArrayList<>();
    choices.add(TrackAppearance.ORIGINAL);
    for (String id : TrackAppearance.CANDIDATES)
      if (new TrackAppearance(id, -1).supports(rail.family)
          && TrackStylesCatalog.snapshot().containsKey(new TrackAppearance(id, -1).style()))
        choices.add(id);
    if (!choices.contains(rail.appearance.id())) {
      choices.add(rail.appearance.id());
      savedStatus = "Saved style unavailable; original geometry is shown";
    }
    choice = choices.indexOf(rail.appearance.id());
    int left = width / 2 - 150, top = height / 2 - 70;
    addRenderableWidget(
        Button.builder(
                Component.literal(name(choices.get(choice))),
                button -> {
                  choice = (choice + 1) % choices.size();
                  button.setMessage(Component.literal(name(choices.get(choice))));
                })
            .bounds(left, top + 26, 300, 20)
            .build());
    color = new EditBox(font, left, top + 65, 300, 20, Component.literal("Spine and tie color"));
    color.setMaxLength(7);
    color.setValue(rail.appearance.formattedColor());
    addRenderableWidget(color);
    addRenderableWidget(
        Button.builder(
                Component.literal("Apply"),
                button -> {
                  try {
                    var selected =
                        TrackAppearance.parse(choices.get(choice) + ";" + color.getValue());
                    PacketDistributor.sendToServer(
                        new EditorNetwork.Edit(
                            railId, revision, 5, selected.id() + ";" + selected.formattedColor()));
                    onClose();
                  } catch (IllegalArgumentException failure) {
                    savedStatus = failure.getMessage();
                  }
                })
            .bounds(left, top + 102, 145, 20)
            .build());
    addRenderableWidget(
        Button.builder(Component.literal("Cancel"), button -> onClose())
            .bounds(left + 155, top + 102, 145, 20)
            .build());
  }

  @Override
  public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    renderBackground(graphics, mouseX, mouseY, partialTick);
    super.render(graphics, mouseX, mouseY, partialTick);
    int left = width / 2 - 150, top = height / 2 - 70;
    graphics.drawString(font, title, left, top + 4, 0xffffff);
    graphics.drawString(
        font, "Spine / tie RGB (#RRGGBB), empty = family color", left, top + 53, 0xb7c9cd);
    graphics.drawString(font, savedStatus, left, top + 130, 0xffca70);
  }
}
