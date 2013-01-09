public class PiggyBank {
	static String dp[][];
	public static String DP(int [] money, int m,int q, int i)
	{
		if(q>m || q<0)
			return "-1,;";
		
		if(i==money.length)
			return q+",;";
		else
			if(dp[q][i]!=null)
				return dp[q][i];
			else
			{
				String[]split1=DP(money, m, q+money[i], i+1).split(",");
				String[]split2=DP(money, m, q-money[i], i+1).split(",");
				int a=Integer.parseInt(split1[0]);
				int b=Integer.parseInt(split2[0]);
				String max=(a>b)?(a==-1?a+",;":a+","+"+"+split1[1]):(b==-1?b+",;":b+","+"-"+split2[1]);
				dp[q][i]=max;
				return max;
			}
	}
	public static String getMaxAmount(int [] money,int m, int q)
	{
		dp=new String[1002][52];
		String max=DP(money,m,q,0);
		return max.substring(0, max.length()-1);
	}
	
	public static void main(String[] args) {
		int money [] = {15,2,9,10};
		int q=8;
		int m=20;
		System.out.println(getMaxAmount(money,m,q));
	}
}