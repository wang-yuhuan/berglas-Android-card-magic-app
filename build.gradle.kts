plugins {
    id("com.android.application") version "8.9.2" apply false
    id("org.jetbrains.kotlin.android") version "2.1.20" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.20" apply false
}

// Windows Java argument files and aapt2 disagree about Unicode paths on some systems.
// Keep generated files in an ASCII path; the source project stays where the user put it.
if (System.getProperty("os.name").startsWith("Windows") && rootDir.path.any { it.code > 127 }) {
    val projectKey = Integer.toHexString(rootDir.absolutePath.hashCode())
    val generatedRoot = java.io.File(System.getProperty("java.io.tmpdir"), "atelier-cards-build/$projectKey")
    allprojects { layout.buildDirectory.set(java.io.File(generatedRoot, name)) }
}
