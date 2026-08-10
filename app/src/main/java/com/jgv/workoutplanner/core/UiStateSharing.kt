package com.jgv.workoutplanner.core

import kotlinx.coroutines.flow.SharingStarted

/**
 * The sharing policy every screen's `uiState` uses.
 *
 * `WhileSubscribed` with a short grace period: the upstream flow stops when the screen
 * goes away, but survives a configuration change or a brief navigation away, so rotating
 * the device does not re-read the profile from disk. Zero would restart it every
 * rotation; `Eagerly` would keep it running for screens nobody is looking at.
 *
 * Five seconds is the value Android's architecture samples use, and the reasoning is
 * theirs: it comfortably exceeds a configuration change and is far below anything a user
 * would notice.
 */
val WhileUiSubscribed: SharingStarted = SharingStarted.WhileSubscribed(5_000L)
