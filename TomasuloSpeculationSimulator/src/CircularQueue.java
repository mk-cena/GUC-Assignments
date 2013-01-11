
public class CircularQueue {
	int head;
	int tail;
	Object [] object;
	int size;
	int numberOfElements;
	
	public CircularQueue(int size)
	{
		object=new Object[size];
		head=0;
		tail=0;
		this.size=size;
		this.numberOfElements=0;
	}
	public void EnQueue(Object o)
	{
		object[tail]=o;
		tail+=1;
		tail%=size;
		if(!this.IsFull())
			numberOfElements+=1;
	}
	public Object DeQueue()
	{
		Object o=object[head];
		head+=1;
		head%=size;
		if(!this.IsEmpty())
			this.numberOfElements-=1;
		return o;
	}
	public boolean IsFull()
	{
		return this.numberOfElements==this.size;
	}
	public boolean IsEmpty()
	{
		return this.numberOfElements==0;
	}
	public Object GetHead()
	{
		return object[head];
	}
	public void Flush()
	{
		this.numberOfElements=0;
		head=0;
		tail=0;
	}
}
