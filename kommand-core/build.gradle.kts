import net.md_5.specialsource.provider.JointProvider
import net.md_5.specialsource.Jar as SpecialJar
import net.md_5.specialsource.provider.JarProvider
import net.md_5.specialsource.JarMapping
import net.md_5.specialsource.JarRemapper

dependencies {
    implementation(project(":kommand-api"))
}

subprojects {
    configurations {
        create("mojangMapping")
        create("spigotMapping")
    }

    repositories {
        mavenLocal()
        maven("https://libraries.minecraft.net")
    }

    dependencies {
        implementation(project(":kommand-api"))
        implementation(requireNotNull(parent))
    }
}

tasks {
    create<Jar>("paperJar") {
        listOf(project(":kommand-api")) + project(":kommand-core").let { listOf(it) + it.subprojects }.forEach {
            from(it.sourceSets["main"].output)
        }

        doLast {
            fun remap(jarFile: File, outputFile: File, mappingFile: File, reversed: Boolean = false) {
                val inputJar = SpecialJar.init(jarFile)

                val mapping = JarMapping()
                mapping.loadMappings(mappingFile.canonicalPath, reversed, false, null, null)

                val provider = JointProvider()
                provider.add(JarProvider(inputJar))
                mapping.setFallbackInheritanceProvider(provider)

                val mapper = JarRemapper(mapping)
                mapper.remapJar(inputJar, outputFile)
                inputJar.close()
            }

            val archiveFile = archiveFile.get().asFile

            val obfOutput = File(archiveFile.parentFile, "remapped-obf.jar")
            val spigotOutput = File(archiveFile.parentFile, "remapped-spigot.jar")

            subprojects.forEach { nmsProject ->
                val configurations = nmsProject.configurations
                val mojangMapping = configurations.named("mojangMapping").get().firstOrNull()
                val spigotMapping = configurations.named("spigotMapping").get().firstOrNull()

                if (mojangMapping != null && spigotMapping != null) {
                    remap(archiveFile, obfOutput, mojangMapping, true)
                    remap(obfOutput, spigotOutput, spigotMapping)

                    spigotOutput.copyTo(archiveFile, true)
                    obfOutput.delete()
                    spigotOutput.delete()
                    println("Successfully remap jar (${nmsProject.name})")
                } else {
                    logger.warn("Mojang and Spigot mapping should be specified for ${
                        path.drop(1).takeWhile { it != ':' }
                    }.")
                }
            }
        }
    }
}