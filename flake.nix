{
  description = "Flutter Template";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";
    flake-utils.url = "github:numtide/flake-utils";
  };

  outputs = {
    nixpkgs,
    flake-utils,
    ...
  }:
    flake-utils.lib.eachDefaultSystem (system:
      let
        pkgs = import nixpkgs {
          inherit system;
          config = {
            android_sdk.accept_license = true;
            allowUnfree = true;
          };
        };
        java = pkgs.openjdk11;
        kotlin = pkgs.kotlin.overrideAttrs (oldAttrs: rec {
          version = "2.1.10";
          src = pkgs.fetchurl {
            url = "https://github.com/JetBrains/kotlin/releases/download/v${version}/kotlin-compiler-${version}.zip";
            sha256 = "sha256-xuniY2iJgo4ZyIEdWriQhiU4yJ3CoxAZVt/uPCqLprE=";
          };
        });
        androidComposition = pkgs.androidenv.composeAndroidPackages {
          platformToolsVersion = "34.0.1";
          includeEmulator = false;
          includeSources = false;
          includeSystemImages = false;
          buildToolsVersions = ["34.0.0"];
          platformVersions = ["31" "28" "34"];
          abiVersions = ["armeabi-v7a" "arm64-v8a"];
          includeNDK = true;
          includeExtras = [
            "extras;android;m2repository"
            "extras;google;m2repository"
          ];
        };
        androidSdk = androidComposition.androidsdk;
      in {
        devShell = pkgs.mkShell {
          buildInputs = [
            # androidSdk
            pkgs.gradle_8
            java
            kotlin
          ];
          
          shellHook = ''
            # export ANDROID_HOME="${androidSdk}/libexec/android-sdk"
            # export ANDROID_NDK_ROOT="${androidSdk}/libexec/android-sdk/ndk-bundle"
            export JAVA_HOME="${java.home}"
            echo "Kotlin version is ${kotlin.version}"
            echo "ANDROID_HOME is set to $ANDROID_HOME"
            echo "ANDROID_NDK_ROOT is set to $ANDROID_NDK_ROOT"
            echo "JAVA_HOME is set to $JAVA_HOME"
          '';
        };

      }
    );
}
