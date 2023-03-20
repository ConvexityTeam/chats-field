package chats.cash.chats_field

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@ExperimentalCoroutinesApi

class CoroutineRule(
    private val dispatcher: TestDispatcher = StandardTestDispatcher()
) : TestWatcher(), CoroutineScope by TestScope(dispatcher) {

    override fun starting(description: Description) {
        super.starting(description)
        Dispatchers.setMain(dispatcher)
    }

    override fun finished(description: Description) {
        super.finished(description)
//        cleanupTestCoroutines()
        Dispatchers.resetMain()
    }
}