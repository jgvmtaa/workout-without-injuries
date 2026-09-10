# Follow-ups

This file contains only unfinished, actionable work. Remove an item when it is complete.
Use [`README.md`](../README.md) for the product overview,
[`spec.md`](spec.md) for app behavior, and [`tests.spec`](../tests.spec) for required
visual coverage.

## Testing

- [ ] Implement the Robolectric and Roborazzi screenshot suite defined in
  [`tests.spec`](../tests.spec), commit its baselines, and run it in CI.
- [ ] Add instrumentation coverage for persistence across actual process death,
  including a draft profile, completed profile, saved workout plan, and returning-user
  start destination.
- [ ] Run `connectedDebugAndroidTest` in an emulator-backed CI job.
## Product improvements

- [ ] Use the Android SplashScreen API to bridge profile loading and avoid flashing the
  in-app loading spinner during startup.
- [ ] Add a step counter or progress indicator to the six-step onboarding flow.
- [ ] Model time-based prescriptions so isometric exercises such as front and side
  planks display a hold duration instead of `1–1 reps`.
- [ ] Search localized exercise and filter labels rather than enum identifiers while
  preserving normalized matching.
- [ ] Show which selected injury or injuries produced each suggested movement
  limitation.

## Release readiness

- [ ] Configure release signing through secrets, decide whether to enable R8/minification,
  maintain the required keep rules, and validate the signed release bundle.
- [ ] Define the protection policy for health-adjacent profile data stored in
  `profile.json`; document the threat model and add encryption if required.
