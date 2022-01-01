package ru.vladrus13.requestmaster.requests

import ru.vladrus13.requestmaster.server.ServerRequest

sealed class Request(private val url: String, private val requestMethod: String) {
    private val keys = ArrayList<Pair<String, String>>()

    fun addKey(key: String, value: String): Request {
        keys.add(Pair(key, value))
        return this
    }

    fun toServerRequest(url: String): ServerRequest {
        val request = StringBuilder()
        request.append(url).append(this.url).append("?")
        for (key in keys) {
            request.append(key.first).append("=").append(key.second).append("&")
        }
        return ServerRequest(requestMethod, request.toString())
    }

    class Get(url: String) : Request(url, "GET")

    class Post(url: String) : Request(url, "POST")


}