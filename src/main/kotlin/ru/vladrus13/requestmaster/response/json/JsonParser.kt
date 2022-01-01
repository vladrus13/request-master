package ru.vladrus13.requestmaster.response.json

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import ru.vladrus13.requestmaster.response.BasicServerExceptionParser
import ru.vladrus13.requestmaster.response.Response
import ru.vladrus13.requestmaster.response.json.annotation.Jsonable
import ru.vladrus13.requestmaster.server.ServerRespond
import java.math.BigDecimal
import java.math.BigInteger
import kotlin.reflect.*

@Suppress("UNCHECKED_CAST")
class JsonParser : BasicServerExceptionParser<Any>() {

    private fun throwIncomparableTypesException(name: String, excepted: KClass<*>, find: KClass<*>): Nothing =
        throw IllegalArgumentException("Incomparable types exception on $name: find ${find.simpleName}, excepted ${excepted.simpleName}")

    interface OpenJsonParser<T> {
        fun check(jsonElement: JsonElement): Boolean
        fun cast(jsonElement: JsonElement): T
    }

    open class EnterJsonParser<T>(val checker: JsonElement.() -> Boolean, val caster: JsonElement.() -> T) :
        OpenJsonParser<T> {
        override fun check(jsonElement: JsonElement): Boolean = jsonElement.checker()

        override fun cast(jsonElement: JsonElement): T = jsonElement.caster()
    }

    class PrimitiveJsonParser<T>(checker: JsonPrimitive.() -> Boolean, caster: JsonElement.() -> T) :
        EnterJsonParser<T>({
            if (!this.isJsonPrimitive) {
                false
            } else {
                checker(this as JsonPrimitive)
            }
        }, caster)

    private val standards: Map<KClass<*>, EnterJsonParser<*>> = hashMapOf(
        Int::class to PrimitiveJsonParser(JsonPrimitive::isNumber, JsonElement::getAsInt),
        Long::class to PrimitiveJsonParser(JsonPrimitive::isNumber, JsonElement::getAsLong),
        String::class to PrimitiveJsonParser(JsonPrimitive::isString, JsonElement::getAsString),
        Double::class to PrimitiveJsonParser(JsonPrimitive::isNumber, JsonElement::getAsDouble),
        BigDecimal::class to PrimitiveJsonParser(JsonPrimitive::isNumber, JsonElement::getAsBigDecimal),
        BigInteger::class to PrimitiveJsonParser(JsonPrimitive::isNumber, JsonElement::getAsBigInteger),
        Boolean::class to PrimitiveJsonParser(JsonPrimitive::isBoolean, JsonElement::getAsBoolean)
    )

    private fun <T : Any> getCollection(
        @Suppress("UNUSED_PARAMETER") clazz: KClass<T>,
        jsonArray: JsonArray,
        pre: JsonElement.() -> Any
    ): ArrayList<T> {
        return ArrayList<T>().apply {
            addAll(jsonArray.map { pre(it) as T })
        }
    }

    private fun <T : Any> parse(
        name: String,
        currentJson: JsonElement,
        element: KClass<T>,
        genericTypes: List<KType> = listOf()
    ): T {
        if (standards.containsKey(element)) {
            val parser = standards[element]!!
            if (!parser.checker(currentJson)) {
                throwIncomparableTypesException(name, element, currentJson::class)
            }
            return parser.caster(currentJson)!! as T
        }
        return when (element) {
            ArrayList::class -> {
                if (!currentJson.isJsonArray) {
                    throwIncomparableTypesException(name, ArrayList::class, currentJson::class)
                }
                val generic = genericTypes[0].classifier as KClass<*> // (genericTypes[0].javaType as Class<*>).kotlin

                getCollection(generic, currentJson.asJsonArray) {
                    parse("arrayPart", this, generic)
                } as T
            }
            else -> {
                if (!currentJson.isJsonObject) {
                    throwIncomparableTypesException(name, element, currentJson::class)
                }
                for (constructor in element.constructors) {
                    if (constructor.parameters.filter { !it.isOptional }
                            .all { parameter -> parameter.annotations.any { annotation -> annotation is Jsonable } }) {
                        val parameters = mutableListOf<Pair<KParameter, Any>>()
                        for (parameter in constructor.parameters) {
                            if (parameter.annotations.any{ annotation -> annotation is Jsonable }) {
                                val annotation = parameter.annotations.find { annotation -> annotation is Jsonable }!! as Jsonable
                                val parameterJson = currentJson.asJsonObject.get(annotation.name)
                                if (parameterJson == null) {
                                    if (annotation.isRequired) throw IllegalStateException("Can't find ${annotation.name}")
                                    continue
                                }
                                val asType = parameter.type
                                parameters.add(Pair(parameter, parse(annotation.name, parameterJson, asType.classifier as KClass<*>, asType.arguments.mapNotNull {
                                    it.type
                                })))
                            }
                        }
                        return constructor.callBy(parameters.associate { it.first to it.second })
                    }
                }
                throw IllegalStateException("Can't found constructor with Jsonable")
            }
        }
    }

    override fun parseSuccessful(input: ServerRespond.Successful, kClass: KClass<out Any>, genericTypes: List<KType>): Response<out Any> {
        return try {
            val jsonElement = com.google.gson.JsonParser.parseString(input.respond)
            Response.SuccessfulResponse(
                parse(
                    "data",
                    jsonElement,
                    kClass,
                    genericTypes
                )
            )
        } catch (e: Exception) {
            Response.ParseException(e)
        }
    }
}