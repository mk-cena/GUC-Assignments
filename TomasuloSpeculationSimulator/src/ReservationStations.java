import java.util.ArrayList;
public class ReservationStations {
	ArrayList <ReservationStationsEntry>Load;
	ArrayList <ReservationStationsEntry>Store;
	ArrayList <ReservationStationsEntry>Add;
	ArrayList <ReservationStationsEntry>Mul;
	int load;
	int store;
	int add;
	int mul;
	public ReservationStations(int load, int store, int add, int mul)
	{
		this.load=load;
		this.store=store;
		this.add=add;
		this.mul=mul;
		Load=new ArrayList<ReservationStationsEntry>(load);
		Store=new ArrayList<ReservationStationsEntry>(store);
		Add=new ArrayList<ReservationStationsEntry>(add);
		Mul=new ArrayList<ReservationStationsEntry>(mul);	
	}
	
	public boolean IsReservationStationFull(InstructionWord instruction)
	{
		if(instruction.type.equals("LW"))
			return IsLoadFull();
		if(instruction.type.equals("SW"))
			return IsStoreFull();
		if(instruction.type.equals("ADD")||
		   instruction.type.equals("ADDI")||
		   instruction.type.equals("NAND")||
		   instruction.type.equals("JALR")||
		   instruction.type.equals("RET")||
		   instruction.type.equals("JMP")||
		   instruction.type.equals("BEQ"))
			return IsAddFull();
		if(instruction.type.equals("MUL")||
		   instruction.type.equals("DIV"))
			return IsMulFull();
		
		return true;
	}
	public boolean IsLoadFull()
	{
		return Load.size()==this.load;
	}
	public boolean IsStoreFull()
	{
		return Store.size()==this.store;
	}
	public boolean IsAddFull()
	{
		return Add.size()==this.add;
	}
	public boolean IsMulFull()
	{
		return Mul.size()==this.mul;
	}
	public void Insert(ReservationStationsEntry e)
	{
		if(e.Op.equals("LW"))
			Load.add(e);
		if(e.Op.equals("SW"))
			Store.add(e);
		if(e.Op.equals("ADD")||
		   e.Op.equals("ADDI")||
		   e.Op.equals("NAND")||
		   e.Op.equals("JALR")||
		   e.Op.equals("RET")||
		   e.Op.equals("JMP")||
		   e.Op.equals("BEQ"))
			Add.add(e);
		if(e.Op.equals("MUL")||
		   e.Op.equals("DIV"))
			Mul.add(e);
	}
}
