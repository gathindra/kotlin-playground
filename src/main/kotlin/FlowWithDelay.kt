import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import java.util.*
import kotlin.system.exitProcess

// Define the channel
val delayChannel = Channel<Int>()

// Subscribe the channel as a flow and delay the stream 1 sec and execute in IO thread
val delayConsumer = delayChannel
    .receiveAsFlow()
    .onEach { delay(1000L) }
    .flowOn(Dispatchers.IO)

// We need to set main coroutine scope as run-blocking as we
// need to exit the routine after consumer receive all the contents
fun main(args: Array<String>) = runBlocking {

    // Start the another coroutine scope in IO context
    withContext(Dispatchers.IO) {
        // Kick off consumer in another coroutine to run both
        // consumer and producer. Otherwise consumer indefinitely
        // wait for producer
        launch {
            consumeDataWithDelay()
        }

        produceDataWithNoDelays()
    }

}

suspend fun consumeDataWithDelay() = coroutineScope {
    delayConsumer.collect {
        println("Consumer received $it ${Date().toString()}")
        if (it == 20) {
            println("Consumer done!")
            exitProcess(0)
        }
    }
}

suspend fun produceDataWithNoDelays() = coroutineScope {
    for (i in 1..20) {
        delayChannel.send(i)
    }
    println("Producer done!")
}