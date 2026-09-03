package com.ktconsulting.transfer_app.exception

import com.ktconsulting.transfer_app.dto.response.ErrorResponse
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import jakarta.validation.ConstraintViolationException
import org.slf4j.LoggerFactory

@RestControllerAdvice
class GlobalExceptionHandler(private val messageSource: MessageSource) {

    private val log = LoggerFactory.getLogger(javaClass)

    private fun resolve(key: String, args: Array<out Any> = emptyArray()): String {
        val locale = LocaleContextHolder.getLocale()
        return messageSource.getMessage(key, args, key, locale) ?: key
    }

    private fun error(status: HttpStatus, message: String) =
        ResponseEntity.status(status).body(ErrorResponse(status.value(), status.reasonPhrase, message))

    @ExceptionHandler(ResourceNotFoundException::class)
    fun handle(ex: ResourceNotFoundException) =
        error(HttpStatus.NOT_FOUND, resolve(ex.messageKey, ex.args))

    @ExceptionHandler(DuplicateResourceException::class)
    fun handle(ex: DuplicateResourceException) =
        error(HttpStatus.CONFLICT, resolve(ex.messageKey, ex.args))

    @ExceptionHandler(UnauthorizedException::class)
    fun handle(ex: UnauthorizedException) =
        error(HttpStatus.FORBIDDEN, resolve(ex.messageKey, ex.args))

    @ExceptionHandler(InvalidCredentialsException::class)
    fun handle(ex: InvalidCredentialsException) =
        error(HttpStatus.UNAUTHORIZED, resolve(ex.messageKey, ex.args))

    @ExceptionHandler(BusinessRuleException::class)
    fun handle(ex: BusinessRuleException) =
        error(HttpStatus.UNPROCESSABLE_ENTITY, resolve(ex.messageKey, ex.args))

    @ExceptionHandler(ConflictException::class)
    fun handle(ex: ConflictException) =
        error(HttpStatus.CONFLICT, resolve(ex.messageKey, ex.args))

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handle(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val messages = ex.bindingResult.fieldErrors.joinToString("; ") { fieldError ->
            "${fieldError.field}: ${fieldError.defaultMessage}"
        }
        return error(HttpStatus.BAD_REQUEST, messages)
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handle(ex: ConstraintViolationException): ResponseEntity<ErrorResponse> {
        val messages = ex.constraintViolations.joinToString("; ") { it.message }
        return error(HttpStatus.BAD_REQUEST, messages)
    }

    @ExceptionHandler(AccessDeniedException::class)
    fun handle(ex: AccessDeniedException) =
        error(HttpStatus.FORBIDDEN, resolve("error.auth.unauthorized"))

    @ExceptionHandler(Exception::class)
    fun handleAll(ex: Exception): ResponseEntity<ErrorResponse> {
        log.error("Unhandled exception", ex)
        return error(HttpStatus.INTERNAL_SERVER_ERROR, resolve("error.internal"))
    }
}
