
public class TimeRecordEntry {
	InstructionWord instruction;
	int issued;
	int executed;
	int writeback;
	int committed;
	public TimeRecordEntry(InstructionWord instruction)
	{
		this.instruction=instruction;
	}
}
