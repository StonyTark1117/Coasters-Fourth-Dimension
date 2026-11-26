package dev.fourthdimension.client.moving;
import dev.fourthdimension.moving.*;
import java.util.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
/** In-game construction and compatibility teaching reads the same finite permission catalog. */
public final class MovingGuideScreen extends Screen {
  private record Line(FormattedCharSequence text,int color){}
  private final Screen parent;private final MovingMechanism mechanism;private final boolean compatibility;private int page;
  public MovingGuideScreen(Screen parent,MovingMechanism mechanism,boolean compatibility){super(MovingScreen.tr(compatibility?"compatibility":"guide"));this.parent=parent;this.mechanism=mechanism;this.compatibility=compatibility;}
  @Override public boolean isPauseScreen(){return false;}
  @Override public void renderBackground(GuiGraphics g,int mx,int my,float dt){}
  private int contentWidth(){return Math.min(400,width-24);}
  private void add(List<Line> lines,Component text,int color){for(var line:font.split(text,contentWidth()))lines.add(new Line(line,color));}
  private List<List<Line>> pages(){
    int rows=Math.max(1,(height-78)/11);var result=new ArrayList<List<Line>>();
    if(compatibility){
      var current=new ArrayList<Line>();
      for(var entry:MovingCapabilityCatalog.entries()){
        var lines=new ArrayList<Line>();add(lines,MovingScreen.tr("configuration."+entry.configuration().id()),0xffffff);
        var decision=entry.permission(mechanism,mechanism==MovingMechanism.TILT?Math.toRadians(90):mechanism==MovingMechanism.TEETER||mechanism==MovingMechanism.COMPOUND?Math.toRadians(15):0);
        add(lines,MovingScreen.tr("decision."+decision.code().name().toLowerCase(Locale.ROOT)),decision.allowed()?0x90eea0:0xffb090);
        add(lines,MovingScreen.tr("compatibility_details",Component.translatable("moving.fourthdimension.contact."+entry.insert().contacts()),Component.translatable("moving.fourthdimension.mount."+entry.insert().mount()),Math.min(4,entry.maxCars()),Math.round(Math.toDegrees(entry.maxTiltRadians()))),0xbccbd9);
        lines.add(new Line(Component.empty().getVisualOrderText(),0xffffff));
        if(!current.isEmpty()&&current.size()+lines.size()>rows){result.add(List.copyOf(current));current.clear();}
        for(var line:lines){if(current.size()==rows){result.add(List.copyOf(current));current.clear();}current.add(line);}
      }
      if(!current.isEmpty())result.add(List.copyOf(current));
    }else for(int chapter=0;chapter<=10;chapter++){
      var lines=new ArrayList<Line>();add(lines,MovingScreen.tr("guide.page."+chapter),0xdce7ec);
      for(int start=0;start<lines.size();start+=rows)result.add(List.copyOf(lines.subList(start,Math.min(lines.size(),start+rows))));
    }
    if(result.isEmpty())result.add(List.of());return result;
  }
  @Override protected void init(){int span=contentWidth(),left=(width-span)/2,button=(span-16)/3;
    addRenderableWidget(Button.builder(MovingScreen.tr("previous"),b->{page=Math.max(0,page-1);}).bounds(left,height-28,button,20).build());
    addRenderableWidget(Button.builder(MovingScreen.tr("next"),b->{page=Math.min(pages().size()-1,page+1);}).bounds(left+button+8,height-28,button,20).build());
    addRenderableWidget(Button.builder(MovingScreen.tr("back"),b->onClose()).bounds(left+2*(button+8),height-28,button,20).build());
  }
  @Override public void onClose(){minecraft.setScreen(parent);}
  @Override public void render(GuiGraphics g,int mx,int my,float dt){
    g.fill(0,0,width,height,0xee18202a);super.render(g,mx,my,dt);g.drawCenteredString(font,title,width/2,12,0xffffff);
    var pages=pages();page=Math.min(page,pages.size()-1);int left=(width-contentWidth())/2,y=38;
    for(var line:pages.get(page)){g.drawString(font,line.text(),left,y,line.color());y+=11;}
    g.drawCenteredString(font,Component.literal((page+1)+" / "+pages.size()),width/2,height-42,0xbccbd9);
  }
}
