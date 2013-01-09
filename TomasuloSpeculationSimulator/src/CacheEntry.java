
public class CacheEntry {
	boolean valid;
	int tag;
	MemoryWord words[];
	boolean dirty;
	Cache cache;
	
	public CacheEntry(Cache cache, int tag, MemoryWord[]words)
	{
		dirty=false;
		this.valid=true;
		this.tag=tag;
		this.cache=cache;
		this.words=new MemoryWord[cache.wordsPerBlock];
		for(int i=0; i<words.length;i++)
			this.words[i]=words[i];
		
	}

}
