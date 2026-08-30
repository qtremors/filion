plugins {
    `kotlin-dsl`
}

dependencies {
    testImplementation(gradleTestKit())
    testImplementation(kotlin("test"))
}

gradlePlugin {
    plugins {
        register("filionAndroidApplicationConventions") {
            id = "filion.android.application.conventions"
            implementationClass = "dev.qtremors.filion.buildlogic.FilionAndroidApplicationConventionsPlugin"
        }
    }
}
