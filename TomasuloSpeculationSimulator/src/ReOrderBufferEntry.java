public class ReOrderBufferEntry {
	InstructionWord instruction;
	int dest;
	int value;
	boolean ready;
	TimeRecordEntry timeRecordEntry;
	
	public ReOrderBufferEntry(InstructionWord instruction, int antiPredict)
	{
		this.instruction=instruction;
		this.ready=false;
		this.SetDest(antiPredict);
	}
	public void InsertValue(int value)
	{
		this.ready=true;
		this.value=value;
	}
	public void SetDest(int antiPredict)
	{
		String type=this.instruction.type;
		if(type.equals("LW")||
		   type.equals("ADD")||
		   type.equals("NAND")||
		   type.equals("ADDI")||
		   type.equals("MUL")||
		   type.equals("DIV")||
		   type.equals("JALR"))
			this.dest=this.instruction.Rd;
		if(type.equals("SW"))
			this.dest=this.instruction.imm+this.instruction.Rs;
		if(type.equals("BEQ"))
			this.dest=antiPredict;
	}
	
}
