package ru.vladrus13.requestmaster.response

import ru.vladrus13.requestmaster.server.ServerRespond
import kotlin.reflect.KClass
import kotlin.reflect.KType

interface ResponseParser<R : Any> {
    fun parse(input: ServerRespond, kclass: KClass<out R>?, genericTypes: List<KType> = listOf()): Response<out R>
}