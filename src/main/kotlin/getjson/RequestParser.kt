package getjson.http

import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.URLDecoder

/**
 * Utility to parse a raw HTTP request. Supports only GET requests.
 */
object RequestParser {
    /** Simple data class for a parsed HTTP GET request. */
    data class HttpRequest(val path: String, val queryParams: Map<String, String>)

    /**
     * Parses an HTTP request from the given input stream.
     * @return HttpRequest with path and query parameters, or null if parsing fails or method is not GET.
     */
    fun parse(input: InputStream): HttpRequest? {
        val reader = BufferedReader(InputStreamReader(input))
        val requestLine = reader.readLine() ?: return null  // no data
        val parts = requestLine.split(" ")
        if (parts.size < 3) return null
        val method = parts[0]
        val resource = parts[1]  // e.g. "/api/endpoint?x=1&y=2"
        if (method.uppercase() != "GET") {
            return null  // Only GET is supported
        }

        // Separate path and query string
        val questionIdx = resource.indexOf('?')
        var path = if (questionIdx >= 0) {
            resource.substring(1, questionIdx)
        } else {
            resource.substring(1)
        }
        // Normalize path: remove any trailing slash
        if (path.endsWith("/")) {
            path = path.removeSuffix("/")
        }
        val queryString = if (questionIdx >= 0) resource.substring(questionIdx + 1) else ""
        val queryParams = mutableMapOf<String, String>()
        if (queryString.isNotEmpty()) {
            // Parse key=value pairs in the query string
            val pairs = queryString.split("&")
            for (pair in pairs) {
                val eqIdx = pair.indexOf('=')
                val key = if (eqIdx >= 0) pair.substring(0, eqIdx) else pair
                val value = if (eqIdx >= 0) pair.substring(eqIdx + 1) else ""
                // Decode URL-encoded components (e.g. %20 for spaces)
                val decodedKey = URLDecoder.decode(key, "UTF-8")
                val decodedValue = URLDecoder.decode(value, "UTF-8")
                queryParams[decodedKey] = decodedValue
            }
        }

        // Consume the remaining header lines (not used in this basic implementation)
        while (true) {
            val line = reader.readLine() ?: break
            if (line.isEmpty()) break  // headers end with a blank line
        }
        return HttpRequest(path, queryParams)
    }
}
