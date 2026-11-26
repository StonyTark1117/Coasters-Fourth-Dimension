package dev.fourthdimension.client.moving;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
public final class MovingBillScreen extends Screen {
  private final Screen parent;private final ListTag costs;private int page;
  public MovingBillScreen(Screen parent,ListTag costs){super(MovingScreen.tr("materials"));this.parent=parent;this.costs=costs.copy();}
  @Override public boolean isPauseScreen(){return false;}
  @Override public void renderBackground(GuiGraphics g,int mx,int my,float dt){}
  private int rows(){return Math.max(1,(height-80)/18);}
  @Override protected void init(){int x=width/2-180;addRenderableWidget(Button.builder(MovingScreen.tr("previous"),b->{page=Math.max(0,page-1);}).bounds(x,height-28,110,20).build());addRenderableWidget(Button.builder(MovingScreen.tr("next"),b->{page=Math.min(Math.max(0,(costs.size()-1)/rows()),page+1);}).bounds(x+125,height-28,110,20).build());addRenderableWidget(Button.builder(MovingScreen.tr("back"),b->onClose()).bounds(x+250,height-28,110,20).build());}
  @Override public void onClose(){minecraft.setScreen(parent);}
  @Override public void render(GuiGraphics g,int mx,int my,float dt){g.fill(0,0,width,height,0xee18202a);g.drawCenteredString(font,title,width/2,12,0xffffff);int x=width/2-180,y=42;
    if(costs.isEmpty())g.drawString(font,MovingScreen.tr("materials_preview"),x,y,0xffcc99);
    for(int i=page*rows();i<Math.min(costs.size(),page*rows()+rows());i++){var t=costs.getCompound(i);var item=BuiltInRegistries.ITEM.get(ResourceLocation.parse(t.getString("item")));g.drawString(font,Component.literal(t.getInt("count")+" × ").append(item.getDescription()),x,y,0xffffff);y+=18;}
    super.render(g,mx,my,dt);
  }
}
