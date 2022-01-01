package ru.vladrus13.requestmaster.exceptions

class OutputTypeRequiredException : ApiMasterException(IllegalStateException("Output type required"))