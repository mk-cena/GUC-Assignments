import java.util.ArrayList;


public class Main {
	public static int PlaceCodeAndData(ArrayList<String>program,Memory memory,int [] latencies)
	{
		program.remove(0);
		int Address=Integer.parseInt(program.remove(0).split(":")[1]);
		int firstInstructionAddress=Address;
		boolean data=false;
		while(!program.isEmpty())
		{
			
			if(program.get(0).equals("#Data"))
			{
				data=true;
				program.remove(0);
				program.remove(0);
			}
			if(!data)
			{
				String op[]=program.remove(0).split(" ");
				String instruction=op[0];
				if(instruction.equals("ADD")||
				   instruction.equals("MUL")||
				   instruction.equals("DIV")||
				   instruction.equals("NAND"))
				{
					int rd=Integer.parseInt(op[1].charAt(1)+"");
					int rs=Integer.parseInt(op[2].charAt(1)+"");
					int rt=Integer.parseInt(op[3].charAt(1)+"");
					int latency=-1;
					if(instruction.equals("ADD"))latency=latencies[0];
					if(instruction.equals("MUL"))latency=latencies[1];
					if(instruction.equals("DIV"))latency=latencies[2];
					if(instruction.equals("NAND"))latency=latencies[3];
					MemoryWord i=new InstructionWord(Address,latency,instruction,rd,rs,rt,-1);
					memory.mainMemoryI.Initialize(Address, i);
				}
				else
					if(instruction.equals("SW")||
					   instruction.equals("BEQ"))
					{
						int rs=Integer.parseInt(op[2].charAt(1)+"");
						int rt=Integer.parseInt(op[1].charAt(1)+"");
						int imm=Integer.parseInt(op[3]);
						int latency=-1;
						if(instruction.equals("BEQ"))
							latency=latencies[10];
						MemoryWord i=new InstructionWord(Address,latency,instruction,-1,rs,rt,imm);
						memory.mainMemoryI.Initialize(Address, i);
					}
					else
						if(instruction.equals("LW")||
						   instruction.equals("ADDI"))
						{
							int rs=Integer.parseInt(op[2].charAt(1)+"");
							int rd=Integer.parseInt(op[1].charAt(1)+"");
							int imm=Integer.parseInt(op[3]);
							int latency=-1;
							if(instruction.equals("ADDI"))
								latency=latencies[4];
							MemoryWord i=new InstructionWord(Address,latency,instruction,rd,rs,-1,imm);
							memory.mainMemoryI.Initialize(Address, i);
						}
						else
							if(instruction.equals("RET")||
							   instruction.equals("END"))
							{
								MemoryWord i=new InstructionWord(Address,latencies[7],instruction);
								memory.mainMemoryI.Initialize(Address, i);
							}
							else
								if(instruction.equals("JMP")||
								   instruction.equals("JALR"))
								{
									int imm=Integer.parseInt(op[1]);
									int latency=-1;
									if(instruction.equals("JMP"))
										latency=latencies[8];
									if(instruction.equals("JALR"))
										latency=latencies[9];
									MemoryWord i=new InstructionWord(Address,latency,instruction,-1,-1,-1,imm);
									memory.mainMemoryI.Initialize(Address, i);
								}
						
						
				Address+=2;
			}
			else
			{
				String[] word=program.remove(0).split(":");
				int address=Integer.parseInt(word[0]);
				int value=Integer.parseInt(word[1]);
				MemoryWord d=new DataWord(address,value);
				memory.mainMemoryD.Initialize(address,d);
			}
			
		}
		return firstInstructionAddress;
	}
	public static Memory OrganizeMemory(ArrayList<String>memoryData)
	{
		 int numberOfDCaches=0;
	     int allCacheDGeometry[][]=null;
	     int mainMemoryLatencyD=0;
	     int mainMemorySizeD=0;
	     int mainMemoryBlockSizeD=0;
	     int numberOfICaches=0;
	     int allCacheIGeometry[][]=null;
	     int mainMemoryLatencyI=0;
	     int mainMemorySizeI=0;
	     int mainMemoryBlockSizeI=0;
	     
	     memoryData.remove(0);
	     String s[]=memoryData.remove(0).split(":");
	     numberOfICaches=Integer.parseInt(s[1]);
	     allCacheIGeometry=new int[numberOfICaches][6];
	     memoryData.remove(0);
	     memoryData.remove(0);
	     
	     for(int i =0; i<numberOfICaches;i++)
	     {
	    	 String geometry[]=memoryData.remove(0).split(":");
	    	 allCacheIGeometry[i][0]=Integer.parseInt(geometry[5]);
	    	 allCacheIGeometry[i][1]=Integer.parseInt(geometry[1]);
	    	 allCacheIGeometry[i][2]=Integer.parseInt(geometry[2]);
	    	 allCacheIGeometry[i][3]=Integer.parseInt(geometry[3]);
	    	 allCacheIGeometry[i][4]=Integer.parseInt(geometry[4]);
	    	 allCacheIGeometry[i][5]=Integer.parseInt(geometry[5]);
	     }
	     
	     memoryData.remove(0);
	     memoryData.remove(0);
	 
	     s=memoryData.remove(0).split(":");
	     mainMemorySizeI=Integer.parseInt(s[0]);
	     mainMemoryBlockSizeI=Integer.parseInt(s[1]);
	     mainMemoryLatencyI=Integer.parseInt(s[2]);
	     
	     
	     memoryData.remove(0);
	     numberOfDCaches=Integer.parseInt(memoryData.remove(0).split(":")[1]);
	     allCacheDGeometry=new int[numberOfDCaches][6];
	     memoryData.remove(0);
	     memoryData.remove(0);
	     
	     for(int i =0; i<numberOfDCaches;i++)
	     {
	    	 String geometry[]=memoryData.remove(0).split(":");
	    	 allCacheDGeometry[i][0]=Integer.parseInt(geometry[5]);
	    	 allCacheDGeometry[i][1]=Integer.parseInt(geometry[1]);
	    	 allCacheDGeometry[i][2]=Integer.parseInt(geometry[2]);
	    	 allCacheDGeometry[i][3]=Integer.parseInt(geometry[3]);
	    	 allCacheDGeometry[i][4]=Integer.parseInt(geometry[4]);
	    	 allCacheDGeometry[i][5]=Integer.parseInt(geometry[5]);
	     }
	     
	     memoryData.remove(0);
	     memoryData.remove(0);
	     
	     s=memoryData.remove(0).split(":");
	     mainMemorySizeD=Integer.parseInt(s[0]);
	     mainMemoryBlockSizeD=Integer.parseInt(s[1]);
	     mainMemoryLatencyD=Integer.parseInt(s[2]);
	     
	     return new Memory(numberOfDCaches,
	    		 		   allCacheDGeometry,
	    		 		   mainMemoryLatencyD,
	    		 		   mainMemorySizeD,
	    		 	 	   mainMemoryBlockSizeD,
	    		 		   numberOfICaches,
	    		 		   allCacheIGeometry,
	    		 		   mainMemoryLatencyI,
	    		 		   mainMemorySizeI,
	    		 	 	   mainMemoryBlockSizeI);
	}
	public static int[] HardwareOrganize(ArrayList<String>hardwareData)
	{
		int[] hardwareOrganization=new int[17];
		hardwareData.remove(0);
		for(int i=0; i<11;i++)
		{
			hardwareOrganization[i]=Integer.parseInt(hardwareData.remove(0).split(":")[1]);
		}
		hardwareData.remove(0);
		hardwareOrganization[11]=Integer.parseInt(hardwareData.remove(0).split(":")[1]);
		hardwareOrganization[12]=Integer.parseInt(hardwareData.remove(0).split(":")[1]);
		hardwareData.remove(0);
		hardwareData.remove(0);
		String[] rs=hardwareData.remove(0).split(":");
		hardwareOrganization[13]=Integer.parseInt(rs[0]);
		hardwareOrganization[14]=Integer.parseInt(rs[1]);
		hardwareOrganization[15]=Integer.parseInt(rs[2]);
		hardwareOrganization[16]=Integer.parseInt(rs[3]);
		return hardwareOrganization;
	}
	
	public static void main(String[] args) {
		
		//Organize Memory
		Input memoryOrganization=new Input("MemoryOrganization.txt");
		ArrayList<String>memoryData=memoryOrganization.Read();
		Memory memory=OrganizeMemory(memoryData);
		
		
		
		//Organize Hardware
		Input hardwareOrganization=new Input("HardwareOrganization.txt");
		ArrayList<String>hardwareData=hardwareOrganization.Read();
		int[] HWO=HardwareOrganize(hardwareData);
		int latencies[]=new int[11];
		for(int i=0;i<11;i++)
			latencies[i]=HWO[i];
		
		
		
		//Read Code and Data into memory
		Input code=new Input("Code.txt");
		ArrayList<String>program=code.Read();
		int firstAddress=PlaceCodeAndData(program,memory,latencies);
		
		
		
		
		//Run
		Scheduler tomasulo=new Scheduler(firstAddress,memory,HWO[11],HWO[12],HWO[13],HWO[14],HWO[15],HWO[16]);
		tomasulo.Run();
		
		
		//Output
		ArrayList<String>outputs=new ArrayList<String>();
		TimeRecord timeTable=tomasulo.timeTable;
		RegisterFile regFile=tomasulo.regFile;
		int totalCycles=timeTable.timeRecords.get(timeTable.timeRecords.size()-2).committed;
		int totalInstructions=timeTable.timeRecords.size()-1;
		
		
		outputs.add("################################################Outputs##############################################");
		//IPC
		outputs.add("\n");
		outputs.add( "IPC: "+totalInstructions+"/"+totalCycles);
		outputs.add("\n");
		
		//Registers
		outputs.add("Registers Content:-");
		for(int i=1; i<regFile.registers.length;i++)
			outputs.add("R"+i+": "+regFile.registers[i]);
		
		
		//Time Table
		outputs.add("\n");
		outputs.add("Time Table:-");
        outputs.add("Instruction-----------Issued-----------Executed-----------WriteBack-----------Committed");
        while(!timeTable.timeRecords.isEmpty())
        {
        	
        	TimeRecordEntry e=timeTable.timeRecords.remove(0);
        	String op=e.instruction.type;
        	int issued=e.issued;
        	int exec=e.executed;
        	int wb=e.writeback;
        	int com=e.committed;
        	outputs.add(op +"       "+"           "+"   "+issued+"           "+"    "+exec+"           "+"     "+wb+"            "+"     "+com);
        }
        
        //ICache Hit
        outputs.add("\n");
        outputs.add("ICache Hit Ratio:-");
        Cache currentCache=memory.InstructionCache;
        int i=0;
        while(currentCache!=null)
        {
        	outputs.add("Level"+(i++)+": "+currentCache.hits+"/"+currentCache.references);
        	currentCache=currentCache.nextLevel;
        }
        	
        
        //DCache Hit
        outputs.add("\n");
        outputs.add("DCache Hit Ratio:-");
        currentCache=memory.DataCache;
        i=0;
        while(currentCache!=null)
        {
        	outputs.add("Level"+(i++)+": "+currentCache.hits+"/"+currentCache.references);
        	currentCache=currentCache.nextLevel;
        }
        	
        
		Output out=new Output("Outputs.txt",outputs);
		out.Write();
	}
}