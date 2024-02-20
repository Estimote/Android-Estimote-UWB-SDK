pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://estimote.jfrog.io/artifactory/android-proximity-sdk/") }
    }
}

rootProject.name = "UWB Demo"
include(":app")
