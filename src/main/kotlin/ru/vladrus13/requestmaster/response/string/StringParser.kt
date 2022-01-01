package ru.vladrus13.requestmaster.response.string

import ru.vladrus13.requestmaster.response.BasicServerExceptionParser
import ru.vladrus13.requestmaster.response.Response
import ru.vladrus13.requestmaster.server.ServerRespond
import kotlin.reflect.KClass
import kotlin.reflect.KType

class StringParser : BasicServerExceptionParser<String>() {

    override fun parseSuccessful(input: ServerRespond.Successful, kClass: KClass<out String>, genericTypes: List<KType>): Response<out String> =
        Response.SuccessfulResponse(input.respond)
}