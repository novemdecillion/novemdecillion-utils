package io.github.novemdecillion.utils.spring.boot

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling

const val SPRING_SCHEDULING_ENABLED_KEY = "spring.scheduling.enable"

@Configuration
@EnableScheduling
@ConditionalOnProperty(value = [SPRING_SCHEDULING_ENABLED_KEY], matchIfMissing = false, havingValue = "true")
class SchedulingConfiguration
