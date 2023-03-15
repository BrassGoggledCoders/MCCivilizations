package xyz.brassgoggledcoders.mccivilizations.util.function;

public interface ThrowingTriFunction<T, U, V, W, E extends Throwable> {
    W apply(T t, U u, V v) throws E;
}
