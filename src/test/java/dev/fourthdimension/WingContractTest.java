package dev.fourthdimension;

import dev.fourthdimension.contract.*;
import dev.fourthdimension.contract.TrackContract.*;
import java.util.Set;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WingContractTest {
  @Test void fixedSeatsNeedMeasuredOrdinaryFitWithoutRotaryMechanisms() {
    var gear=new Gear(1.544,.2,.9375,.55);
    var envelope=new Clearance(4.8,3.6,.8);
    var wing=new VehicleContract(Family.WING,"wing_fixed_v1",gear,envelope,Set.of(),1);
    var ordinary=new TrackContract("simulatedcoasters:0.1.5",null,"ordinary",gear,Set.of(),envelope);
    assertEquals(VehicleContract.Eligibility.FIXED_SEAT,wing.evaluate(ordinary));
    assertFalse(wing.evaluate(new TrackContract("fd:original",Family.ARROW,"arrow",gear,
        Set.of(Mechanism.ROTARY_CLEARANCE,Mechanism.CONTROL_RAILS),envelope)).allowed());
    assertEquals(VehicleContract.Eligibility.CLEARANCE_MISMATCH,wing.evaluate(new TrackContract(
        "simulatedcoasters:0.1.5",null,"ordinary",gear,Set.of(),new Clearance(4,3.6,.8))));
    assertEquals(VehicleContract.Eligibility.CONTACT_MISMATCH,wing.evaluate(new TrackContract(
        "other:unverified",null,"ordinary",new Gear(1.544,.2,.9,.55),Set.of(),envelope)));
    assertFalse(new VehicleContract(Family.ARROW,"arrow",gear,envelope,Set.of(),2).evaluate(ordinary).allowed());
  }
}
