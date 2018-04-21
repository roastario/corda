package net.corda.core.internal

import java.io.IOException
import java.nio.file.*
import java.nio.file.Files.createDirectory
import java.nio.file.Files.readAttributes
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.attribute.FileAttribute
import java.nio.file.spi.FileSystemProvider

class CreateDirectoriesWithLinks private constructor() {

    companion object {
        @Throws(IOException::class)
        fun createDirectories(dir: Path, vararg attrs: FileAttribute<*>): Path {
            var dir = dir
            // attempt to create the directory
            try {
                createAndCheckIsDirectory(dir, *attrs)
                return dir
            } catch (x: FileAlreadyExistsException) {
                // file exists and is not a directory
                throw x
            } catch (x: IOException) {
                // parent may not exist or other reason
            }

            var se: SecurityException? = null
            try {
                dir = dir.toAbsolutePath()
            } catch (x: SecurityException) {
                // don't have permission to get absolute path
                se = x
            }

            // find a decendent that exists
            var parent: Path? = dir.parent
            while (parent != null) {
                try {
                    provider(parent).checkAccess(parent)
                    break
                } catch (x: NoSuchFileException) {
                    // does not exist
                }

                parent = parent.parent
            }
            if (parent == null) {
                // unable to find existing parent
                if (se == null) {
                    throw FileSystemException(dir.toString(), null,
                            "Unable to determine if root directory exists")
                } else {
                    throw se
                }
            }

            // create directories
            var child: Path = parent
            for (name in parent.relativize(dir)) {
                child = child.resolve(name)
                createAndCheckIsDirectory(child, *attrs)
            }
            return dir
        }

        @Throws(IOException::class)
        private fun createAndCheckIsDirectory(dir: Path,
                                              vararg attrs: FileAttribute<*>) {
            try {
                createDirectory(dir, *attrs)
            } catch (x: FileAlreadyExistsException) {
                if (!isDirectory(dir))
                    throw x
            }
        }

        private fun provider(path: Path): FileSystemProvider {
            return path.fileSystem.provider()
        }

        private fun isDirectory(path: Path): Boolean {
            try {
                return readAttributes<BasicFileAttributes>(path, BasicFileAttributes::class.java).isDirectory()
            } catch (ioe: IOException) {
                return false
            }

        }
    }

}

