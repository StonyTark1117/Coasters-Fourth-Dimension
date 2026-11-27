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
  private int panelX,panelY,panelW,panelH,controlTop,feedbackTop,buttonW,buttonH,controlGap;
  private boolean compact;private int nextControl;
  public OperatorScreen(EditorNetwork.Target target,Screen parent){super(Component.literal("Ride operator panel"));this.target=target;this.parent=parent;}
  @Override public boolean isPauseScreen(){return false;}
  @Override protected void init(){
    compact=height<300;
    panelW=Math.min(436,Math.max(1,width-8)); panelH=Math.min(280,Math.max(1,height-8));
    panelX=(width-panelW)/2; panelY=(height-panelH)/2;
    buttonW=Math.max(1,(panelW-16)/3); buttonH=compact?14:20; controlGap=compact?2:3;
    int statusBottom=panelY+21+11*(compact?9:11);
    controlTop=statusBottom+(compact?4:8); nextControl=0;
    action(state.getBoolean("external")?"Hold equipment":"Capture / stop",10); action("Close restraints",11); action("Open restraints",12);
    action("Dispatch",13); action("Retain",14); action("Release retention",15);
    if(state.getBoolean("pax")){action("Resume shuttle",92);action("Recover shuttle",93);}
    else if(state.getBoolean("powered"))action("Resume powered",71);
    else if(state.getBoolean("authority"))action("Resume authority",state.getBoolean("external")?61:62);
    action("Brake recovery tow",22); action("Admit route train",94); action("Resume route",95);
    screen(compact?"Vehicle / recovery":"Vehicle / recovery controls",this::onClose);
    screen("Close",()->minecraft.setScreen(null));
    feedbackTop=controlTop+((nextControl+2)/3)*(buttonH+controlGap)+4;
  }
  private void action(String label,int action){
    int index=nextControl++,x=panelX+4+(index%3)*(buttonW+4),y=controlTop+(index/3)*(buttonH+controlGap);
    var b=addRenderableWidget(Button.builder(Component.literal(label),w->{if(pending!=null)return;pending=new EditorNetwork.Edit(target.id(),state.getLong("revision"),action,state.getBoolean("external")&&(action==10||action==13||action==61)?EditorNetwork.OPERATOR_REQUEST_PREFIX+Long.toString(state.getCompound("operator").getLong("request_revision")):"");feedback="Waiting for server…";PacketDistributor.sendToServer(pending);rebuildWidgets();}).bounds(x,y,buttonW,buttonH).build());
    var snapshot=RideTelemetry.load(state);
    b.active=pending==null&&ticks-received<60&&snapshot.available()
      &&(action!=13||snapshot.dispatchReady())
      &&((action!=94&&action!=95)||state.getBoolean("route_managed"));
    if(action==13&&!snapshot.dispatchReady())b.setTooltip(Tooltip.create(Component.literal(snapshot.dispatchDenial())));
  }
  private void screen(String label,Runnable close){
    int index=nextControl++,x=panelX+4+(index%3)*(buttonW+4),y=controlTop+(index/3)*(buttonH+controlGap);
    var b=addRenderableWidget(Button.builder(Component.literal(label),w->close.run()).bounds(x,y,buttonW,buttonH).build());
    if(compact&&label.startsWith("Vehicle")) b.setTooltip(Tooltip.create(Component.literal("Vehicle / recovery controls")));
  }
  @Override public void tick(){if(ticks++%20==0)poll();if(ticks-received==60){feedback="State timed out; commands disabled until refreshed.";rebuildWidgets();}}
  private void poll(){PacketDistributor.sendToServer(new EditorNetwork.Edit(target.id(),0,91,""));}
  public void receive(ToolsNetwork.State update){if(!update.id().equals(target.id())||update.tag()==null)return;state=update.tag();received=ticks;if(feedback.startsWith("Waiting for authoritative"))feedback="Server state received; commands recheck permission and revision.";rebuildWidgets();}
  public void receive(EditorNetwork.Result result){if(pending==null||!result.id().equals(pending.id())||result.action()!=pending.action()||result.requestedRevision()!=pending.revision())return;pending=null;feedback=result.message();received=-1000;rebuildWidgets();poll();}
  @Override public void onClose(){minecraft.setScreen(parent);}
  @Override public void renderBackground(GuiGraphics g,int x,int y,float pt){}
  @Override public void render(GuiGraphics g,int mouseX,int mouseY,float pt){int x=panelX,y=panelY;g.fill(x,y,x+panelW,y+panelH,0xf018202a);g.drawString(font,title,x+8,y+7,0xffffff);var t=RideTelemetry.load(state);
    String[] lines={t.family()+" | "+t.phase()+" | "+(t.available()?"Available":"Unavailable"),
      "Whole consist: "+t.occupied()+" / "+t.capacity()+" riders, "+t.cars()+" cars",
      "Restraints "+(t.restraintsClosed()?"closed":"open/mixed")+" | captured "+t.captured()+" | retained "+t.retained(),
      "Controller: "+state.getString("controller"),"Power: "+state.getString("power"),"Recovery: "+state.getString("recovery"),
      "Dispatch: "+(t.dispatchReady()?"ready":t.dispatchDenial()),"Fault: "+(t.fault().isBlank()?"none":t.fault()),
      "Route: "+state.getString("route")+" | trains "+state.getInt("route_trains")+" | block "+(state.getInt("route_block")<0?"—":state.getInt("route_block")),
      "Route status: "+(state.getString("route_denial").isBlank()?"ready":state.getString("route_denial")),state.getString("optional")};
    int lineHeight=compact?9:11;
    for(int i=0;i<lines.length;i++){int row=y+21+i*lineHeight;g.drawString(font,font.plainSubstrByWidth(lines[i],panelW-16),x+8,row,i==6?0xffd185:0xc9d5e8);if(mouseX>=x&&mouseX<x+panelW&&mouseY>=row&&mouseY<row+lineHeight)g.renderTooltip(font,font.split(Component.literal(lines[i]),panelW-16),mouseX,mouseY);}
    g.drawWordWrap(font,Component.literal(feedback),x+8,Math.min(feedbackTop,y+panelH-18),panelW-16,0xffd185);super.render(g,mouseX,mouseY,pt);
  }
}
