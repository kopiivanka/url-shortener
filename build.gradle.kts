import org.jooq.meta.jaxb.*

val jooqVersion = "3.20.8"
configure<io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension> {
    imports { mavenBom("org.jooq:jooq-bom:$jooqVersion") }
}
plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    kotlin("plugin.jpa") version "1.9.25"
    id("org.springframework.boot") version "3.2.4"
    id("io.spring.dependency-management") version "1.1.4"
    id("org.jooq.jooq-codegen-gradle") version "3.20.8"
}

group = "org.kopytsia"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("io.jsonwebtoken:jjwt-api:0.11.5")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.flywaydb:flyway-core:10.15.0")
    implementation("org.flywaydb:flyway-database-postgresql:10.15.0")
    implementation("org.springframework.boot:spring-boot-starter-aop")
    implementation("org.springframework.boot:spring-boot-starter-jooq")

    runtimeOnly("org.postgresql:postgresql")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5")
    jooqCodegen("org.postgresql:postgresql")

    developmentOnly("org.springframework.boot:spring-boot-devtools")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("io.mockk:mockk:1.13.11")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

jooq {
    configuration {
        jdbc {
            driver = "org.postgresql.Driver"
            url = "jdbc:postgresql://localhost:15432/url_shortener"
            user = "postgres"
            password = "postgres"
        }
        generator {
            name = "org.jooq.codegen.KotlinGenerator"
            database {
                inputSchema = "public"
                includes = "^(click_event|urls|users)$"
                excludes = "(?i)flyway_schema_history|pg_.*|information_schema.*"

                forcedTypes = listOf(
                    ForcedType().apply {
                        name = "JSONB"
                        includeTypes = "JSONB"
                    }
                )
            }
            generate {
                kotlinNotNullRecordAttributes = true
                records = true
                pojos = false
                daos = false
                routines = false
                sequences = false
                udts = false
            }
            target {
                packageName = "org.kopytsia.jooq"
                directory = "build/generated-src/jooq/main"
            }
        }
    }
}
sourceSets {
    val main by getting {
        java.srcDir("build/generated-src/jooq/main")
    }
}

tasks.named("compileKotlin").configure {
    dependsOn("jooqCodegen")
}