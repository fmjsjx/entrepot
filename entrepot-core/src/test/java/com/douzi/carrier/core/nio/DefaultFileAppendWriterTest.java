package com.douzi.carrier.core.nio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.channels.ClosedChannelException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.fmjsjx.entrepot.core.nio.DefaultFileAppendWriter;

import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;

public class DefaultFileAppendWriterTest {

    private static final File testDir = new File("xxxxyyyyzzzz");

    @BeforeAll
    public static void init() throws Throwable {
        if (testDir.exists()) {
            testDir.delete();
        }
        testDir.mkdirs();
    }

    @AfterAll
    public static void destroy() throws Throwable {
        for (var f : testDir.listFiles()) {
            f.delete();
        }
        testDir.delete();
    }

    @BeforeEach
    public void setUp() throws Throwable {
        var empty = new File(testDir, "empty");
        empty.createNewFile();

        var oneLine = new File(testDir, "oneLine");
        try (var writer = new FileWriter(oneLine)) {
            writer.write("The first line.\n");
        }
    }

    @AfterEach
    public void tearDown() throws Throwable {
        var empty = new File(testDir, "empty");
        empty.delete();

        var oneLine = new File(testDir, "oneLine");
        oneLine.delete();
    }

    @Test
    public void testIsOpen() {
        try {
            var empty = new File(testDir, "empty");
            try (var writer = new DefaultFileAppendWriter(empty)) {
                assertTrue(writer.isOpen());
                writer.close();
                assertFalse(writer.isOpen());
            }
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    public void testPosition() {
        try {
            var empty = new File(testDir, "empty");
            try (var writer = new DefaultFileAppendWriter(empty)) {
                assertEquals(empty.length(), writer.position());
            }

            var oneLine = new File(testDir, "oneLine");
            try (var writer = new DefaultFileAppendWriter(oneLine)) {
                assertEquals(oneLine.length(), writer.position());
            }
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    public void testClose() {
        try {
            var empty = new File(testDir, "empty");
            DefaultFileAppendWriter writer = null;
            try {
                writer = new DefaultFileAppendWriter(empty);
                writer.close();
                try {
                    writer.write("error".getBytes());
                    fail("Should throw closed channel exception");
                } catch (IOException e) {
                    assertEquals(ClosedChannelException.class, e.getClass());
                }
                try {
                    writer.position();
                    fail("Should throw closed channel exception");
                } catch (IOException e) {
                    assertEquals(ClosedChannelException.class, e.getClass());
                }
            } finally {
                if (writer != null && writer.isOpen()) {
                    writer.close();
                }
            }
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    public void testWriteByteBuf() {
        var buf = Unpooled.directBuffer();
        buf.writeCharSequence("123456789\n", CharsetUtil.UTF_8);
        buf.markReaderIndex();
        try {
            var empty = new File(testDir, "empty");
            try (var writer = new DefaultFileAppendWriter(empty)) {
                assertEquals(0, empty.length());
                assertEquals(10, writer.write(buf));
                writer.force(true);
                assertEquals(10, empty.length());
            } catch (IOException e) {
                fail(e);
            }

            var oneLine = new File(testDir, "oneLine");
            try (var writer = new DefaultFileAppendWriter(oneLine)) {
                buf.resetReaderIndex();
                var baseLength = oneLine.length();
                assertEquals(10, writer.write(buf));
                writer.force(true);
                assertEquals(10 + baseLength, oneLine.length());
            } catch (IOException e) {
                fail(e);
            }
        } finally {
            buf.release();
        }
    }

    @Test
    public void testWriteByteBufs() {
        var buf1 = Unpooled.directBuffer();
        buf1.writeCharSequence("123456789", CharsetUtil.UTF_8);
        buf1.markReaderIndex();

        var buf2 = Unpooled.directBuffer(1, 1).writeByte('\n');
        buf2.markReaderIndex();
        try {
            var empty = new File(testDir, "empty");
            try (var writer = new DefaultFileAppendWriter(empty)) {
                assertEquals(0, empty.length());
                assertEquals(10, writer.write(buf1, buf2));
                writer.force(true);
                assertEquals(10, empty.length());
            } catch (IOException e) {
                fail(e);
            }

            var oneLine = new File(testDir, "oneLine");
            try (var writer = new DefaultFileAppendWriter(oneLine)) {
                buf1.resetReaderIndex();
                buf2.resetReaderIndex();
                var baseLength = oneLine.length();
                assertEquals(10, writer.write(buf1, buf2));
                writer.force(true);
                assertEquals(10 + baseLength, oneLine.length());
            } catch (IOException e) {
                fail(e);
            }
        } finally {
            buf1.release();
            buf2.release();
        }
    }

}
