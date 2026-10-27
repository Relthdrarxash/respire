# Respire

**English** · [Français](README.fr.md)

A small Android app against doomscrolling. Respire puts a breathing pause in front of the apps you open on autopilot, limits how long a session lasts, caps how many times a day you can open them, and can hide Instagram Reels and YouTube Shorts while leaving the rest of those apps usable.

No Internet permission, no trackers, no account. Everything stays on your phone.

Available in English, French, Spanish and German.

## Why these features

Respire combines the techniques with the most support in published research on reducing smartphone overuse:

| Feature | What it does | Evidence |
|---|---|---|
| **Pause before opening** | A breathing screen with a countdown and the question "Why are you opening it now?". Closing is available immediately, opening only once the countdown ends. | Friction before opening social apps reduced opening attempts and made openings more intentional (Grüning, Riedel & Lorenz-Spreen, *PNAS* 2023; Haliburton et al., *CHI* 2024). |
| **Growing delay** | Each new attempt within the hour adds a few seconds. | Same friction principle, stronger when the urge repeats. |
| **Session end** | An opening only lasts a set time (10 min by default). If you are still in the app, the pause comes back mid-scroll. | Targets the scrolling itself, not just the opening. |
| **Daily limit** | After a set number of opens per app per day, the app is blocked until tomorrow. Extending a session counts as an open. | Hard-to-override limits cut social media use by about 16% over 12 weeks, and most people kept them (Allcott, Gentzkow & Song, NBER 2021). |
| **Loosening lock** | Removing an app or relaxing a rule requires a 30-second wait. | People tend to pause such tools and relapse quickly (Haliburton et al., 2024). |
| **Reels and Shorts filter** | Leaves Instagram or YouTube when the full-screen short-video player appears. Reels sent in a direct message still open. | Removes the endless feed itself while keeping messages, search and regular videos. |

Default settings: 10 s wait, +5 s per attempt within the hour, 10-minute sessions, 6 opens per app per day.

## Install

1. Download `Respire.apk` from the [latest release](../../releases/tag/latest) on your phone and install it. Android will ask you to allow installs from your browser or file manager.
2. Open Respire, tap **Choose apps** and turn on the apps to slow down.
3. Tap **Open settings** (or the **Inactive** badge) and turn on **Respire** in Accessibility settings.
4. **Android 13 and later**: if the switch is greyed out ("restricted setting"), go to Settings > Apps > Respire > ⋮ menu > **Allow restricted settings**, then repeat step 3.
5. On some Android skins (Xiaomi, Huawei, Oppo...), turn off battery optimization for Respire, or the system may stop the service.

On Android 13 and later, you can pick the app language in Settings > Apps > Respire > Language.

## Privacy

Respire uses an Accessibility Service, which is the only way on Android to detect which app is opening.

- **Pauses** only use the package name of the app in the foreground.
- **Reels and Shorts filter** (off by default): only inside Instagram and YouTube, the service checks the technical IDs of on-screen elements to spot the short-video player. It never reads text, messages or images.
- Settings and counters stay in the app's local storage. The manifest declares no `INTERNET` permission, so nothing can be sent anywhere.

## Build from source

The repository builds itself with GitHub Actions on every push and publishes the APK to the `latest` release.

To build locally, open the folder in Android Studio, or run:

```bash
./gradlew assembleDebug
```

Requirements: JDK 17. The app uses Kotlin and Jetpack Compose, with a minimum of Android 8.0 (API 26).

## Known limits

- Anyone can turn the service off in Accessibility settings. The lock only protects Respire's own settings.
- The Reels and Shorts filter relies on view IDs that Instagram or YouTube may change in an update.
- Apps opened from a link inside another app also trigger the pause. This is intended.

## Credits

- View IDs for Reels and Shorts detection: [short-video-rules](https://github.com/bhargavprajapati949/short-video-rules) (MIT) and [appblock](https://github.com/rubberpencils/appblock).
- Fonts: [Fraunces](https://github.com/undercasetype/Fraunces) and [Nunito](https://github.com/googlefonts/nunito), SIL Open Font License, see `licences/`.

## References

- Grüning, D. J., Riedel, F., & Lorenz-Spreen, P. (2023). Directing smartphone use through the self-nudge app one sec. *PNAS*. [doi:10.1073/pnas.2213114120](https://doi.org/10.1073/pnas.2213114120)
- Haliburton, L., Grüning, D. J., Riedel, F., Schmidt, A., & Terzimehić, N. (2024). A longitudinal in-the-wild investigation of design frictions to prevent smartphone overuse. *CHI 2024*.
- Allcott, H., Gentzkow, M., & Song, L. (2021). Digital Addiction. *NBER Working Paper* 28936. [nber.org/papers/w28936](https://www.nber.org/papers/w28936)
