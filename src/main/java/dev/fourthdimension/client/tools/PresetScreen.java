package dev.fourthdimension.client.tools;

import dev.fourthdimension.editor.EditorNetwork;
import dev.fourthdimension.runtime.EligibilityHooks;
import dev.fourthdimension.tools.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

public final class PresetScreen extends Screen {
  private final EditorNetwork.Target target;
  private final Screen parent;
  private EditBox name, code, marker;
  private String feedback = "Save current settings or paste a shared preset; review before applying.";
  private boolean bind, pending;
  private int selected, section;
  private long revision;
  public PresetScreen(EditorNetwork.Target target, Screen parent) { super(Component.literal("Setting presets")); this.target=target; this.parent=parent; }
  @Override public boolean isPauseScreen() { return false; }
  @Override protected void init() {
    int x=width/2-210, y=height/2-112;
    String n=name==null?"My preset":name.getValue(), c=code==null?"":code.getValue(), m=marker==null?"":marker.getValue();
    name=box(x,y+24,410,64,"Preset name",n); code=box(x,y+60,410,SettingPreset.LIMIT,"Shared preset code",c); marker=box(x,y+148,190,64,"Destination marker x y z",m);
    code.setResponder(this::describeCode);
    describeCode(code.getValue());
    button("Capture settings",x,y+86,130,()->attempt(()->{
      var data=EligibilityHooks.data(minecraft.level);
      var preset=PresetService.capture(data,target.id(),target.vehicle(),name.getValue(),section);
      code.setValue(preset.exportText());bind=false;feedback="Captured "+preset.kind()+"; no world references copied.";rebuildWidgets();
    }));
    button("Next route section",x+140,y+86,130,()->{section++;feedback="Capture route section index "+section+" (wraps within route).";});
    button("Import clipboard",x+280,y+86,130,()->attempt(()->{
      var preset=SettingPreset.parse(minecraft.keyboardHandler.getClipboard());code.setValue(preset.exportText());name.setValue(preset.name());bind=false;feedback="Imported "+preset.kind()+" / "+preset.variant();rebuildWidgets();
    }));
    button("Save / replace name",x,y+110,130,()->attempt(()->{
      var p=SettingPreset.parse(code.getValue());p=new SettingPreset(name.getValue(),p.family(),p.variant(),p.kind(),p.settings());
      PresetLibrary.save(path(),p);code.setValue(p.exportText());feedback="Saved locally as "+p.name();
    }));
    button("Load next saved",x+140,y+110,130,()->attempt(()->{
      var entries=PresetLibrary.read(path());if(entries.isEmpty())throw new IllegalArgumentException("No saved presets yet");
      var p=entries.get(Math.floorMod(selected++,entries.size()));name.setValue(p.name());code.setValue(p.exportText());bind=false;feedback="Loaded "+p.name()+" / "+p.kind();rebuildWidgets();
    }));
    button("Export clipboard",x+280,y+110,130,()->attempt(()->{minecraft.keyboardHandler.setClipboard(SettingPreset.parse(code.getValue()).exportText());feedback="Preset copied for sharing.";}));
    button(bind?"Destination: confirmed":"Bind selected destination",x+200,y+148,210,()->{bind=!bind;rebuildWidgets();});
    button("Apply preset",x,y+174,130,()->attempt(()->{
      if(!bind)throw new IllegalArgumentException("Confirm the selected destination before applying");
      SettingPreset.parse(code.getValue());var data=EligibilityHooks.data(minecraft.level);
      revision=target.vehicle()?data.vehicles.get(target.id()).revision:data.rails.stream().filter(r->r.id.equals(target.id())).findFirst().orElseThrow().revision;
      pending=true;feedback="Waiting for server…";PacketDistributor.sendToServer(new EditorNetwork.Edit(target.id(),revision,90,"BIND_SELECTED\n"+marker.getValue()+"\n"+code.getValue()));rebuildWidgets();
    }));
    button("Back",x+280,y+174,130,this::onClose);
    if(pending)for(var child:children())if(child instanceof AbstractWidget w)w.active=false;
  }
  private void describeCode(String text) {
    try {
      var p = SettingPreset.parse(text);
      code.setTooltip(Tooltip.create(Component.literal(p.kind() + " / " + p.family() + " / " + p.variant() + "\nSettings: " + p.settings())));
    } catch (IllegalArgumentException failure) {
      code.setTooltip(Tooltip.create(Component.literal("Paste or capture a valid preset to inspect its decoded settings.")));
    }
  }
  private java.nio.file.Path path(){return minecraft.gameDirectory.toPath().resolve("config/fourthdimension/presets.txt");}
  private EditBox box(int x,int y,int w,int max,String label,String value){var b=new EditBox(font,x,y,w,20,Component.literal(label));b.setMaxLength(max);b.setValue(value);addRenderableWidget(b);return b;}
  private void button(String label,int x,int y,int w,Runnable action){addRenderableWidget(Button.builder(Component.literal(label),b->action.run()).bounds(x,y,w,20).build());}
  private interface Work { void run() throws Exception; }
  private void attempt(Work work){try{work.run();}catch(Exception failure){feedback=failure.getMessage()==null?"Unable to use preset":failure.getMessage();}}
  public void receive(EditorNetwork.Result result){if(!pending||!result.id().equals(target.id())||result.action()!=90||result.requestedRevision()!=revision)return;pending=false;bind=false;feedback=result.message();rebuildWidgets();}
  @Override public void onClose(){minecraft.setScreen(parent);}
  @Override public void renderBackground(GuiGraphics g,int x,int y,float pt){}
  @Override public void render(GuiGraphics g,int x,int y,float pt){int left=width/2-218,top=height/2-120;g.fill(left,top,left+436,top+240,0xf018202a);g.drawString(font,title,left+8,top+8,0xffffff);g.drawString(font,"Name (saving replaces this name only)",left+8,top+20,0xc9d5e8);g.drawString(font,"Shared code (hover to inspect decoded settings)",left+8,top+56,0xc9d5e8);g.drawString(font,"Target routes: new owned marker x y z",left+8,top+142,0xc9d5e8);g.drawWordWrap(font,Component.literal(feedback),left+8,top+206,415,0xffd185);super.render(g,x,y,pt);}
}
