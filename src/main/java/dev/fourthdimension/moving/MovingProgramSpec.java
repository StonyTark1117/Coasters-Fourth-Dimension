package dev.fourthdimension.moving;

import java.util.Objects;

/**
 * Finite save/packet program schema. Persist mechanism.id(), these integer fields and VERSION;
 * reject unknown versions before construction. No arbitrary poses, velocities or segment lists.
 * Recovery is derived from a base program plus its stopped on-path progress, not this preset alone.
 */
public record MovingProgramSpec(MovingMechanism mechanism,int amount,int fromPort,int toPort,boolean fullShowTurn) {
  public static final int VERSION=1;
  public MovingProgramSpec {
    Objects.requireNonNull(mechanism);
    switch(mechanism){
      case TRANSFER->{amount(amount,4,8,16);unusedPorts(fromPort,toPort,fullShowTurn);}
      case DROP->{amount(amount,4,8,12);unusedPorts(fromPort,toPort,fullShowTurn);}
      case TILT->{amount(amount,30,90);unusedPorts(fromPort,toPort,fullShowTurn);}
      case ELEVATOR->{amount(amount,4,8,16,32);unusedPorts(fromPort,toPort,fullShowTurn);}
      case TEETER->{amount(amount,1,2,3);unusedPorts(fromPort,toPort,fullShowTurn);}
      case SIDE_DROP->{amount(amount,4,8);unusedPorts(fromPort,toPort,fullShowTurn);}
      case TURNTABLE->{amount(amount,0);port(fromPort);port(toPort);}
      case COMPOUND->{amount(amount,-15,15);port(fromPort);port(toPort);
        if(fromPort==toPort||fullShowTurn)throw new IllegalArgumentException("Compound requires distinct ports and no full-show turn");}
    }
  }
  public MovingProgram build(){return switch(mechanism){
    case TRANSFER->MovingProgram.transfer(amount);case DROP->MovingProgram.drop(amount);
    case TILT->MovingProgram.tilt(amount);case ELEVATOR->MovingProgram.elevator(amount);
    case TEETER->MovingProgram.teeter(amount);case SIDE_DROP->MovingProgram.sideDrop(amount);
    case TURNTABLE->MovingProgram.turntable(fromPort,toPort,fullShowTurn);
    case COMPOUND->MovingProgram.compound(fromPort,toPort,amount);
  };}
  private static void unusedPorts(int from,int to,boolean show){if(from!=0||to!=0||show)throw new IllegalArgumentException("This program has no yaw ports/show turn");}
  private static void port(int p){amount(p,0,90,180,270);}
  private static void amount(int v,int...allowed){for(int a:allowed)if(v==a)return;throw new IllegalArgumentException("Unsupported program value: "+v);}
}
