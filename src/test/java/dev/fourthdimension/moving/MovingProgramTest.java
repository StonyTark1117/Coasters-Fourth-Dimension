package dev.fourthdimension.moving;

import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MovingProgramTest {
  private List<MovingProgram> programs(){return List.of(MovingProgram.transfer(16),MovingProgram.turntable(270,90,true),
      MovingProgram.drop(12),MovingProgram.tilt(90),MovingProgram.elevator(32),MovingProgram.teeter(3),
      MovingProgram.sideDrop(8),MovingProgram.compound(270,90,-15));}
  @Test void allEightProgramsHaveBoundedDerivativesAndRestEndpoints(){
    assertEquals(8,programs().stream().map(MovingProgram::mechanism).distinct().count());
    for(var p:programs()){
      double offset=0;
      for(var leg:p.segments()){
        for(int i=0;i<=400;i++){
          var s=p.sample(offset+leg.seconds()*i/400.0);var l=p.limits();
          assertTrue(s.velocity().linearMagnitude()<=l.linearSpeed()+1e-8,p.mechanism().id());
          assertTrue(s.acceleration().linearMagnitude()<=l.linearAcceleration()+1e-8);
          assertTrue(s.jerk().linearMagnitude()<=l.linearJerk()+1e-8);
          for(double v:new double[]{s.velocity().yaw(),s.velocity().pitch()})assertTrue(Math.abs(v)<=l.angularSpeed()+1e-8);
          for(double v:new double[]{s.acceleration().yaw(),s.acceleration().pitch()})assertTrue(Math.abs(v)<=l.angularAcceleration()+1e-8);
          for(double v:new double[]{s.jerk().yaw(),s.jerk().pitch()})assertTrue(Math.abs(v)<=l.angularJerk()+1e-8);
        }
        offset+=leg.seconds();
        var join=p.sample(offset);
        assertEquals(0,join.velocity().linearMagnitude(),1e-8);
        assertEquals(0,join.velocity().yaw(),1e-8);assertEquals(0,join.velocity().pitch(),1e-8);
        assertEquals(0,join.acceleration().pitch(),1e-8);assertEquals(0,join.jerk().pitch(),1e-8);
      }
      assertEquals(p.end(),p.sample(p.durationSeconds()).pose());
      assertEquals(p.start(),p.reverse().end());
    }
  }
  @Test void derivativesAreDerivativesOfThePhysicalTarget(){
    for(var p:programs()){
      double t=p.segments().getFirst().seconds()*.37,h=1e-4;
      var a=p.sample(t-h);var b=p.sample(t+h);var mid=p.sample(t);
      assertEquals((b.pose().y()-a.pose().y())/(2*h),mid.velocity().y(),1e-6);
      assertEquals((b.pose().yaw()-a.pose().yaw())/(2*h),mid.velocity().yaw(),1e-6);
      assertEquals((b.velocity().pitch()-a.velocity().pitch())/(2*h),mid.acceleration().pitch(),1e-6);
      assertEquals((b.acceleration().pitch()-a.acceleration().pitch())/(2*h),mid.jerk().pitch(),1e-6);
      assertEquals((b.angularVelocity().x()-a.angularVelocity().x())/(2*h),mid.angularAcceleration().x(),1e-6);
      assertEquals((b.angularAcceleration().z()-a.angularAcceleration().z())/(2*h),mid.angularJerk().z(),1e-6);
    }
  }
  @Test void compoundIsConcurrentAndClockwiseTieBreakIsStable(){
    var p=MovingProgram.compound(270,90,15);var s=p.sample(p.segments().getFirst().seconds()/2);
    assertTrue(s.velocity().yaw()>0);assertTrue(s.velocity().pitch()>0);
    assertEquals(Math.PI,MovingProgram.shortestYaw(270,90),1e-12);
    assertEquals(-Math.PI/2,MovingProgram.shortestYaw(0,270),1e-12);
    assertEquals(0,p.end().pitch());
  }
  @Test void continuousSweepIncludesIntermediateRotatedCorners(){
    var local=new MovingProgram.Bounds(-3,-5,-24,3,4,24);
    for(var p:programs()){
      var cells=p.sweptVolumes(local,.20);var envelope=p.sweptEnvelope(local);
      for(int i=0;i<=700;i++)for(var corner:local.corners()){
        var world=p.sample(p.durationSeconds()*i/700.0).pose().transform(corner);
        assertTrue(envelope.contains(world));assertTrue(cells.stream().anyMatch(b->b.contains(world)),p.mechanism().id());
      }
    }
    var yaw=MovingProgram.turntable(0,180,false);
    assertTrue(yaw.sweptEnvelope(local).maxX()>23); // endpoint hull has only +-3 X, so middle is essential
  }
  @Test void repeatedInterruptionsMapToOriginalPathWithoutDerivativeJump(){
    for(var p:programs())for(boolean returning:new boolean[]{false,true}){
      double originalTime=p.durationSeconds()*.63;
      for(int round=0;round<4;round++){
        var r=p.recoveryFrom(originalTime,returning);var first=r.sample(0);
        assertPose(p.sample(originalTime).pose(),first.motion().pose());
        assertEquals(MovingProgram.Pose.ZERO,first.motion().velocity());
        assertEquals(MovingProgram.Pose.ZERO,first.motion().acceleration());
        assertEquals(MovingProgram.Pose.ZERO,first.motion().jerk());
        var stopped=r.sample(r.program().durationSeconds()*.31);
        originalTime=stopped.originalSeconds();
        assertPose(p.sample(originalTime).pose(),stopped.motion().pose());
        var end=r.sample(r.program().durationSeconds());
        assertEquals(returning?0:p.durationSeconds(),end.originalSeconds(),1e-8);
        assertPose(returning?p.start():p.end(),end.motion().pose());
      }
    }
  }
  @Test void measuredLagProjectsOnlyOntoSavedLegAndCannotJumpShowBranches(){
    for(var p:programs()){
      double saved=p.durationSeconds()*.63,actual=p.durationSeconds()*.625;var pose=p.sample(actual).pose();
      var projection=p.projectStoppedPose(saved,new MovingProgram.Point(pose.x(),pose.y(),pose.z()),pose.quaternion(),1,Math.toRadians(15),.025,Math.toRadians(.25));
      assertPose(pose,projection.pose());
      var recovery=p.recoveryFrom(projection.originalSeconds(),false);
      assertEquals(MovingProgram.Pose.ZERO,recovery.sample(0).motion().velocity());
      assertThrows(IllegalArgumentException.class,()->p.projectStoppedPose(saved,new MovingProgram.Point(pose.x()+10,pose.y()+10,pose.z()),pose.quaternion(),1,Math.toRadians(15),.025,Math.toRadians(.25)));
    }
    var turn=MovingProgram.turntable(0,90,true);
    // Identical quaternion after a complete show revolution must not jump back to the start.
    double saved=turn.segments().getFirst().seconds()*.99;
    var projected=turn.projectStoppedPose(saved,new MovingProgram.Point(0,0,0),MovingProgram.Pose.ZERO.quaternion(),1,Math.toRadians(15),.025,Math.toRadians(.25));
    assertTrue(projected.originalSeconds()>turn.segments().getFirst().seconds()*.9);
  }
  @Test void finitePresetsAndBadInputsFailClosed(){
    assertThrows(IllegalArgumentException.class,()->MovingProgram.transfer(7));
    assertThrows(IllegalArgumentException.class,()->MovingProgram.tilt(45));
    assertThrows(IllegalArgumentException.class,()->MovingProgram.compound(0,0,15));
    assertThrows(IllegalArgumentException.class,()->MovingProgram.elevator(64));
    assertThrows(IllegalArgumentException.class,()->new MovingProgramSpec(MovingMechanism.DROP,4,90,0,false));
    assertThrows(IllegalArgumentException.class,()->MovingProgram.drop(4).sample(Double.NaN));
    assertThrows(IllegalArgumentException.class,()->MovingProgram.drop(4).remainingFrom(-1));
    assertThrows(IllegalArgumentException.class,()->new MovingProgram.Bounds(1,0,0,0,1,1));
    var spec=new MovingProgramSpec(MovingMechanism.COMPOUND,15,270,90,false);
    assertEquals(spec.build().durationSeconds(),spec.build().durationSeconds());
  }
  private static void assertPose(MovingProgram.Pose a,MovingProgram.Pose b){
    assertEquals(a.x(),b.x(),1e-7);assertEquals(a.y(),b.y(),1e-7);assertEquals(a.z(),b.z(),1e-7);
    assertEquals(a.yaw(),b.yaw(),1e-7);assertEquals(a.pitch(),b.pitch(),1e-7);
  }
}
