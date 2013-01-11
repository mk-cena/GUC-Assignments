import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class Output {
	String filename;
	ArrayList<String> outputs;
	public Output(String filename, ArrayList<String>outputs)
	{
		this.filename=filename;
		this.outputs=outputs;
	}
	public void Write()
	{
		try{
			File file=new File("Outputs.txt");
			if(!file.exists())
				file.createNewFile();
			FileWriter fw=new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw=new BufferedWriter(fw);
			while(!this.outputs.isEmpty())
				bw.write(this.outputs.remove(0)+'\n');
			bw.close();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	public static void main(String[] args) {
		ArrayList<String> outputs=new ArrayList<String>();
		outputs.add("outs");
		outputs.add("cena");
		outputs.add("cena");
		Output out= new Output("Outputs",outputs);
		out.Write();

	}

}
