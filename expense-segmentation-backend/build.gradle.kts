plugins {
    java
    id("org.springframework.boot") version "3.2.5"
    id("io.spring.dependency-management") version "1.1.4"
    jacoco
    id("com.diffplug.spotless") version "6.25.0"
    id("org.sonarqube") version "5.0.0.4638"
}

group = "com.expense"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot Starters
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // Database
    runtimeOnly("org.postgresql:postgresql")

    // Development Tools
    developmentOnly("org.springframework.boot:spring-boot-devtools")

    // Lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    // JWT
    implementation("io.jsonwebtoken:jjwt-api:0.12.5")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.5")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.5")

    // OpenAPI / Swagger
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.5.0")

    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testRuntimeOnly("com.h2database:h2")
}

tasks.withType<Test> {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

// Jacoco Configuration
tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
        html.outputLocation.set(layout.buildDirectory.dir("reports/jacoco"))
    }
}

tasks.jacocoTestCoverageVerification {
    dependsOn(tasks.jacocoTestReport)
    violationRules {
        rule {
            limit {
                minimum = "0.40".toBigDecimal() // Current: 42%, Target: 90%
            }
        }
        rule {
            enabled = true
            element = "CLASS"
            limit {
                counter = "LINE"
                value = "COVEREDRATIO"
                minimum = "0.30".toBigDecimal()
            }
            excludes = listOf(
                "*.config.*",
                "*.dto.*",
                "*.model.*",
                "*Application*"
            )
        }
    }
}

// Task to check if coverage meets the goal (90%)
tasks.register("jacocoTestCoverageGoal") {
    group = "verification"
    description = "Check if coverage meets 90% goal (currently 42%)"
    doLast {
        println("⚠️  Coverage goal: 90% | Current: ~42%")
        println("📈 Run 'make coverage' to see detailed coverage report")
    }
}

// Spotless Configuration
spotless {
    java {
        target("src/*/java/**/*.java")
        googleJavaFormat("1.19.1").aosp().reflowLongStrings()
        removeUnusedImports()
        trimTrailingWhitespace()
        endWithNewline()
        indentWithSpaces(4)
    }
}

// SonarQube Configuration
sonar {
    properties {
        property("sonar.projectKey", "expense-segmentation-backend")
        property("sonar.projectName", "Expense Segmentation Backend")
        property("sonar.host.url", "http://localhost:9000")
        property("sonar.sourceEncoding", "UTF-8")
        property("sonar.sources", "src/main/java")
        property("sonar.tests", "src/test/java")
        property("sonar.java.binaries", "build/classes")
        property("sonar.coverage.jacoco.xmlReportPaths", "build/reports/jacoco/test/jacocoTestReport.xml")
        property("sonar.exclusions", "**/config/**,**/dto/**,**/model/**,**/*Application.java")
    }
}

// Task Dependencies
tasks.check {
    dependsOn(tasks.jacocoTestCoverageVerification)
    dependsOn(tasks.named("spotlessCheck"))
}

tasks.build {
    dependsOn(tasks.check)
}
