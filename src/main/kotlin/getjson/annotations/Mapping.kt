package getjson.annotations

/**
 * Annotation for declaring HTTP endpoint mappings.
 *
 * This annotation can be applied to:
 * - Classes to specify a base path for all methods in the controller
 * - Methods to specify their individual paths
 *
 * Paths can contain template variables in {variable} format.
 *
 * @property value The path template for the endpoint
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Mapping(val value: String)