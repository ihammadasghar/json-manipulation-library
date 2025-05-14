import com.sun.net.httpserver.HttpExchange
import java.lang.reflect.InvocationTargetException
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.full.findAnnotation

annotation class Mapping(val value: String)
annotation class Path(val value: String)
annotation class Param(val value: String)

class Router(controllers: List<KClass<*>>) {
    private val routes: MutableMap<String, Pair<KFunction<*>, Any>> = mutableMapOf()

    init {
        controllers.forEach { controllerClass ->
            val controllerInstance = controllerClass.createInstance() // Use createInstance()
            val methods = controllerClass.declaredFunctions

            val classMapping = controllerClass.findAnnotation<Mapping>()?.value ?: ""

            methods.forEach { method ->
                val methodMapping = method.findAnnotation<Mapping>()?.value ?: ""
                val path = "$classMapping/$methodMapping".removePrefix("/").replace("//", "/")
                routes[path] = Pair(method, controllerInstance)
            }
        }
    }

    fun handle(exchange: HttpExchange) {
        try {
            val path = exchange.requestURI.path.removePrefix("/")
            val (method, instance) = routes[path] ?: run {
                sendError(exchange, 404, "Not Found")
                return
            }

            val parameters = getParameters(exchange, method)
            val result = method.call(instance, *parameters) ?: ""  //handles null
            val jsonResponse = toJsonValue(result).toJsonString()
            sendResponse(exchange, 200, jsonResponse)
        } catch (e: InvocationTargetException) {
            val cause = e.cause ?: e
            println("Exception during method invocation: ${cause.message}") // Log the error
            sendError(exchange, 500, "Internal Server Error: ${cause.message}")
        } catch (e: Exception) {
            println("Exception: ${e.message}")
            sendError(exchange, 500, "Internal Server Error: ${e.message}")
        }
    }

    private fun getParameters(exchange: HttpExchange, method: KFunction<*>): Array<Any?> {
        val queryParams = parseQueryString(exchange.requestURI.query)
        val pathSegments = exchange.requestURI.path.removePrefix("/").split("/")
        val methodSegments = method.findAnnotation<Mapping>()?.value?.removePrefix("/")?.split("/") ?: emptyList()
        val parameters = mutableListOf<Any?>()

        method.parameters.forEach { parameter ->
            when {
                parameter.findAnnotation<Path>() != null -> {
                    val pathParamName = parameter.findAnnotation<Path>()!!.value
                    val pathParamIndex = methodSegments.indexOf(pathParamName)
                    if (pathParamIndex != -1 && pathParamIndex < pathSegments.size) {
                        parameters.add(URLDecoder.decode(pathSegments[pathParamIndex], StandardCharsets.UTF_8.name()))
                    } else {
                        parameters.add(null) // Or throw an exception: throw IllegalArgumentException("Path parameter '$pathParamName' not found")
                    }
                }
                parameter.findAnnotation<Param>() != null -> {
                    val paramName = parameter.findAnnotation<Param>()!!.value
                    parameters.add(queryParams[paramName])
                }
                else -> {
                    parameters.add(null)
                }
            }
        }
        return parameters.toTypedArray()
    }

    private fun parseQueryString(query: String?): Map<String, String?> {
        if (query.isNullOrEmpty()) return emptyMap()
        return query.split("&")
            .mapNotNull {
                val parts = it.split("=")
                if (parts.size == 2) {
                    Pair(
                        URLDecoder.decode(parts[0], StandardCharsets.UTF_8.name()),
                        URLDecoder.decode(parts[1], StandardCharsets.UTF_8.name())
                    )
                } else null
            }
            .associate { it }
    }

    private fun sendResponse(exchange: HttpExchange, code: Int, response: String) {
        exchange.responseHeaders.set("Content-Type", "application/json")
        val bytes = response.toByteArray(StandardCharsets.UTF_8)
        exchange.sendResponseHeaders(code, bytes.size.toLong())
        val outputStream = exchange.responseBody
        outputStream.write(bytes)
        outputStream.close()
        exchange.close()
    }

    private fun sendError(exchange: HttpExchange, code: Int, message: String) {
        exchange.sendResponseHeaders(code, message.length.toLong())
        val outputStream = exchange.responseBody
        outputStream.write(message.toByteArray(StandardCharsets.UTF_8))
        outputStream.close()
        exchange.close()
    }
}
