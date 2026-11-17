package dev.fourthdimension.client.tools;

import dev.fourthdimension.editor.EditorNetwork;
import dev.fourthdimension.integration.RideTelemetry;
import dev.fourthdimension.tools.ToolsNetwork;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

public final class OperatorScreen extends Screen {
  private final EditorNetwork.Target target;private final Screen parent;
  private CompoundTag state=new CompoundTag();private int ticks,received=-1000;
  private EditorNetwork.Edit pending;private String feedback="Waiting for authoritative ride state…";
  public OperatorScreen(EditorNetwork.Target target,Screen parent){super(Component.literal("Ride operator panel"));this.target=target;this.parent=parent;}
  @Override public boolean isPauseScreen(){return false;}
  @Override protected void init(){int x=width/2-210,y=height/2-112;
    button("Capture / stop",x,y+122,10);button("Close restraints",x+140,y+122,11);button("Open restraints",x+280,y+122,12);
    button("Dispatch",x,y+144,13);button("Retain",x+140,y+144,14);button("Release retention",x+280,y+144,15);
    if(state.getBoolean("pax")){button("Resume shuttle",x,y+166,92);button("Recover shuttle",x+140,y+166,93);}
    else if(state.getBoolean("powered"))button("Resume powered",x,y+166,71);
    else if(state.getBoolean("authority"))button("Resume authority",x,y+166,62);
    button("Brake recovery tow",x+280,y+166,22);
    addRenderableWidget(Button.builder(Component.literal("Vehicle / recovery controls"),b->onClose()).bounds(x,y+188,270,20).build());
    addRenderableWidget(Button.builder(Component.literal("Close"),b->minecraft.setScreen(null)).bounds(x+280,y+188,130,20).build());
  }
  private void button(String label,int x,int y,int action){
    var b=addRenderableWidget(Button.builder(Component.literal(label),w->{if(pending!=null)return;pending=new EditorNetwork.Edit(target.id(),state.getLong("revision"),action,"");feedback="Waiting for server…";PacketDistributor.sendToServer(pending);rebuildWidgets();}).bounds(x,y,130,20).build());
    var snapshot=RideTelemetry.load(state);
    b.active=pending==null&&ticks-received<60&&snapshot.available()&&(action!=13||snapshot.dispatchReady());
    if(action==13&&!snapshot.dispatchReady())b.setTooltip(Tooltip.create(Component.literal(snapshot.dispatchDenial())));
  }
  @Override public void tick(){if(ticks++%20==0)poll();if(ticks-received==60){feedback="State timed out; commands disabled until refreshed.";rebuildWidgets();}}
  private void poll(){PacketDistributor.sendToServer(new EditorNetwork.Edit(target.id(),0,91,""));}
  public void receive(ToolsNetwork.State update){if(!update.id().equals(target.id())||update.tag()==null)return;state=update.tag();received=ticks;if(feedback.startsWith("Waiting for authoritative"))feedback="Server state received; commands recheck permission and revision.";rebuildWidgets();}
  public void receive(EditorNetwork.Result result){if(pending==null||!result.id().equals(pending.id())||result.action()!=pending.action()||result.requestedRevision()!=pending.revision())return;pending=null;feedback=result.message();received=-1000;rebuildWidgets();poll();}
  @Override public void onClose(){minecraft.setScreen(parent);}
  @Override public void renderBackground(GuiGraphics g,int x,int y,float pt){}
  @Override public void render(GuiGraphics g,int mouseX,int mouseY,float pt){int x=width/2-218,y=height/2-120;g.fill(x,y,x+436,y+240,0xf018202a);g.drawString(font,title,x+8,y+8,0xffffff);var t=RideTelemetry.load(state);
    String[] lines={t.family()+" | "+t.phase()+" | "+(t.available()?"Available":"Unavailable"),
      "Whole consist: "+t.occupied()+" / "+t.capacity()+" riders, "+t.cars()+" cars",
      "Restraints "+(t.restraintsClosed()?"closed":"open/mixed")+" | captured "+t.captured()+" | retained "+t.retained(),
      "Controller: "+state.getString("controller"),"Power: "+state.getString("power"),"Recovery: "+state.getString("recovery"),
      "Dispatch: "+(t.dispatchReady()?"ready":t.dispatchDenial()),"Fault: "+(t.fault().isBlank()?"none":t.fault()),state.getString("optional")};
    for(int i=0;i<lines.length;i++){int row=y+25+i*11;g.drawString(font,font.plainSubstrByWidth(lines[i],416),x+8,row,i==6?0xffd185:0xc9d5e8);if(mouseX>=x&&mouseX<x+436&&mouseY>=row&&mouseY<row+12)g.renderTooltip(font,font.split(Component.literal(lines[i]),380),mouseX,mouseY);}
    g.drawWordWrap(font,Component.literal(feedback),x+8,y+218,416,0xffd185);super.render(g,mouseX,mouseY,pt);
  }
}
