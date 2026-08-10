package com.jgv.workoutplanner.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import com.jgv.workoutplanner.core.designsystem.AppTheme
import com.jgv.workoutplanner.core.designsystem.Dimens

/**
 * Selection controls shared by the onboarding screens (README §4.3–§4.5).
 *
 * Each one puts the click handler on the whole row or chip rather than on the checkbox
 * or radio button, so the touch target is the size of the thing that looks tappable, and
 * sets [Role] so a screen reader announces what kind of control it is. The inner
 * `Checkbox`/`RadioButton` takes a `null` callback: the parent already owns the
 * interaction, and leaving it clickable would announce two controls for one choice.
 */

/** A heading above a group of options, with optional supporting copy. */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
        )
        if (description != null) {
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = Dimens.SpacingExtraSmall),
            )
        }
    }
}

/**
 * One option from a mutually exclusive set.
 *
 * A radio row rather than a chip when the options are sentences, or when there are few
 * enough that showing them stacked costs nothing and reads more clearly.
 */
@Composable
fun <T> SingleChoiceRow(
    label: String,
    selected: Boolean,
    value: T,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = Dimens.MinTouchTarget)
            .selectable(
                selected = selected,
                role = Role.RadioButton,
                onClick = { onSelect(value) },
            )
            .padding(vertical = Dimens.SpacingExtraSmall),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = null)
        Column(modifier = Modifier.padding(start = Dimens.SpacingMedium)) {
            Text(text = label, style = MaterialTheme.typography.bodyLarge)
            if (supportingText != null) {
                Text(
                    text = supportingText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/** One independently selectable option. */
@Composable
fun CheckboxRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    supportingText: String? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = Dimens.MinTouchTarget)
            .toggleable(
                value = checked,
                enabled = enabled,
                role = Role.Checkbox,
                onValueChange = onCheckedChange,
            )
            .padding(vertical = Dimens.SpacingExtraSmall),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(checked = checked, onCheckedChange = null, enabled = enabled)
        Column(modifier = Modifier.padding(start = Dimens.SpacingMedium)) {
            Text(text = label, style = MaterialTheme.typography.bodyLarge)
            if (supportingText != null) {
                Text(
                    text = supportingText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/**
 * A wrapping row of short options, one of which is selected.
 *
 * For values whose labels are a word or two — days per week, session length — where a
 * stack of radio rows would waste a screen of vertical space.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun <T> SingleChoiceChipRow(
    options: List<T>,
    selected: T?,
    onSelect: (T) -> Unit,
    // Composable so callers can resolve a string resource per option — several of these
    // labels are plurals or formatted values rather than plain text.
    label: @Composable (T) -> String,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingSmall),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpacingExtraSmall),
    ) {
        options.forEach { option ->
            FilterChip(
                selected = option == selected,
                onClick = { onSelect(option) },
                label = { Text(text = label(option)) },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SelectionControlsPreview() {
    AppTheme {
        Column(
            modifier = Modifier.padding(Dimens.ScreenPadding),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpacingMedium),
        ) {
            SectionHeader(
                title = "Training days per week",
                description = "This decides how your week is split.",
            )
            SingleChoiceChipRow(
                options = listOf(2, 3, 4, 5),
                selected = 3,
                onSelect = {},
                label = { "$it days" },
            )
            SingleChoiceRow(
                label = "Build muscle",
                selected = true,
                value = Unit,
                onSelect = {},
            )
            CheckboxRow(
                label = "Avoid pressing overhead",
                checked = true,
                onCheckedChange = {},
                supportingText = "Suggested by: Rotator cuff injury",
            )
            CheckboxRow(
                label = "Bodyweight",
                checked = true,
                onCheckedChange = {},
                enabled = false,
                supportingText = "Always available",
            )
        }
    }
}
