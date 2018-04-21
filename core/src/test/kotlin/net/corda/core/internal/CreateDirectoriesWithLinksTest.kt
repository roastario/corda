package net.corda.core.internal

import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert
import org.junit.Test
import java.io.File
import java.nio.file.FileAlreadyExistsException
import java.nio.file.Files

class CreateDirectoriesWithLinksTest {


    @Test
    fun `should allow create directories with target of concrete folder`() {
        val parentDir = Files.createTempDirectory("cdTest")
        val child1 = File(parentDir.toFile(), "child1")
        val child2 = File(parentDir.toFile(), "child2")
        Files.createDirectory(child1.toPath())
        Files.createSymbolicLink(child2.toPath(), child1.toPath())
        val child1AttemptToCreate = Files.createDirectories(child1.toPath())
        Assert.assertThat(child1AttemptToCreate.toAbsolutePath().toFile().absolutePath, `is`(child1.absolutePath))
    }

    @Test(expected = FileAlreadyExistsException::class)
    fun `should fail to create directories with target of linked folder`() {
        val parentDir = Files.createTempDirectory("cdTest")
        val child1 = File(parentDir.toFile(), "child1")
        val child2 = File(parentDir.toFile(), "child2")
        Files.createDirectory(child1.toPath())
        Files.createSymbolicLink(child2.toPath(), child1.toPath())
        val child2AttemptToCreate = Files.createDirectories(child2.toPath())
    }

    @Test
    fun `should allow create directories with target of linked folder`() {
        val parentDir = Files.createTempDirectory("cdTest")
        val child1 = File(parentDir.toFile(), "child1")
        val child2 = File(parentDir.toFile(), "child2")
        Files.createDirectory(child1.toPath())
        Files.createSymbolicLink(child2.toPath(), child1.toPath())
        val child2AttemptToCreate = CreateDirectoriesWithLinks.createDirectories(child2.toPath())
        Assert.assertThat(child2AttemptToCreate.toAbsolutePath().toFile().absolutePath, `is`(child2.absolutePath))
    }


}
