package dev.fourthdimension.client.moving;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.*;
/** Exact server-owned block identities; fractional rail endpoints are installed only by Build. */
public final class MovingSocketScreen extends Screen {
  private final MovingScreen parent;private final ListTag docks;
  public MovingSocketScreen(MovingScreen parent,CompoundTag state){super(MovingScreen.tr("sockets"));this.parent=parent;docks=state.getList("docks",Tag.TAG_COMPOUND).copy();}
  @Override public boolean isPauseScreen(){return false;}
  @Override public void renderBackground(GuiGraphics g,int mx,int my,float dt){}
  @Override protected void init(){addRenderableWidget(Button.builder(MovingScreen.tr("back"),b->onClose()).bounds(width/2-70,height-28,140,20).build());}
  @Override public void onClose(){minecraft.setScreen(parent);}
  @Override public void render(GuiGraphics g,int mx,int my,float dt){
    g.fill(0,0,width,height,0xee18202a);super.render(g,mx,my,dt);g.drawCenteredString(font,title,width/2,12,0xffffff);int left=Math.max(12,width/2-190),y=38;
    if(docks.isEmpty())g.drawString(font,MovingScreen.tr("sockets_preview"),left,y,0xffd090);
    for(var raw:docks){var dock=(CompoundTag)raw;g.drawString(font,MovingScreen.tr("socket_name",dock.getString("name")),left,y,0x88ffbb);y+=16;
      for(String end:new String[]{"from","to"}){var p=BlockPos.of(dock.getLong(end));g.drawString(font,MovingScreen.tr("socket_position",end,p.getX(),p.getY(),p.getZ()),left+8,y,0xffffff);y+=14;}y+=12;
    }
    y+=6;for(var line:font.split(MovingScreen.tr("sockets_teaching"),Math.min(380,width-24))){g.drawString(font,line,left,y,0xbfd4df);y+=11;}
  }
}
