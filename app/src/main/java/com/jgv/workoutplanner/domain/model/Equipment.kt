package com.jgv.workoutplanner.domain.model

/**
 * Equipment the user can train with (spec §4.3).
 *
 * An exercise declares what it *requires*; anything the user has not selected makes
 * that exercise `Unavailable` rather than `Excluded` (spec §9) — a different
 * category, because missing equipment is a logistics problem, not a safety one.
 *
 * [BODYWEIGHT] is modelled as real equipment rather than "no equipment" so that
 * every exercise has a non-empty requirement set and bodyweight alternatives can be
 * matched like any other. Onboarding always includes it in the available set.
 */
enum class Equipment {
    BODYWEIGHT,
    DUMBBELLS,
    BARBELL,
    BENCH,
    CABLE_MACHINE,
    SELECTORIZED_MACHINE,
    PULL_UP_BAR,
    RESISTANCE_BAND,
    CARDIO_MACHINE,
}
