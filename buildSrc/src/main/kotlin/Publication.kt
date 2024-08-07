import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPom
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.the
import org.gradle.plugins.signing.SigningExtension

fun Project.configurePublication() {
    apply(plugin = "maven-publish")

    the<PublishingExtension>().apply {
        publications.create<MavenPublication>("release") {
            afterEvaluate {
                if (isAndroid) {
                    from(components.getByName("release"))
                } else {
                    from(components.getByName("java"))
                }
            }

            if (isAndroid) {
                println("Android project detected. Using android- prefix for artifactId.")
                artifactId = "android-${project.name}"
            }

            createPom {
                name.set(project.name)
                description.set(project.description)
            }
        }

        repositories {
            maven {
                if (isRelease) {
                    setUrl("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
                } else {
                    println("Using SNAPSHOT repository")
                    setUrl("https://oss.sonatype.org/content/repositories/snapshots/")
                }

                credentials {
                    username = Publication.Repository.username
                    password = Publication.Repository.password
                }
            }
        }
    }

    if (Publication.Signing.canSign) {
        println("Signing publication")
        apply(plugin = "signing")

        the<SigningExtension>().apply {
            sign(the<PublishingExtension>().publications)
        }
    } else {
        println("No signing key found. Publication will not be signed.")
    }
}

val Project.isRelease: Boolean
    get() = version.toString().endsWith("-SNAPSHOT").not()

val Project.isAndroid: Boolean
    get() = project.hasProperty("android")

fun MavenPublication.createPom(
    configure: MavenPom.() -> Unit = {}
): Unit =
    pom {
        url.set(Publication.Pom.URL)
        packaging = Publication.Pom.PACKAGING

        scm {
            connection.set(Publication.Pom.Scm.CONNECTION)
            developerConnection.set(Publication.Pom.Scm.DEVELOPER_CONNECTION)
            url.set(Publication.Pom.Scm.URL)
        }

        developers {
            developer {
                id.set(Publication.Pom.Developer.ID)
                name.set(Publication.Pom.Developer.NAME)
            }
        }

        licenses {
            license {
                name.set(Publication.Pom.License.NAME)
                url.set(Publication.Pom.License.URL)
                distribution.set(Publication.Pom.License.DISTRIBUTION)
            }
        }
        configure()
    }
