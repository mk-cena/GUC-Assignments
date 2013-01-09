public class ReOrderBuffer {
	CircularQueue rob;
	Memory memroy;
	RegisterFile regFile;
	
	public ReOrderBuffer(int size, Memory memory, RegisterFile regs)
	{
		rob = new CircularQueue(size);
		this.regFile=regs;
		this.memroy=memory;
	}
	
	public boolean EnQueue(ReOrderBufferEntry e)
	{
		if(this.rob.IsFull())
			return false;
		else
			rob.EnQueue(e);
		
		return true;
	}
	
	public boolean Commit()
	{
		ReOrderBufferEntry head=((ReOrderBufferEntry)rob.GetHead());
		String type=head.instruction.type;
		if(head.ready)
		{
			if(type.equals(InstructionWord.LW))
			{
				this.regFile.registers[head.dest]=head.value;	
				return true;
			}
			if(type.equals(InstructionWord.SW))
			{
				this.memroy.DataCache.WriteWord(new DataWord(head.dest,head.value));
				return true;
			}
			if(type.equals(InstructionWord.ADD)||
			   type.equals(InstructionWord.ADDI)||
			   type.equals(InstructionWord.MUL)||
			   type.equals(InstructionWord.DIV)||
			   type.equals(InstructionWord.NAND))
			{
				this.regFile.registers[head.dest]=head.value;
				return true;
			}
			if(type.equals(InstructionWord.BEQ))
			{
				if(head.value==0)
				{
					this.regFile.PC=head.dest;
					this.rob.Flush();
				}
				return true;
			}
			
		}
		
		return false;
	}
}
