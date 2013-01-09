
public class RegisterFile {
	int PC;
	int[] registers;
	final int R0=0;
	
	public RegisterFile(int PC)
	{
		this.PC=PC;
		registers=new int[8];
	}
	public void UpdatePC(InstructionWord instruction)
	{
		if(instruction.type.equals("BEQ")&&instruction.imm<0)
		{
			PC+=(2+instruction.imm);
			return;
		}
			
		if(instruction.type.equals("JMP"))
		{
			PC+=(2+instruction.imm+instruction.Rs);
			return;
		}
			
		if(instruction.type.equals("JALR")||instruction.type.equals("RET"))
		{
			PC=instruction.Rs;
			return;
		}
		PC+=2;
			
	}
}
