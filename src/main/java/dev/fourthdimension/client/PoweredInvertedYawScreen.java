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

/** Upright yaw/travel authoring and normal retained vehicle operation. */
public final class PoweredInvertedYawScreen extends Screen {
  private final EditorNetwork.Target target;
  private final List<EditBox> fields=new ArrayList<>();
  private static final String[] LABELS={"Section name","Start distance","End distance","Mode: PROFILE / TARGET / SHOW_STOP","Yaw keys: distance:degrees; …","Marker position: x y z","Blend distance","Winding: NEAREST / POSITIVE / NEGATIVE","Yaw speed (rad/s, max0.6)","Yaw acceleration (rad/s², max0.8)","Show / stop name","Show duration (ticks)","Travel speed (blocks/s, max8)","Stop distance (blank = no stop)","Dwell (20–1200 ticks)","After stop: -1 reverse / 0 keep / 1 forward"};
  private String feedback="",draft;
  private EditorNetwork.Edit pending;
  private long revision;private int selected;
  public PoweredInvertedYawScreen(EditorNetwork.Target target){super(Component.literal("Powered Inverted Yaw"));this.target=target;}
  @Override public boolean isPauseScreen(){return false;}
  @Override protected void init(){fields.clear();var data=EligibilityHooks.data(minecraft.level);if(data==null){onClose();return;}int left=width/2-220,top=Math.max(12,height/2-155);
    if(target.vehicle()){
      var v=data.vehicles.get(target.id());if(v==null){onClose();return;}revision=v.revision;
      button("Capture / unwind",left,top+106,10,"");button("Close restraints",left+145,top+106,11,"");button("Open restraints",left+290,top+106,12,"");
      button("Dispatch route",left,top+132,13,"");button("Explicit resume",left+145,top+132,71,"");button("Retain / stop",left+290,top+132,14,"");
      button("Set forward",left,top+158,72,"");button("Set reverse",left+145,top+158,73,"");button("Toggle event chime",left+290,top+158,74,"");
    }else{
      var r=data.rails.stream().filter(a->a.id.equals(target.id())).findFirst().orElse(null);if(r==null){onClose();return;}revision=r.revision;var program=r.poweredYaw;
      String values=draft!=null?draft:program.yaw.intervals.isEmpty()?"Section|0|24|PROFILE|0:0;24:0||4|NEAREST|0.6|0.8|Show|200|4||200|0":PoweredInvertedYawEditor.format(program,program.yaw.intervals.get(Math.min(selected,program.yaw.intervals.size()-1)));var split=values.split("\\|",-1);
      for(int i=0;i<LABELS.length;i++){int x=left+(i%2)*225,y=top+38+(i/2)*29;var box=new EditBox(font,x,y+10,215,16,Component.literal(LABELS[i]));box.setMaxLength(i==4?4096:128);box.setValue(split[i]);fields.add(box);addRenderableWidget(box);}
      addRenderableWidget(Button.builder(Component.literal("Save section"),b->submit(70,form())).bounds(left,top+280,140,20).build());
      addRenderableWidget(Button.builder(Component.literal("Next section"),b->{selected=(selected+1)%Math.max(1,program.yaw.intervals.size());draft=null;rebuildWidgets();}).bounds(left+145,top+280,140,20).build());
    }
    addRenderableWidget(Button.builder(Component.literal("Done"),b->onClose()).bounds(left+290,top+280,140,20).build());if(pending!=null)for(var child:children())if(child instanceof AbstractWidget w)w.active=false;
  }
  private String form(){return fields.stream().map(EditBox::getValue).collect(java.util.stream.Collectors.joining("|"));}
  private void button(String label,int x,int y,int action,String value){addRenderableWidget(Button.builder(Component.literal(label),b->submit(action,value)).bounds(x,y,140,20).build());}
  private void submit(int action,String value){if(pending!=null)return;pending=new EditorNetwork.Edit(target.id(),revision,action,value);feedback="Waiting for server…";for(var child:children())if(child instanceof AbstractWidget w)w.active=false;PacketDistributor.sendToServer(pending);}
  public void receive(EditorNetwork.Result result){if(pending==null||!pending.id().equals(result.id())||pending.revision()!=result.requestedRevision()||pending.action()!=result.action())return;int action=pending.action();draft=target.vehicle()?null:form();pending=null;feedback=result.message();if(result.success()&&(action==13||action==71)){onClose();return;}rebuildWidgets();}
  @Override public void renderBackground(GuiGraphics g,int x,int y,float pt){}
  @Override public void render(GuiGraphics g,int mouseX,int mouseY,float pt){int left=width/2-220,top=Math.max(12,height/2-155);g.fill(left-8,top-8,left+448,top+308,0xf018202a);g.drawString(font,title,left,top,0xffffff);g.drawString(font,font.plainSubstrByWidth(feedback.isBlank()?"Upright seats; independent yaw; one powered circuit controller.":feedback,435),left,top+15,0xc9d5e8);
    var data=EligibilityHooks.data(minecraft.level);if(target.vehicle()&&data!=null){var v=data.vehicles.get(target.id());if(v!=null){var s=v.poweredYaw;var c=v.carriers.getFirst();g.drawString(font,String.format(Locale.ROOT,"Yaw %.1f° (%.2f turns), target %.1f°",Math.toDegrees(c.angle),c.angle/(2*Math.PI),Math.toDegrees(s.target)),left,top+38,0xffffff);g.drawString(font,s.phase+" | "+s.mode+" | dwell "+s.dwellRemaining+" ticks",left,top+52,0xc9d5e8);g.drawString(font,String.format(Locale.ROOT,"Requested travel %+.2f b/s | direction %s",s.travelRequest,s.direction>0?"forward":"reverse"),left,top+66,0xc9d5e8);g.drawString(font,font.plainSubstrByWidth(v.fault.isBlank()?s.status:v.fault,435),left,top+80,0xffd185);g.drawString(font,"Restraints "+(v.restraints?"closed":"open")+" | "+(v.boardingReady?"ready to unload":"keep riders retained"),left,top+194,0xc9d5e8);g.drawString(font,"Owner "+(v.authority.suspended?"suspended":v.authority.held?"held":"active")+" | event sequence "+s.sequence,left,top+208,0xc9d5e8);}}
    else for(int i=0;i<LABELS.length;i++)g.drawString(font,font.plainSubstrByWidth(LABELS[i],215),left+(i%2)*225,top+38+(i/2)*29,0xc9d5e8);super.render(g,mouseX,mouseY,pt);
  }
}
