#!/usr/bin/env bash
set -euo pipefail

if [[ $# -lt 3 || $# -gt 5 ]]; then
  echo "usage: $0 <platform> <arch> <output-dir> [package-id] [minimum-macos-version]" >&2
  exit 64
fi

platform="$1"
arch="$2"
out_dir="$3"
package_id="${4:-${platform}-${arch}}"
minimum_macos_version="${5:-}"
project="rom2nxt"
version="${GITHUB_REF_NAME:-local}"
jar="target/${project}-1.0-SNAPSHOT.jar"
stage="target/release/${project}-${version}-${package_id}"
image_parent="target/jpackage"

if [[ ! -f "$jar" ]]; then
  echo "Missing shaded jar: $jar" >&2
  echo "Run 'mvn package' before packaging a release." >&2
  exit 66
fi

rm -rf "$stage" "$image_parent"
mkdir -p "$stage" "$image_parent" "$out_dir"

java_options=(
  --java-options "--enable-native-access=ALL-UNNAMED"
  --java-options "-Djava.library.path=lib"
)
if [[ "$platform" == "macos" ]]; then
  java_options+=(--java-options "-XstartOnFirstThread")
fi

jpackage \
  --type app-image \
  --name "$project" \
  --input target \
  --main-jar "${project}-1.0-SNAPSHOT.jar" \
  --main-class ua.millfreedom.rom2.starter.Rom2StarterLWJGL \
  --dest "$image_parent" \
  "${java_options[@]}"

app_image="$image_parent/$project"
if [[ "$platform" == "macos" ]]; then
  app_image="$image_parent/$project.app"
fi
if [[ ! -e "$app_image" ]]; then
  echo "Missing jpackage app image: $app_image" >&2
  echo "Available jpackage output:" >&2
  find "$image_parent" -maxdepth 2 -mindepth 1 -print >&2
  exit 67
fi

cp -R "$app_image" "$stage/"
cp "$jar" "$stage/${project}.jar"
cp README.md LICENSE "$stage/"
[[ -f server2.cfg ]] && cp server2.cfg "$stage/"
[[ -f portals.txt ]] && cp portals.txt "$stage/"

cat > "$stage/COMPATIBILITY.md" <<COMPATIBILITY
# Package compatibility

Package id: \`${package_id}\`
Platform: \`${platform}\`
Architecture: \`${arch}\`
Minimum macOS target: \`${minimum_macos_version:-default runner/JDK support}\`
COMPATIBILITY

cat > "$stage/RUNNING.md" <<'RUNNING'
# Running rom2nxt

Use the bundled `rom2nxt` launcher in this directory's application image, or run the portable shaded jar with:

```bash
java --enable-native-access=ALL-UNNAMED -jar rom2nxt.jar
```

Run commands from the repository/game-data root when using original ROM2 resources, because the runtime loads the original data layout relative to the working directory.
RUNNING

archive_base="${project}-${version}-${package_id}"
case "$platform" in
  windows)
    jar --create --file "$out_dir/${archive_base}.zip" -C "$(dirname "$stage")" "$(basename "$stage")"
    ;;
  *)
    tar -C "$(dirname "$stage")" -czf "$out_dir/${archive_base}.tar.gz" "$(basename "$stage")"
    ;;
esac
