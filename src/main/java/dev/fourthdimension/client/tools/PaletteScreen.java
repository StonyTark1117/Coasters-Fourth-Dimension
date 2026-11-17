package dev.fourthdimension.client.tools;

import dev.fourthdimension.tools.VehiclePalette;
import dev.fourthdimension.runtime.EligibilityHooks;
import dev.fourthdimension.editor.EditorNetwork;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

/** Local world preview is discarded on leaving; only Apply asks the server to persist it. */
public final class PaletteScreen extends Screen {
  private final EditorNetwork.Target target;private final Screen parent;
  private VehiclePalette selected;private boolean pending;private long revision;private String feedback="Preview changes this view only. Apply saves on the server.";
  public PaletteScreen(EditorNetwork.Target target,Screen parent){super(Component.literal("Original upholstery colors"));this.target=target;this.parent=parent;}
  @Override public boolean isPauseScreen(){return false;}
  @Override protected void init(){var data=EligibilityHooks.data(minecraft.level);var v=data==null?null:data.vehicles.get(target.id());if(v==null){onClose();return;}if(selected==null)selected=v.palette;
    int x=width-164,y=30;
    for(var palette:VehiclePalette.values()){addRenderableWidget(Button.builder(Component.literal((selected==palette?"Preview: ":"")+palette.label),b->{selected=palette;PaletteModels.preview(target.id(),palette);rebuildWidgets();}).bounds(x,y,156,20).build()).active=!pending;y+=24;}
    addRenderableWidget(Button.builder(Component.literal("Apply color"),b->{revision=v.revision;pending=true;PacketDistributor.sendToServer(new EditorNetwork.Edit(target.id(),revision,81,selected.name()));rebuildWidgets();}).bounds(x,y+8,156,20).build()).active=!pending;
    addRenderableWidget(Button.builder(Component.literal("Back / cancel preview"),b->onClose()).bounds(x,y+32,156,20).build());
  }
  public void receive(EditorNetwork.Result r){if(!pending||!r.id().equals(target.id())||r.action()!=81||r.requestedRevision()!=revision)return;pending=false;feedback=r.message();PaletteModels.clearPreview();rebuildWidgets();}
  @Override public void removed(){PaletteModels.clearPreview();}
  @Override public void onClose(){PaletteModels.clearPreview();minecraft.setScreen(parent);}
  @Override public void renderBackground(GuiGraphics g,int x,int y,float pt){}
  @Override public void render(GuiGraphics g,int x,int y,float pt){g.fill(width-172,0,width,height,0xe018202a);g.drawWordWrap(font,title,width-164,8,156,0xffffff);g.drawWordWrap(font,Component.literal(feedback),width-164,210,156,0xffd185);g.fill(0,height-40,width-176,height,0xe018202a);g.drawWordWrap(font,Component.literal("Original seat cushions only. Safety colors, mechanisms and optional chassis stay unchanged."),8,height-34,Math.max(140,width-190),0xc9d5e8);super.render(g,x,y,pt);}
}
