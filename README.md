# ClawPet 🐾

A live virtual pet widget for your Android home screen — the OpenClaw mascot, right on your dashboard.

## Features

- **Home screen widget** — see your pet's mood, stats, and quick-action buttons at a glance
- **Interactive** — Feed 🍖, Pet 🤗, or Play 🎮 with your pet from the widget or app
- **Animated pet** — the claw mascot bounces, breathes, and changes expression based on mood
- **Decay system** — stats decay over time; neglect your pet and it gets sad/hungry/sleepy
- **Leveling** — gain XP from interactions, level up your pet
- **6 moods** — Happy, Hungry, Sleepy, Sad, Excited, and… Dead 💀

## Architecture

- **Kotlin** + **Jetpack Compose** (app UI with animated Canvas pet)
- **Glance** for home screen widget (3×3 cells)
- **Room** for persistent pet state
- **Hilt** for dependency injection
- **WorkManager** (background stat decay)

## Building

```bash
./gradlew assembleRelease
```

Requires Android SDK 34+ and JDK 17.

## CI

GitHub Actions builds release APKs on push to `main` and publishes to GitHub Releases. Tag `v*` pushes create stable releases.

## License

MIT