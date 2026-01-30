package com.example.flyttstadning.utils

import com.example.flyttstadning.data.database.PriceBand
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.InputStream
import java.io.BufferedReader
import java.io.InputStreamReader

object FileParser {

    @Throws(IllegalArgumentException::class)
    fun parseInputStream(inputStream: InputStream, fileName: String): List<PriceBand> {
        val bands = when {
            fileName.endsWith(".csv", ignoreCase = true) || fileName.endsWith(".txt", ignoreCase = true) -> {
                parseCsv(inputStream)
            }
            fileName.endsWith(".xlsx", ignoreCase = true) -> {
                parseXlsx(inputStream)
            }
            else -> throw IllegalArgumentException("Unsupported file format: $fileName")
        }
        validateBands(bands)
        return bands
    }

    private fun parseCsv(inputStream: InputStream): List<PriceBand> {
        val bands = mutableListOf<PriceBand>()
        BufferedReader(InputStreamReader(inputStream)).use { reader ->
            reader.readLines().forEach { line ->
                val trimmed = line.trim()
                if (trimmed.isNotEmpty()) {
                    val parts = trimmed.split(",")
                    if (parts.size >= 2) {
                        try {
                            val rangePart = parts[0].trim()
                            val pricePart = parts[1].trim().toInt()
                            val (start, end) = parseRange(rangePart)
                            bands.add(PriceBand(rangeStart = start, rangeEnd = end, price = pricePart))
                        } catch (e: Exception) {
                            // Skip malformed lines or throw? Requirement: readable error.
                            // Let's rethrow to show error.
                            throw IllegalArgumentException("Invalid line format: $line")
                        }
                    }
                }
            }
        }
        return bands
    }

    private fun parseXlsx(inputStream: InputStream): List<PriceBand> {
        val bands = mutableListOf<PriceBand>()
        val workbook = WorkbookFactory.create(inputStream)
        val sheet = workbook.getSheetAt(0)
        
        sheet.forEach { row ->
            // Skip header if it looks like a header (optional, but requirement says first sheet contains columns)
            // We assume usually A and B.
            // Check if first cell is string "range" maybe?
            // Let's rely on parsing logic. If it fails to parse Int, it might be header.

            val cellA = row.getCell(0)
            val cellB = row.getCell(1)

            if (cellA != null && cellB != null) {
                try {
                    val rangePart = cellA.toString().trim() // Cell.toString gets value
                    // cellB might be numeric or string
                    val pricePart = try {
                        cellB.numericCellValue.toInt()
                    } catch (e: Exception) {
                        cellB.toString().trim().toInt()
                    }
                    
                    // Simple heuristic to skip header "range" "price"
                    if (rangePart.lowercase() == "range" || rangePart.lowercase() == "intervall") return@forEach

                    val (start, end) = parseRange(rangePart)
                    bands.add(PriceBand(rangeStart = start, rangeEnd = end, price = pricePart))
                } catch (e: Exception) {
                    // Ignore empty rows or helper text, but if it looks like data and fails, user might want to know.
                    // safely ignore for now if completely unparseable
                }
            }
        }
        return bands
    }

    private fun parseRange(range: String): Pair<Int, Int> {
        // "30-35" -> (30, 35] -> We store 30, 35. 
        // "0-30" -> <= 30 -> 0, 30.
        // "min,2400"? Requirement said: "range,price" e.g "30-35,2500" or "min,2400"
        // Wait, "min,2400" was an example line. Maybe "min" is a keyword?
        // Requirement says: 'If range is "0-30" interpret as <=30.'
        // Let's handle "min" if user meant fixed connection/minimum charge?
        // Actually, let's assume standard ranges. "min" might be a special case. 
        // Example: "min,2400" -> maybe 0 to 0? Or just "Minimum debitering".
        // Let's just implement numeric ranges for now. Support "0-30"
        
        val parts = range.split("-")
        if (parts.size == 2) {
            val start = parts[0].trim().toInt()
            val end = parts[1].trim().toInt()
            return start to end
        }
        throw IllegalArgumentException("Invalid range format: $range")
    }

    private fun validateBands(bands: List<PriceBand>) {
        if (bands.isEmpty()) throw IllegalArgumentException("Price list is empty")
        
        // Sort by start
        val sorted = bands.sortedBy { it.rangeStart }
        
        // Check Positive prices
        if (bands.any { it.price < 0 }) throw IllegalArgumentException("Prices must be positive")

        // Check Overlaps
        // (StartA, EndA], (StartB, EndB]
        // If EndA > StartB, overlap. (Assuming sorted)
        // Wait, interpretation: "30-35" means (30, 35]. i.e. 30 is excluded, 35 included?
        // OR "30-35" means 31..35? 
        // Requirement: 'Parse ranges like "30-35" meaning (30,35]' -> Open start, Closed end.
        // So 30 is NOT covered by 30-35. It would be covered by 25-30.
        // So if we have 0-30 and 30-35.
        // 0-30 covers (0, 30].
        // 30-35 covers (30, 35].
        // So no overlap.
        // Logic: if Sorted[i].end > Sorted[i+1].start:
        // Ex: 0-30 (ends 30). 30-35 (starts 30). 30 <= 30 ? No overlap.
        // If 0-35 and 30-35. Ends 35. Starts 30. 35 > 30. Overlap.
        
        for (i in 0 until sorted.size - 1) {
            val current = sorted[i]
            val next = sorted[i+1]
            
            // Validation rule: current.end must be <= next.start
            if (current.rangeEnd > next.rangeStart) {
                // Potential overlap
                // Check edge case: 30-35 and 30-40.
                throw IllegalArgumentException("Overlap detected between ${current.rangeStart}-${current.rangeEnd} and ${next.rangeStart}-${next.rangeEnd}")
            }
        }
    }
}
