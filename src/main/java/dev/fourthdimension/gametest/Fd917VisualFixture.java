package dev.fourthdimension.gametest;

import dev.fourthdimension.contract.Family;
import dev.fourthdimension.physics.RotationProfile;
import dev.fourthdimension.runtime.FoundationData;
import dev.fourthdimension.track.TrackGeometry;
import dev.fourthdimension.vehicle.VehicleOperations;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartResolve;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import java.util.*;

/** Opt-in setup/observations for the finite FD917 visual route, never normal-player evidence. */
@EventBusSubscriber(modid="fourthdimension")
public final class Fd917VisualFixture {
  @SubscribeEvent
  public static void commands(RegisterCommandsEvent event) {
    if(!Boolean.getBoolean("fourthdimension.validation"))return;
    var root=Commands.literal("fd917").requires(s->s.hasPermission(2));
    for(String action:List.of("inspect","dispatch","capture","reverse","stroke"))
      root.then(Commands.literal(action).executes(ctx->run(ctx.getSource(),action)));
    event.getDispatcher().register(root);
  }
  private static int run(net.minecraft.commands.CommandSourceStack source,String action) {
    try {
      var level=source.getLevel();var data=FoundationData.get(level);
      var cars=data.vehicles.values().stream().filter(v->v.family==Family.ARROW&&!FoundationData.VARIANT.equals(v.variant)).toList();
      if(cars.size()!=1)throw new IllegalArgumentException("Fixture requires exactly one authored Arrow car");
      var car=cars.getFirst();
      if(action.equals("dispatch")){VehicleOperations.restraints(level,car,true);VehicleOperations.dispatch(level,car);}
      if(action.equals("capture"))VehicleOperations.capture(level,car);
      if(action.equals("reverse")) {
        if(!car.stopping)throw new IllegalArgumentException("Capture before reversing fixture motors");
        int changed=0;
        for(var rail:data.rails)if(rail.family==Family.ARROW)for(var d:net.minecraft.core.Direction.values())
          if(level.getBlockEntity(rail.from.relative(d)) instanceof com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity motor){
            motor.generatedSpeed.setValue(-motor.generatedSpeed.getValue());changed++;
          }
        if(changed!=8)throw new IllegalArgumentException("Expected eight isolated example motors, changed="+changed);
      }
      if(action.equals("stroke")) {
        if(!car.stopping||car.carriers.stream().anyMatch(c->!c.capture||!c.occupants.isEmpty()))
          throw new IllegalArgumentException("Empty captured car required for fixture profile setup");
        var rails=data.rails.stream().filter(r->r.family==Family.ARROW).toList();
        if(rails.size()!=8)throw new IllegalArgumentException("Expected isolated eight-edge example loop");
        double[] lengths=rails.stream().mapToDouble(r->TrackGeometry.table(level,TrackGeometry.resolve(level,r)).length()).toArray();
        double total=Arrays.stream(lengths).sum(),offset=0;
        for(int i=0;i<rails.size();i++) {
          double len=lengths[i];var points=new TreeSet<Double>();points.add(0.0);points.add(len);
          for(int q=1;q<16;q++){double d=q*total/16-offset;if(d>1e-8&&d<len-1e-8)points.add(d);}
          var keys=new ArrayList<RotationProfile.Key>();
          for(double d:points){double phase=2*Math.PI*(offset+d)/total;
            double sine=Math.sin(phase),cosine=Math.cos(phase);
            keys.add(new RotationProfile.Key(d,Math.abs(sine)<1e-12?0:8*Math.PI*sine,
                Math.abs(cosine)<1e-12?0:16*Math.PI*Math.PI/total*cosine));}
          rails.get(i).profile=new RotationProfile(keys);rails.get(i).revision++;offset+=len;
        }
        data.setDirty();
      }
      var body=SubLevelContainer.getContainer(level).getSubLevel(car.chassis);
      var center=CoasterCartResolve.cartCenterWorld(body);
      source.sendSuccess(()->Component.literal("FD917 "+action+" chassis="+car.chassis+" center="+center
          +" stopping="+car.stopping+" fault="+car.fault+" carriers="+car.carriers.stream()
          .map(c->"angle="+c.angle+",capture="+c.capture+",coupled="+c.controlCoupled).toList()),true);
      return 1;
    } catch(IllegalArgumentException failure){source.sendFailure(Component.literal(failure.getMessage()));return 0;}
  }
}
