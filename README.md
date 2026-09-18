<div align="center">
<img width="1200" height="475" alt="GHBanner" src="https://ai.google.dev/static/site-assets/images/share-ais-513315318.png" />
</div>

# Run and deploy your AI Studio app

This contains everything you need to run your app locally.

View your app in AI Studio: https://ai.studio/apps/9826b6ef-9be9-4e55-a9b3-f6bb6c36519b

## Run Locally

**Prerequisites:**  [Android Studio](https://developer.android.com/studio)


1. Open Android Studio
2. Select **Open** and choose the directory containing this project
3. Allow Android Studio to fix any incompatibilities as it imports the project (it will regenerate the Gradle wrapper jar automatically, since this export doesn't include the binary).
4. (Optional) Create a file named `.env` in the project directory and set `GEMINI_API_KEY` in that file to your Gemini API key (see `.env.example` for an example) — only needed if/when a Gemini-powered feature is wired up.
5. Run the app on an emulator or physical device. Both the `debug` and `release` build types work out of the box with no signing setup, since `app/build.gradle.kts` falls back to the default debug keystore when no real release keystore is configured.
6. Before publishing to the Play Store, set `KEYSTORE_PATH`, `STORE_PASSWORD`, and `KEY_PASSWORD` (env vars locally, or repo secrets in CI) to point at a real upload keystore — the release build will automatically switch to using it. If you have already published your app in AI Studio, please [request upload key reset](https://support.google.com/googleplay/android-developer/answer/9842756#zippy=%2Crequest-an-upload-key-reset) in Google Play Console.

## CI: building the APK automatically

`.github/workflows/build-apk.yml` builds both the debug and release APKs on every push to `main` (and can be run manually from the **Actions** tab). It uses Gradle's own `setup-gradle` action rather than `./gradlew`, since this export doesn't include the wrapper jar — no extra setup needed on your end. Finished APKs are attached to each workflow run as downloadable artifacts (`dark-go-debug-apk` / `dark-go-release-apk`).
