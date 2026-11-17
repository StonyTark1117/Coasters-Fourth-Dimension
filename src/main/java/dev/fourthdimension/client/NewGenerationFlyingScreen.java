package dev.fourthdimension.client;

import dev.fourthdimension.editor.EditorNetwork;
import dev.fourthdimension.runtime.*;
import dev.fourthdimension.vehicle.*;
import java.util.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

/** Finite section form and physical-state controls; ordinary rider look remains independent. */
public final class NewGenerationFlyingScreen extends Screen {
  private final EditorNetwork.Target target;
  private final List<EditBox> fields=new ArrayList<>();
  private static final String[] LABELS={"Section name","Start distance","End distance","Mode: PROFILE / TARGET / SHOW_STOP","Start posture (degrees)","End posture (degrees)","Yaw keys: distance:degrees; …","Marker position: x y z","Blend distance","Winding: NEAREST / POSITIVE / NEGATIVE","Max yaw speed (rad/s)","Max yaw acceleration (rad/s²)","Named show stop","Show duration (ticks)"};
  private String feedback="",draft;
  private EditorNetwork.Edit pending;
  private long revision;
  private int selected;
  public NewGenerationFlyingScreen(EditorNetwork.Target target){super(Component.literal("New-generation Flying"));this.target=target;}
  @Override public boolean isPauseScreen(){return false;}
  @Override protected void init(){fields.clear();var data=EligibilityHooks.data(minecraft.level);if(data==null){onClose();return;}
    int left=width/2-220,top=Math.max(12,height/2-135);
    if(target.vehicle()){
      var v=data.vehicles.get(target.id());if(v==null){onClose();return;}revision=v.revision;
      button("Stop",left,top+92,10,"");button(v.flying.body?"Open body":"Close body",left+145,top+92,v.flying.body?35:34,"");button(v.flying.legs?"Open legs":"Close legs",left+290,top+92,v.flying.legs?37:36,"");
      button("Dispatch route",left,top+118,13,"");button("Unwind + seated",left+145,top+118,40,"");button("Retry / revalidate",left+290,top+118,41,"");
      var rail=data.rails.stream().filter(r->r.matches(v.boundFrom,v.boundTo)).findFirst().orElse(null);var interval=rail==null?null:rail.yaw.intervals.stream().filter(i->i.id().equals(v.yaw.section)).findFirst().orElse(null);
      if(interval!=null&&interval.mode()==YawState.Mode.SHOW_STOP)button("Start "+interval.stopName(),left,top+144,42,UUID.randomUUID()+"|"+v.yaw.epoch+"|"+interval.stopName());
    }else{
      var r=data.rails.stream().filter(a->a.id.equals(target.id())).findFirst().orElse(null);if(r==null){onClose();return;}revision=r.revision;
      String values=draft!=null?draft:r.yaw.intervals.isEmpty()?"Section|0|24|PROFILE|0|90|0:90;24:180||8|NEAREST|0.6|0.8|Show|200":NewGenerationFlyingEditor.format(r.yaw.intervals.get(Math.min(selected,r.yaw.intervals.size()-1)));
      var split=values.split("\\|",-1);
      for(int i=0;i<LABELS.length;i++){int x=left+(i%2)*225,y=top+38+(i/2)*29;var box=new EditBox(font,x,y+10,215,16,Component.literal(LABELS[i]));box.setMaxLength(i==6?4096:128);box.setValue(split[i]);fields.add(box);addRenderableWidget(box);}
      addRenderableWidget(Button.builder(Component.literal("Save section"),b->submit(8,form())).bounds(left,top+246,140,20).build());
      addRenderableWidget(Button.builder(Component.literal("Next section"),b->{selected=(selected+1)%Math.max(1,r.yaw.intervals.size());draft=null;rebuildWidgets();}).bounds(left+145,top+246,140,20).build());
    }
    dev.fourthdimension.client.tools.ToolNavigation.add(this, target, left + 290, top, 140, this::addRenderableWidget);
    addRenderableWidget(Button.builder(Component.literal("Done"),b->onClose()).bounds(left+290,top+246,140,20).build());
    if(pending!=null)for(var child:children())if(child instanceof AbstractWidget w)w.active=false;
  }
  private String form(){return fields.stream().map(EditBox::getValue).collect(java.util.stream.Collectors.joining("|"));}
  private void button(String label,int x,int y,int action,String value){addRenderableWidget(Button.builder(Component.literal(label),b->submit(action,value)).bounds(x,y,140,20).build());}
  private void submit(int action,String value){if(pending!=null)return;pending=new EditorNetwork.Edit(target.id(),revision,action,value);feedback="Waiting for server…";for(var child:children())if(child instanceof AbstractWidget w)w.active=false;PacketDistributor.sendToServer(pending);}
  public void receive(EditorNetwork.Result result){if(pending==null||!pending.id().equals(result.id())||pending.revision()!=result.requestedRevision()||pending.action()!=result.action())return;draft=target.vehicle()?null:form();pending=null;feedback=result.message();rebuildWidgets();}
  @Override public void renderBackground(GuiGraphics g,int x,int y,float pt){}
  @Override public void render(GuiGraphics g,int mouseX,int mouseY,float pt){int left=width/2-220,top=Math.max(12,height/2-135);g.fill(left-8,top-8,left+448,top+274,0xf018202a);g.drawString(font,title,left,top,0xffffff);g.drawString(font,font.plainSubstrByWidth(feedback.isBlank()?"Physical yaw follows the chassis plane; your look stays independent.":feedback,435),left,top+15,0xc9d5e8);
    var data=EligibilityHooks.data(minecraft.level);
    if(target.vehicle()&&data!=null){var v=data.vehicles.get(target.id());if(v!=null){g.drawString(font,String.format(Locale.ROOT,"Yaw %.1f° (%.2f turns) | posture %.1f° | %s",Math.toDegrees(v.yaw.angle),v.yaw.angle/(2*Math.PI),Math.toDegrees(-v.carriers.getFirst().angle),v.flying.lock),left,top+36,0xffffff);g.drawString(font,font.plainSubstrByWidth(v.yaw.mode+" | "+v.yaw.status+" | "+v.fault,435),left,top+50,0xffd185);g.drawString(font,"Body "+(v.flying.body?"closed":"open")+" | legs "+(v.flying.legs?"closed":"open")+" | "+(v.boardingReady?"ready to board/unload":"hold restraints"),left,top+64,0xc9d5e8);drawProjection(g,v,left,top+178);}}
    else for(int i=0;i<LABELS.length;i++)g.drawString(font,LABELS[i],left+(i%2)*225,top+38+(i/2)*29,0xc9d5e8);
    super.render(g,mouseX,mouseY,pt);
  }
  private void drawProjection(GuiGraphics g,FoundationData.Vehicle v,int x,int y){
    var container=dev.ryanhcode.sable.api.sublevel.SubLevelContainer.getContainer(minecraft.level);
    if(container==null)return;
    var raw=container.getSubLevel(v.chassis);var deckRaw=container.getSubLevel(v.yaw.deck);
    if(!(raw instanceof dev.ryanhcode.sable.sublevel.ClientSubLevel chassis)||!(deckRaw instanceof dev.ryanhcode.sable.sublevel.ClientSubLevel deck)||container.getSubLevel(v.carriers.getFirst().body)==null){g.drawString(font,"Waiting for complete native hierarchy",x,y,0xffd185);return;}
    var data=EligibilityHooks.data(minecraft.level);var rail=data.rails.stream().filter(r->r.matches(v.boundFrom,v.boundTo)).findFirst().orElse(null);
    var section=rail==null?null:rail.yaw.intervals.stream().filter(i->i.id().equals(v.yaw.section)).findFirst().orElse(null);
    int cx=x+32,cy=y+27;g.fill(cx-25,cy-25,cx+26,cy+26,0xff283847);line(g,cx-25,cy,cx+25,cy,0xff617084);line(g,cx,cy-25,cx,cy+25,0xff617084);
    double actual=dev.fourthdimension.physics.CarrierAxis.Y.angle(v.yaw.angle,new org.joml.Quaterniond(chassis.renderPose().orientation()).conjugate().mul(deck.renderPose().orientation()));
    line(g,cx,cy,cx+(int)(22*Math.sin(actual)),cy-(int)(22*Math.cos(actual)),0xff5bc38f);
    String text="Actual physical heading (green)";
    if(section!=null&&section.markerPos()!=null){var p=deck.getPlot().getCenterBlock();var pivot=deck.renderPose().transformPosition(new org.joml.Vector3d(p.getX()+.5,p.getY()+.5,p.getZ()+.5));var m=section.markerPos();double projected=YawController.projected(chassis.renderPose().orientation(),pivot,new org.joml.Vector3d(m.getX()+.5,m.getY()+.5,m.getZ()+.5));
      if(Double.isFinite(projected)){line(g,cx,cy,cx+(int)(22*Math.sin(projected)),cy-(int)(22*Math.cos(projected)),0xffeeaa35);text=String.format(Locale.ROOT,"Target amber | heading error %.2f°",Math.toDegrees(Math.abs(Math.atan2(Math.sin(projected-actual),Math.cos(projected-actual)))));}
      else text="Target projection is degenerate";
    }
    g.drawString(font,text,x+70,y+11,0xc9d5e8);g.drawString(font,"Chassis plane; target height is ignored",x+70,y+25,0xc9d5e8);
  }
  private static void line(GuiGraphics g,int x,int y,int a,int b,int color){int steps=Math.max(Math.abs(a-x),Math.abs(b-y));for(int n=0;n<=steps;n++){int px=steps==0?x:x+(a-x)*n/steps,py=steps==0?y:y+(b-y)*n/steps;g.fill(px,py,px+2,py+2,color);}}

}
