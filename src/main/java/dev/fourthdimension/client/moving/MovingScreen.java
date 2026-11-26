package dev.fourthdimension.client.moving;

import dev.fourthdimension.moving.*;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

/** Server snapshots are authoritative; changing a selector only changes an uncommitted preview. */
public final class MovingScreen extends Screen {
  private final BlockPos controller;
  private CompoundTag snapshot;
  private MovingConstruction.ConfigureRequest selected;
  private boolean pending;
  private Component feedback=tr("no_spend");
  private EditBox x,y,z;
  private int destination;
  private ListTag costs=new ListTag();private CompoundTag previewState=new CompoundTag();
  public MovingScreen(MovingNetwork.State state){super(tr("title"));controller=state.controller();snapshot=state.tag();
    MovingMechanism mechanism=MovingMechanism.byId(snapshot.getString("mechanism"));selected=MovingConstruction.defaults(controller,mechanism);var prior=MovingPreview.current(controller);if(prior.contains("request")){selected=MovingConstruction.ConfigureRequest.read(prior.getCompound("request"));previewState=prior.getCompound("assembly").copy();costs=prior.getList("costs",Tag.TAG_COMPOUND).copy();}
  }
  static Component tr(String id,Object...args){return Component.translatable("moving.fourthdimension."+id,args);}
  private CompoundTag assembly(){return snapshot.getCompound("assembly");}
  private boolean built(){return assembly().hasUUID("id");}
  @Override public boolean isPauseScreen(){return false;}
  private Button button(String key,int x,int y,int w,Runnable action){var b=addRenderableWidget(Button.builder(tr(key),a->action.run()).bounds(x,y,w,20).build());b.active=!pending;b.setTooltip(Tooltip.create(tr(key)));return b;}
  private void updateRequest(){if(x==null)return;var origin=new BlockPos(Integer.parseInt(x.getValue()),Integer.parseInt(y.getValue()),Integer.parseInt(z.getValue()));if(!origin.equals(selected.origin()))selectionChanged();selected=new MovingConstruction.ConfigureRequest(selected.configuration(),selected.length(),origin,selected.facing(),selected.program());}
  private int column(){return Math.min(212,(width-28)/2);}
  private int left(){return (width-(column()*2+12))/2;}
  private void selectionChanged(){costs=new ListTag();previewState=new CompoundTag();MovingPreview.clear();feedback=tr("no_spend");}
  @Override protected void init(){
    int left=left(),col=column(),half=(col-4)/2,third=(col-8)/3,top=30;
    if(!built()){
      button("configuration",left,top,col,()->{updateSafely();var all=MovingCapabilityCatalog.Configuration.values();int n=MovingCapabilityCatalog.Configuration.byId(selected.configuration()).ordinal();selected=new MovingConstruction.ConfigureRequest(all[(n+1)%all.length].id(),selected.length(),selected.origin(),selected.facing(),selected.program());selectionChanged();rebuildWidgets();}).setMessage(tr("configuration_value",tr("configuration."+selected.configuration())));
      button("length",left,top+24,half,()->{updateSafely();selected=new MovingConstruction.ConfigureRequest(selected.configuration(),selected.length()==16?32:selected.length()==32?48:16,selected.origin(),selected.facing(),selected.program());selectionChanged();rebuildWidgets();}).setMessage(tr("length_value",selected.length()));
      button("facing",left+half+4,top+24,half,()->{updateSafely();selected=new MovingConstruction.ConfigureRequest(selected.configuration(),selected.length(),selected.origin(),selected.facing().getClockWise(),selected.program());selectionChanged();rebuildWidgets();}).setMessage(tr("facing_value",selected.facing().getName()));
      x=coordinate(left,top+60,third,"X",selected.origin().getX());y=coordinate(left+third+4,top+60,third,"Y",selected.origin().getY());z=coordinate(left+2*(third+4),top+60,third,"Z",selected.origin().getZ());
      button("preset",left,top+88,col,()->cyclePreset()).setMessage(tr("preset_value",selected.program().amount(),selected.program().fromPort(),selected.program().toPort(),selected.program().fullShowTurn()));
      button("preview",left,top+112,half,()->request("preview"));button("validate",left+half+4,top+112,half,()->request("validate"));
      button("configure",left,top+136,col,()->request("configure"));
    }else{
      var actions=new ArrayList<>(List.of("capture","move","resume","return","release","evacuate","dismantle"));if(assembly().getString("configuration").equals("native_service"))actions.add("dispatch_service");int row=0;
      for(var action:actions){button(action,left,top+row*22,col,()->request(action));row++;}
      button("destination",left,top+row*22,col,()->{destination++;rebuildWidgets();}).setMessage(tr("destination_value",destinationName()));
    }
    button("compatibility",left+col+12,top,col,()->minecraft.setScreen(new MovingGuideScreen(this,selected.program().mechanism(),true)));
    button("guide",left+col+12,top+24,col,()->minecraft.setScreen(new MovingGuideScreen(this,selected.program().mechanism(),false)));
    button("refresh",left+col+12,top+48,half,()->request("refresh"));
    button("materials",left+col+12+half+4,top+48,half,()->minecraft.setScreen(new MovingBillScreen(this,costs)));
    button("sockets",left+col+12,top+72,half,()->minecraft.setScreen(new MovingSocketScreen(this,built()?assembly():previewState)));
    button("clear_preview",left+col+12+half+4,top+72,half,()->MovingPreview.clear());
    button("details",left+col+12,height-28,half,()->minecraft.setScreen(new MovingMessageScreen(this,feedback)));
    button("close",left+col+12+half+4,height-28,half,()->onClose());
  }
  private EditBox coordinate(int left,int top,int size,String label,int value){var box=new EditBox(font,left,top,size,18,Component.literal(label));box.setMaxLength(10);box.setValue(Integer.toString(value));box.setFilter(s->s.matches("-?[0-9]*"));addRenderableWidget(box);return box;}
  private void updateSafely(){try{updateRequest();}catch(IllegalArgumentException ignored){feedback=tr("coordinates_invalid");}}
  private void cyclePreset(){updateSafely();var m=selected.program().mechanism();var options=new ArrayList<MovingProgramSpec>();
    int[] amounts=switch(m){case TRANSFER->new int[]{4,8,16};case DROP->new int[]{4,8,12};case ELEVATOR->new int[]{4,8,16,32};case TILT->new int[]{30,90};case TEETER->new int[]{1,2,3};case SIDE_DROP->new int[]{4,8};case COMPOUND->new int[]{-15,15};case TURNTABLE->new int[]{0};};
    if(m==MovingMechanism.TURNTABLE||m==MovingMechanism.COMPOUND){for(int a:amounts)for(int from:new int[]{0,90,180,270})for(int to:new int[]{0,90,180,270})if(from!=to){options.add(new MovingProgramSpec(m,a,from,to,false));if(m==MovingMechanism.TURNTABLE)options.add(new MovingProgramSpec(m,a,from,to,true));}}else for(int a:amounts)options.add(new MovingProgramSpec(m,a,0,0,false));
    int current=options.indexOf(selected.program());selected=new MovingConstruction.ConfigureRequest(selected.configuration(),selected.length(),selected.origin(),selected.facing(),options.get((current+1)%options.size()));selectionChanged();rebuildWidgets();
  }
  private String destinationName(){var list=assembly().getList("docks",Tag.TAG_COMPOUND);return list.isEmpty()?"":list.getCompound(Math.floorMod(destination,list.size())).getString("name");}
  private void request(String operation){
    if(pending)return;
    try{if(!built())updateRequest();}catch(IllegalArgumentException e){feedback=tr("coordinates_invalid");return;}
    var state=assembly();pending=true;feedback=tr("waiting");rebuildWidgets();
    PacketDistributor.sendToServer(new MovingNetwork.Request(controller,operation,state.hasUUID("id")?state.getUUID("id"):new UUID(0,0),state.getLong("epoch"),state.getLong("sequence"),destinationName(),selected.save()));
  }
  public void receive(MovingNetwork.State state){if(!state.controller().equals(controller))return;snapshot=state.tag();pending=false;var result=snapshot.getCompound("result");
    if(result.contains("sweep")){MovingPreview.show(controller,result);previewState=result.getCompound("assembly").copy();}if(built())MovingPreview.clear();
    if(result.contains("costs"))costs=result.getList("costs",Tag.TAG_COMPOUND).copy();
    feedback=result.contains("error")?Component.literal(result.getString("error")):snapshot.contains("error")?Component.literal(snapshot.getString("error")):tr(result.contains("valid")?(result.getBoolean("valid")?"valid":"invalid"):"updated");
    rebuildWidgets();
  }
  @Override public void onClose(){super.onClose();}
  @Override public void renderBackground(GuiGraphics g,int mx,int my,float dt){}
  @Override public void render(GuiGraphics g,int mx,int my,float dt){
    int left=left(),col=column();g.fill(left-6,0,left+2*col+18,height,0xe018202a);g.drawCenteredString(font,title,width/2,10,0xffffff);super.render(g,mx,my,dt);
    int tx=left+col+16,ty=128;g.drawString(font,tr("power_value",String.format(Locale.ROOT,"%.0f",snapshot.getDouble("rpm"))),tx,ty,0xffdd99);
    var state=assembly();if(built()){ty+=16;g.drawString(font,tr("phase_value",state.getString("phase")),tx,ty,0xffffff);ty+=16;g.drawString(font,tr("locks_value",state.getBoolean("locks")),tx,ty,0xffffff);}
    else {ty+=12;var c=MovingCapabilityCatalog.entry(MovingCapabilityCatalog.Configuration.byId(selected.configuration()));var decision=c.permission(selected.program().mechanism(),selected.program().build().maxAbsPitch());for(var line:font.split(tr("decision."+decision.code().name().toLowerCase(Locale.ROOT)),col-8)){g.drawString(font,line,tx,ty,decision.allowed()?0x90eea0:0xffb090);ty+=10;}}
    ty=Math.max(ty+12,166);for(var line:font.split(feedback,col-8)){g.drawString(font,line,tx,ty,0xffdfb0);ty+=10;if(ty>height-45)break;}
    if(!built())g.drawString(font,tr("origin"),left,79,0xc0cbd9);
  }
  public static void accept(MovingNetwork.State state){var mc=Minecraft.getInstance();if(state.open()){
      if(!state.tag().contains("mechanism")){if(mc.player!=null)mc.player.displayClientMessage(Component.literal(state.tag().getString("error")),false);return;}mc.setScreen(new MovingScreen(state));
    }else if(mc.screen instanceof MovingScreen screen)screen.receive(state);}
}
