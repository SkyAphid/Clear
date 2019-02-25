package nokori.util.pool;


public abstract class SynchronizedPool<E extends Poolable> extends Pool<E>{
	
	@Override
	public E get(){
		synchronized(this){
			return super.get();
		}
	}
	
	@Override
	public void recycle(E poolable){
		poolable.reset(); //Reset immediately so we don't get a lot of systems using a lot of memory in the pool
		synchronized(this){
			pool.add(poolable);
		}
	}
}