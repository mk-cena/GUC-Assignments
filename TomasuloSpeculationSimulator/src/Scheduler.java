
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
		ReOrderBufferEntry head= (ReOrderBufferEntry) rob.rob.GetHead();
		while(!head.instruction.type.equals("END"))
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
		   nextInstruction.type.equals("END"))
		{
			ReOrderBufferEntry end=new ReOrderBufferEntry(nextInstruction,0);
			end.ready=true;
			this.rob.EnQueue(end);
			return;
		}
			
			
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
			this.regFile.UpdatePC(nextInstruction);
			return;
		}
		
		if(!fetching&&
		  (nextInstruction==null||
		  nextInstruction.physicalAddress!=regFile.PC))
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
		//MUL reservation stations checking
		for(int i=0; i<rs.Mul.size();i++)
			if(rs.Mul.get(i)!=null&&
			   rs.Mul.get(i).Qj==0&&
			   rs.Mul.get(i).Qk==0)
			{
				ReservationStationsEntry rse=rs.Mul.get(i);
				if(rse.remainingCycles==0)
				{
					int op1=this.regFile.registers[rse.Vj];
					int op2=this.regFile.registers[rse.Vk];
					if(rse.Op.equals("MUL"))
						((ReOrderBufferEntry)this.rob.rob.object[rse.Dest]).value=op1*op2;
					else
						((ReOrderBufferEntry)this.rob.rob.object[rse.Dest]).value=op1/op2;
					((ReOrderBufferEntry)this.rob.rob.object[rse.Dest]).timeRecordEntry.executed=clock;
				}
				else
					 rse.remainingCycles--;
					
			}
		
		//ADD reservation station checking
		for(int i=0; i<rs.Add.size();i++)
			if(rs.Add.get(i)!=null)
			{
				ReservationStationsEntry e=rs.Add.get(i);
				if((e.Op.equals("ADD")||
				   e.Op.equals("ADDI")||
				   e.Op.equals("NAND")||
				   e.Op.equals("BEQ"))&&
				   e.Qj==0&&
				   e.Qk==0)
				{
					if(e.remainingCycles==0)
					{
						int op1=this.regFile.registers[e.Vj];
						int op2=this.regFile.registers[e.Vk];
						
						if(e.Op.equals("ADD")||e.Op.equals("ADDI"))
							((ReOrderBufferEntry)this.rob.rob.object[e.Dest]).value=op1+op2;
						else
							if(e.Op.equals("NAND"))
								((ReOrderBufferEntry)this.rob.rob.object[e.Dest]).value=(~(op1&op2));
							else
								if(e.Op.equals("BEQ"))
									((ReOrderBufferEntry)this.rob.rob.object[e.Dest]).value=(op1-op2==0?1:0);
						((ReOrderBufferEntry)this.rob.rob.object[e.Dest]).timeRecordEntry.executed=clock;
					}
					else
						e.remainingCycles--;
				}
				else
					if(e.Op.equals("RET")||e.Op.equals("JMP")||e.Op.equals("END"))
					{
						if(e.remainingCycles==0)
							((ReOrderBufferEntry)this.rob.rob.object[e.Dest]).timeRecordEntry.executed=clock;
						else
							e.remainingCycles--;		
					}
					else
						if(e.Op.equals("JALR"))
						{
							if(e.remainingCycles==0)
							{
								
								((ReOrderBufferEntry)this.rob.rob.object[e.Dest]).value=this.regFile.PC+2;
								((ReOrderBufferEntry)this.rob.rob.object[e.Dest]).timeRecordEntry.executed=clock;
							}
							else
								e.remainingCycles--;
								
						}
			}
		//Load reservation stations checking
		for(int i=0;i<this.rs.Load.size();i++)
		{
			boolean storeFirst=false;
			ReservationStationsEntry e=this.rs.Load.get(i);
			ReOrderBufferEntry store=null;
			ReOrderBufferEntry load=(ReOrderBufferEntry)this.rob.rob.object[this.rs.Load.get(i).Dest];
			for(int j=0;j<this.rs.Store.size();j++)
			{
				ReservationStationsEntry temp=rs.Store.get(j);
				store=(ReOrderBufferEntry)this.rob.rob.object[temp.Dest];
				if(store!=null&&
				   store.timeRecordEntry.issued<load.timeRecordEntry.issued)
					{
						storeFirst=true;
						break;
					}
			}
			int firstLoadDest=this.regFile.registers[e.Vj]+e.A;
			if(e!=null&&e.Qj==0&&(!storeFirst||(storeFirst&&store.dest!=-1&&store.dest!=firstLoadDest)))
			{
				
				if(e.remainingCycles==-1)
				{
					e.A+=this.regFile.registers[e.Vj];
					MemoryWordTimeStamp mwts=this.memory.DataCache.ReadWord(e.A);
					((ReOrderBufferEntry)this.rob.rob.object[e.Dest]).value=((DataWord) mwts.words[0]).data;
					e.remainingCycles=mwts.latency;
					load.instruction.latency=mwts.latency;
				}
				else
					if(e.remainingCycles==0)
						((ReOrderBufferEntry)this.rob.rob.object[e.Dest]).timeRecordEntry.executed=clock;
					else
						e.remainingCycles--;
			}
				
		}
		
		//Store reservation stations checking
		for(int i=0;i<this.rs.Store.size();i++)
		{
			ReservationStationsEntry e=this.rs.Store.get(i);
			if(e!=null&&e.Qj==0)
			{
				if(e.remainingCycles==-1)
					e.remainingCycles=1;
				else
					if(e.remainingCycles==0)
					{
						e.A+=this.regFile.registers[e.Vj];
						((ReOrderBufferEntry)(this.rob.rob.object[e.Dest])).timeRecordEntry.executed=clock;
						((ReOrderBufferEntry)(this.rob.rob.object[e.Dest])).dest=e.A;
					}
					else
						e.remainingCycles--;
			}
		}
		
			
	}
	
	public void WriteBack()
	{
		
	}
	public void Commit()
	{
		
	}

}
