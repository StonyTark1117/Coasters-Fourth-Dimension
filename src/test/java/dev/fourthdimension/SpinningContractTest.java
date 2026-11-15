package dev.fourthdimension;
import static org.junit.jupiter.api.Assertions.*;
import dev.fourthdimension.physics.*;
import dev.fourthdimension.contract.NativeTrackFit;
import org.joml.Quaterniond;
import org.junit.jupiter.api.Test;

class SpinningContractTest {
  @Test void axisSamplingPreservesTurnsAndOriginalX() {
    for(var axis:CarrierAxis.values()) {
      double previous=0;
      for(int i=0;i<2000;i++) {
        double angle=i*.02;
        var q=new Quaterniond().rotationAxis(angle,axis.vector().x,axis.vector().y,axis.vector().z);
        previous=axis.angle(previous,q);
        assertEquals(angle,previous,1e-10);
      }
    }
    assertEquals(.7,CarrierAxis.X.angle(.6,new Quaterniond(Math.sin(.35),0,0,Math.cos(.35))),1e-12);
    assertEquals(.7,CarrierAxis.Y.angle(.6,new Quaterniond(0,Math.sin(.35),0,Math.cos(.35))),1e-12);
  }
  @Test void captureCountsServerTicksAndInvalidatesImmediately() {
    var latch=new DockCapture();
    for(int tick=0;tick<4;tick++)for(int substep=0;substep<10;substep++)
      assertFalse(latch.update(tick,true,.01,.01,.01));
    assertTrue(latch.update(4,true,.01,.01,.01));
    assertFalse(latch.update(4,false,.01,.01,.01));
    assertFalse(latch.update(5,true,.01,.01,.01));
    assertFalse(latch.update(6,true,.01,.01,.06));
    assertFalse(latch.update(7,true,Double.NaN,.01,0));
  }
  @Test void newCaptureLimitsDoNotChangeOriginalDefaults() {
    var slow=new RotationServo(0,.35,.5);var old=new RotationServo(0);
    double target=0,priorVelocity=0,oldTarget=0;
    for(int i=0;i<160;i++) {
      double next=slow.advance(10,target,.05);
      assertTrue(Math.abs(next-target)<=.35*.05+1e-10);
      assertTrue(Math.abs(slow.velocity()-priorVelocity)<=.5*.05+1e-10);
      target=next;priorVelocity=slow.velocity();oldTarget=old.advance(10,oldTarget,.05);
    }
    assertTrue(oldTarget>target+5);
  }
  @Test void captureDeceleratesWithoutSnappingAtGoal() {
    var servo=new RotationServo(0,.35,.5);double actual=0,previousVelocity=0;
    for(int i=0;i<400;i++) {
      actual=servo.advance(1,actual,.05);
      assertTrue(Math.abs(servo.velocity()-previousVelocity)<=.025+1e-10);
      previousVelocity=servo.velocity();
    }
    assertEquals(1,actual,1e-9);
    assertEquals(0,servo.velocity(),1e-9);
  }
  @Test void longitudinalExtentAndMountAreFunctional() {
    var e=new NativeTrackFit.Envelope(2.25,3.65,.95,2.25);
    var car=new NativeTrackFit("n_v1","above_v1",e);
    assertTrue(car.accepts(car));
    assertFalse(new NativeTrackFit("n_v1","overhead_v1",e).accepts(car));
    assertFalse(new NativeTrackFit("n_v1","above_v1",new NativeTrackFit.Envelope(2.25,3.65,.95,2)).accepts(car));
  }
}
