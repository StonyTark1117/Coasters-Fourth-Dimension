package dev.fourthdimension;

import dev.fourthdimension.physics.MagneticSpan;
import dev.fourthdimension.physics.S2SpinningField;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class S2SpinningContractTest {
  @Test void damperCannotReverseOrAddRelativeSpinEnergy(){
    var span=new MagneticSpan(0,20,MagneticSpan.Mode.DAMP,1,20,.1,1);
    for(double omega:new double[]{-4,-.001,0,.001,4}){
      double next=omega+.05*S2SpinningField.acceleration(span,omega,1,.05);
      assertTrue(next*next<=omega*omega+1e-12);
      assertTrue(next*omega>=0);
    }
  }
  @Test void induceDirectionIsCanonicalAndGuardBecomesDamping(){
    for(int sign:new int[]{-1,1}){
      var span=new MagneticSpan(0,20,MagneticSpan.Mode.INDUCE,sign,20,.1,1);
      assertEquals(sign,S2SpinningField.acceleration(span,0,1,.05),1e-12);
      for(double omega:new double[]{-3,-4,3,4})
        assertTrue(omega*S2SpinningField.acceleration(span,omega,1,.05)<0);
    }
  }
}
