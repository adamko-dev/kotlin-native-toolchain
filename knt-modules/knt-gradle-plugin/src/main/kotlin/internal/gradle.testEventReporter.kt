package dev.adamko.kntoolchain.internal

import java.time.Clock
import java.util.concurrent.atomic.AtomicInteger
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import org.gradle.api.tasks.testing.GroupTestEventReporter
import org.gradle.api.tasks.testing.TestEventReporter

internal sealed class BaseEventReporterContext(
  private val reporter: TestEventReporter,
  protected val failedCount: AtomicInteger,
  protected val clock: Clock = Clock.systemUTC(),
) : AutoCloseable {
  fun succeeded() {
    reporter.succeeded(clock.instant())
  }

  fun failed(message: String, details: String? = null) {
    reporter.failed(clock.instant(), message, details)
  }

  fun skipped() {
    reporter.skipped(clock.instant())
  }
}

internal class TestEventReporterContext(
  private val reporter: TestEventReporter,
  failedCount: AtomicInteger,
) : BaseEventReporterContext(reporter, failedCount) {
  override fun close() {
    reporter.close()
  }
}

internal class GroupTestEventReporterContext(
  private val reporter: GroupTestEventReporter,
  failedCount: AtomicInteger,
  clock: Clock,
) : BaseEventReporterContext(reporter, failedCount, clock) {

  fun reportTest(name: String, displayName: String = name): TestEventReporterContext {
    val reporter = reporter.reportTest(name, displayName)
    reporter.started(clock.instant())
    return TestEventReporterContext(reporter, failedCount)
  }

  fun reportGroup(name: String): GroupTestEventReporterContext {
    val reporter = reporter.reportTestGroup(name)
    reporter.started(clock.instant())
    return GroupTestEventReporterContext(reporter, failedCount, clock)
  }

  override fun close() {
    if (failedCount.get() > 0) {
      reporter.failed(clock.instant())
    } else {
      reporter.succeeded(clock.instant())
    }
    reporter.close()
  }
}

@OptIn(ExperimentalContracts::class)
internal fun GroupTestEventReporter.start(
  body: (GroupTestEventReporterContext) -> Unit,
) {
  contract { callsInPlace(body, InvocationKind.EXACTLY_ONCE) }
  val clock = Clock.systemUTC()
  started(clock.instant())
  GroupTestEventReporterContext(this, AtomicInteger(0), clock = clock).use(body)
}
