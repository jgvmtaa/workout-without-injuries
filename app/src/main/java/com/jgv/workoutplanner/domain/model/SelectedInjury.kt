package com.jgv.workoutplanner.domain.model

/**
 * One injury the user reported, with the detail they chose to give (README §4.4).
 *
 * [affectedSide] is captured but **not** read by the MVP engine. Filtering is
 * bilateral: if a movement is excluded it is excluded on both sides. The field is
 * preserved so a later version can build unilateral plans without a data migration
 * (README §4.4).
 */
data class SelectedInjury(
    val injuryId: InjuryId,
    val affectedSide: BodySide = BodySide.NOT_SPECIFIED,
    val status: InjuryStatus = InjuryStatus.HISTORICAL,
)

/** Which side of the body an injury affects (README §4.4). */
enum class BodySide {
    LEFT,
    RIGHT,
    BOTH,
    NOT_SPECIFIED,
}

/**
 * How current an injury is (README §4.4).
 *
 * Recorded for the user's own review on the profile screen. Like [BodySide] it does
 * not change eligibility in the MVP — a historical injury still produces the same
 * *suggested* limitations, and the user decides which ones currently apply.
 */
enum class InjuryStatus {
    CURRENTLY_SYMPTOMATIC,
    HISTORICAL,
    RECOVERING,
    NOT_SPECIFIED,
}
