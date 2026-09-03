package com.ktconsulting.transfer_app.exception

open class ApiException(
    val messageKey: String,
    val args: Array<out Any> = emptyArray(),
    cause: Throwable? = null
) : RuntimeException(messageKey, cause)

class ResourceNotFoundException(messageKey: String, vararg args: Any)
    : ApiException(messageKey, args)

class DuplicateResourceException(messageKey: String, vararg args: Any)
    : ApiException(messageKey, args)

class UnauthorizedException(messageKey: String = "error.auth.unauthorized", vararg args: Any)
    : ApiException(messageKey, args)

class InvalidCredentialsException(messageKey: String = "error.auth.invalid_credentials")
    : ApiException(messageKey)

class BusinessRuleException(messageKey: String, vararg args: Any)
    : ApiException(messageKey, args)

class ConflictException(messageKey: String, vararg args: Any)
    : ApiException(messageKey, args)
