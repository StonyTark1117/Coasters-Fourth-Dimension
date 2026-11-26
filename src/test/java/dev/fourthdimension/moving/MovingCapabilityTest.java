package dev.fourthdimension.moving;

import dev.fourthdimension.contract.Family;
import dev.fourthdimension.moving.MovingCapabilityCatalog.*;
import java.util.EnumSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MovingCapabilityTest {
  private static final Set<Configuration> ADVANCED=Set.of(Configuration.WING,Configuration.ARROW,Configuration.SPINNING,
      Configuration.S2_SPINNING,Configuration.PIPELINE,Configuration.DIVE,Configuration.FIXED_INVERTED);
  @Test void exactTwentyTwoConfigurationsAndMotionMatrix(){
    assertEquals(22,MovingCapabilityCatalog.entries().size());
    for(var e:MovingCapabilityCatalog.entries())for(var m:MovingMechanism.values()){
      assertEquals(m.levelService()||ADVANCED.contains(e.configuration()),e.permission(m,0).allowed(),e.configuration()+" "+m);
    }
    assertTrue(MovingCapabilityCatalog.entry(Configuration.SPINNING).permission(MovingMechanism.TILT,Math.PI/6).allowed());
    assertEquals(Code.SHALLOW_TILT_ONLY,MovingCapabilityCatalog.entry(Configuration.SPINNING).permission(MovingMechanism.TILT,Math.PI/2).code());
    assertFalse(MovingCapabilityCatalog.find(Family.STANDING,"seated_side_rail_v1").isPresent());
    assertEquals(Configuration.STANDING_SURF,MovingCapabilityCatalog.find(Family.STANDING,"standing_surf_v1").orElseThrow().configuration());
    assertTrue(MovingCapabilityCatalog.find(null,"native_service_v1").isEmpty());
  }
  @Test void preservesFamilyConsistLimitsAndMeasuredEndMargins(){
    for(var e:MovingCapabilityCatalog.entries()){
      var r=request(e.configuration());
      assertTrue(MovingCapabilityCatalog.assess(r).allowed(),e.configuration().id());
      var train=new Train(e.maxCars()+1,0,r.train().occupiedBounds(),true,true,false,true);
      assertEquals(Code.TOO_MANY_CARS,MovingCapabilityCatalog.assess(train(r,train)).code());
      var exact=new Train(1,0,new MovingProgram.Bounds(-2,-4,-6,2,3,6),true,true,false,true);
      assertTrue(MovingCapabilityCatalog.assess(train(r,exact)).allowed());
      var oversize=new Train(1,0,new MovingProgram.Bounds(-2,-4,-6.001,2,3,6),true,true,false,true);
      assertEquals(Code.OVERSIZE_TRAIN,MovingCapabilityCatalog.assess(train(r,oversize)).code());
    }
    assertEquals(2,MovingCapabilityCatalog.entry(Configuration.WING).maxCars());
    assertEquals(3,MovingCapabilityCatalog.entry(Configuration.DIVE).maxCars());
    assertEquals(2,MovingCapabilityCatalog.entry(Configuration.AXIS).maxCars());
    assertEquals(1,MovingCapabilityCatalog.entry(Configuration.STANDING_SURF).maxCars());
  }
  @Test void allThreeInsertsNeedExactIdentityContactsMountAndCapabilities(){
    var r=request(Configuration.PIPELINE);var i=r.bed();
    var wrongContacts=new Insert(i.provider(),1,i.family(),i.variant(),"coasters_n_contacts_v1",i.mount(),i.mechanisms(),true);
    assertEquals(Code.CONTACT_MISMATCH,MovingCapabilityCatalog.assess(inserts(r,wrongContacts,i,i)).code());
    var wrongMount=new Insert(i.provider(),1,i.family(),i.variant(),i.contacts(),"above_rail_v1",i.mechanisms(),true);
    assertEquals(Code.MOUNT_MISMATCH,MovingCapabilityCatalog.assess(inserts(r,i,wrongMount,i)).code());
    var unknown=new Insert(i.provider(),2,i.family(),i.variant(),i.contacts(),i.mount(),i.mechanisms(),true);
    assertEquals(Code.UNKNOWN_PROVIDER_VERSION,MovingCapabilityCatalog.assess(inserts(r,i,i,unknown)).code());
    var ctnb=new Insert(i.provider(),1,i.family(),i.variant(),i.contacts(),i.mount(),i.mechanisms(),false);
    assertEquals(Code.NON_NATIVE_MATERIAL,MovingCapabilityCatalog.assess(inserts(r,i,ctnb,i)).code());
    var surf=request(Configuration.STANDING_SURF);
    assertEquals(Code.WRONG_INSERT,MovingCapabilityCatalog.assess(inserts(surf,surf.bed(),request(Configuration.STANDING_STANDARD).bed(),surf.bed())).code());
    for(var e:MovingCapabilityCatalog.entries())for(var missing:e.insert().mechanisms()){
      var actual=e.insert().certifiedNativeInsert();var remaining=EnumSet.copyOf(actual.mechanisms());remaining.remove(missing);
      var bad=new Insert(actual.provider(),1,actual.family(),actual.variant(),actual.contacts(),actual.mount(),remaining,true);
      assertEquals(Code.MISSING_INTERFACE,MovingCapabilityCatalog.checkInsert(e,bad,"test").code());
    }
  }
  @Test void carrierLocksAndProviderOwnershipCannotBeInventedByAppearance(){
    for(var e:MovingCapabilityCatalog.entries())for(var missing:e.requiredHolds()){
      var r=request(e.configuration());var held=EnumSet.copyOf(e.requiredHolds());held.remove(missing);
      var ready=new Readiness(held,true,true,true,true,true,true,false,false);
      assertEquals(Code.CARRIER_NOT_HELD,MovingCapabilityCatalog.assess(ready(r,ready)).code());
    }
    var r=request(Configuration.WING);var h=r.readiness().held();
    assertEquals(Code.LAUNCH_SYSTEMS,MovingCapabilityCatalog.assess(ready(r,new Readiness(h,true,true,true,true,true,true,false,true))).code());
    assertEquals(Code.MANAGED_CIRCUIT,MovingCapabilityCatalog.assess(ready(r,new Readiness(h,true,true,true,true,true,true,true,false))).code());
    assertEquals(Code.PROPULSION_OWNER,MovingCapabilityCatalog.assess(ready(r,new Readiness(h,true,true,true,true,true,false,false,false))).code());
  }
  @Test void incompleteMixedAndOccupiedNativeTrainsAreRefused(){
    var r=request(Configuration.NATIVE_SERVICE);var b=r.train().occupiedBounds();
    assertEquals(Code.EMPTY_SERVICE_ONLY,MovingCapabilityCatalog.assess(train(r,new Train(1,1,b,true,true,false,true))).code());
    assertEquals(Code.CUSTOM_NATIVE_CART,MovingCapabilityCatalog.assess(train(r,new Train(1,0,b,true,true,false,false))).code());
    assertEquals(Code.PARTIAL_TRAIN,MovingCapabilityCatalog.assess(train(r,new Train(1,0,b,false,true,false,true))).code());
    assertEquals(Code.MIXED_TRAIN,MovingCapabilityCatalog.assess(train(r,new Train(1,0,b,true,false,false,true))).code());
    assertEquals(Code.RECOVERY_CART,MovingCapabilityCatalog.assess(train(r,new Train(1,0,b,true,true,true,true))).code());
  }
  private static Request request(Configuration c){
    var e=MovingCapabilityCatalog.entry(c);var i=e.insert().certifiedNativeInsert();
    return new Request(c,MovingProgram.transfer(4),16,i,i,i,new Train(1,0,new MovingProgram.Bounds(-2,-4,-2,2,3,2),true,true,false,true),
        new Readiness(e.requiredHolds(),true,true,true,true,true,true,false,false));
  }
  private static Request train(Request r,Train t){return new Request(r.configuration(),r.program(),r.deckLength(),r.bed(),r.source(),r.destination(),t,r.readiness());}
  private static Request ready(Request r,Readiness q){return new Request(r.configuration(),r.program(),r.deckLength(),r.bed(),r.source(),r.destination(),r.train(),q);}
  private static Request inserts(Request r,Insert b,Insert s,Insert d){return new Request(r.configuration(),r.program(),r.deckLength(),b,s,d,r.train(),r.readiness());}
}
