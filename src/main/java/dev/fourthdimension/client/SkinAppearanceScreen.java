package dev.fourthdimension.client;

import dev.fourthdimension.editor.EditorNetwork;
import dev.fourthdimension.integration.*;
import dev.fourthdimension.runtime.EligibilityHooks;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

public final class SkinAppearanceScreen extends Screen {
  private final EditorNetwork.Target target;
  private String feedback="";
  private boolean pending;
  public SkinAppearanceScreen(EditorNetwork.Target target) {
    super(Component.literal("Chassis appearance"));this.target=target;
  }
  @Override public boolean isPauseScreen(){return false;}
  @Override protected void init() {
    var data=EligibilityHooks.data(minecraft.level);
    var vehicle=data==null?null:data.vehicles.get(target.id());
    if(vehicle==null){onClose();return;}
    int y=height/2-65;
    for(var choice:SkinAppearance.values()) {
      var button=addRenderableWidget(Button.builder(Component.literal((vehicle.appearance==choice?"Selected: ":"")+choice.label),b->{
        pending=true;rebuildWidgets();
        PacketDistributor.sendToServer(new EditorNetwork.Edit(target.id(),vehicle.revision,80,choice.id));
      }).bounds(width/2-120,y,240,20).build());
      button.active=!pending && (choice==SkinAppearance.ORIGINAL || SkinAdapter.supported(vehicle));
      y+=24;
    }
    addRenderableWidget(Button.builder(Component.literal("Back"),b->onClose()).bounds(width/2-60,y+8,120,20).build());
  }
  public void receive(EditorNetwork.Result result) {
    if(!result.id().equals(target.id())||result.action()!=80)return;
    pending=false;feedback=result.success()?"Appearance saved.":result.message();rebuildWidgets();
  }
  @Override public void onClose(){minecraft.setScreen(new RotationEditorScreen(target));}
  @Override public void render(GuiGraphics graphics,int mouseX,int mouseY,float partialTick) {
    super.render(graphics,mouseX,mouseY,partialTick);
    graphics.drawCenteredString(font,title,width/2,height/2-104,0xffffff);
    graphics.drawCenteredString(font,"Cosmetic chassis; original body, seats and restraints retained",width/2,height/2-89,0xbbbbbb);
    String note=feedback.isEmpty()?"Requires Coaster Skin 1.1.0. Missing provider/resources show Original.":feedback;
    graphics.drawWordWrap(font,Component.literal(note),width/2-170,height/2+100,340,0xffffff);
  }
}
