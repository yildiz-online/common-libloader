/*
 * This file is part of the Yildiz-Engine project, licenced under the MIT License  (MIT)
 *
 * Copyright (c) 2019 Grégory Van den Borre
 *
 * More infos available: https://engine.yildiz-games.be
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the "Software"), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify, merge,
 * publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons
 * to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS  OR COPYRIGHT  HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE  SOFTWARE.
 */

package be.yildizgames.common.libloader;

import java.util.Objects;

/**
 * Global loader, can be used to use the same loader, no matter where called from.
 * @author Grégory Van den Borre
 */
public class GlobalNativeResourceLoader {

    /**
     * Unique instance.
     */
    private static final GlobalNativeResourceLoader INSTANCE = new GlobalNativeResourceLoader();

    /**
     * Current loader, default is in jar.
     */
    private NativeResourceLoader loader = NativeResourceLoader.inJar();

    /**
     * Provide the global loader instance.
     * @return The global loader.
     */
    public static GlobalNativeResourceLoader getInstance() {
        return INSTANCE;
    }

    public final void setNativeResourceLoader(NativeResourceLoader loader) {
        Objects.requireNonNull(loader);
        this.loader = loader;
    }

    public final NativeResourceLoader getLoader() {
        return this.loader;
    }
}
