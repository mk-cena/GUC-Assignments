
public class PiggyBank {

	/**
	 * @param args
	 */
	static int dp[][];
	public static int DP(int [] money, int m,int q, int i)
	{
		if(q>m || q<0)
			return -1;
		
		if(i==money.length)
			return q;
		else
		{
			int m1,m2;
			
			
			if(q+money[i]<=m&&dp[q+money[i]][i+1]!=-2)
				m1=dp[q+money[i]][i+1];
			else
			{
				m1=DP(money, m, q+money[i], i+1);
				if(q+money[i]<=m)
					dp[q+money[i]][i+1]=m1;
			}
				
			
			
			if(q-money[i]>=0&&dp[q-money[i]][i+1]!=-2)
				m2=dp[q-money[i]][i+1];
			else
			{
				m2=DP(money, m, q-money[i], i+1);
				if(q-money[i]>=0)
					dp[q-money[i]][i+1]=m1;
			}
				
				
			
			
				return Math.max(m1, m2);
		}
			
	}
	
	public static int getMaxAmount(int [] money,int m, int q)
	{
		dp=new int[1000][52];
		for(int i=0;i<dp.length;i++)
			for(int j=0;j<dp[i].length;j++)
				dp[i][j]=-2;
		return DP(money,m,q,0);
	}
	
	public static void main(String[] args) {
		int money [] = {5,3,7};
		int q=5;
		int m=10;
		System.out.println(getMaxAmount(money,m,q));

	}

}
