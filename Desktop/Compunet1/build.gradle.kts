plugins {
    id("java")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

tasks.register<Jar>("serverJar") {
    manifest {
        attributes["Main-Class"] = "org.compunet1.server.ChatServer"
    }
    archiveBaseName.set("ChatServer")
    from(sourceSets.main.get().output)
}

tasks.register<Jar>("clientJar") {
    manifest {
        attributes["Main-Class"] = "org.compunet1.client.ChatClient"
    }
    archiveBaseName.set("ChatClient")
    from(sourceSets.main.get().output)
}
