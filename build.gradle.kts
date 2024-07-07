import org.jetbrains.kotlin.gradle.plugin.mpp.pm20.archivesName
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.10"
    id("com.github.johnrengelman.shadow") version "7.0.0"
    id("org.ajoberstar.grgit") version "4.1.0"

    // Check for new versions at https://plugins.gradle.org/plugin/io.papermc.paperweight.userdev
    id("io.papermc.paperweight.userdev") version "1.7.1"
    `maven-publish`
}

val group_name: String by project
group = group_name
version = project.properties["version"].toString()
val id: String by project

val plugin_name: String by project
val plugin_main_class_name: String by project
val plugin_author: String by project
val include_commit_hash: String by project

val ktgui_version: String by project
val paper_version: String by project

repositories {
    mavenCentral()
    maven("https://maven.pvphub.me/releases")
}

dependencies {
    testImplementation(kotlin("test"))
    compileOnly(kotlin("stdlib"))

    compileOnly("com.mattmx:ktgui:${ktgui_version}")
    paperweight.paperDevBundle(paper_version)
}

tasks {
    base {
        archivesName = id
    }

    withType<ProcessResources> {
        val props = mapOf(
            "name" to plugin_name,
            "main" to "${group_name}.${id}.${plugin_main_class_name}",
            "author" to plugin_author,
            "version" to if (include_commit_hash.toBoolean()) "${rootProject.version}-commit-${grgit.head().abbreviatedId}" else rootProject.version.toString()
        )
        inputs.properties(props)
        filteringCharset = "UTF-8"
        filesMatching("plugin.yml") {
            expand(props)
        }
    }

    shadowJar {
        mergeServiceFiles()
    }

    test {
        useJUnitPlatform()
    }

    assemble {
        dependsOn("reobfJar")
    }

    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }
}

java {
    withJavadocJar()
    withSourcesJar()
}

sourceSets["main"].resources.srcDir("src/resources/")

publishing {
    repositories {
        maven {
            name = "pvphub-releases"
            url = uri("https://maven.pvphub.me/releases")
            credentials {
                username = System.getenv("PVPHUB_MAVEN_USERNAME")
                password = System.getenv("PVPHUB_MAVEN_SECRET")
            }
        }
    }
    publications {
        create<MavenPublication>(id) {
            from(components["java"])
            groupId = group.toString()
            artifactId = id
            version = rootProject.version.toString()
        }
    }
}