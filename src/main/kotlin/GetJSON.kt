import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress
import java.nio.charset.StandardCharsets
import java.util.concurrent.Executors
import kotlin.reflect.KClass

/**
 * Stores controllers and allows for the start of a HttpServer to handle requests.
 *
 * @property controllers list of controllers.
 * @constructor creates a server with a list of controllers.
 */
class GetJson(private val controllers: List<KClass<*>>) {
    /**
     * Starts a HttpServer at port [port].
     * Catches GET requests and sends them to the Router for handling.
     */
    fun start(port: Int) {
        val server = HttpServer.create(InetSocketAddress(port), 0)
        val router = Router(controllers)
        server.createContext("/", HttpHandler { exchange ->
            if (exchange.requestMethod == "GET") {
                router.handle(exchange)
            } else {
                sendError(exchange, 405, "Method Not Allowed")
            }
        })
        server.executor = Executors.newFixedThreadPool(10) //thread pool
        server.start()
        println("Server started on port $port")
    }

    /**
     * Sends an error message [message] with code [code] through the HttpExchange [exchange].
     */
    private fun sendError(exchange: HttpExchange, code: Int, message: String) {
        exchange.sendResponseHeaders(code, message.length.toLong())
        val outputStream = exchange.responseBody
        outputStream.write(message.toByteArray(StandardCharsets.UTF_8))
        outputStream.close()
        exchange.close()
    }
}