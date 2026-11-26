package dev.fourthdimension.client.moving;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Full authoritative server feedback remains readable even in a small game window. */
public final class MovingMessageScreen extends Screen {
  private final Screen parent;
  private final Component message;
  private int page;
  public MovingMessageScreen(Screen parent,Component message){super(MovingScreen.tr("details"));this.parent=parent;this.message=message;}
  private int rows(){return Math.max(1,(height-80)/11);}
  private int textWidth(){return Math.min(400,width-24);}
  private int pages(){return Math.max(1,(font.split(message,textWidth()).size()+rows()-1)/rows());}
  @Override protected void init(){int w=Math.min(100,(width-32)/3),left=(width-3*w-8)/2;
    addRenderableWidget(Button.builder(MovingScreen.tr("previous"),b->{page=Math.max(0,page-1);}).bounds(left,height-28,w,20).build());
    addRenderableWidget(Button.builder(MovingScreen.tr("back"),b->onClose()).bounds(left+w+4,height-28,w,20).build());
    addRenderableWidget(Button.builder(MovingScreen.tr("next"),b->{page=Math.min(pages()-1,page+1);}).bounds(left+2*w+8,height-28,w,20).build());
  }
  @Override public void renderBackground(GuiGraphics g,int mx,int my,float dt){}
  @Override public void render(GuiGraphics g,int mx,int my,float dt){g.fill(0,0,width,height,0xf018202a);g.drawCenteredString(font,title,width/2,10,0xffffff);
    var lines=font.split(message,textWidth());int start=page*rows(),x=(width-textWidth())/2;
    for(int i=start;i<Math.min(lines.size(),start+rows());i++)g.drawString(font,lines.get(i),x,34+(i-start)*11,0xffdfb0);
    g.drawCenteredString(font,Component.literal((page+1)+" / "+pages()),width/2,height-43,0xaabbd0);super.render(g,mx,my,dt);
  }
  @Override public void onClose(){minecraft.setScreen(parent);}
}
