package dev.fourthdimension;
import dev.fourthdimension.contract.*;
import dev.fourthdimension.contract.TrackContract.*;
import java.util.Set;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class FixedInvertedContractTest {
 @Test void fixedOverheadRequiresMountCaptureAndMeasuredEnvelopeWithoutRotor(){
  var gear=new Gear(1.544,.2,.9375,.55);var bound=new Clearance(2.25,1.1,4.5);
  var car=new VehicleContract(Family.FIXED_INVERTED,"four_abreast_v1",gear,bound,Set.of(),1);
  var good=new TrackContract("original",Family.FIXED_INVERTED,"four_abreast_v1",gear,Set.of(Mechanism.OVERHEAD_MOUNT,Mechanism.STATION_CAPTURE),bound);
  assertEquals(VehicleContract.Eligibility.FIXED_SEAT,car.evaluate(good));
  assertFalse(Family.FIXED_INVERTED.rotary());assertFalse(Family.FIXED_INVERTED.foundationFixture());
  assertEquals(VehicleContract.Eligibility.WRONG_FAMILY,car.evaluate(new TrackContract("ordinary",null,"ordinary",gear,Set.of(),bound)));
  for(var mechanism:good.mechanisms())assertEquals(VehicleContract.Eligibility.MISSING_MECHANISM,car.evaluate(new TrackContract("incomplete",good.family(),good.variant(),gear,Set.of(mechanism),bound)));
  assertEquals(VehicleContract.Eligibility.CLEARANCE_MISMATCH,car.evaluate(new TrackContract("small",good.family(),good.variant(),gear,good.mechanisms(),new Clearance(2.25,1.1,4.25))));
  var needed=new NativeTrackFit(NativeTrackFit.N_CONTACTS,NativeTrackFit.OVERHEAD,new NativeTrackFit.Envelope(2.25,1.1,4.5,1.75));
  assertFalse(new NativeTrackFit(NativeTrackFit.N_CONTACTS,NativeTrackFit.ABOVE,needed.envelope()).accepts(needed));
  assertFalse(new NativeTrackFit(NativeTrackFit.N_CONTACTS,NativeTrackFit.OVERHEAD,new NativeTrackFit.Envelope(2.25,1.1,4.5,1.5)).accepts(needed));
 }
}
