# Workout Planner

Workout Planner is an Android app that creates workout plans around a person's
training preferences, available equipment, and movement limitations.

Instead of treating an injury as a universal rule, the app lets the user confirm
which movements they currently need to avoid. It then filters exercises that may
conflict with those limitations and explains why each exercise was excluded.

## What The App Does

- Collects the user's goal, experience level, weekly schedule, preferred workout
  duration, and available equipment.
- Lets the user record previous injuries and review suggested movement
  limitations.
- Requires the user to explicitly confirm which limitations currently apply.
- Generates a consistent workout plan from suitable exercises.
- Shows why exercises were excluded or are unavailable.
- Lets the user browse exercises and remove, replace, reorder, or regenerate
  exercises in a plan.
- Saves the profile and current workout plan on the device.

## How It Works

1. Complete the training profile and safety acknowledgement.
2. Select any relevant injury history.
3. Review and confirm movement limitations.
4. Generate a workout plan using the available exercises.
5. Review or adjust the plan and browse exercise alternatives.

## Safety

Workout Planner is not medical advice. It does not diagnose injuries, prescribe
rehabilitation, or replace guidance from a physician or physical therapist.
Users should stop any exercise that causes pain and follow restrictions provided
by their healthcare professional.

## MVP Scope

The initial app works locally and does not require an account, cloud service,
subscription, social features, or AI-generated recommendations.

This repository currently contains the implementation plan for the Android MVP.
See the [task breakdown](tasks/README.md) for project phases and progress, and
the [specification](docs/spec.md) for the numbered sections (`§1`–`§31`) that the
tasks and the code comments reference.
