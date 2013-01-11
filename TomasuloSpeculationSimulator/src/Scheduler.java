public class Scheduler {
	int clock;
	
	RegisterFile regFile;
	Memory memory;
	
	InstructionQueue instructionBuffer;
	TimeRecord timeTable;
	ReservationStations rs;
	RegisterStatus registerStatus;
	ReOrderBuffer rob;
	
	
	boolean fetching;
	int fetchingLatency;
	boolean END;
	boolean stopIssuing;
	public Scheduler(int startAddress, Memory memory, int IBSize, int ROBSize, int RSLw, int RSSw, int RSAdd, int RSMul)
	{
		clock=0;
		stopIssuing=false;
		fetching=true;
		regFile=new RegisterFile(startAddress);
		this.memory=memory;
		this.instructionBuffer=new InstructionQueue(IBSize);
		timeTable=new TimeRecord();
		this.registerStatus=new RegisterStatus();
		rob=new ReOrderBuffer(ROBSize);
		rs=new ReservationStations(RSLw,RSSw,RSAdd,RSMul);
		fetchingLatency=this.instructionBuffer.Fetch(regFile.PC, memory);
		END=false;
		
	}
	
	public void Run()
	{
		while(!END)
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
	
		if(stopIssuing)
			return;
		
		InstructionWord nextInstruction=null;
		if(!fetching&&instructionBuffer.instructionQueue.size()>0)
			nextInstruction=instructionBuffer.instructionQueue.get(0);
			
		
		if(!fetching&&
		   nextInstruction!=null&&
		   nextInstruction.physicalAddress==regFile.PC&&
		   !rob.rob.IsFull()&&
		   nextInstruction.type.equals("END"))
		{
			ReOrderBufferEntry end=new ReOrderBufferEntry(nextInstruction,0);
			end.ready=true;
			this.rob.EnQueue(end);
			instructionBuffer.instructionQueue.remove(0);
			stopIssuing=true;
			return;
		}
			
			
		if(!fetching&&
		   nextInstruction!=null&&
		   nextInstruction.physicalAddress==regFile.PC&&
		  !rob.rob.IsFull()&&
		  !rs.IsReservationStationFull(nextInstruction))
		{
			
			ReservationStationsEntry e=new ReservationStationsEntry(null,null,-1,-1,-1,-1,-1,-1);
			
			
			if(!nextInstruction.type.equals("JMP")&&
			   !nextInstruction.type.equals("RET")&&
			   !nextInstruction.type.equals("JALR"))
			{
				
					
				//All instruction use RS
				if(this.registerStatus.registerStatus[nextInstruction.Rs]!=-1&&!rob.rob.IsEmpty())
				{
					int h=this.registerStatus.registerStatus[nextInstruction.Rs];
					ReOrderBufferEntry robe=((ReOrderBufferEntry)rob.rob.object[h]);
					if(robe.ready)
					{
						e.Qj=-1;
						e.Vj=robe.value;
						
					}
					else
						e.Qj=h;
						
				}
				else
				{
					e.Vj=this.regFile.registers[nextInstruction.Rs];
					e.Qj=-1;
					
				}
				
				
			}
			
			
			//FP,SW and BEQ uses Rt
			if(nextInstruction.type.equals("SW")||
			   nextInstruction.type.equals("ADD")||
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
						e.Qk=-1;
						e.Vk=robe.value;
					}
					else
						e.Qk=h;
				}
				else
				{
					e.Vk=this.regFile.registers[nextInstruction.Rt];
					e.Qk=-1;
				}
				
			
			}
			
			e.Dest=rob.rob.tail;
			
			//FP,LW and JALR uses RD
			if(nextInstruction.type.equals("LW")||
			   nextInstruction.type.equals("ADD")||
			   nextInstruction.type.equals("ADDI")||
			   nextInstruction.type.equals("MUL")||
			   nextInstruction.type.equals("DIV")||
			   nextInstruction.type.equals("NAND"))
			{
				
				this.registerStatus.registerStatus[nextInstruction.Rd]=e.Dest;
			}
			
			
			//imm
			if(nextInstruction.type.equals("LW")||
			   nextInstruction.type.equals("SW")||
			   nextInstruction.type.equals("JALR")||
			   nextInstruction.type.equals("JMP")||
			   nextInstruction.type.equals("ADDI")||
			   nextInstruction.type.equals("BEQ"))
			{
				e.A=nextInstruction.imm;
				
			}
			
			
			
			e.SetOp(nextInstruction,rs.Load.size()+1,rs.Store.size()+1,rs.Add.size()+1,rs.Mul.size()+1);
			e.remainingCycles=nextInstruction.latency;
			if(nextInstruction.type.equals("LW")||nextInstruction.type.equals("SW"))
			{
				e.remainingCycles=-1;
			}
				
			rs.Insert(e);
			int antiPredict=-1;
			if(nextInstruction.type.equals("BEQ"))
			{
				
				if(nextInstruction.imm<0)
					antiPredict=this.regFile.PC+2;
				else
					antiPredict=this.regFile.PC+2+nextInstruction.imm;
			}
			if(nextInstruction.type.equals("JALR"))
				this.regFile.registers[7]=this.regFile.PC+2;
			
			
			ReOrderBufferEntry reOrderBufferEntry=new ReOrderBufferEntry(nextInstruction,antiPredict);
			reOrderBufferEntry.timeRecordEntry.issued=this.clock+1;
			rob.EnQueue(reOrderBufferEntry);
			this.regFile.UpdatePC(nextInstruction);
			instructionBuffer.instructionQueue.remove(0);
			
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
		
		if(fetching&&fetchingLatency==0)
			fetching=false;
		
		if(fetching&&fetchingLatency>0)
		{
			
			fetchingLatency--;
			return;
		}
		
	}
	
	public void Execute()
	{
		
		//MUL reservation stations checking
		for(int i=0; i<rs.Mul.size();i++)
			if(rs.Mul.get(i)!=null&&
			   rs.Mul.get(i).Qj==-1&&
			   rs.Mul.get(i).Qk==-1)
			{
				
				ReservationStationsEntry rse=rs.Mul.get(i);
				if(rse.remainingCycles==0)
				{
					
					int op1=rse.Vj;
					int op2=rse.Vk;
					rse.remainingCycles=-5;
					if(rse.Op.equals("MUL"))
						((ReOrderBufferEntry)this.rob.rob.object[rse.Dest]).value=op1*op2;
					else
						((ReOrderBufferEntry)this.rob.rob.object[rse.Dest]).value=op1/op2;
					((ReOrderBufferEntry)this.rob.rob.object[rse.Dest]).timeRecordEntry.executed=clock+1;
				}
				else
					if(rse.remainingCycles!=-6)
						rse.remainingCycles--;
					
					
			}
		
		//ADD reservation station checking
		for(int i=0; i<rs.Add.size();i++)
			if(rs.Add.get(i)!=null)
			{
				
				ReservationStationsEntry e=rs.Add.get(i);
				if((e.Op.equals("ADD")||
				   e.Op.equals("NAND")||
				   e.Op.equals("BEQ"))&&
				   e.Qj==-1&&
				   e.Qk==-1)
				{
					if(e.remainingCycles==0)
					{
						int op1=e.Vj;
						int op2=e.Vk;
						e.remainingCycles=-5;
						if(e.Op.equals("ADD"))
						{
							((ReOrderBufferEntry)this.rob.rob.object[e.Dest]).value=op1+op2;
							
						}
							
						else
							if(e.Op.equals("NAND"))
								((ReOrderBufferEntry)this.rob.rob.object[e.Dest]).value=(~(op1&op2));
							else
								if(e.Op.equals("BEQ"))
								{
									int diff=op1-op2;
									if(e.A<0&&diff==0)
										((ReOrderBufferEntry)this.rob.rob.object[e.Dest]).value=1;
									if(e.A<0&&diff!=0)
										((ReOrderBufferEntry)this.rob.rob.object[e.Dest]).value=0;
									if(e.A>0&&diff==0)
										((ReOrderBufferEntry)this.rob.rob.object[e.Dest]).value=0;
									if(e.A>0&&diff!=0)
										((ReOrderBufferEntry)this.rob.rob.object[e.Dest]).value=1;
								}
									
						((ReOrderBufferEntry)this.rob.rob.object[e.Dest]).timeRecordEntry.executed=clock+1;
					}
					else
						if(e.remainingCycles!=-6)
							e.remainingCycles--;
				}
				else
					if(e.Op.equals("RET")||e.Op.equals("JMP")||e.Op.equals("END")||e.Op.equals("JALR"))
					{
						if(e.remainingCycles==0)
						{
							e.remainingCycles=-5;
							((ReOrderBufferEntry)this.rob.rob.object[e.Dest]).timeRecordEntry.executed=clock+1;
						}
							
						else
							if(e.remainingCycles!=-6)
								e.remainingCycles--;
					}
					else
						if(e.Op.equals("ADDI")&&e.Qj==-1)
						{
							if(e.remainingCycles==0)
							{
								
								e.remainingCycles=-5;
								((ReOrderBufferEntry)this.rob.rob.object[e.Dest]).value=e.Vj+e.A;
								
								((ReOrderBufferEntry)this.rob.rob.object[e.Dest]).timeRecordEntry.executed=clock+1;
							}
							else
								if(e.remainingCycles!=-6)
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
			
			for(int j=0;j<this.rob.rob.object.length;j++)
			{
				
				store=(ReOrderBufferEntry)this.rob.rob.object[j];
				if(!this.rob.rob.IsEmpty()&&
				   store!=null&&
				   store.instruction.type.equals("SW")&&
				   store.timeRecordEntry.issued<load.timeRecordEntry.issued&&
				   store.timeRecordEntry.committed==-1)
					{
					
						storeFirst=true;
						break;
					}
			}
			int firstLoadDest=e.Vj+e.A;
	
			if((e!=null&&e.Qj==-1&&(!storeFirst||(storeFirst&&store.dest!=-1&&store.dest!=firstLoadDest&&e.remainingCycles==-1)))||(e!=null&&e.remainingCycles!=-1&&firstLoadDest!=-1))
			{
				
				
				if(e.remainingCycles==-1)
				{
					
					e.A+=e.Vj;
					
					MemoryWordTimeStamp mwts=this.memory.DataCache.ReadWord(e.A);
					((ReOrderBufferEntry)this.rob.rob.object[e.Dest]).value=((DataWord) mwts.words[0]).data;
					e.remainingCycles=mwts.latency;
					load.instruction.latency=mwts.latency;
					
					
				}
				else
					if(e.remainingCycles==0)
					{
						
						e.remainingCycles=-5;
						((ReOrderBufferEntry)this.rob.rob.object[e.Dest]).timeRecordEntry.executed=clock+1;
					}
					else
						if(e.remainingCycles!=-6)
							e.remainingCycles--;
				
						
			}
				
		}

		//Store reservation stations checking
		for(int i=0;i<this.rs.Store.size();i++)
		{
			
			ReservationStationsEntry e=this.rs.Store.get(i);
			
			if(e!=null&&e.Qj==-1)
			{
				
				if(e.remainingCycles==-1)
					e.remainingCycles=1;
				else
					if(e.remainingCycles==0)
					{
						
						e.A+=e.Vj;
						e.remainingCycles=-5;
						((ReOrderBufferEntry)(this.rob.rob.object[e.Dest])).timeRecordEntry.executed=clock+1;
						((ReOrderBufferEntry)(this.rob.rob.object[e.Dest])).dest=e.A;
					}
					else
					{
						
						if(e.remainingCycles!=-6)
							e.remainingCycles--;
					}
						
			}
		}
		
			
	}
	
	public void WriteBack()
	{
		
		if(CheckMulRS())
			return;
		if(CheckLoadRS())
			return;
		if(CheckAddRS())
			return;
		if(CheckStoreRS())
			return;
	}

	public void Commit()
	{
		ReOrderBufferEntry head=((ReOrderBufferEntry)rob.rob.GetHead());
		int wb=-1;
		if(head!=null)
			wb=head.timeRecordEntry.writeback;
		if(head!=null&&head.ready&&!this.rob.rob.IsEmpty()&&wb<clock)
		{
			
			
			String type=head.instruction.type;
			if(type.equals(InstructionWord.LW))
			{
				
				this.regFile.registers[head.dest]=head.value;
				
				head.timeRecordEntry.committed=clock;
				this.timeTable.timeRecords.add(head.timeRecordEntry);
				if(this.registerStatus.registerStatus[head.dest]==rob.rob.head)
					this.registerStatus.registerStatus[head.dest]=-1;
				rob.rob.DeQueue();
				return;
			}
			
			if(type.equals(InstructionWord.SW))
			{
				
					this.memory.DataCache.WriteWord(new DataWord(head.dest,head.value));
					head.timeRecordEntry.committed=clock;
					this.timeTable.timeRecords.add(head.timeRecordEntry);
					rob.rob.DeQueue();
				return;
				
			}
			
			if(type.equals(InstructionWord.ADD)||
			   type.equals(InstructionWord.ADDI)||
			   type.equals(InstructionWord.MUL)||
			   type.equals(InstructionWord.DIV)||
			   type.equals(InstructionWord.NAND))
			{
				
				this.regFile.registers[head.dest]=head.value;
				head.timeRecordEntry.committed=clock;
				this.timeTable.timeRecords.add(head.timeRecordEntry);
				if(this.registerStatus.registerStatus[head.dest]==rob.rob.head)
				{
					
					this.registerStatus.registerStatus[head.dest]=-1;
				}
					
	
		
				rob.rob.DeQueue();
				return;
			}
			
			if(type.equals(InstructionWord.BEQ))
			{
				
				head.timeRecordEntry.committed=clock;
				this.timeTable.timeRecords.add(head.timeRecordEntry);
				if(head.value==0)
				{
					this.regFile.PC=head.dest;
					this.rob.rob.Flush();
					this.instructionBuffer.instructionQueue.clear();
					this.rs.Add.clear();
					this.rs.Load.clear();
					this.rs.Store.clear();
					this.rs.Mul.clear();
					for(int i=0; i<this.registerStatus.registerStatus.length;i++)
						this.registerStatus.registerStatus[i]=-1;
					

				}
				else
				{
					rob.rob.DeQueue();
				}
				return;
			}
			else
			{
				
				head.timeRecordEntry.committed=clock;
				this.timeTable.timeRecords.add(head.timeRecordEntry);
				if(this.registerStatus.registerStatus[head.dest]==rob.rob.head)
					this.registerStatus.registerStatus[head.dest]=-1;
				rob.rob.DeQueue();
				
				if(head.instruction.type.equals("END"))
					END=true;
				
			}
			
			
		}
		

	}
	
	public boolean CheckMulRS()
	{
		for(int i=0; i<this.rs.Mul.size();i++)
		{
			
			ReservationStationsEntry e=this.rs.Mul.get(i);
			ReOrderBufferEntry re=((ReOrderBufferEntry)this.rob.rob.object[e.Dest]);
			int executed=re.timeRecordEntry.executed;
			if(e!=null&&re.timeRecordEntry.executed!=-1&&re.timeRecordEntry.writeback==-1&&e.remainingCycles==-6&&clock>executed)
			{
				for(int j=0;j<this.rs.Add.size();j++)
				{

					ReservationStationsEntry waiting=this.rs.Add.get(j);
					if(waiting!=null&&(waiting.Qj==e.Dest||waiting.Qk==e.Dest))
					{
						if(waiting.Qj==e.Dest)
						{
							waiting.Qj=-1;
							waiting.Vj=re.value;
						}
						if(waiting.Qk==e.Dest)
						{
							waiting.Qk=-1;
							waiting.Vk=re.value;
						}
					}
				}
				for(int j=0;j<this.rs.Mul.size();j++)
				{

					ReservationStationsEntry waiting=this.rs.Mul.get(j);
					if(waiting!=null&&(waiting.Qj==e.Dest||waiting.Qk==e.Dest))
					{
						if(waiting.Qj==e.Dest)
						{
							waiting.Qj=-1;
							waiting.Vj=re.value;
						}
						if(waiting.Qk==e.Dest)
						{
							waiting.Qk=-1;
							waiting.Vk=re.value;
						}
					}
				}
				
				for(int j=0;j<this.rs.Load.size();j++)
				{
					
					ReservationStationsEntry waiting=this.rs.Load.get(j);
					if(waiting!=null&&(waiting.Qj==e.Dest||waiting.Qk==e.Dest))
					{
						if(waiting.Qj==e.Dest)
						{
							waiting.Qj=-1;
							waiting.Vj=re.value;
						}
						if(waiting.Qk==e.Dest)
						{
							waiting.Qk=-1;
							waiting.Vk=re.value;
						}
					}
				}
				
				for(int j=0;j<this.rs.Store.size();j++)
				{
					
					ReservationStationsEntry waiting=this.rs.Store.get(j);
					if(waiting!=null&&(waiting.Qj==e.Dest||waiting.Qk==e.Dest))
					{
						if(waiting.Qj==e.Dest)
						{
							waiting.Qj=-1;
							waiting.Vj=re.value;
						}
						if(waiting.Qk==e.Dest)
						{
							waiting.Qk=-1;
							waiting.Vk=re.value;
						}
					}
				}
				
				re.ready=true;
				re.timeRecordEntry.writeback=clock;
				rs.Mul.remove(i);
				return true;
			}
		}
		return false;
	}
		
	public boolean CheckLoadRS()
	{			
		for(int i=0; i<this.rs.Load.size();i++)
		{
		
			ReservationStationsEntry e=this.rs.Load.get(i);
			ReOrderBufferEntry re=((ReOrderBufferEntry)this.rob.rob.object[e.Dest]);
			int executed=re.timeRecordEntry.executed;
			if(e!=null&&re.timeRecordEntry.executed!=-1&&re.timeRecordEntry.writeback==-1&&e.remainingCycles==-6&&clock>executed)
			{
				
				for(int j=0;j<this.rs.Add.size();j++)
				{
					
					ReservationStationsEntry waiting=this.rs.Add.get(j);
					if(waiting!=null&&(waiting.Qj==e.Dest||waiting.Qk==e.Dest))
					{
						
						if(waiting.Qj==e.Dest)
						{
							waiting.Qj=-1;
							waiting.Vj=re.value;
						}
						if(waiting.Qk==e.Dest)
						{
							waiting.Qk=-1;
							waiting.Vk=re.value;
						}
					}
				}
				for(int j=0;j<this.rs.Mul.size();j++)
				{

					ReservationStationsEntry waiting=this.rs.Mul.get(j);
					if(waiting!=null&&(waiting.Qj==e.Dest||waiting.Qk==e.Dest))
					{
						if(waiting.Qj==e.Dest)
						{
							waiting.Qj=-1;
							waiting.Vj=re.value;
						}
						if(waiting.Qk==e.Dest)
						{
							waiting.Qk=-1;
							waiting.Vk=re.value;
						}
					}
				}
				
				for(int j=0;j<this.rs.Load.size();j++)
				{
					ReservationStationsEntry waiting=this.rs.Load.get(j);
					if(waiting!=null&&(waiting.Qj==e.Dest||waiting.Qk==e.Dest))
					{
						
						if(waiting.Qj==e.Dest)
						{
							waiting.Qj=-1;
							waiting.Vj=re.value;
						}
						if(waiting.Qk==e.Dest)
						{
							waiting.Qk=-1;
							waiting.Vk=re.value;
						}
					}
				}
				
				for(int j=0;j<this.rs.Store.size();j++)
				{
					
					ReservationStationsEntry waiting=this.rs.Store.get(j);
					if(waiting!=null&&(waiting.Qj==e.Dest||waiting.Qk==e.Dest))
					{
						
						if(waiting.Qj==e.Dest)
						{
							waiting.Qj=-1;
							waiting.Vj=re.value;
						}
						if(waiting.Qk==e.Dest)
						{
							waiting.Qk=-1;
							waiting.Vk=re.value;
						}
					}
				}
				
				re.ready=true;
				re.timeRecordEntry.writeback=clock;
				rs.Load.remove(i);
				return true;
			}
		
		}
		return false;
	}

	public boolean CheckAddRS()
	{
		for(int i=0; i<this.rs.Add.size();i++)
		{
			
			ReservationStationsEntry e=this.rs.Add.get(i);
			ReOrderBufferEntry re=((ReOrderBufferEntry)this.rob.rob.object[e.Dest]);
			int executed=re.timeRecordEntry.executed;
			if(e!=null&&
			  re.timeRecordEntry.executed!=-1&&
			  re.timeRecordEntry.writeback==-1&&
			  e.remainingCycles==-6&&
			  clock>executed&&
			 (e.Op.equals("ADD")||
			  e.Op.equals("ADDI")||
			  e.Op.equals("NAND")||
			  e.Op.equals("JALR")))
			{
				for(int j=0;j<this.rs.Add.size();j++)
				{
					
					ReservationStationsEntry waiting=this.rs.Add.get(j);
					if(waiting!=null&&(waiting.Qj==e.Dest||waiting.Qk==e.Dest))
					{
						if(waiting.Qj==e.Dest)
						{
							waiting.Qj=-1;
							waiting.Vj=re.value;
						}
						if(waiting.Qk==e.Dest)
						{
							waiting.Qk=-1;
							waiting.Vk=re.value;
						}
					}
				}
				for(int j=0;j<this.rs.Mul.size();j++)
				{
					
					ReservationStationsEntry waiting=this.rs.Mul.get(j);
					if(waiting!=null&&(waiting.Qj==e.Dest||waiting.Qk==e.Dest))
					{
						if(waiting.Qj==e.Dest)
						{
							waiting.Qj=-1;
							waiting.Vj=re.value;
						}
						if(waiting.Qk==e.Dest)
						{
							waiting.Qk=-1;
							waiting.Vk=re.value;
						}
					}
				}
				
				for(int j=0;j<this.rs.Load.size();j++)
				{
					
					ReservationStationsEntry waiting=this.rs.Load.get(j);
					if(waiting!=null&&(waiting.Qj==e.Dest||waiting.Qk==e.Dest))
					{
						
						if(waiting.Qj==e.Dest)
						{
							
							waiting.Qj=-1;
							waiting.Vj=re.value;
							
						}
						if(waiting.Qk==e.Dest)
						{
							
							waiting.Qk=-1;
							waiting.Vk=re.value;
						}
					}
				}
				
				for(int j=0;j<this.rs.Store.size();j++)
				{
					
					ReservationStationsEntry waiting=this.rs.Store.get(j);
					if(waiting!=null&&(waiting.Qj==e.Dest||waiting.Qk==e.Dest))
					{
						if(waiting.Qj==e.Dest)
						{
							waiting.Qj=-1;
							waiting.Vj=re.value;
						}
						if(waiting.Qk==e.Dest)
						{
							waiting.Qk=-1;
							waiting.Vk=re.value;
						}
					}
				}
				
				re.ready=true;
				re.timeRecordEntry.writeback=clock;
				rs.Add.remove(i);
				return true;
			}
			else
				if(e!=null&&
				  re.timeRecordEntry.executed!=-1&&
			      re.timeRecordEntry.writeback==-1&&
			      e.remainingCycles==-6&&
			      clock>executed&&
				  (e.Op.equals("RET")||
				   e.Op.equals("JMP")||
				   e.Op.equals("BEQ")))
				{
					re.ready=true;
					re.timeRecordEntry.writeback=clock;
					rs.Add.remove(i);
					return true;
				}
			
		}
		
		return false;
	}

	public boolean CheckStoreRS()
	{
		for(int i=0; i<this.rs.Store.size();i++)
		{
			
			ReservationStationsEntry e=this.rs.Store.get(i);
			ReOrderBufferEntry re=((ReOrderBufferEntry)this.rob.rob.object[e.Dest]);
			int executed=re.timeRecordEntry.executed;
			if(e!=null&&
			  re.timeRecordEntry.executed!=-1&&
			  re.timeRecordEntry.writeback==-1&&
			  e.Qk==-1&&
			  clock>executed&&
			  e.remainingCycles==-6)
			{
				
				re.ready=true;
				re.timeRecordEntry.writeback=clock;
				rs.Store.remove(i);
				re.value=e.Vk;
				return true;
			}
		}
		return false;
	}
}
