package xyz.brassgoggledcoders.mccivilizations.util.function;

public interface ThrowingBiFunction<T, U, V, E extends Throwable> {
    V apply(T t, U u) throws E;
}
