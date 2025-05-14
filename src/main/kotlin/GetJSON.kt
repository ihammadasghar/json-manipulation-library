import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress
import java.nio.charset.StandardCharsets
import java.util.concurrent.Executors
import kotlin.reflect.KClass

class GetJson(private val controllers: List<KClass<*>>) {
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

    private fun sendError(exchange: HttpExchange, code: Int, message: String) {
        exchange.sendResponseHeaders(code, message.length.toLong())
        val outputStream = exchange.responseBody
        outputStream.write(message.toByteArray(StandardCharsets.UTF_8))
        outputStream.close()
        exchange.close()
    }
}