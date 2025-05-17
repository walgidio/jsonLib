package getjson.annotations

/**
 * Annotation for binding query parameters to method arguments.
 *
 * Applied to method parameters to indicate they should be populated from
 * HTTP query string parameters.
 *
 * @property name The name of the query parameter (defaults to parameter name)
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class Param(val name: String)