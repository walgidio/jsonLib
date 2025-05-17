package getjson.annotations

/**
 * Annotation for binding path variables to method arguments.
 *
 * Applied to method parameters to indicate they should be populated from
 * path segments marked with {variable} syntax in the [Mapping] annotation.
 *
 * @property name The name of the path variable (defaults to parameter name)
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class Path(val name: String)