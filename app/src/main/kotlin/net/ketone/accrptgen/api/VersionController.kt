package net.ketone.accrptgen.api

import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@RestController
class VersionController {

    @Value("\${build.version}")
    lateinit var buildVersion: String

    @Value("\${build.timestamp}")
    lateinit var buildTimestamp: String

    @GetMapping("/api/version")
    fun getVersion(): Map<String, String> {
        val df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        val localDateTime = LocalDateTime.parse(buildTimestamp, df)
        val timestamp = df.format(localDateTime.toInstant(ZoneOffset.UTC).atOffset(ZoneOffset.of("+8")))
        return hashMapOf("version" to buildVersion, "timestamp" to timestamp)
    }


}