package dev.fourthdimension.vehicle;

/** Pure fail-closed policy for the rider layers covered by the FD402 geometry audit. */
final class RecoveryClearancePolicy {
  private RecoveryClearancePolicy() {}

  static boolean certifiedEquipment(
      boolean mainHandClear,
      boolean offHandClear,
      boolean headCertified,
      boolean chestCertified,
      boolean legsCertified,
      boolean feetCertified) {
    return mainHandClear
        && offHandClear
        && headCertified
        && chestCertified
        && legsCertified
        && feetCertified;
  }
}
