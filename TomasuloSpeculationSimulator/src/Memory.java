public class Memory {
	MainMemory mainMemoryD;
	Cache DataCache;
	MainMemory mainMemoryI;
	Cache InstructionCache;
	
	public Memory(
			     int numberOfDCaches, 
			     int allCacheDGeometry[][],
			     int mainMemoryLatencyD,
			     int mainMemorySizeD, 
			     int mainMemoryBlockSizeD,
			     int numberOfICaches,
			     int allCacheIGeometry[][],
			     int mainMemoryLatencyI,
			     int mainMemorySizeI, 
			     int mainMemoryBlockSizeI)
	{
	
		Cache currentLevel=null;
		Cache nextLevel=null;
		Cache previousLevel=null;
		Cache[] cacheArray=new Cache[numberOfICaches];
		mainMemoryI=new MainMemory(mainMemoryLatencyI,mainMemorySizeI,mainMemoryBlockSizeI,previousLevel);
		for(int i=0; i<numberOfICaches;i++)
		{	
			cacheArray[i]=new Cache(previousLevel,
								  nextLevel,
								  mainMemoryI,
								  allCacheIGeometry[i][0],//latency
								  allCacheIGeometry[i][1],//totalSize in bytes
								  allCacheIGeometry[i][2],//lineSize
								  allCacheIGeometry[i][3],//associativity
								  allCacheIGeometry[i][4]==0?false:true,//write through
								  allCacheIGeometry[i][5]==0?false:true//write around
										  );
		}
		for(int i=0; i<numberOfICaches;i++)
		{
			previousLevel=currentLevel;
			currentLevel=cacheArray[i];
			currentLevel.previousLevel=previousLevel;
			if(i==numberOfICaches-1)
			{
				currentLevel.mainMemory=mainMemoryI;
				mainMemoryI.previousCache=currentLevel;
				
			}
			else
			{
				nextLevel=cacheArray[i+1];
				currentLevel.nextLevel=nextLevel;	
			}
		}
		InstructionCache=cacheArray[0];
	
		
		currentLevel=null;
		nextLevel=null;
		previousLevel=null;	
		mainMemoryD=new MainMemory(mainMemoryLatencyD,mainMemorySizeD,mainMemoryBlockSizeD,previousLevel);
		cacheArray=new Cache[numberOfDCaches];
		for(int i=0; i<numberOfDCaches;i++)
		{	
			cacheArray[i]=new Cache(previousLevel,
								  nextLevel,
								  mainMemoryD,
								  allCacheDGeometry[i][0],//latency
								  allCacheDGeometry[i][1],//totalSize in bytes
								  allCacheDGeometry[i][2],//lineSize
								  allCacheDGeometry[i][3],//associativity
								  allCacheDGeometry[i][4]==0?false:true,//write through
								  allCacheDGeometry[i][5]==0?false:true//write around
										  );
		}
		for(int i=0; i<numberOfDCaches;i++)
		{
			previousLevel=currentLevel;
			currentLevel=cacheArray[i];
			currentLevel.previousLevel=previousLevel;
			if(i==numberOfDCaches-1)
			{
				currentLevel.mainMemory=mainMemoryD;
				mainMemoryD.previousCache=currentLevel;
				
			}
			else
			{
				nextLevel=cacheArray[i+1];
				currentLevel.nextLevel=nextLevel;	
			}
		}
		DataCache=cacheArray[0];
		
		
		
	}

	public Memory() {
		// TODO Auto-generated constructor stub
	}
	
	public static void main(String []args)
	{
		
		Memory memory=new Memory();
	}
	
}
