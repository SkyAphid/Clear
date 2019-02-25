package nokori.util.pool;

import nokori.util.FastArrayList;


public abstract class Pool<E extends Poolable> {
	
	protected FastArrayList<E> pool;
	private int allocationCount;
	
	public Pool() {
		pool = new FastArrayList<>();
	}
	
	public E get(){
		if(pool.isEmpty()){
			allocationCount++;
			return create();
		}else{
			return pool.removeLast();
		}
	}
	
	protected abstract E create();
	
	public void recycle(E poolable){
		poolable.reset(); //Reset immediately so we don't get a lot of systems using a lot of memory in the pool
		pool.add(poolable);
	}
	
	public int getAllocationCount(){
		return allocationCount;
	}
}