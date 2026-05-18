rootProject.name = "monk-asura-plugin"

plugins {
    id("dev.scaffoldit") version "0.2.+"
}

hytale {
    usePatchline("release")
    useVersion("latest")

    manifest {
        Group = "MonkAsura"
        Name = "MonkAsura"
        Main = "com.monk.asura.MonkAsuraPlugin"
    }
}
