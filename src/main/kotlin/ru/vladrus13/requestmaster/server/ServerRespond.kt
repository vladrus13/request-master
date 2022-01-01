package ru.vladrus13.requestmaster.server

sealed class ServerRespond {
    class ExceptionOnExecution(val e: Exception) : ServerRespond()
    class Successful(val respond: String) : ServerRespond()
}