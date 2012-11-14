
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
			if(dp[q][i]!=-2)
				return dp[q][i];
			else
			{
				int max=Math.max(DP(money, m, q+money[i], i+1), DP(money, m, q-money[i], i+1));
				dp[q][i]=max;
				return max;
			}
	}
	
	public static int getMaxAmount(int [] money,int m, int q)
	{
		dp=new int[1002][52];
		for(int i=0;i<dp.length;i++)
			for(int j=0;j<dp[i].length;j++)
				dp[i][j]=-2;
		return DP(money,m,q,0);
	}
	
	public static void main(String[] args) {
		int money [] = {74,39,127,95,63,140,99,96,154,18,137,162,14,88};
		int q=40;
		int m=243;
		System.out.println(getMaxAmount(money,m,q));

	}

}
