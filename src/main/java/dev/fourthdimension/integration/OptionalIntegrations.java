package dev.fourthdimension.integration;

import java.util.List;
import java.util.Objects;
import net.neoforged.fml.ModList;

/** Metadata-only discovery: inspecting a release is not a gameplay compatibility certificate. */
public final class OptionalIntegrations {
  public static final String TRACK_STYLES = "coasterssimulatedextratypes";
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
    return classify(
        modId,
        ModList.get()
            .getModContainerById(modId)
            .map(container -> container.getModInfo().getVersion().toString())
            .orElse(null));
  }

  public static Release classify(String modId, String version) {
    boolean known =
        (TRACK_STYLES.equals(modId) && "1.2.0".equals(version))
            || (ENGINEERED.equals(modId) && "1.1.1".equals(version));
    return new Release(
        modId,
        version == null ? "absent" : version,
        version == null
            ? Status.ABSENT
            : known ? Status.INSPECTED_RELEASE : Status.UNVERIFIED_VERSION);
  }

  public static List<Release> installed() {
    return List.of(installed(TRACK_STYLES), installed(ENGINEERED));
  }
}
