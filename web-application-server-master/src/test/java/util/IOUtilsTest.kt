package util

import org.junit.Test
import org.slf4j.LoggerFactory
import util.IOUtils.readData
import java.io.BufferedReader
import java.io.StringReader

class IOUtilsTest {
    private val logger = LoggerFactory.getLogger(IOUtilsTest::class.java)

    @Test
    fun readData() {
        val data = "abcd123"
        val sr = StringReader(data)
        val br = BufferedReader(sr)
        logger.debug("parse body : {}", readData(br, data.length))
    }
}
