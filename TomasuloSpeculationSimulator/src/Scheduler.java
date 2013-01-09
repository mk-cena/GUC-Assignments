
public class Scheduler {
	int clock;
	
	RegisterFile regFile;
	Memory memory;
	
	InstructionQueue instructionBuffer;
	TimeRecord timeTable;
	ReservationStations rs;
	RegisterStatus registerStatus;
	ReOrderBuffer rob;
	
	
	boolean fetching=false;
	int fetchingLatency;
	public Scheduler(int startAddress)
	{
		fetchingLatency=0;
		regFile=new RegisterFile(startAddress);
		
	}
	
	public void Run()
	{
		while(true)
		{
			Issue();
			
			Execute();
			WriteBack();
			Commit();
			clock++;
		}
	}
	
	public void Issue()
	{
		InstructionWord nextInstruction=instructionBuffer.instructionQueue.remove(0);
		if(!fetching&&
		   nextInstruction!=null&&
		   nextInstruction.physicalAddress==regFile.PC&&
		  !rob.rob.IsFull()&&
		  !rs.IsReservationStationFull(nextInstruction))
		{
			ReservationStationsEntry e=new ReservationStationsEntry(null,null,-1,-1,-1,-1,-1,-1);
			//All instruction use RS
			if(this.registerStatus.registerStatus[nextInstruction.Rs]!=-1)
			{
				int h=this.registerStatus.registerStatus[nextInstruction.Rs];
				ReOrderBufferEntry robe=((ReOrderBufferEntry)rob.rob.object[h]);
				if(robe.ready)
				{
					e.Qj=0;
					e.Vj=robe.value;
				}
				else
					e.Qj=h;
			}
			else
			{
				e.Vj=this.regFile.registers[nextInstruction.Rs];
				e.Qj=0;
			}
			e.Dest=rob.rob.tail;
			e.SetOp(nextInstruction,rs.Load.size()+1,rs.Store.size()+1,rs.Add.size()+1,rs.Mul.size()+1);
			rs.Insert(e);
			int antiPredict=-1;
			if(nextInstruction.type.equals("BEQ"))
			{
				if(nextInstruction.imm<0)
					antiPredict=this.regFile.PC+2;
				else
					antiPredict=this.regFile.PC+2+nextInstruction.imm;
			}
			
			ReOrderBufferEntry reOrderBufferEntry=new ReOrderBufferEntry(nextInstruction,antiPredict);
			reOrderBufferEntry.timeRecordEntry=new TimeRecordEntry(nextInstruction);
			reOrderBufferEntry.timeRecordEntry.issued=this.clock+1;
			rob.EnQueue(reOrderBufferEntry);
			
			//FP,SW and BEQ uses Rt
			if(nextInstruction.type.equals("SW")||
			   nextInstruction.type.equals("ADD")||
			   nextInstruction.type.equals("ADDI")||
			   nextInstruction.type.equals("MUL")||
			   nextInstruction.type.equals("DIV")||
			   nextInstruction.type.equals("BEQ")||
			   nextInstruction.type.equals("NAND"))
			{
				if(this.registerStatus.registerStatus[nextInstruction.Rt]!=-1)
				{
					int h=this.registerStatus.registerStatus[nextInstruction.Rt];
					ReOrderBufferEntry robe=((ReOrderBufferEntry)rob.rob.object[h]);
					if(robe.ready)
					{
						e.Qk=0;
						e.Vk=robe.value;
					}
					else
						e.Qk=h;
				}
				else
				{
					e.Vk=this.regFile.registers[nextInstruction.Rt];
					e.Qk=0;
				}
			}
			
			
			//FP,LW and JALR uses RD
			if(nextInstruction.type.equals("LW")||
			   nextInstruction.type.equals("ADD")||
			   nextInstruction.type.equals("ADDI")||
			   nextInstruction.type.equals("MUL")||
			   nextInstruction.type.equals("DIV")||
			   nextInstruction.type.equals("BEQ")||
			   nextInstruction.type.equals("NAND")||
			   nextInstruction.type.equals("JALR"))
			{
				this.registerStatus.registerStatus[nextInstruction.Rd]=e.Dest;
			}
			
			if(nextInstruction.type.equals("LW")||
			   nextInstruction.type.equals("SW"))
			{
				e.A=nextInstruction.imm;
			}
			e.remainingCycles=nextInstruction.latency;
			return;
		}
		
		if(!fetching&&
		  nextInstruction==null||
		  nextInstruction.physicalAddress!=regFile.PC)
		{
			fetchingLatency=this.instructionBuffer.Fetch(regFile.PC, memory);
			fetching=true;
			return;
		}
		
		if(fetching)
		{
			fetchingLatency--;
			return;
		}
		
		if(fetchingLatency==0)
			fetching=false;
	}
	
	public void Execute()
	{
		
	}
	
	public void WriteBack()
	{
		
	}
	public void Commit()
	{
		
	}

}
