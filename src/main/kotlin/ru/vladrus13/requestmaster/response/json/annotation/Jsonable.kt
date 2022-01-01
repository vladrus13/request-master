package ru.vladrus13.requestmaster.response.json.annotation

import java.lang.annotation.Inherited

@Inherited
@Retention
annotation class Jsonable(val name: String, val isRequired: Boolean)