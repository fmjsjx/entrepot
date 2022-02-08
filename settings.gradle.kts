pluginManagement {
    repositories {
        maven(url = "https://maven.aliyun.com/repository/gradle-plugin")
    }
}

rootProject.name = "entrepot"
include(":entrepot-core")
include(":entrepot-server")
