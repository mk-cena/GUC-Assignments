import java.util.HashMap;


public class MainMemory {
	HashMap<Integer,MemoryWord>mainMemory;
	int latency;
	int blockSize; //in words
	int size;//in bytes
	Cache previousCache;
	
	int numberOfAddressBits;
	int numberOfindexBits;
	int wordOffsetMask;
	
	
	
	public MainMemory(int latency, int size, int blockSize, Cache previousCache)
	{
		this.blockSize=blockSize;
		this.latency=latency;
		this.size=size;
		mainMemory=new HashMap<Integer,MemoryWord>(size);
		this.previousCache=previousCache;
		int numberOfBlocks=size/(2*blockSize);
		numberOfindexBits=(int) Math.ceil(Math.log(numberOfBlocks)/Math.log(2));
		numberOfAddressBits=(int) Math.ceil(Math.log(size/2)/Math.log(2));
		this.wordOffsetMask=0;
		CreateWordOffsetMask();
	}
	
	
	public MemoryWord[] ReadBlock(int address)
	{
		int index=GetBlockAddress(address);
		MemoryWord block[]=new MemoryWord[blockSize];
		for(int i=index;i<index+blockSize;i++)
			block[i-index]= mainMemory.get(i);
		return block;
	}
	
	public void Write(int address, MemoryWord[]data)
	{
		int k=this.GetWordLocationInBlock(address)/data.length;
		k*=data.length;
		int index=GetBlockAddress(address);
		for(int i=0;i<data.length;i++)
			mainMemory.put(index+k+i,data[i]);
	}
	
	
	private int GetBlockAddress(int address)
	{
		int hashedAddress=address;
		for(int i=0;i<=numberOfAddressBits-numberOfindexBits;i++)
			hashedAddress>>=1;
		
		for(int i=0;i<numberOfAddressBits-numberOfindexBits;i++)
			hashedAddress<<=1;
		
		return hashedAddress;
	}
	
	private void CreateWordOffsetMask()
	{
		int wordBits=numberOfAddressBits-numberOfindexBits;
		for(int i=0; i<wordBits; i++)
			this.wordOffsetMask|=(1<<i);
	}
	
	public int GetWordLocationInBlock(int address)
	{
		return (address>>1)&(this.wordOffsetMask);
	}
}
