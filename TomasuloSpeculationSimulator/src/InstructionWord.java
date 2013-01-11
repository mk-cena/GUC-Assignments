public class InstructionWord extends MemoryWord{
	int latency;
	String type;
	static final String LW="LW";
	static final String SW="SW";
	
	static final String BEQ="BEQ";
	
	static final String JMP="JMP";
	static final String JALR="JALR";
	static final String RET="RET";
	
	static final String ADD="ADD";
	static final String ADDI="ADDI";
	static final String NAND="NAND";
	static final String MUL="MUL";
	static final String DIV="DIV";
	int Rs;
	int Rt;
	int Rd;
	int imm;
	public InstructionWord(int address,int latency, String type)
	{
		super(address);
		this.latency=latency;
		this.type=type;

	}
	//Load-Store
	public InstructionWord(int address,int latency, String type, int Rd, int Rs, int Rt, int imm)
	{
		this(address, latency, type);
		if(type.equals(LW)||
		   type.equals(ADDI))
		{
			this.Rd=Rd;
			this.Rs=Rs;
			this.imm=imm;
			return;
		}
		if(type.equals(SW)||
		   type.equals(BEQ))
		{
			this.Rt=Rt;
			this.Rs=Rs;
			this.imm=imm;
			return;
		}
		if(type.equals(ADD)||
		   type.equals(MUL)||
		   type.equals(DIV)||
		   type.equals(NAND))
		{
			this.Rd=Rd;
			this.Rt=Rt;
			this.Rs=Rs;
			return;
		}
		if(type.equals(JMP))
		{
			this.imm=imm;
			return;
		}
		if(type.equals(JALR))
		{
			this.imm=imm;
			return;
		}
	}
}
