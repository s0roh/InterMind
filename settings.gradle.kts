pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "InterMind"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
include(":app")
include(":core:data")
include(":core:designsystem")
include(":core:domain")
include(":core:navigation")
include(":core:ui")
include(":feature:addeditcard:api")
include(":feature:addeditcard:impl")
include(":feature:addeditdeck:api")
include(":feature:addeditdeck:impl")
include(":feature:auth:api")
include(":feature:auth:impl")
include(":feature:deckdetails:api")
include(":feature:deckdetails:impl")
include(":feature:decks:api")
include(":feature:decks:impl")
include(":feature:explore:api")
include(":feature:explore:impl")
include(":feature:history:api")
include(":feature:history:impl")
include(":feature:profile:api")
include(":feature:profile:impl")
include(":feature:training:api")
include(":feature:training:impl")
include(":feature:trainingmodesettings:api")
include(":feature:trainingmodesettings:impl")

