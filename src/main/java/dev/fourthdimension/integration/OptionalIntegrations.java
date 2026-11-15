package dev.fourthdimension.integration;

import java.util.List;
import java.util.Objects;
import net.neoforged.fml.ModList;

/** Metadata-only discovery: inspecting a release is not a gameplay compatibility certificate. */
public final class OptionalIntegrations {
  public static final String TRACK_STYLES = "coasterssimulatedextratypes";
  public static final String ADDITIONS = "coasterfins", CTNB = "coasters_extras", MAGNETIZED = "coastersmagnetized";
  public static final String ENGINEERED = "coastersengineered";

  public enum Status {
    ABSENT,
    INSPECTED_RELEASE,
    UNVERIFIED_VERSION
  }

  public record Release(String modId, String version, Status status) {
    public Release {
      Objects.requireNonNull(modId);
      Objects.requireNonNull(version);
      Objects.requireNonNull(status);
    }

    public boolean mayInspect() {
      return status == Status.INSPECTED_RELEASE;
    }
  }

  private OptionalIntegrations() {}

  public static Release installed(String modId) {
    var release = classify(
        modId,
        ModList.get()
            .getModContainerById(modId)
            .map(container -> container.getModInfo().getVersion().toString())
            .orElse(null));
    if (ADDITIONS.equals(modId) && release.mayInspect() && !additionsFingerprint())
      return new Release(modId,release.version(),Status.UNVERIFIED_VERSION);
    return release;
  }

  private static volatile Boolean additionsVerified;
  private static boolean additionsFingerprint() {
    if(additionsVerified!=null)return additionsVerified;
    var expected=java.util.Map.of(
        "dev/plushivr/coasterfins/event/StationHandler.class","93a3d998db292b5deff1c4f1121602bb75e23bc5a44dda2fa2054b8df8f0ea1a",
        "dev/plushivr/coasterfins/block/StationBlockEntity.class","a5ea07845cc14c9621b754fa1ba213625f1732431927217ba2417f32d2c4281d",
        "dev/plushivr/coasterfins/block/CoasterSeatBlock.class","0ce0450e1d48242e52a5ebc6bb555bd1a70b720a4268fa2800c5875b2bce0bfd",
        "dev/plushivr/coasterfins/event/SeatLockTicker.class","93b8b128f5e8528c0268bf735ef09937d854fff74d90e2046dbab2ca2b66bb7b");
    try {
      var file=ModList.get().getModFileById(ADDITIONS).getFile();
      var digest=java.security.MessageDigest.getInstance("SHA-256");
      for(var entry:expected.entrySet()) {
        var bytes=java.nio.file.Files.readAllBytes(file.findResource(entry.getKey()));
        if(!java.util.HexFormat.of().formatHex(digest.digest(bytes)).equals(entry.getValue()))return additionsVerified=false;
      }
      return additionsVerified=true;
    } catch(java.io.IOException|java.security.NoSuchAlgorithmException|RuntimeException unavailable) {return additionsVerified=false;}
  }

  public static Release classify(String modId, String version) {
    boolean known =
        (TRACK_STYLES.equals(modId) && "1.2.0".equals(version))
            || (ENGINEERED.equals(modId) && "1.1.1".equals(version))
            || (ADDITIONS.equals(modId) && "0.2.2".equals(version))
            || (CTNB.equals(modId) && "1.2".equals(version))
            || (MAGNETIZED.equals(modId) && "1.1.0".equals(version));
    return new Release(
        modId,
        version == null ? "absent" : version,
        version == null
            ? Status.ABSENT
            : known ? Status.INSPECTED_RELEASE : Status.UNVERIFIED_VERSION);
  }

  public static List<Release> installed() {
    return List.of(installed(TRACK_STYLES), installed(ENGINEERED), installed(ADDITIONS), installed(CTNB), installed(MAGNETIZED));
  }
}
