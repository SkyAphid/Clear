package nokori.util.pool;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class ReflectionPool<E extends Poolable> extends Pool<E> {
	
	private static final Object[] EMPTY_ARRAY = new Object[0]; 
	
	private Class<E> clazz;
	private Constructor<E> constructor;
	
	public ReflectionPool(Class<E> clazz) {
		this.clazz = clazz;
		try {
			constructor = clazz.getDeclaredConstructor((Class[]) null);
			constructor.setAccessible(true);
		} catch (NoSuchMethodException | SecurityException e) {
			System.err.println("Unable to create pool for class " + clazz.getName() + ": unable to find constructor");
			e.printStackTrace();
		}
	}

	@Override
	protected E create() {
			try {
				return constructor.newInstance(EMPTY_ARRAY);
			} catch (InstantiationException | IllegalAccessException| IllegalArgumentException | InvocationTargetException e) {
				System.err.println("Unable to create instance of class " + clazz.getName() + ": unable to find constructor");
				e.printStackTrace();
			}
		return null;
	}
	
}
