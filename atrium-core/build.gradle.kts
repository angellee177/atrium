description = "Core module of Atrium, containing all contracts/interfaces and default implementations"

val kboxVersion: String by rootProject.extra

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                apiWithExclude("ch.tutteli.kbox:kbox:$kboxVersion")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(prefixedProject("api-infix-en_GB"))
                implementation(prefixedProject("specs"))
            }
        }
    }
}
