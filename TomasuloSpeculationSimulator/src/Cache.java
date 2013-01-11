import java.util.HashMap;
public class Cache {
	
	int latency;
	int totalSize;
	int lineSize;
	int associativity;
	boolean writeThrough;
	boolean writeAround;
	
	int wordsPerBlock;
	int wordsOffset;
	
	int totalSets;
	int indexBits;
	
	int indexMask;
	int wordOffsetMask;
	
	int references;
	int hits;
	
	HashMap<Integer,CacheEntry[]>cache;
	
	Cache nextLevel;
	Cache previousLevel;
	MainMemory mainMemory;
	public Cache(Cache previousLevel, Cache nextLevel, MainMemory mainMemory,int latency, int totalSize, int lineSize, int associativity, boolean writeThrough, boolean writeAround)
	{
		this.latency=latency;
		this.totalSize=totalSize;
		this.lineSize=lineSize;
		this.associativity=associativity;
		this.writeThrough=writeThrough;
		this.writeAround=writeAround;
		this.mainMemory=mainMemory;
		this.nextLevel=nextLevel;
		this.previousLevel=previousLevel;
		
		wordsPerBlock=lineSize;
		wordsOffset=(int) Math.ceil((Math.log(wordsPerBlock)/Math.log(2)));
		
		totalSets=totalSize/(lineSize*this.associativity);
		indexBits=(int) Math.ceil((Math.log(totalSets)/Math.log(2)));
		indexMask=0;
		wordOffsetMask=0;
		CreateIndexMask(indexBits);
		CreateWordOffsetMask();
		cache=new HashMap<Integer,CacheEntry[]>(totalSets);
		
		
		references=0;
		hits=0;
		
		
	}
	
	public MemoryWordTimeStamp ReadBlock(int address)
	{
		int latency=0;
		Cache currentCache=this;
		MemoryWord[]words = null;
		boolean found=false;
		while(currentCache!=null&&!found)
		{
			currentCache.references++;
			latency+=currentCache.latency;
			int index=currentCache.GetSetAddress(address);
			int tag=currentCache.GetTag(address);
			CacheEntry[] cacheSet=currentCache.cache.get(index);
			if(cacheSet!=null)
			for(int i=0; i<cacheSet.length;i++)
				if(cacheSet[i]!=null && cacheSet[i].tag==tag && cacheSet[i].valid)
				{
					currentCache.hits++;
					words= cacheSet[i].words;
					found=true;
					break;
				}
			if(!found)
				currentCache=currentCache.nextLevel;
		}
		int i;
		if(currentCache==null)
		{
			
			i=mainMemory.GetWordLocationInBlock(address);
			words=mainMemory.ReadBlock(address);
			latency+=mainMemory.latency;
			currentCache=mainMemory.previousCache;
			i/=(currentCache.wordsPerBlock);
			i*=currentCache.wordsPerBlock;


		}
		else
		{
			
			i=currentCache.GetWordLocationInBlock(address);
			currentCache=currentCache.previousLevel;
			if(currentCache!=null)
			{
				i/=currentCache.wordsPerBlock;
				i*=currentCache.wordsPerBlock;
			}
		}
		
		
		if(currentCache==null)
			return new MemoryWordTimeStamp(words, latency);
		MemoryWord readWords[]=null;
		while(currentCache!=null)
		{
			latency+=currentCache.latency;
			readWords=new MemoryWord[currentCache.wordsPerBlock];
			for(int j=i;j<i+currentCache.wordsPerBlock;j++)
				readWords[j-i]=words[j];
			currentCache.ReplaceBlock(address, readWords);
			i=currentCache.GetWordLocationInBlock(address);
			currentCache=currentCache.previousLevel;
			if(currentCache!=null)
			{
				i/=currentCache.wordsPerBlock;
				i*=currentCache.wordsPerBlock;
			}
			words=readWords;
		}
		MemoryWordTimeStamp dts=new MemoryWordTimeStamp(readWords,latency);
		return dts;
	}
	
	public void ReplaceBlock(int address, MemoryWord[] data)
	{
		int index=this.GetSetAddress(address);
		int tag=this.GetTag(address);
		CacheEntry cacheEntry=new CacheEntry(this,tag,data);
		CacheEntry[] cacheSet=this.cache.get(index);
		
		if(cacheSet==null)
		{
			this.cache.put(index,new CacheEntry[this.associativity]);
			cacheSet=this.cache.get(index);
		}
			
		
		
		//1st priority to null or invalid
		for(int i=0;i<cacheSet.length;i++)
			if(cacheSet[i]==null||(!cacheSet[i].valid))
				{
					this.cache.get(index)[i]=cacheEntry;
					return;
				}
		//2nd priority to valid and not dirty
		for(int i=0;i<cacheSet.length;i++)
			if((!cacheSet[i].dirty))
				{
					this.cache.get(index)[i]=cacheEntry;
					return;
				}
		
		CacheEntry replaced=this.cache.get(index)[0];
		int replacedAddress=this.GetPhysicalAddress(replaced.tag, index);
		this.cache.get(index)[0]=cacheEntry;
		if(this.nextLevel!=null)
			this.nextLevel.WriteBlock(replacedAddress, replaced.words);
		else
			this.mainMemory.Write(replacedAddress, replaced.words);
		
	}
	
	
	public int WriteBlock(int address, MemoryWord[] data)
	{
		int index=GetSetAddress(address);
		int tag=GetTag(address);
		CacheEntry []cacheSet=cache.get(index);
		int latency=0;
		
		this.references++;
		if(this.writeThrough)
		{
			latency+=this.latency;
			//hit
			if(cacheSet!=null)
				for(int i=0; i<cacheSet.length;i++)
					if(cacheSet[i]!=null&&cacheSet[i].tag==tag)
					{
						this.hits++;
						int k=this.GetWordLocationInBlock(address)/data.length;
						k*=data.length;
						for(int j=k;j<k+data.length;j++)
							this.cache.get(index)[i].words[j]=data[j-k];
						if(this.nextLevel!=null)
							latency+=this.nextLevel.WriteBlock(address, data);
						else
						{
							latency+=this.mainMemory.latency;
							this.mainMemory.Write(address, data);
						}
						return latency;
					}
			
			//miss
			if(this.writeAround)
			{
				if(this.nextLevel!=null)	
					latency+=this.nextLevel.WriteBlock(address, data);
				else
				{
					latency+=this.mainMemory.latency;
					this.mainMemory.Write(address, data);
				}
				return latency;
			}
			else
			{
				if(this.nextLevel!=null)	
					latency+=this.nextLevel.WriteBlock(address, data);
				else
				{
					latency+=this.mainMemory.latency;
					this.mainMemory.Write(address, data);
				}
				latency+=this.ReadBlock(address).latency;
				return latency;
			}
							
		}
		else
		{
			latency+=this.latency;
			//hit
			if(cacheSet!=null)
				for(int i=0; i<cacheSet.length;i++)
					if(cacheSet[i]!=null&&cacheSet[i].tag==tag)
					{
						this.hits++;
						int k=this.GetWordLocationInBlock(address)/data.length;
						k*=data.length;
						for(int j=k;j<k+data.length;j++)
							this.cache.get(index)[i].words[j]=data[j-k];
						this.cache.get(index)[i].dirty=true;
						return latency;
					}
			
			//miss
			if(this.writeAround)
			{
				if(this.nextLevel!=null)	
					latency+=this.nextLevel.WriteBlock(address, data);
				else
				{
					latency+=this.mainMemory.latency;
					this.mainMemory.Write(address, data);
				}
					
				return latency;
			}
			else
			{

				if(this.nextLevel!=null)
					latency+=this.nextLevel.WriteBlock(address, data);
				else
				{
					latency+=this.mainMemory.latency;
					this.mainMemory.Write(address, data);
				}
					
					
				latency+=this.ReadBlock(address).latency;
				return latency;
			}
			
		}
	}
	
	private int GetPhysicalAddress(int tag,int index)
	{
		int address=tag;
		address<<=indexBits;
		address|=index;
		address<<=this.wordsOffset;
		address<<=1;
		return address;
	}
	private void CreateWordOffsetMask()
	{
		for(int i=0; i<wordsOffset; i++)
			this.wordOffsetMask|=(1<<i);
	}
	private void CreateIndexMask(int indexBits)
	{
		for(int i=0; i<indexBits; i++)
			this.indexMask|=(1<<i);
	}
	
	private int GetSetAddress(int address)
	{
		int hashedAddress=address;
		hashedAddress>>=1;
		hashedAddress>>=wordsOffset;
		hashedAddress&=indexMask;
		
		return hashedAddress;
	}
	private int GetTag(int address)
	{
		int tag=address;
		tag>>=1;
		tag>>=wordsOffset;
		tag>>=indexBits;
		
		return tag;
	}
	private int GetWordLocationInBlock(int address)
	{
		return address>>1&(this.wordOffsetMask);
	}
	public MemoryWordTimeStamp ReadWord(int address)
	{	
		MemoryWordTimeStamp mwts=this.ReadBlock(address);
		MemoryWord []block=mwts.words;
		int wordBits=(address>>1)&this.wordOffsetMask;
		MemoryWord word[]=new MemoryWord [1];
		word[0]=block[wordBits];
		MemoryWordTimeStamp ret= new MemoryWordTimeStamp(word,mwts.latency);
		return ret;
	}
	public int WriteWord(DataWord dataword)
	{
		Cache currentCache=this;
		MemoryWord[] data=new MemoryWord[this.wordsPerBlock];
		boolean found=false;
		
		while(currentCache!=null)
		{
			int index=currentCache.GetSetAddress(dataword.physicalAddress);
			int tag=currentCache.GetTag(dataword.physicalAddress);
			CacheEntry []cacheSet=currentCache.cache.get(index);
			
				if(cacheSet!=null)
					for(int i=0; i<cacheSet.length;i++)
						if(cacheSet[i]!=null&&cacheSet[i].tag==tag)
						{
							
							int k=this.GetWordLocationInBlock(dataword.physicalAddress)/this.wordsPerBlock;
							k*=this.wordsPerBlock;
							for(int j=k;j<k+this.wordsPerBlock;j++)
								data[j-k]=currentCache.cache.get(index)[i].words[j];
							found=true;
							
							break;
						}
				if(!found)
					currentCache=currentCache.nextLevel;
				else
					break;
				
		}
		if(currentCache==null)
		{
			currentCache=this;
			while(currentCache.nextLevel!=null)
				currentCache=currentCache.nextLevel;
			MainMemory mainMemory=currentCache.mainMemory;
			MemoryWord[] words= mainMemory.ReadBlock(dataword.physicalAddress);
			int k=mainMemory.GetWordLocationInBlock(dataword.physicalAddress)/this.wordsPerBlock;
			k*=this.wordsPerBlock;
			for(int i=k; i<k+this.wordsPerBlock;i++)
			{
				data[i-k]=words[k];
			}
		}
		
	    data[this.GetWordLocationInBlock(dataword.physicalAddress)]=dataword;
		return this.WriteBlock(dataword.physicalAddress, data);	
	}
		
}
