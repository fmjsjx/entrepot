plugins {
	`java-library`
}

group = "com.github.fmjsjx"
version = "1.0.0"

repositories {
    maven {
        url = uri("https://mirrors.cloud.tencent.com/nexus/repository/maven-public/")
    }
    mavenCentral()
}

dependencies {
    // libcommon-bom
    api(platform("com.github.fmjsjx:libcommon-bom:2.6.1"))
    // libnetty-bom
    api(platform("com.github.fmjsjx:libnetty-bom:2.4.2"))
    // myboot-bom
    implementation(platform("com.github.fmjsjx:myboot-bom:1.1.9"))

    constraints {
        implementation("com.lmax:disruptor:3.4.4")
        implementation("org.javassist:javassist:3.28.0-GA")
	}
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

tasks.compileJava {
    options.encoding = "UTF-8"
    options.release.set(17)
}

tasks.test {
    // Use junit platform for unit tests.
    useJUnitPlatform()
    val javaToolchains = project.extensions.getByType<JavaToolchainService>()
    javaLauncher.set(javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(17))
    })
}

tasks.jar {
    // Disable plain Jar for Sprint Boot
    enabled = false
}

tasks.javadoc {
    enabled = false
}
