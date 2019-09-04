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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * @author Grégory Van den Borre
 */
public class GlobalNativeResourceLoaderTest {

    @Nested
    public class GetInstance {

        @Test
        public void happyFlow() {
            Assertions.assertSame(GlobalNativeResourceLoader.getInstance(), GlobalNativeResourceLoader.getInstance());
        }
    }

    @Nested
    public class GetLoader {

        @Test
        public void happyFlow() {
            Assertions.assertNotNull(GlobalNativeResourceLoader.getInstance().getLoader());
        }
    }

    @Nested
    public class SetNativeResourceLoader {

        @Test
        public void happyFlow() {
            GlobalNativeResourceLoader.getInstance().setNativeResourceLoader(NativeResourceLoader.inJar());
        }

        @Test
        public void withNull() {
            Assertions.assertThrows(NullPointerException.class, () -> GlobalNativeResourceLoader.getInstance().setNativeResourceLoader(null));
        }
    }
}
