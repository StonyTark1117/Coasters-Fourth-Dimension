# Selected JAR Iris/Solas rendering observation

This is a bounded rendering observation of the selected clean packaged artifact `coasters-fourth-dimension-0.9.0-dev.27.jar` (SHA-256 `656725bcbacc8f491de274c6c058027d0aaa67cfc6f893ba6d7e4a9eb24abbc7`). The input world is the committed FANCY repeat-3 validation world, loaded with the same `FD401StopCycle` quick-play entry point. The screenshot `solas-world.png` shows the FANCY-style red/orange track in the loaded world after applying Solas Shader V3.7b through Iris. `latest.log` confirms Iris used the named pack and created its overworld pipeline. Iris ran with Sodium 0.8.13; Iris logged Flywheel falling back from `flywheel:indirect` to `flywheel:off`. This verifies rendering and pipeline startup only; it does not establish accelerated Flywheel rendering or shader performance.

## Pinned inputs

- Main artifact: `coasters-fourth-dimension-0.9.0-dev.27.jar`, SHA-256 `656725bcbacc8f491de274c6c058027d0aaa67cfc6f893ba6d7e4a9eb24abbc7`.
- Iris NeoForge 1.8.14-beta.1 development-fixed build, SHA-256 `49f783b85cda7fc75e05cf993b80db70bdb5dae55d5426d0536bbe831a44da7`.
- Sodium NeoForge 0.8.13, SHA-256 `575d187b15328d7bddd8a9b1bee64333c22c14735f5780ec49eac0cd5503c08d`.
- Solas Shader V3.7b, SHA-256 `b7dfe4c7353d677a92cc4c86b87bc0633f1014305d927ca6a482c303427bf825`.

## Run and limits

The client used Java 21, `-Xmx1536M`, private Xvfb display `:267`, and a dedicated 4 GiB systemd scope. Initial attempt used an invalid `-Dfml.modFolders=/.../mods` override and crashed before world startup; its logs are retained as `attempt-1-*`. Retry removed that override (`-Dfml.modFolders=`), using the isolated game directory's normal mod discovery. The default 3 GiB scope then hit its cap during shader activation (systemd recorded `Result=oom-kill`, peak 3 GiB). After a fresh host memory check showed 25 GiB available, the same run was repeated with a 4 GiB scope and 1536 MiB heap. It reached the world with the shader active, screenshots were captured, and the world was saved before quitting. Systemd recorded a successful exit.

The `final-world.tar.gz` is the saved world after the observation. Full Java and program arguments, client classpath, Iris config, options, console output, and client log are included.
