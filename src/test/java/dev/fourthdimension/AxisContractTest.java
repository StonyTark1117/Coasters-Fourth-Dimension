package dev.fourthdimension;
import dev.fourthdimension.contract.*;
import dev.fourthdimension.physics.CarrierAxis;
import dev.fourthdimension.track.TrackDesign;
import org.joml.Quaterniond;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class AxisContractTest {
 @Test void continuousSignedLongitudinalAxis(){
  double angle=0;
  for(int i=0;i<1000;i++){double expected=i*.025;angle=CarrierAxis.NEGATIVE_Z.angle(angle,new Quaterniond().rotateZ(-expected));assertEquals(expected,angle,1e-10);}
  assertEquals(0,CarrierAxis.NEGATIVE_Z.angle(0,new Quaterniond().rotateX(.7)),1e-10);
 }
 @Test void dedicatedCapabilityAndEnvelope(){
  var d=TrackDesign.AXIS;var c=d.contract();
  assertEquals(8,d.axleSpacing());assertEquals(1,d.groups());assertTrue(d.canLink());assertFalse(d.overhead());
  assertTrue(c.mechanisms().contains(TrackContract.Mechanism.ROTATION_DRIVE));assertFalse(c.mechanisms().contains(TrackContract.Mechanism.CONTROL_RAILS));
  var v=new VehicleContract(Family.AXIS,d.variant,c.mainRails(),c.clearance(),java.util.Set.of(),1);
  assertTrue(v.evaluate(c).allowed());
  var missing=new TrackContract(c.provider(),c.family(),c.variant(),c.mainRails(),java.util.Set.of(TrackContract.Mechanism.ROTARY_CLEARANCE),c.clearance());
  assertEquals(VehicleContract.Eligibility.MISSING_MECHANISM,v.evaluate(missing));
 }
}
