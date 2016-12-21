package lass;

public interface SqlCallable<T> {
	T run(SqlConnection conn);
}
