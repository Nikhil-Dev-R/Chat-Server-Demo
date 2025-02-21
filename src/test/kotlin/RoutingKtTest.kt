import io.ktor.client.plugins.websocket.*
import io.ktor.server.testing.*
import kotlin.test.Test

class RoutingKtTest {

    @Test
    fun testWebsocketWs() = testApplication {
        application {
            TODO("Add the Ktor module for the test")
        }
        val client = createClient {
            install(WebSockets)
        }
        client.webSocket("/ws") {
            TODO("Please write your test here")
        }
    }
}