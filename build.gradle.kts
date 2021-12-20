import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.plugin.SpringBootPlugin

plugins {
  java
  idea
  id("org.springframework.boot") version "2.6.1" apply false
  id("io.spring.dependency-management") version "1.0.11.RELEASE"
  kotlin("jvm") version "1.6.0"
  kotlin("plugin.spring") version "1.6.0"
}

allprojects {
  repositories {
    mavenCentral()
  }
}

subprojects {
  apply {
    plugin("io.spring.dependency-management")
    plugin("org.jetbrains.kotlin.jvm")
    plugin("org.jetbrains.kotlin.plugin.spring")
  }

  group = "io.github.novemdecillion.utils"
  version = "1.0.0-SNAPSHOT"
  java.sourceCompatibility = JavaVersion.VERSION_11

  dependencyManagement {
    imports {
      mavenBom(SpringBootPlugin.BOM_COORDINATES)
    }
  }

  dependencies {
    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
  }

  tasks.withType<KotlinCompile> {
    kotlinOptions {
      freeCompilerArgs = listOf("-Xjsr305=strict")
      jvmTarget = "11"
    }
  }

  tasks.withType<Test> {
    useJUnitPlatform()
  }
}

idea {
  module {
    isDownloadSources = true
  }
}
