tasks.named<Jar>("jar") {
    finalizedBy("deployMod")
}

tasks.register<Copy>("deployMod") {
    dependsOn(tasks.jar)
    from(tasks.jar)
    into(rootProject.projectDir.parentFile.resolve("Server/mods"))
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}
