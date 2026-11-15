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
    if(MAGNETIZED.equals(modId)&&release.mayInspect()&&!magnetizedFingerprint())
      return new Release(modId,release.version(),Status.UNVERIFIED_VERSION);
    if(CTNB.equals(modId)&&release.mayInspect()&&!ctnbFingerprint())
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

  private static volatile Boolean magnetizedVerified;
  private static boolean magnetizedFingerprint(){
    if(magnetizedVerified!=null)return magnetizedVerified;
    var expected=java.util.Map.of(
      "magnet/MagnetBoost.class","dec5df29cc8c76232fad2bd46b5e9413683c5cbafb15bad9e0bc10ab9a9c1cfd",
      "magnet/MagnetizedAnchors.class","9739fc5c50a75bcc4ca36582a92f1eff34e71725f0a9a1499952d95b45e130bd",
      "magnet/MagnetScrollValue.class","e92c9b10bda5a35f15082b25c9f1af7422e644a1d6ffcfa44eecfbf039435118",
      "mixin/CoasterCartMagnetDriveMixin.class","c5850472ccabb22355bf3ea5f74fb54d2446288d7e7f53de33fd5537a6a4901e");
    try{
      var file=ModList.get().getModFileById(MAGNETIZED).getFile();var digest=java.security.MessageDigest.getInstance("SHA-256");
      for(var e:expected.entrySet())if(!java.util.HexFormat.of().formatHex(digest.digest(java.nio.file.Files.readAllBytes(
          file.findResource("net/antopfr/coastersmagnetized/"+e.getKey())))).equals(e.getValue()))return magnetizedVerified=false;
      return magnetizedVerified=true;
    }catch(java.io.IOException|java.security.NoSuchAlgorithmException|RuntimeException unavailable){return magnetizedVerified=false;}
  }

  private static volatile Boolean ctnbVerified;
  private static boolean ctnbFingerprint(){
    if(ctnbVerified!=null)return ctnbVerified;
    try{
      var file=ModList.get().getModFileById(CTNB).getFile();
      var digest=java.security.MessageDigest.getInstance("SHA-256");
      var paths=new java.util.ArrayList<String>();
      for(var c:java.util.List.of("mixin/CoasterCartDriveMixin","mixin/ResourceLocationCoasterAliasMixin","mixin/CurveBerMaterialsMixin","client/BeamModels","track/TrackRepaint","StationBoostBehaviour","SendDirectionBehaviour","LaunchTriggerBehaviour","net/SensorTracker","net/StationTracker","net/RideTelemetry"))paths.add("dev/notzyvex/coasters_extras/"+c+".class");
      for(var k:java.util.List.of("boost_track","powered_boost_track","station_track","launch_track","reverse_track","brake_track","sensor_track"))
        for(var p:java.util.List.of("segment_left","segment_right","segment_center_beam","tie"))
          for(var ext:java.util.List.of("obj","json"))paths.add("assets/coasters_extras/models/block/track/"+k+"/"+p+"."+ext);
      for(var path:paths)digest.update(java.nio.file.Files.readAllBytes(file.findResource(path)));
      return ctnbVerified=java.util.HexFormat.of().formatHex(digest.digest()).equals("b042191997cfb47be87a4df167e9512a2a41ad749dbc7e91c6be6dc6bb41b7ce");
    }catch(java.io.IOException|java.security.NoSuchAlgorithmException|RuntimeException unavailable){return ctnbVerified=false;}
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
