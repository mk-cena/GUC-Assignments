public class MemoryWordTimeStamp {
	MemoryWord[] words;
	int latency;
	public MemoryWordTimeStamp(MemoryWord [] data, int latency)
	{
		this.latency=latency;
		
		this.words=new DataWord[data.length];
		for(int i=0;i<data.length;i++)
			this.words[i]=data[i];
	}
}
