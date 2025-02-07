import com.adarshr.gradle.testlogger.theme.ThemeType

plugins {
	application
	id("org.springframework.boot") version "3.3.5-SNAPSHOT"
	id("io.spring.dependency-management") version "1.1.6"
	id("checkstyle")
	jacoco
	id("com.github.johnrengelman.shadow") version "8.1.1"
	id("com.adarshr.test-logger") version "4.0.0"
	id("io.freefair.lombok") version "8.4"
	id("io.sentry.jvm.gradle") version "4.12.0"
}

group = "hexlet.code"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
	maven { url = uri("https://repo.spring.io/snapshot") }
}

dependencies {
	// Tests
	testImplementation(platform("org.junit:junit-bom:5.10.0"))
	testImplementation("org.junit.jupiter:junit-jupiter")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	testImplementation("org.assertj:assertj-core:3.26.3")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	testImplementation("org.springframework.boot:spring-boot-starter-test")

	// Utils
	implementation("org.apache.commons:commons-lang3:3.14.0")
	implementation("org.apache.commons:commons-collections4:4.4")
	compileOnly("org.projectlombok:lombok")
	annotationProcessor("org.projectlombok:lombok")

	// DB
	implementation("org.postgresql:postgresql:42.7.3")
	runtimeOnly("com.h2database:h2")

	// Spring
	implementation("org.springframework.boot:spring-boot-starter")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-devtools")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("jakarta.validation:jakarta.validation-api")
	implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0")
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-api:2.6.0")

	// Logger
	implementation("io.sentry:sentry-spring-boot-starter-jakarta:7.15.0")
	implementation("io.sentry:sentry-logback:7.15.0")
	implementation("io.sentry:sentry-log4j2:7.15.0")

	// Mapper
	implementation("org.mapstruct:mapstruct:1.5.5.Final")
	annotationProcessor("org.mapstruct:mapstruct-processor:1.5.5.Final")
	implementation("org.openapitools:jackson-databind-nullable:0.2.6")

	// Tests
	implementation("net.datafaker:datafaker:2.0.1")
	implementation("org.instancio:instancio-junit:3.3.0")
	testImplementation("net.javacrumbs.json-unit:json-unit-assertj:3.2.2")
	testImplementation("org.springframework.security:spring-security-test")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

testlogger {
	showFullStackTraces = true
	theme = ThemeType.MOCHA
}

tasks.jacocoTestReport {
	dependsOn(tasks.withType<Test>()) // tests are required to run before generating the report
	reports {
		xml.required = true
	}
}

application {
	mainClass.set("hexlet.code.AppApplication")
}

checkstyle {
	toolVersion = "10.18.2"
	configFile = file("config/checkstyle/checkstyle.xml")
	isIgnoreFailures = false
	isShowViolations = true
}

tasks.checkstyleMain {
	dependsOn(tasks.compileJava)
	outputs.upToDateWhen { false }
	reports {
		xml.required.set(true)
		html.required.set(true)
	}
}

tasks.checkstyleTest {
	dependsOn(tasks.compileTestJava)
	outputs.upToDateWhen { false }
	reports {
		xml.required.set(true)
		html.required.set(true)
	}
}

if(System.getenv("SPRING_PROFILES_ACTIVE") != null
	&& System.getenv("SPRING_PROFILES_ACTIVE").equals("prod")) {
	sentry {
		includeSourceContext = true

		org = "brocodex"
		projectName = "java-spring-boot"
		authToken = System.getenv("SENTRY_AUTH_TOKEN")
	}
}