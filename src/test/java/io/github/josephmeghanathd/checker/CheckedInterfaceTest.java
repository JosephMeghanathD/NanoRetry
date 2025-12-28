package io.github.josephmeghanathd.checker;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CheckedInterfaceTest {

    @Test
    @DisplayName("CheckedSupplier should allow throwing checked exceptions")
    void testCheckedSupplier() {
        CheckedSupplier<String> supplier = () -> {
            throw new IOException("Checked Exception");
        };
        assertThrows(IOException.class, supplier::get);
    }

    @Test
    @DisplayName("CheckedRunnable should allow throwing checked exceptions")
    void testCheckedRunnable() {
        CheckedRunnable runnable = () -> {
            throw new Exception("Checked Exception");
        };
        assertThrows(Exception.class, runnable::run);
    }
}