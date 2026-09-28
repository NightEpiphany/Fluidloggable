import java.net.URI

plugins {
	id("net.fabricmc.fabric-loom")
	`maven-publish`
}

base {
	archivesName = providers.gradleProperty("archives_base_name").map { "$it-fabric" }
}


// Keep the mod version unchanged while using a consistent distributable filename.
tasks.withType<Jar>().configureEach {
    val minecraftVersion = providers.gradleProperty("minecraft_version").get()
    val modVersion = providers.gradleProperty("mod_version").get().removeSuffix("-mc$minecraftVersion")
    archiveVersion = "${modVersion}_mc$minecraftVersion"
}
loom {
	accessWidenerPath = file("src/main/resources/fluidloggable.accessWidener")
}

version = providers.gradleProperty("mod_version").get()
group = providers.gradleProperty("maven_group").get()

repositories {
	maven {
		name = "Modrinth"
		url = URI("https://api.modrinth.com/maven")
	}
	maven {
		url = URI("https://cursemaven.com")
	}
	maven {
		name = "Fuzs Mod Resources"
		url = URI("https://raw.githubusercontent.com/Fuzss/modresources/main/maven/")
	}
}

dependencies {
	// To change the versions see the gradle.properties file
	minecraft("com.mojang:minecraft:${providers.gradleProperty("minecraft_version").get()}")
	implementation("net.fabricmc:fabric-loader:${providers.gradleProperty("loader_version").get()}")
	implementation("maven.modrinth:modmenu:${providers.gradleProperty("modmenu_version").get()}")
	implementation("maven.modrinth:cloth-config:${providers.gradleProperty("cloth_config_version").get()}")
	// Sodium
	implementation("maven.modrinth:AANobbMI:v4PSXean")
	compileOnly("maven.modrinth:ox3rDp1B:4pyW4Uba")
	compileOnly("maven.modrinth:create-fly:26.2-rc-2-6.0.9-1")
	compileOnly("maven.modrinth:copycats+-createfly-port:3.0.7-createfly+mc.26.2")
	// Fabric API. This is technically optional, but you probably want it anyway.
	implementation("net.fabricmc.fabric-api:fabric-api:${providers.gradleProperty("fabric_api_version").get()}")
	compileOnly("maven.modrinth:comforts:${providers.gradleProperty("comfort_version").get()}")
	implementation("maven.modrinth:farmers-delight-refabricated:${providers.gradleProperty("fdrf_version").get()}") {
		exclude(group = "net.fabricmc")
	}
	testImplementation(platform("org.junit:junit-bom:5.13.4"))
	testImplementation("org.junit.jupiter:junit-jupiter")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	// Kaleidoscope series
	implementation ("maven.modrinth:kaleidoscope-cookery-refabricated:${providers.gradleProperty("kaleidoscope_cookery_version").get()}-fabric+mc${providers.gradleProperty("minecraft_version").get()}")
	implementation ("maven.modrinth:kaleidoscope-tavern-refabricated:${providers.gradleProperty("kaleidoscope_tavern_version").get()}-fabric+mc${providers.gradleProperty("minecraft_version").get()}")
	implementation ("maven.modrinth:kaleidoscope-nether-refabricated:${providers.gradleProperty("kaleidoscope_nether_version").get()}-fabric+mc${providers.gradleProperty("minecraft_version").get()}")
	implementation ("maven.modrinth:kaleidoscope-end-refabricated:${providers.gradleProperty("kaleidoscope_end_version").get()}-fabric+mc${providers.gradleProperty("minecraft_version").get()}")
	implementation("curse.maven:kaleidoscopechinesefood-refabricated-1674961:8990867")
	implementation("curse.maven:kaleidoscope-world-liquor-refabricated-1693193:8958198")
	implementation("fuzs.forgeconfigapiport:forgeconfigapiport-fabric:${providers.gradleProperty("forge_config_api_version").get()}")
}

tasks.test {
	useJUnitPlatform()
}

fabricApi {
	configureTests {
		createSourceSet.set(true)
		modId.set("fluidloggable-gametest")
		enableClientGameTests.set(true)
	}
}

tasks.processResources {
	val version = version
	inputs.property("version", version)

	filesMatching("fabric.mod.json") {
		expand("version" to version)
	}
}

tasks.withType<JavaCompile>().configureEach {
	options.release = 25
}

java {
	// Loom will automatically attach sourcesJar to a RemapSourcesJar task and to the "build" task
	// if it is present.
	// If you remove this line, sources will not be generated.
	withSourcesJar()

	sourceCompatibility = JavaVersion.VERSION_25
	targetCompatibility = JavaVersion.VERSION_25
}

tasks.jar {
	val projectName = project.name
	inputs.property("projectName", projectName)

	from("LICENSE") {
		rename { "${it}_$projectName" }
	}
}

// configure the maven publication
publishing {
	publications {
		register<MavenPublication>("mavenJava") {
			from(components["java"])
		}
	}

	// See https://docs.gradle.org/current/userguide/publishing_maven.html for information on how to set up publishing.
	repositories {
		// Add repositories to publish to here.
		// Notice: This block does NOT have the same function as the block in the top level.
		// The repositories here will be used for publishing your artifact, not for
		// retrieving dependencies.
	}
}
