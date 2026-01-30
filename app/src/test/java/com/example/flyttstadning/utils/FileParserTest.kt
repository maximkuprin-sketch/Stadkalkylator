package com.example.flyttstadning.utils

import com.example.flyttstadning.data.database.PriceBand
import org.junit.Assert.*
import org.junit.Test
import java.io.ByteArrayInputStream

class FileParserTest {

    @Test
    fun parseCsv_validContent_returnsBands() {
        val content = "30-35,2500\n0-30,1500"
        val inputStream = ByteArrayInputStream(content.toByteArray())
        val bands = FileParser.parseInputStream(inputStream, "test.csv")

        assertEquals(2, bands.size)
        // Check sorting
        assertEquals(0, bands[0].rangeStart)
        assertEquals(30, bands[0].rangeEnd)
        assertEquals(1500, bands[0].price)
        
        assertEquals(30, bands[1].rangeStart)
        assertEquals(35, bands[1].rangeEnd)
        assertEquals(2500, bands[1].price)
    }

    @Test(expected = IllegalArgumentException::class)
    fun parseCsv_overlap_throwsException() {
        // 0-30 and 25-35 (overlap 25-30)
        val content = "0-30,1500\n25-35,2500"
        val inputStream = ByteArrayInputStream(content.toByteArray())
        FileParser.parseInputStream(inputStream, "test.csv")
    }

    @Test
    fun parseCsv_noOverlapHeader_valid() {
        // 0-30, 30-35. (30 end vs 30 start is logic valid? "30-35" = (30,35])
        // Code check logic: current.end > next.start
        // 30 > 30 is false. So valid.
        val content = "0-30,1500\n30-35,2500"
        val inputStream = ByteArrayInputStream(content.toByteArray())
        val bands = FileParser.parseInputStream(inputStream, "test.csv")
        assertEquals(2, bands.size)
    }

    @Test(expected = IllegalArgumentException::class)
    fun parseCsv_negativePrice_throwsException() {
        val content = "0-30,-100"
        val inputStream = ByteArrayInputStream(content.toByteArray())
        FileParser.parseInputStream(inputStream, "test.csv")
    }
}
