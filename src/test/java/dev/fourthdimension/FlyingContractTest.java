package dev.fourthdimension;

import dev.fourthdimension.contract.*;
import dev.fourthdimension.contract.TrackContract.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FlyingContractTest {
 @Test void postureNeedsDedicatedMountDockAndLockCapabilities(){
  var gear=new Gear(1.544,.2,.9375,.55);var bound=new Clearance(2.25,1.1,4.5);
  var car=new VehicleContract(Family.FLYING,"station_changeover_v1",gear,bound,Set.of(),1);
  var required=Set.of(Mechanism.ROTARY_CLEARANCE,Mechanism.STATION_CAPTURE,Mechanism.OVERHEAD_MOUNT,Mechanism.POSTURE_LOCK);
  var track=new TrackContract("original",Family.FLYING,"station_changeover_v1",gear,required,bound);
  assertEquals(VehicleContract.Eligibility.FOUR_D,car.evaluate(track));
  for(var missing:required){var available=new HashSet<>(required);available.remove(missing);
   assertEquals(VehicleContract.Eligibility.MISSING_MECHANISM,car.evaluate(new TrackContract("incomplete",Family.FLYING,track.variant(),gear,available,bound)),missing.name());}
  assertEquals(VehicleContract.Eligibility.WRONG_FAMILY,car.evaluate(new TrackContract("fixed",Family.FIXED_INVERTED,"four_abreast_v1",gear,required,bound)));
  var fit=new NativeTrackFit(NativeTrackFit.N_CONTACTS,NativeTrackFit.OVERHEAD,new NativeTrackFit.Envelope(2.25,1.1,4.5,2.5));
  assertFalse(new NativeTrackFit(NativeTrackFit.N_CONTACTS,NativeTrackFit.OVERHEAD,new NativeTrackFit.Envelope(2.25,1.1,4.5,2)).accepts(fit),"Moving-row envelope must not omit the fixed fork ends");
 }
}
