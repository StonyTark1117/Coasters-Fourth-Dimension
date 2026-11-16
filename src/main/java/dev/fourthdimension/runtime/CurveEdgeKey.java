package dev.fourthdimension.runtime;

/** Per-connection cache of a pure endpoint key, not of editable curve geometry or handle lengths. */
public interface CurveEdgeKey {
  record Key(long first, long second, String text) {}

  String fourthdimension$edgeKey();
}
