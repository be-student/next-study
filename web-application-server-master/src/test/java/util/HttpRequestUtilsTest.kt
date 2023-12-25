package util

import org.hamcrest.CoreMatchers
import org.junit.Assert
import org.junit.Test
import util.HttpRequestUtils.getKeyValue
import util.HttpRequestUtils.parseCookies
import util.HttpRequestUtils.parseHeader
import util.HttpRequestUtils.parseQueryString

class HttpRequestUtilsTest {
    @Test
    fun parseQueryString() {
        var queryString = "userId=javajigi"
        var parameters: MutableMap<String, String?>? = parseQueryString(queryString)
        Assert.assertThat(parameters!!["userId"], CoreMatchers.`is`("javajigi"))
        Assert.assertThat(parameters["password"], CoreMatchers.`is`(CoreMatchers.nullValue()))
        queryString = "userId=javajigi&password=password2"
        parameters = parseQueryString(queryString)
        Assert.assertThat(parameters!!["userId"], CoreMatchers.`is`("javajigi"))
        Assert.assertThat(parameters["password"], CoreMatchers.`is`("password2"))
    }

    @Test
    fun parseQueryString_null() {
        var parameters: Map<String, String?>? = parseQueryString(null)
        Assert.assertThat(parameters!!.isEmpty(), CoreMatchers.`is`(true))
        parameters = parseQueryString("")
        Assert.assertThat(parameters!!.isEmpty(), CoreMatchers.`is`(true))
        parameters = parseQueryString(" ")
        Assert.assertThat(parameters!!.isEmpty(), CoreMatchers.`is`(true))
    }

    @Test
    fun parseQueryString_invalid() {
        val queryString = "userId=javajigi&password"
        val parameters: Map<String, String?>? = parseQueryString(queryString)
        Assert.assertThat(parameters!!["userId"], CoreMatchers.`is`("javajigi"))
        Assert.assertThat(parameters["password"], CoreMatchers.`is`(CoreMatchers.nullValue()))
    }

    @Test
    fun parseCookies() {
        val cookies = "logined=true; JSessionId=1234"
        val parameters: Map<String, String?>? = parseCookies(cookies)
        Assert.assertThat(parameters!!["logined"], CoreMatchers.`is`("true"))
        Assert.assertThat(parameters["JSessionId"], CoreMatchers.`is`("1234"))
        Assert.assertThat(parameters["session"], CoreMatchers.`is`(CoreMatchers.nullValue()))
    }

    @Test
    @Throws(Exception::class)
    fun getKeyValue() {
        val pair = getKeyValue("userId=javajigi", "=")
        Assert.assertThat(pair, CoreMatchers.`is`(HttpRequestUtils.Pair("userId", "javajigi")))
    }

    @Test
    @Throws(Exception::class)
    fun getKeyValue_invalid() {
        val pair = getKeyValue("userId", "=")
        Assert.assertThat(pair, CoreMatchers.`is`(CoreMatchers.nullValue()))
    }

    @Test
    @Throws(Exception::class)
    fun parseHeader() {
        val header = "Content-Length: 59"
        val pair = parseHeader(header)
        Assert.assertThat(pair, CoreMatchers.`is`(HttpRequestUtils.Pair("Content-Length", "59")))
    }
}
