/*
 * This file is part of the Yildiz-Engine project, licenced under the MIT License  (MIT)
 *
 *  Copyright (c) 2019 Grégory Van den Borre
 *
 *  More infos available: https://engine.yildiz-games.be
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 *  documentation files (the "Software"), to deal in the Software without restriction, including without
 *  limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 *  of the Software, and to permit persons to whom the Software is furnished to do so,
 *  subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all copies or substantial
 *  portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 *  WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 *  OR COPYRIGHT  HOLDERS BE LIABLE FOR ANY CLAIM,
 *  DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE  SOFTWARE.
 *
 */

package be.yildizgames.common.libloader;


import be.yildizgames.common.compression.CompressionFactory;
import be.yildizgames.common.compression.Unpacker;
import be.yildizgames.common.os.OperatingSystem;
import be.yildizgames.common.os.SystemUtil;
import be.yildizgames.common.os.factory.OperatingSystems;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

/**
 * Utility class to load the native library from the classpath or a jar.
 *
 * @author Grégory Van den Borre
 */
public final class NativeResourceLoader {

    /**
     * Internal logger.
     */
    private static final System.Logger LOGGER = System.getLogger(NativeResourceLoader.class.getName());

    /**
     * Directory containing the native libraries, win34,
     * linux64 depending on the operating system and the underlying
     * architecture.
     */
    public final String directory;

    /**
     * Will contains the native libraries to be loaded.
     */
    public final Path libDirectory;

    /**
     * Library file extension, can be .dll on windows, .so on linux.
     */
    public final String libraryExtension;

    /**
     * Contains the found native libraries and their full path.
     */
    private final Map<String, String> availableLib = new HashMap<>();

    /**
     * Utility to unpack the jars, use zip algorithm.
     */
    private final Unpacker jarHandler = CompressionFactory.zipUnpacker();

    /**
     * Create a new instance, root path to use is defaulted as user_home/app-root/data.
     * @param decompress Flag to unpack or not.
     * @param systemToSupport List of systems to support, cannot be null.
     */
    private NativeResourceLoader(boolean decompress, OperatingSystem... systemToSupport) {
        this(Paths.get(System.getProperty("user.home")).resolve("app-root").resolve("data").toString(), decompress, systemToSupport);
    }

    /**
     * Create a new instance
     * @param path Path where are stored the libs, cannot be null.
     * @param decompress Flag to unpack or not.
     * @param systemToSupport List of systems to support, cannot be null.
     */
    private NativeResourceLoader(String path, boolean decompress, OperatingSystem... systemToSupport) {
        super();
        OperatingSystem nos = this.findSystem(systemToSupport);
        this.libraryExtension = nos.getExtension();
        this.directory = nos.getName();
        this.libDirectory = Paths.get(path);
        if (decompress) {
            LOGGER.log(System.Logger.Level.DEBUG, "Unpacking {} folder from jar to {} folder.", this.directory, libDirectory);

            Arrays.stream(System.getProperty("java.class.path", "").split(File.pathSeparator))
                    .filter(s -> s.endsWith(".jar"))
                    .map(Paths::get)
                    .filter(Files::exists)
                    .forEach(app -> jarHandler.unpackDirectoryToDirectory(app, this.directory, libDirectory));
        }
        try {
            this.registerLibInDir();
        } catch (IOException e) {
            LOGGER.log(System.Logger.Level.ERROR, "Cannot register libs", e);
        }
    }

    /**
     * Retrieve the libraries in the class pass, decompress them and register them.
     * @param systemToSupport The list of system to support, cannot be null.
     * @return The created loader, never null.
     */
    public static NativeResourceLoader inJar(OperatingSystem... systemToSupport) {
        return new NativeResourceLoader(true, systemToSupport);
    }

    /**
     * Retrieve the libraries in the class pass, decompress them and register them, support all systems.
     * @return The created loader, never null.
     */
    public static NativeResourceLoader inJar() {
        return new NativeResourceLoader(true, OperatingSystems.getAll());
    }

    /**
     * Retrieve the libraries in the class pass, decompress them in the provided path and register them.
     * @param path Directory where the libs will be copied, cannot be null.
     * @param systemToSupport The list of system to support, cannot be null.
     * @return The created loader, never null.
     */
    public static NativeResourceLoader inJar(String path, OperatingSystem... systemToSupport) {
        return new NativeResourceLoader(path, true, systemToSupport);
    }

    public static NativeResourceLoader inJar(String path) {
        return new NativeResourceLoader(path, true, OperatingSystems.getAll());
    }

    public static NativeResourceLoader inPath(String path, OperatingSystem... systemToSupport) {
        return new NativeResourceLoader(path,false, systemToSupport);
    }

    public static NativeResourceLoader inTestPath(OperatingSystem... systemToSupport) {
        return new NativeResourceLoader(new File("").getAbsolutePath() + "/target/classes",false, systemToSupport);
    }

    /**
     * Use libraries from a given path and register them.
     * @param  systemToSupport The list of system to support.
     * @return The created loader.
     */
    public static NativeResourceLoader external(OperatingSystem... systemToSupport) {
        String path = new File("").getParentFile().getAbsolutePath();
        return new NativeResourceLoader(path, false, systemToSupport);
    }

    private OperatingSystem findSystem(OperatingSystem[] systemToSupport) {
        return Arrays
                .stream(systemToSupport)
                .filter(OperatingSystem::isCurrent)
                .findFirst()
                .orElseThrow(IllegalStateException::new);
    }

    /**
     * Give the full path of a registered native library.
     *
     * @param lib Library to check.
     * @return The absolute path of the given library.
     */
    public String getLibPath(final String lib) {
        Path f = Paths.get(lib.endsWith(libraryExtension) ? lib : lib + libraryExtension);
        if (Files.exists(f)) {
            return f.toAbsolutePath().toString();
        }
        String nativePath = this.availableLib.get(f.getFileName().toString());
        if (nativePath == null) {
            throw new IllegalStateException(lib + " has not been found in path.");
        }
        return nativePath;
    }

    /**
     * Load a native library, it will check if it is contained in a jar, if so,
     * the library will be extracted in a temporary place and loaded from there.
     *
     * @param libs Native library name to load, cannot be null.
     */
    public void loadLibrary(final String... libs) {
        String nativePath;
        for (String lib : libs) {
            nativePath = getLibPath(lib);
            LOGGER.log(System.Logger.Level.DEBUG, "Loading native : {}", nativePath);
            System.load(nativePath);
            LOGGER.log(System.Logger.Level.DEBUG, "{} loaded.", nativePath);
        }
    }

    /**
     * Register the found libraries in a directory to be ready to be loaded.
     *
     * @param dir Directory holding the libraries.
     */
    private void registerLibInDir(final Path dir) throws IOException {
        if (Files.exists(dir) && Files.isDirectory(dir)) {
            try(Stream<Path> walk = Files.walk(dir)) {
                walk
                        .filter(p -> Files.isRegularFile(p) && p.toString().endsWith(this.libraryExtension))
                        .forEach(p -> this.availableLib.put(p.getFileName().toString(), p.toAbsolutePath().toString()));
            }
        }
    }

    private void registerLibInDir() throws IOException {
        registerLibInDir(libDirectory.resolve(this.directory).toAbsolutePath());
    }

    /**
     * To load the shared libraries, only used for windows, on linux, will not
     * load anything.
     *
     * @param libs Libraries to be loaded only on windows.
     */
    public void loadBaseLibrary(String... libs) {
        if (SystemUtil.isWindows()) {
            loadLibrary("libgcc_s_seh-1.dll", "libstdc++-6.dll");
            if(libs != null && libs.length > 0) {
                loadLibrary(libs);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public List<String> getLoadedLibraries() {
        try {
            Field lib = ClassLoader.class.getDeclaredField("loadedLibraryNames");
            lib.setAccessible(true);
            return new ArrayList<>(Vector.class.cast(lib.get(ClassLoader.getSystemClassLoader())));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new IllegalArgumentException(e);
        }
    }

}
