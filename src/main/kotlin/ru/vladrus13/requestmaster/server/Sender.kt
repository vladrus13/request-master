package ru.vladrus13.requestmaster.server

import java.io.BufferedReader
import java.io.Closeable
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

class Sender(timeout: Long) : Closeable {
    private val service = Executors.newSingleThreadScheduledExecutor()
    private val queue = ConcurrentLinkedQueue<RequestPair>()
    private val answers = ConcurrentHashMap<Long, ServerRespond>()
    private val counter = AtomicLong(0)

    init {
        val runnable = Runnable {
            val requestPair = queue.poll() ?: return@Runnable
            try {
                val url = URL(requestPair.request.request)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = requestPair.request.requestMethod
                val content = StringBuilder()
                BufferedReader(InputStreamReader(connection.inputStream)).use { input ->
                    var inputLine: String?
                    while (input.readLine().also { inputLine = it } != null) {
                        content.append(inputLine)
                    }
                }
                answers[requestPair.id] = ServerRespond.Successful(content.toString())
            } catch (e: IOException) {
                answers[requestPair.id] = ServerRespond.ExceptionOnExecution(e)
            }
        }
        service.scheduleAtFixedRate(runnable, 0, timeout, TimeUnit.MILLISECONDS)
    }

    fun run(request: ServerRequest): ServerRespond {
        val id = counter.incrementAndGet()
        queue.add(RequestPair(id, request))
        while (true) {
            if (answers.containsKey(id)) {
                val answer = answers[id]!!
                answers.remove(id)
                return answer
            }
        }
    }

    override fun close() {
        service.shutdown()
    }

    class RequestPair(val id: Long, val request: ServerRequest)
}