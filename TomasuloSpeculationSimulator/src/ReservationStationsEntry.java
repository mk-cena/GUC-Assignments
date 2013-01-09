
public class ReservationStationsEntry {
	String name;
	boolean busy;
	String Op;
	int Vj;
	int Vk;
	int Qj;
	int Qk;
	int Dest;
	int A;
	int remainingCycles;
	
	public ReservationStationsEntry(String name,String Op,int Vj,int Vk,int Qj,int Qk,int Dest,int A)
	{
		this.name=name;
		this.busy=true;
		this.Op=Op;
		this.Vj=Vj;
		this.Vk=Vk;
		this.Qj=Qj;
		this.Qk=Qk;
		this.Dest=Dest;
		this.A=A;
	}
	public void SetOp(InstructionWord instruction,int lwnum, int swnum,int addnum,int mulnum)
	{
		this.Op=instruction.type;
		if(instruction.type.equals("LW"))
		{
			this.name="LOAD"+lwnum;
			return;
		}
		if(instruction.type.equals("SW"))
		{
			this.name="STORE"+swnum;
			return;
		}
		if(instruction.type.equals("ADD")||
		   instruction.type.equals("ADDI")||
		   instruction.type.equals("NAND")||
		   instruction.type.equals("JALR")||
		   instruction.type.equals("RET")||
		   instruction.type.equals("JMP"))
		{	
			this.name="ADD"+addnum;
		}
		if(instruction.type.equals("MUL")||
		   instruction.type.equals("DIV"))
		{
			this.name="MUL"+mulnum;
			return;
		}

	}
}
