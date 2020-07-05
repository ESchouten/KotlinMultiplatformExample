plugins {
    kotlin("multiplatform") version "1.4-M2"
}

repositories {
    mavenCentral()
    jcenter()
    maven { url = uri("https://dl.bintray.com/kotlin/kotlin-eap") }
}

kotlin {
    jvm()
    js {
        browser {
            binaries.executable()
        }
    }

    sourceSets {
        val commonMain by getting {
            kotlin.srcDir("kotlin")
            resources.srcDir("resources")
            dependencies {
                implementation(kotlin("stdlib-common"))
            }
        }

        jvm().compilations["main"].defaultSourceSet {
            dependencies {
                implementation(kotlin("stdlib-jdk8"))
            }
        }

        js().compilations["main"].defaultSourceSet {
            dependencies {
                implementation(kotlin("stdlib-js"))
            }
        }
    }
}

tasks.register<Delete>("MPClean") {
    dependsOn(tasks.named("clean"))
    val node = file("node")
    //Delete all old files from folder excluding .npmrc
    node.listFiles()?.forEach {
        if (!it.name.startsWith('.')) {
            delete(it)
        }
    }
}

//Copy files to different folder to allow modifications for the TS fixes
tasks.register<Copy>("MPBuild") {
    dependsOn(tasks.named("MPClean"), tasks.named("build"))
    val root = file("$buildDir/js/packages/${project.name}")
    from(root)
    into("node")

    doLast {
        fixTS()
    }
}

fun fixTS() {
    val packageJson = file("node/package.json")
    val pj = org.jetbrains.kotlin.js.parser.sourcemaps.parseJson(packageJson) as org.jetbrains.kotlin.js.parser.sourcemaps.JsonObject
    pj.properties["name"] = org.jetbrains.kotlin.js.parser.sourcemaps.JsonString("@ESchouten/kotlinmultiplatformexample")
    pj.properties["publishConfig"] = org.jetbrains.kotlin.js.parser.sourcemaps.JsonObject("registry" to org.jetbrains.kotlin.js.parser.sourcemaps.JsonString("https://npm.pkg.github.com/"))
    pj.properties["repository"] = org.jetbrains.kotlin.js.parser.sourcemaps.JsonString("git://github.com/ESchouten/kotlinmultiplatformexample.git")
    (pj.properties["dependencies"] as org.jetbrains.kotlin.js.parser.sourcemaps.JsonObject).apply {
        this.properties.remove("kotlin")
        this.properties.remove("kotlin-source-map-loader")
    }
    packageJson.writeText(pj.toString())

    val commons = file("node/kotlin/KotlinMultiplatformExample.d.ts")
    val content = commons.readLines().toMutableList()
    if (content.size > 0) {
        // drop declare namespace wrapper
        if (content[0].startsWith("declare namespace")) {
            content[0] = ""
            content[content.lastIndex] = ""
        }
        for ((i, _) in content.withIndex()) {
            if (content[i].contains("namespace") && !content[i].contains("export namespace")) {
                content[i] = content[i].replaceFirst("namespace", "export namespace")
            }
            content[i] = content[i].replace("kotlin.Long", "number")
            content[i] = content[i].replace("kotlin.Char", "string")
        }
    }
    commons.writeText(content.joinToString(separator = "\n"))
}

tasks.register<Exec>("MPPublish") {
    dependsOn(tasks.named("MPBuild"))
    workingDir = file("node")
    commandLine = listOf("npm", "publish")
}
