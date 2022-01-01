package ru.vladrus13.requestmaster

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import ru.vladrus13.requestmaster.response.Response
import ru.vladrus13.requestmaster.response.json.JsonParser
import ru.vladrus13.requestmaster.response.json.annotation.Jsonable
import ru.vladrus13.requestmaster.server.ServerRespond
import kotlin.time.ExperimentalTime

class TestObjects {
    class ABCD {
        class A(
            @Jsonable(name = "b", isRequired = true)
            var b: B? = null,

            @Jsonable(name = "c", isRequired = true)
            var c: C? = null
        )

        class B(
            @Jsonable(name = "d", isRequired = true)
            var d: ArrayList<D> = ArrayList()
        )

        class C(
            @Jsonable(name = "d", isRequired = true)
            var d: D? = null
        )

        class D(
            @param:Jsonable(name = "a", isRequired = false)
            var a: A? = null
        )

        class Contester(
            @param:Jsonable(name = "a", isRequired = true)
            val a: A,
            @param:Jsonable(name = "b", isRequired = true)
            val b: String,
            @param:Jsonable(name = "c", isRequired = true)
            val c: ArrayList<Int>
        )
    }
}

@ExperimentalTime
@Execution(ExecutionMode.CONCURRENT)
class RequestMasterTest {
    @Nested
    inner class ABCD {

        private val test01 = """
            {
              "a": {
                "b": {
                  "d": [
                    {
                    },
                    {
                    }
                  ]
                },
                "c": {
                  "d": {
                  }
                }
              },
              "b": "BBB",
              "c": [
                1, 2, 3
              ]
            }
        """.trimIndent()

        @Test
        fun test1() {
            val x = JsonParser().parse(ServerRespond.Successful(test01), TestObjects.ABCD.Contester::class)
            assert(
                x is Response.SuccessfulResponse
            ) { "Result is not successful" }
        }
    }
}