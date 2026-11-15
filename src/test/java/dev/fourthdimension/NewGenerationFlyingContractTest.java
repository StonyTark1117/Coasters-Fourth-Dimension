package dev.fourthdimension;

import dev.fourthdimension.contract.*;
import dev.fourthdimension.contract.TrackContract.*;
import dev.fourthdimension.physics.RotationProfile;
import dev.fourthdimension.vehicle.*;
import java.util.*;
import org.joml.Quaterniond;
import org.joml.Vector3d;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class NewGenerationFlyingContractTest {
 @Test void requiresYawAndTheLargerCompoundEnvelope(){
  var gear=new Gear(1.544,.2,.9375,.55);var clearance=new Clearance(2.75,1.1,5.25);
  var car=new VehicleContract(Family.FLYING,"route_changeover_yaw_v1",gear,clearance,Set.of(),1);
  var all=Set.of(Mechanism.ROTARY_CLEARANCE,Mechanism.STATION_CAPTURE,Mechanism.OVERHEAD_MOUNT,Mechanism.POSTURE_LOCK,Mechanism.YAW);
  assertTrue(car.evaluate(new TrackContract("ng",Family.FLYING,car.variant(),gear,all,clearance)).allowed());
  for(var missing:all){var remaining=new HashSet<>(all);remaining.remove(missing);assertEquals(VehicleContract.Eligibility.MISSING_MECHANISM,car.evaluate(new TrackContract("ng",Family.FLYING,car.variant(),gear,remaining,clearance)));}
  assertEquals(VehicleContract.Eligibility.CLEARANCE_MISMATCH,car.evaluate(new TrackContract("ng",Family.FLYING,car.variant(),gear,all,new Clearance(2.25,1.1,4.5))));
 }
 @Test void windingRetainsTurnsAndHasDeterministicHalfTurnTie(){
  assertEquals(Math.PI,YawController.resolve(-Math.PI,0,YawState.Winding.NEAREST),1e-10);
  assertEquals(4*Math.PI+.2,YawController.resolve(.2,4*Math.PI,YawState.Winding.NEAREST),1e-10);
  assertTrue(Double.isNaN(YawController.resolve(Math.PI/2,8*Math.PI,YawState.Winding.POSITIVE)));
  assertTrue(Double.isNaN(YawController.resolve(-Math.PI/2,-8*Math.PI,YawState.Winding.NEGATIVE)));
  assertEquals(8*Math.PI,YawController.resolve(0,7.75*Math.PI,YawState.Winding.POSITIVE),1e-10);
 }
 @Test void projectionUsesChassisPlaneAtBankAndInversion(){
  var pivot=new Vector3d(11,12,13);
  for(double bank:new double[]{0,.6,Math.PI/2,Math.PI,1.5*Math.PI}){
   var nativeFrame=new Quaterniond().rotationZ(bank);var design=new Quaterniond(nativeFrame).rotateY(Math.PI);
   var marker=new Vector3d(4,80,3).rotate(design).add(pivot);
   assertEquals(Math.atan2(4,3),YawController.projected(nativeFrame,pivot,marker),1e-12);
   assertTrue(Double.isNaN(YawController.projected(nativeFrame,pivot,new Vector3d(0,10,0).rotate(design).add(pivot))));
   for(double phi:new double[]{0,.4,Math.PI/2}){
    var nativeBody=new Quaterniond(nativeFrame).rotateY(.7).rotateX(-phi);
    var heading=new Vector3d(0,Math.sin(phi),-Math.cos(phi)).rotate(nativeBody);
    var expected=new Vector3d(0,0,1).rotate(new Quaterniond(design).rotateY(.7));
    assertEquals(1,heading.dot(expected),1e-12);
   }
  }
 }
 @Test void referenceAndFaultBrakingRespectTimeBounds(){
  double angle=0,velocity=0,dt=.01;
  for(int i=0;i<10000;i++){
   double target=i<4000?4*Math.PI:-2*Math.PI;var next=YawController.advance(angle,velocity,target,.6,.8,dt);
   assertTrue(Math.abs(next.velocity())<=.60000001);assertTrue(Math.abs(next.velocity()-velocity)<=.8*dt+1e-10);
   angle=next.angle();velocity=next.velocity();
  }
  assertEquals(-2*Math.PI,angle,.005);
  double returning=-Math.PI/2,rate=0;
  for(int i=0;i<2000;i++){var next=YawController.advance(returning,rate,0,.18,.15,.05);assertTrue(next.angle()<=1e-12,"Seated hard endpoint must not overshoot");assertTrue(next.angle()>=returning-1e-12);assertTrue(Math.abs(next.velocity()-rate)<=.15*.05+1e-12);returning=next.angle();rate=next.velocity();}
  assertEquals(0,returning,1e-9);
  for(int i=0;i<100;i++){var next=YawController.brake(angle,velocity,.8,dt);assertTrue(Math.abs(next.velocity())<=Math.abs(velocity)+1e-12);assertTrue(Math.abs(next.velocity()-velocity)<=.8*dt+1e-12);angle=next.angle();velocity=next.velocity();}assertEquals(0,velocity,1e-12);
 }
 @Test void canonicalDistanceRetracesWithoutClockOrAngleWrapping(){
  var profile=new RotationProfile(List.of(new RotationProfile.Key(0,0),new RotationProfile.Key(128,4*Math.PI)));
  for(int d=0;d<=128;d++)assertEquals(profile.angleAt(d),profile.angleAt(128-(128-d)),0);
  for(int tick=0;tick<200;tick++)assertEquals(2*Math.PI,profile.angleAt(64),1e-12);
  assertEquals(0,profile.slopeAt(0),0);assertEquals(0,profile.slopeAt(128),0);
 }
}
