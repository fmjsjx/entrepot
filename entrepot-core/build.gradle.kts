plugins {
    id("entrepot.parent-conventions")
    id("org.springframework.boot") version "2.6.3" apply false
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
}

description = "Entrepot Core"

dependencyManagement {
    imports {
        mavenBom(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
    }
}

dependencies {
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    implementation("org.slf4j:slf4j-api")
    implementation("com.github.fmjsjx:libcommon-util")
    api("io.netty:netty-buffer")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
