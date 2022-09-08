package optional;

import java.util.function.Supplier;

/**
 * @author leofee
 */
public class OptionalBoolean {

    private final Boolean value;

    private OptionalBoolean(Boolean value) {
        this.value = value;
    }

    public static OptionalBoolean of(Boolean value) {
        return new OptionalBoolean(value);
    }

    public <X extends Throwable> void throwIfFalse(Supplier<? extends X> exceptionSupplier) throws X {
        if (!value) {
            throw exceptionSupplier.get();
        }
    }

    public <X extends Throwable> void throwIfTrue(Supplier<? extends X> exceptionSupplier) throws X {
        if (value) {
            throw exceptionSupplier.get();
        }
    }
}
