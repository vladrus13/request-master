package ru.vladrus13.requestmaster.response

import ru.vladrus13.requestmaster.exceptions.OutputTypeRequiredException
import ru.vladrus13.requestmaster.server.ServerRespond
import kotlin.reflect.KClass
import kotlin.reflect.KType

abstract class BasicServerExceptionParser<R : Any> : ResponseParser<R> {

    abstract fun parseSuccessful(input: ServerRespond.Successful, kClass: KClass<out R>, genericTypes: List<KType> = listOf()): Response<out R>

    override fun parse(input: ServerRespond, kclass: KClass<out R>?, genericTypes: List<KType>): Response<out R> =
        when (input) {
            is ServerRespond.ExceptionOnExecution -> Response.ServerException(input.e)
            is ServerRespond.Successful -> {
                if (kclass == null) Response.ParseException<R>(OutputTypeRequiredException())
                parseSuccessful(input, kclass!!, genericTypes)
            }
        }
}