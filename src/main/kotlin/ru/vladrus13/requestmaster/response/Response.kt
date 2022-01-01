package ru.vladrus13.requestmaster.response

sealed class Response<T> {
    class ServerException<T>(val e: Exception) : Response<T>()

    class ParseException<T>(val e: Exception) : Response<T>()

    class SuccessfulResponse<T>(val data: T) : Response<T>()
}