package lass;

public interface SqlRunner {
	void run(SqlRunnable runnable);
	<T> T call(SqlCallable<T> callable);
}
