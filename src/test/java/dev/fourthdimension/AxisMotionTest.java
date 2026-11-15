package dev.fourthdimension;
import dev.fourthdimension.physics.AxisDrive;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class AxisMotionTest {
 @Test void boundedTurnEndpointAndReversal(){
  var s=new AxisDrive();double actual=0;
  for(int tick=0;tick<1000;tick++){
    double goal=tick<300?2*Math.PI:-2*Math.PI,previousRate=s.commandRate;
    actual=s.advance(goal,actual,.05,1,.75);
    assertTrue(Math.abs(s.commandRate)<=1+1e-12);
    assertTrue(Math.abs(s.commandRate-previousRate)<=.75*.05+1e-12,"Acceleration at tick "+tick);
    if(tick==299)assertEquals(2*Math.PI,actual,1e-8);
  }
  assertEquals(-2*Math.PI,actual,1e-8);
 }
 @Test void reversingMovingTargetDoesNotInstantlyZeroVelocity(){
  var s=new AxisDrive();double actual=0;
  for(int tick=0;tick<200;tick++){
    double previousRate=s.commandRate;
    actual=s.advance(tick<40?10:-10,actual,.05,1,.75);
    assertTrue(Math.abs(s.commandRate-previousRate)<=.75*.05+1e-12,"Reversal acceleration at tick "+tick);
  }
 }
}
