import java.util.ArrayList;
public class InstructionQueue {
	ArrayList<InstructionWord> instructionQueue;
	int size;
	
	public InstructionQueue(int size)
	{
		instructionQueue=new ArrayList<InstructionWord>();
		this.size=size;
	}
	
	public int Fetch(int pc, Memory memory)
	{
		if(instructionQueue.size()>0)
			instructionQueue.clear();
		int latency=0;
		while(this.instructionQueue.size()<this.size)
		{
			MemoryWordTimeStamp mwts=memory.InstructionCache.ReadWord(pc);
			if(mwts==null||mwts.words==null||mwts.words[0]==null)
				break;
			instructionQueue.add(((InstructionWord) mwts.words[0]));
			pc+=2;
			latency+=mwts.latency;
		}
		return latency;
		
	}
	

}
