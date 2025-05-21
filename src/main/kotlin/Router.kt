import com.sun.net.httpserver.HttpExchange
import java.lang.reflect.InvocationTargetException
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.full.findAnnotation

/**
 * Annotation to define URL mappings.
 * @property value the value for this annotation.
 */
annotation class Mapping(val value: String)

/**
 * Annotation to map path segments into function arguments.
 * @property value the value for this annotation.
 */
annotation class Path(val value: String)

/**
 * Annotation to map path arguments into function arguments.
 * @property value the value for this annotation.
 */
annotation class Param(val value: String)

/**
 *
 *
 * @constructor creates a router for the controllers.
 */
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

    /**
     * Tries to handle an [exchange].
     */
    fun handle(exchange: HttpExchange) {
        try {
            val requestPath = exchange.requestURI.path.removePrefix("/")
            val matchingRoute = findMatchingRoute(requestPath)

            if (matchingRoute != null) {
                val (method, instance) = matchingRoute
                val parameters = getParameters(exchange, method, requestPath)
                val result = method.call(instance, *parameters) ?: ""
                val jsonResponse = toJsonValue(result).toJsonString()
                sendResponse(exchange, 200, jsonResponse)
            } else {
                sendError(exchange, 404, "Not Found")
            }
        } catch (e: InvocationTargetException) {
            val cause = e.cause ?: e
            println("Exception during method invocation: ${cause.message}")
            sendError(exchange, 500, "Internal Server Error: ${cause.message}")
        } catch (e: Exception) {
            println("Exception: ${e.message}")
            sendError(exchange, 500, "Internal Server Error: ${e.message}")
        }
    }

    /**
     * Tries to find a matching route between the [requestPath] and the known [routes].
     * @return the route data on success and null on failure.
     */
    private fun findMatchingRoute(requestPath: String): Pair<KFunction<*>, Any>? {
        val requestSegments = requestPath.split("/")
        for ((routePath, routeData) in routes) {
            val routeSegments = routePath.split("/")
            if (routeSegments.size == requestSegments.size) {
                var match = true
                for (i in routeSegments.indices) {
                    if (routeSegments[i] != requestSegments[i] && !routeSegments[i].startsWith("{")) {
                        match = false
                        break
                    }
                }
                if (match) {
                    return routeData
                }
            }
        }
        return null
    }

    /**
     * 
     */
    private fun getParameters(exchange: HttpExchange, method: KFunction<*>, requestPath: String): Array<Any?> {
        val queryParams = parseQueryString(exchange.requestURI.query)
        val pathSegments = requestPath.split("/")
        val methodSegments = method.findAnnotation<Mapping>()?.value?.removePrefix("/")?.split("/") ?: emptyList()
        val argumentsForCall = mutableListOf<Any?>()

        val methodParametersMap = method.parameters.associateBy { it.name }

        val pathParams = mutableMapOf<String, String>()
        for (i in methodSegments.indices) {
            if (methodSegments[i].startsWith("{") && i < pathSegments.size) {
                val paramName = methodSegments[i].removeSurrounding("{", "}")
                pathParams[paramName] = pathSegments[i+1]
            }
        }

        method.parameters.forEach { parameter ->
            if (parameter.kind == KParameter.Kind.INSTANCE) {
                // Skips the instance of the controller itself
                return@forEach
            }

            val paramName = parameter.name
            val paramType = parameter.type.classifier as? KClass<*>

            val value: String? = when {
                parameter.findAnnotation<Path>() != null -> {
                    pathParams[parameter.findAnnotation<Path>()!!.value]
                }
                parameter.findAnnotation<Param>() != null -> {
                    queryParams[parameter.findAnnotation<Param>()!!.value]
                }
                else -> null
            }

            val convertedValue: Any? = when {
                value == null && parameter.type.isMarkedNullable -> null // Allow null for nullable parameters
                value == null && !parameter.type.isMarkedNullable -> {
                    throw IllegalArgumentException("Missing required parameter: ${parameter.name}")
                }
                paramType == String::class -> value
                paramType == Int::class -> value?.toIntOrNull()
                paramType == Double::class -> value?.toDoubleOrNull()
                paramType == Boolean::class -> value?.toBooleanStrictOrNull()
                else -> throw IllegalArgumentException("Unsupported parameter type for conversion: ${paramType?.qualifiedName}")
            }
            argumentsForCall.add(convertedValue)
        }
        return argumentsForCall.toTypedArray()
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

    /**
     * Sends a response [response] with code [code] through the HttpExchange [exchange].
     */
    private fun sendResponse(exchange: HttpExchange, code: Int, response: String) {
        exchange.responseHeaders.set("Content-Type", "application/json")
        val bytes = response.toByteArray(StandardCharsets.UTF_8)
        exchange.sendResponseHeaders(code, bytes.size.toLong())
        val outputStream = exchange.responseBody
        outputStream.write(bytes)
        outputStream.close()
        exchange.close()
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
