package com.jgv.workoutplanner.testing

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * Replaces `Dispatchers.Main` for the duration of a test.
 *
 * `viewModelScope` runs on the main dispatcher, which does not exist in a JVM unit test —
 * without this, touching any ViewModel throws. [UnconfinedTestDispatcher] rather than the
 * standard one so a `launch` inside an event handler runs to completion before the test
 * moves on, which is what makes assertions like "after toggling, the draft says X" read
 * as straight lines.
 *
 * **Construct ViewModels lazily.** JUnit builds the test class — running every field
 * initializer — before it applies rules, and `viewModelScope` resolves
 * `Dispatchers.Main.immediate` the moment a ViewModel is constructed. A ViewModel in a
 * plain `val` therefore throws "Module with the Main dispatcher is missing" before this
 * rule has had a chance to run. `by lazy`, `@Before`, or a factory function all avoid it.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    private val dispatcher: TestDispatcher = UnconfinedTestDispatcher(),
) : TestWatcher() {

    override fun starting(description: Description) {
        Dispatchers.setMain(dispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
