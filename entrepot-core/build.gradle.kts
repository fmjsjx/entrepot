plugins {
    id("entrepot.parent-conventions")
}

description = "Entrepot Core"

dependencies {
    // spring-boot-dependencies
    api(platform("org.springframework.boot:spring-boot-dependencies:2.6.3"))
    annotationProcessor(platform("org.springframework.boot:spring-boot-dependencies:2.6.3"))
    
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    implementation("org.slf4j:slf4j-api")
    implementation("com.github.fmjsjx:libcommon-util")
    api("io.netty:netty-buffer")

    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    testImplementation("org.apache.logging.log4j:log4j-slf4j-impl")

}
