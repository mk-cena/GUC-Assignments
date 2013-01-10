public class ReOrderBuffer {
	CircularQueue rob;

	
	public ReOrderBuffer(int size)
	{
		rob = new CircularQueue(size);
	}
	
	public boolean EnQueue(ReOrderBufferEntry e)
	{
		if(this.rob.IsFull())
			return false;
		else
			rob.EnQueue(e);
		
		return true;
	}	
}
