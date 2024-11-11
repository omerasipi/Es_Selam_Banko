plugins {
	java
	id("org.springframework.boot") version "3.3.5"
	id("io.spring.dependency-management") version "1.1.6"
}

group = "ch.asipi-it"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(23)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	// Main dependencies
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("com.prowidesoftware:pw-iso20022:SRU2024-10.2.3")

	// XML Processing
	implementation("jakarta.xml.bind:jakarta.xml.bind-api:4.0.0")
	implementation("org.glassfish.jaxb:jaxb-runtime:4.0.0")

	// Test dependencies
	testImplementation("org.springframework.boot:spring-boot-starter-test") {
		exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
	}
	testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.1")
	testImplementation("org.junit.jupiter:junit-jupiter-engine:5.10.1")
	testImplementation("org.assertj:assertj-core:3.24.2")
	testImplementation("org.mockito:mockito-core:5.3.1")
	testImplementation("org.mockito:mockito-junit-jupiter:5.3.1")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
	useJUnitPlatform()
	testLogging {
		events("passed", "skipped", "failed")
		showStandardStreams = true
		showExceptions = true
		showCauses = true
		showStackTraces = true
	}
}

tasks.withType<JavaCompile> {
	options.encoding = "UTF-8"
}

// Add this bootJar duplicates strategy to handle duplicate JAR entries
tasks.getByName<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
	duplicatesStrategy = org.gradle.api.file.DuplicatesStrategy.EXCLUDE
}
