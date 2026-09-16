# berglas — Android Magic App

**A wooden table. A deck of cards. A stage for your performance.**

[简体中文](README.md) · [Development](docs/DEVELOPMENT.md) · [Contributing](CONTRIBUTING.md)

berglas is an offline card-magic performance tool for Android, built with Kotlin and Jetpack Compose. Classic red and blue cards, a wooden tabletop, and natural card interactions create a minimal, immersive setting.

This independent project is unaffiliated with any person, brand, or organization sharing its name.

<p align="center"><img src="docs/images/table.png" width="280" alt="berglas cards on a wooden tabletop"></p>

## The effect: ACAAN

berglas is a card-performance tool for Berglas / **ACAAN (Any Card At Any Number)** style magic.

A spectator freely names a playing card, such as the **7 of Hearts**, and a number from **1 to 52**, such as **23**. The magician takes out a deck and counts down to the 23rd card. It is the 7 of Hearts.

**The spectator chooses the card and the number, yet that very card is at that exact position.**

This describes the classic effect and the project's performance direction. The current version is an interactive card tool for use within a performer's routine; it does not yet offer number entry followed by automatic counting. Public documentation does not explain the method.

**Source available for non-commercial use only. Commercial use requires prior written permission from the author.** See the [license](LICENSE).

## Features

- Offline operation: no account, server, or internet permission.
- 52 individually movable cards, free placement, and automatic layering.
- Single-finger double-tap to reveal; both face-down and revealed cards remain draggable.
- Red and blue decks, layered tuck-box artwork, paper edges, and soft shadows.
- A downward sleeve animation over a consistent wooden background.
- Separated state and UI code, with unit and device tests.

## Download and install

Download a publisher-provided APK from this repository's **Releases** page and open it on an Android phone. Requires **Android 8.0 (API 26)** or later. APKs marked debug are intended for testing.

If no release is available, follow the [development guide](docs/DEVELOPMENT.md). The initial build downloads development dependencies; the installed app works offline.

## Basic interactions

1. Launch the app and swipe downward from the center of the tuck box.
2. Drag an accessible card and release it to leave it in place.
3. Tap the same card twice with one finger to reveal it.
4. Double-tap an empty area to spread the deck and retrieve off-screen cards.

Public documentation covers visible interactions and development without explaining the performance method. Full source is included; publishing source does not keep implementation details confidential.

## Development and contributions

Stack: Kotlin, Jetpack Compose, Android SDK 35, and JDK 17.
Contributions to performance, gestures, artwork, accessibility, and device compatibility are welcome.

- [Build and development](docs/DEVELOPMENT.md)
- [Testing checklist](docs/TESTING.md)
- [Asset notes](docs/ASSETS.md)
- [Contribution guide](CONTRIBUTING.md)

Current source version: **2.2.1**. The repository is presented as berglas. The existing application ID, some artwork, and APK filenames retain Atelier naming for compatibility.

## License

Current project-owned code uses the [berglas Non-Commercial Source License](LICENSE), permitting non-commercial study, use, modification, and sharing. **Commercial use requires prior written permission from the author**, including paid distribution, commercial product or service integration, paid performances, and commercial teaching.

This is a source-available project with a non-commercial restriction, not an OSI-approved open-source license. Third-party assets and the Gradle Wrapper retain their own licenses; see [asset notes](docs/ASSETS.md). Previously MIT-licensed versions are not retroactively affected.

## Support the project

If you enjoy the app, share it with fellow magicians and give the project a **Star ⭐**. Your support and feedback help the project improve.

## Contact and collaboration

Interested in magic software development or collaboration? Add me on WeChat (vx): **w2790382370**.
