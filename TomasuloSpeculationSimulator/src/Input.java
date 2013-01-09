import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;


public class Input {
	String filename;
	public Input(String filename)
	{
		this.filename=filename;
	}
	public ArrayList<String> Read()
	{
		ArrayList<String> inputs=new  ArrayList<String>();
		BufferedReader br=null;
		try{
			String line;
			br = new BufferedReader(new FileReader(this.filename));
			while((line=br.readLine())!=null)
				inputs.add(line);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			try{
				if(br!=null)
					br.close();
			}
			catch(IOException ioe)
			{
				ioe.printStackTrace();
			}		
		}
		return inputs;
	}
}