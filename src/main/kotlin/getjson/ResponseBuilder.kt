package getjson

/**
 * Utility for constructing HTTP response messages.
 */
object ResponseBuilder {
    /**
     * Builds a 200 OK response with a JSON body.
     * @param body JSON string to send in the response.
     */
    fun ok(body: String): String {
        val bodyBytes = body.toByteArray(Charsets.UTF_8)
        val contentLength = bodyBytes.size
        return "HTTP/1.1 200 OK\r\n" +
                "Content-Type: application/json\r\n" +
                "Content-Length: $contentLength\r\n" +
                "Connection: close\r\n" +
                "\r\n" +
                body
    }

    /**
     * Builds a 404 Not Found response (text/plain content).
     */
    fun notFound(): String {
        val body = "404 Not Found"
        val bodyBytes = body.toByteArray(Charsets.UTF_8)
        val contentLength = bodyBytes.size
        return "HTTP/1.1 404 Not Found\r\n" +
                "Content-Type: text/plain\r\n" +
                "Content-Length: $contentLength\r\n" +
                "Connection: close\r\n" +
                "\r\n" +
                body
    }

    /**
     * Builds a 400 Bad Request response (text/plain content), including an error message.
     */
    fun badRequest(message: String = "Bad Request"): String {
        val body = "400 Bad Request: $message"
        val bodyBytes = body.toByteArray(Charsets.UTF_8)
        val contentLength = bodyBytes.size
        return "HTTP/1.1 400 Bad Request\r\n" +
                "Content-Type: text/plain\r\n" +
                "Content-Length: $contentLength\r\n" +
                "Connection: close\r\n" +
                "\r\n" +
                body
    }
}
