package ru.vladrus13.requestmaster

import ru.vladrus13.requestmaster.requests.Request
import ru.vladrus13.requestmaster.response.Response
import ru.vladrus13.requestmaster.response.ResponseParser
import ru.vladrus13.requestmaster.server.Sender
import java.io.Closeable
import kotlin.reflect.KClass
import kotlin.reflect.KType

abstract class APIMaster(private val url: String, timeout: Long) : Closeable {

    abstract val parser: ResponseParser<*>

    private val sender: Sender = Sender(timeout)

    protected fun <T : Any> execute(request: Request, kClass: KClass<T>?, genericTypes: List<KType> = listOf()): Response<out T> {
        return (parser as ResponseParser<T>).parse(sender.run(request.toServerRequest(url)), kClass, genericTypes)
    }

    fun get(url: String) = execute<Unit>(Request.Get(url), null)

    fun <T : Any> post(url: String, kClass: KClass<T>) = execute(Request.Post(url), kClass)

    override fun close() {
        sender.close()
    }
}