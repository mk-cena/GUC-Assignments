
public class RegisterFile {
	int PC;
	int[] registers;
	final int R0=0;
	
	public RegisterFile(int PC)
	{
		this.PC=PC;
		registers=new int[8];
	}
}
