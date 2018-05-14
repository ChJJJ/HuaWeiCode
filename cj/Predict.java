package cj;






import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.IllegalFormatCodePointException;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;




public class Predict {

	public static String[] predictVm(String[] ecsContent, String[] inputContent)  {
		
		/*********虚拟机规格列表*************/
		String[] vm_size_total= {"","flavor1","flavor2","flavor3","flavor4","flavor5","flavor6","flavor7","flavor8"
				 , "flavor9","flavor10","flavor11","flavor12","flavor13","flavor14","flavor15"};
		int[] vm_cpu_total= {0,1,1,1,2,2,2,4,4,4,8,8,8,16,16,16};
		int[] vm_mem_total= {0,1024,2048,4096,2048,4096,8192,4096,8192,16384,8192,16384,32768,16384,32768,65536};		
		
		
		/***虚拟机的规格个数***/
		int vm_size_num=Integer.parseInt(inputContent[2]);//输入的虚拟机规格个数
		int[] vm_cpu=new int[vm_size_num];//虚拟机的cpu大小    2
		int[] vm_mem=new int[vm_size_num];//虚拟机的内存大小  3
		String[] vm_size=new String[vm_size_num];//虚拟机的规格
		for(int i=3;i<3+vm_size_num;i++)
		{
			String[] str=inputContent[i].split(" ");
			vm_size[i-3]=str[0]; 
			vm_cpu[i-3]=Integer.parseInt(str[1]); 
			vm_mem[i-3]=Integer.parseInt(str[2]);	
		}
		int[] vm_predict_num=new int[vm_size_num];//预测的虚拟机数量   1
		
		
		 
			try {
				vm_predict_num=vm_combination(inputContent,ecsContent);
			} catch (ParseException e) {
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			}
		 
		
		
		 	
		 /**物理服务器的规格**/
			String[] str1=inputContent[0].split(" ");
			int server_cpu=Integer.parseInt(str1[0]); //物理器CPU大小
		    int server_mem=Integer.parseInt(str1[1])*1024; //物理器内存大小
		    int server_hd=Integer.parseInt(str1[2]); //物理器硬盘大小
			
		   String resource_name=inputContent[4+vm_size_num];//资源名称,CPU 还是MEM
		 
	    
	    /**********分配物理器放置虚拟机,多重背包问题 贪心算法*********/
		/********该方法没考虑另一个资源的限制***/
		 int optimal_value;//一个物理器装的最优值
		 int server_num = 0; //需要的物理器数量  4
		 int[] vm_conbination=new int[vm_size_num]; //虚拟机的组合
		 int iter=0;
		 List<List<Integer>> list=new ArrayList<List<Integer>>();//放置虚拟机全部组合
	
		 
		 
		 /*******声明***********/
		int total_cpu=0;
		 for(int i=0;i<vm_size_num;i++)
		 {
			 total_cpu=total_cpu+vm_predict_num[i]*vm_cpu[i]; //所有虚拟机的总CPU
			
		 }
		 
		 int[] vm_cpu_a=new int[vm_size_num+1];  //把CPU数组扩大一位,前一位为0{0,vm_cpu}
		 vm_cpu_a[0]=0;
		 for(int i=1;i<vm_size_num+1;i++)
		 {
			 vm_cpu_a[i]=vm_cpu[i-1];
		 }
		 
		 int[] vm_predict_num_a=new int[vm_size_num];
			for(int i=0;i<vm_size_num;i++)
			{
				vm_predict_num_a[i]=vm_predict_num[i];
			}
		 
		 int[] vm_mem_a=new int[vm_size_num+1];  //把CPU数组扩大一位,前一位为0{0,vm_cpu}
			 vm_mem_a[0]=0;
		 for(int i=1;i<vm_size_num+1;i++)
			 {
				 vm_mem_a[i]=vm_mem[i-1];
			 }
			
			
			
			
			 int total_mem=0;
			 for(int i=0;i<vm_size_num;i++)
			 {
				 total_mem=total_mem+vm_predict_num[i]*vm_mem[i]; //所有虚拟机的总mem
			 }
			
			
			
			/****CPU**/
			
		if(resource_name.equals("MEM")) {
			
					
		while(total_cpu>0)	
		{	
			
			int nCount=0;
			 int out[][]=new int[vm_size_num+1][server_mem+1];
			for(int i=0;i<=vm_size_num;i++)
			{
				out[i][0]=0;
			}
			for(int v=0;v<=server_mem;v++)
			{
				out[0][v]=0;
			
			}
			for(int i=1;i<=vm_size_num;i++)
			{
				for(int v=vm_mem_a[i];v<=server_mem;v++)
				{
					out[i][v]=0;
					nCount=Math.min(vm_predict_num_a[i-1], v/vm_mem_a[i]);//当前背包总量
					for(int k=0;k<=nCount;k++)
					{	int threshold;
						threshold=Math.max(out[i][v], out[i-1][v-k*vm_mem_a[i]]+k*vm_cpu_a[i]);
						if(threshold<=server_cpu)
							out[i][v]=threshold;
					
					}
					
				}
				
			}
		
			optimal_value=out[vm_size_num][server_mem];
	
			total_cpu=total_cpu-optimal_value;
			
			
			/*****虚拟机的组合*******/
				int j=vm_size_num-1;
				int y=server_mem;
				
				while(j>=0)
				{		
					int count=Math.min(vm_predict_num_a[j], y/vm_mem[j]); //调用的是vm_cpu 而不是vm_cpu_a,因为反推时不需要0
					for(int k=count;k>0;k--)
					{
						if(out[j+1][y]==(out[j][y-vm_mem[j]*k]+k*vm_cpu[j])) //这里的j为vm_size_num-1,所以要j+1
						{
							vm_conbination[j]=k;
							y=y-k*vm_mem[j];
							break;
						}
					}
					j--;
				}
			
				int x=0;
				int[] temp=new int[vm_size_num];
				for(int b=0;b<vm_size_num;b++)
				{
					temp[b]=vm_predict_num_a[b];
				}
				
				for(int m=0;m<vm_size_num;m++)
				{
					temp[m]=temp[m]-vm_conbination[m];
				}
				while(x<vm_size_num)
				{	
					if(vm_conbination[x]>=2)
					{
							for(int k=0;k<vm_size_num;k++)
							{
								if((vm_mem[k]/vm_mem[x])>=2  && ((double)vm_cpu[k]/vm_cpu[x])==((double)vm_mem[k]/vm_mem[x]))
								{	
									int nums=vm_mem[k]/vm_mem[x];
									
									int weight1=vm_conbination[x]/nums;
								if(weight1>=1) {
									for(int p=weight1;p>0;p--)
									{
										
										if(temp[k]>=p )
									{
										vm_conbination[x]=vm_conbination[x]-nums*p;
									vm_conbination[k]=vm_conbination[k]+p;
									temp[k]=temp[k]-p;
									temp[x]=temp[x]+nums*p;
									break;
									}
										}
										
									}
									
								}
							}
						
					}
					x++;
				}
				
				
				
				
				
				
				
		
				List<Integer> l=new ArrayList<Integer>();
				for(int i=0;i<vm_size_num;i++)
				{		
				
				
				
					vm_predict_num_a[i]=vm_predict_num_a[i]-vm_conbination[i];
					if(vm_predict_num_a[i]<0)
						vm_predict_num_a[i]=0;
					
				}
				
				
				
				int res_cpu=0;//定义目前在一个物理器内所有虚拟机的cpu总和
				for(int i=0;i<vm_size_num;i++)
				{
					res_cpu=res_cpu+vm_conbination[i]*vm_cpu[i];
				}
				
				
				
			
			while( y>0 && res_cpu<server_cpu) 
				{ 
						
					for(int size=vm_size_num-1;size>=0;size--)
						{
							int num_add=y/vm_mem[size];
						
							if(num_add>=1) 
								{	
									for(int n=num_add;n>=1;n--)
									{  
								
										if((res_cpu+n*vm_cpu[size])<=server_cpu)
										{	
											
											vm_predict_num[size]+=n;
											vm_conbination[size]+=n;
											y=y-n*vm_mem[size];
							
					
											res_cpu=res_cpu+n*vm_cpu[size];
								
											break;
										}
						
									}
						
								}
					
						}
				}
					
				
				
				
				for(int i=0;i<vm_size_num;i++) //放入虚拟机组合的种类
				{
					l.add(vm_conbination[i]);
					
				}
				
				
				
				
				
				
				
				
				
				
				
				for(int i=0;i<vm_size_num;i++) //把放入组合的数组初始化
				{
					vm_conbination[i]=0;
				}
				
				list.add(l);  //把组合放进list集合里
				
				iter++;
				
		}
	
		}
	
		
		/******MEM***/
		
		
		
		
		
		if(resource_name.equals("CPU")) {
			while(total_mem>0)
			{	int nCount=0;
				int out[][]=new int[vm_size_num+1][server_cpu+1];
				for(int i=0;i<=vm_size_num;i++)
			{
				out[i][0]=0;
			}
				for(int v=0;v<=server_cpu;v++)
			{
				out[0][ v]=0;
			}
				for(int i=1;i<=vm_size_num;i++)
			{
				for(int v=vm_cpu_a[i];v<=server_cpu;v++)
				{
					out[i][v]=0;
					nCount=Math.min(vm_predict_num_a[i-1], v/vm_cpu_a[i]);//当前背包总量
					for(int k=0;k<=nCount;k++)
					{
						
						int threshold;
			
						threshold=Math.max(out[i][v], out[i-1][(v-k*vm_cpu_a[i])]+k*vm_mem_a[i]);
						if(threshold<=server_mem)
							out[i][v]=threshold;
					
						
					}
				}
			}
				
				optimal_value=out[vm_size_num][server_cpu];
				total_mem=total_mem-optimal_value;
			
			
			/*****虚拟机的组合*******/
			int j=vm_size_num-1;
			int y=server_cpu; //物理器CPU大小
			while(j>=0)
			{
				int count=Math.min(vm_predict_num_a[j], y/vm_cpu[j]);
				for(int k=count;k>0;k--)
				{
					if(out[j+1][y]==(out[j][y-vm_cpu[j]*k]+k*vm_mem[j]))
					{
						vm_conbination[j]=k;
						y=y-k*vm_cpu[j];
						break;
					}
				}
				j--;
			}
			
				
			
			
			
			
			/*******对组合好的虚拟机再次进行优化,尽可能让大的虚拟机先放,小的虚拟机后放*******/
			int x=0;
			int[] temp=new int[vm_size_num];
			for(int b=0;b<vm_size_num;b++)
			{
				temp[b]=vm_predict_num_a[b];
			}
			
			for(int m=0;m<vm_size_num;m++)
			{
				temp[m]=temp[m]-vm_conbination[m];
			}
			while(x<vm_size_num)
			{	
				if(vm_conbination[x]>=2)
				{
						for(int k=0;k<vm_size_num;k++)
						{
							if((vm_cpu[k]/vm_cpu[x])>=2  && ((double)vm_cpu[k]/vm_cpu[x])==((double)vm_mem[k]/vm_mem[x]))
							{	
								int nums=vm_cpu[k]/vm_cpu[x];
								
								int weight1=vm_conbination[x]/nums;
							if(weight1>=1) {
								for(int p=weight1;p>0;p--)
								{
									
									if(temp[k]>=p )
								{
									vm_conbination[x]=vm_conbination[x]-nums*p;
								vm_conbination[k]=vm_conbination[k]+p;
								temp[k]=temp[k]-p;
								temp[x]=temp[x]+nums*p;
								break;
								}
									}
									
								}
								
							}
						}
					
				}
				x++;
			}
			
			
			
			List<Integer> l=new ArrayList<Integer>();
			for(int i=0;i<vm_size_num;i++)
			{
				
				vm_predict_num_a[i]=vm_predict_num_a[i]-vm_conbination[i];
				if(vm_predict_num_a[i]<0)
					vm_predict_num_a[i]=0;
			}
			
			
			
			
			
			
			
			
			
			int res_mem=0;//定义目前在一个物理器内所有虚拟机的mem总和
			for(int i=0;i<vm_size_num;i++)
			{
				res_mem=res_mem+vm_conbination[i]*vm_mem[i];
			}
			
			
			
			
		while( y>0 && res_mem<server_mem) 
			{ 
					
				for(int size=vm_size_num-1;size>=0;size--)
					{
						int num_add=y/vm_cpu[size];
					
						if(num_add>=1) 
							{	
								for(int n=num_add;n>=1;n--)
								{  
									
									if((res_mem+n*vm_mem[size])<=server_mem)
									{		
										
										vm_predict_num[size]+=n;
										vm_conbination[size]=vm_conbination[size]+n;
										
										y=y-n*vm_cpu[size];
						
				
										res_mem=res_mem+n*vm_mem[size];
							
										break;
									}
					
								}
					
							}
				
					}
			}
		
			
			/*
			for(int size=0;size<vm_size_num;size++)
			{
				vm_predict_num[size]+=extra_vm[size];
			}*/
			
			
			
			
			for(int i=0;i<vm_size_num;i++) //放入虚拟机组合的种类
			{
				l.add(vm_conbination[i]);
				
			}
			
			
		
			
			list.add(l);
		
			for(int i=0;i<vm_size_num;i++) //把放入组合的数组初始化
			{
				vm_conbination[i]=0;
			}
			
			
			iter++; //物理器的数量
			
		
			}
			
			
		}
	
		
		server_num=iter;//需要多少个物理器
		
		/*****虚拟机的组合*******/
		 
		
		
		/********输出字符串数组*****/
		int length=1+vm_size_num+2+server_num;//输出字符串的长度
		String[] fin_out=new String[length];
		int total=0;
		for(int i=0;i<vm_size_num;i++)
		{
			total=total+vm_predict_num[i];
		}
		fin_out[0]=String.valueOf(total);        //第一行表示虚拟机的总数  1
		
		
		for(int i=1;i<1+vm_size_num;i++)
		{	
			String s1=String.valueOf(vm_size[i-1]) ;
			String s2=String.valueOf(vm_predict_num[i-1]);
			fin_out[i]=s1+" "+s2;      //输出的虚拟机规格数量  2
		}
		
		fin_out[1+vm_size_num]=" ";  //空格 3
		
		fin_out[2+vm_size_num]=String.valueOf(server_num); //物理器的数量 4
		
		
		String[] vm_2=new String[server_num];
		for(int i=0;i<server_num;i++)
		{
			vm_2[i]="";
		}
		for(int i=0;i<server_num;i++)
		{
			List<Integer> list2=new ArrayList<Integer>();
			list2=list.get(i);
			String[] vm_1=new String[vm_size_num];
			for(int j=0;j<vm_size_num;j++)
			{	
				if(list2.get(j)!=0)
				{	
					String s=vm_size[j];
					String s1=String.valueOf(list2.get(j));
					vm_1[j]=s+" "+s1;
				}
			}
			for(int k=0;k<vm_size_num;k++)
			{
				if(vm_1[k]!=null)
				{	
				vm_2[i]=vm_2[i]+" "+vm_1[k];
				}
			
			}
			
		}
		
		
	
		
	
		for(int i=0;i<server_num;i++)
		{	
			
			
			fin_out[3+vm_size_num+i]=String.valueOf(i+1)+" "+vm_2[i];
		
		}
	
		
		
		
		return fin_out;
		
		
		
		
		
		
		
		
		
		 
		
	}
	
	/***********最优放置情况下虚拟机的组合***************/

	
	/*****从训练数据样本中得到某种规格虚拟机的分布情况
	 * @throws ParseException *****/
	
	public static int[] vm_number(String[] ecsContent,String vm_size) throws ParseException//某种虚拟机在一个月内的个数分布情况
	{	
		
		
		int number=0;
		int[] vm_num=new int[ecsContent.length];
		
		/*********计算训练的时间隔间**********/
		String[] str_begin=ecsContent[0].split("[\\t]");
		String beginday=str_begin[2];
		String[] str_end=ecsContent[ecsContent.length-1].split("[\\t]");
		String endday=str_end[2];
		SimpleDateFormat df1=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		Date begin=df1.parse(beginday);
		Date end=df1.parse(endday);
		long day;
		day=((end.getTime()-begin.getTime())/(24*60*60*1000));
	
		
		int[] martix=new int[(int) day];  //装入某种规格虚拟机的数组
		
		
		
		int k=0;
		for(int i=0;i<ecsContent.length;i++) //改为0
		{	
			String[] array = ecsContent[i].split("[\\t ]");
			String uuid = array[0];
			String flavorName = array[1];
			String createTime = array[2];
			
			
			if(flavorName.equals(vm_size))
			{
				number++;
			}
			for(int j=i+1;j<ecsContent.length;j++)
			{	String[] array1=ecsContent[j].split("[\\t ]");
			if(array1[2].equals(array[2]))
			{
				if(array1[1].equals(vm_size))
				{
					number++;
				}
			}
			else {
				martix[k]=number;
				k++;
				i=j-1;
				number=0;
				
				break;
			}
			}
		
		
		}
		
		
		return martix;
	}

	/************计算开始预测与训练集结束之间的间隔天数
	 * @throws ParseException *************/
	public static long dis_day(String beginday,String[] ecsContent) throws ParseException
	{	
		SimpleDateFormat df=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date begin=df.parse(beginday); //预测开始时的日期
		
		
		String[] str_end=ecsContent[ecsContent.length-1].split("[\\t]");
		String endday=str_end[2];
		Date end=df.parse(endday); //训练集结束时的日期
		
		
		long day;
		day=((begin.getTime()-end.getTime())/(24*60*60*1000));
	
		return day;
		
		
		
	}
	
	/********用于计算开始预测到预测结束的间隔天数
	 * @throws ParseException *************/
	public static long calculate_day(String beginday,String endday) throws ParseException //计算间隔天数
	{	
		long  day;
		SimpleDateFormat df=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date begin=df.parse(beginday);
		Date end=df.parse(endday);
		day=((end.getTime()-begin.getTime())/(24*60*60*1000));
		return day;
		
		
	}
	
	
	/**********计算出所有的虚拟机需求
	 * @throws ParseException *************/
	public static int[] vm_combination(String[] inputContent,String[] ecsContent) throws ParseException  
	{	
		/**物理服务器的规格**/
		String[] str1=inputContent[0].split(" ");
		int server_cpu=Integer.parseInt(str1[0]); //物理器CPU大小
	    long server_mem=Long.parseLong(str1[1]); //物理器内存大小
	    long server_hd=Long.parseLong(str1[2]); //物理器硬盘大小
		
		/***虚拟机的规格个数***/
		int vm_size_num=Integer.parseInt(inputContent[2]);
		
		/***虚拟机的各种规格放入字符数组中****/
		String[] vm_size=new String[vm_size_num];//虚拟机的规格
		
		
		long[] vm_cpu=new long[vm_size_num];//虚拟机的cpu大小   1
		long[] vm_mem=new long[vm_size_num];//虚拟机的内存大小 2
		int[] vm_predict_num=new int[vm_size_num];//预测的数量 
		String resource_name=inputContent[4+vm_size_num];//资源名称,CPU 还是MEM
		
		String beginday=inputContent[6+vm_size_num]; //预测开始的时间  
		String endday=inputContent[7+vm_size_num];//预测结束的时间
		
		//String beginday=inputContent[inputContent.length-2]; //预测开始的时间
		//String endday=inputContent[inputContent.length-1];//预测结束的时间
		
		
		long dif_day=dis_day(beginday, ecsContent); //开始预测与训练结束之间的间隔天数
		long interval_day=calculate_day(beginday, endday); //要预测的总天数
		
		
		
		for(int i=3;i<3+vm_size_num;i++)
		{
			String[] str=inputContent[i].split(" ");
			vm_size[i-3]=str[0];
			vm_cpu[i-3]=Long.parseLong(str[1]);
			vm_mem[i-3]=Long.parseLong(str[2]);	
		}
		for(int i=0;i<vm_size_num;i++)
		{
			int[] nums=vm_number(ecsContent, vm_size[i]);
			vm_predict_num[i]=vm_predict(nums,interval_day,dif_day); //预测虚拟机的数量 3
		}
		
		
		return vm_predict_num;
		
	}
	
	
	
	/*****指数平滑算法************/
	/*public static int vm_predict(int[] martix,long day, long interval)
	{
		int t=7;
		double time_interval=interval/t; //训练结束到开始的时间段
		double time_predict=(interval+day)/t; //预测的时间段
		
		
	
	
		
		*//*****计算有需求时的平均值********//*
		
		int sum1=0;
		int num1=0;
		for(int i=0;i<martix.length;i++)
		{
			if(martix[i]!=0)
				{
					sum1+=martix[i];
					num1++;
				}
		}
		double ave=0;
		if(num1!=0)
			ave=sum1/num1; //计算有需求时的平均值
		
		
		
		
	
		
		
		
		*//*******大于五倍平均值则为噪声,消除*********//*
		
		for(int i=0;i<martix.length/t;i++)
		{
			if(martix[i]>5*ave)
				martix[i]=(int) (martix[i]-ave);
			
			
			
		}
	
		
		
		
		
		
		
		
		
		
		List<Integer> vm_number=new ArrayList<Integer>();//时间间隔为t的数量增量
		List<Integer> temp_number=new ArrayList<Integer>();//时间t内的数量
		
		for(int i=0;i<martix.length/t;i++) //时间t内的数量
		{		int sum=0;
			for(int j=0;j<t;j++)
			{
					sum+=martix[t*i+j];
		
			}
			temp_number.add(sum);	
			
		}
		
		
		
		for(int i=0;i<temp_number.size();i++) //时间间隔为t的数量增量
			{
				if(i==0)
					vm_number.add(temp_number.get(i));
				else if((temp_number.get(i)-temp_number.get(i-1))<=0)
				{	
					vm_number.add(0);
				}
				else {
					
					int temp=temp_number.get(i)-temp_number.get(i-1);
					vm_number.add(temp);
				}

			}
		
		List<Double> S1=new ArrayList<Double>(); //一次指数平滑
		
		S1.add((double) ((temp_number.get(0)+ temp_number.get(1))/2));
		
		
		
		
		double a=0.6;//参数
		
		
		
		
		
		for(int i=1;i<vm_number.size();i++)
		{
			double temp1=a*temp_number.get(i)+(1-a)*S1.get(i-1);
			S1.add(temp1);
			
		}
		List<Double> S2=new ArrayList<Double>();//二次指数平滑
		S2.add(S1.get(0));
		
		for(int i=1;i<S1.size();i++)
		{
			double temp2=a*S1.get(i)+(1-a)*S2.get(i-1);
			S2.add(temp2);
		}
		
		double a_t,b_t;//二次指数中的两个参数
		
		a_t=2*S1.get(S1.size()-1)-S2.get(S2.size()-1);
		b_t=a/(1-a)*(S1.get(S1.size()-1)-S2.get(S2.size()-1));
		
		
		
		double delta;
		
		delta=(a_t+b_t*(time_predict));
		
		double result=0;
		
		for(int i=0;i<temp_number.size();i++)
		{
			result+=temp_number.get(i);
		}
	
		result=result+delta;
		 return (int) Math.ceil(result);

		
		
		
	}
	
	
	*/
	
	/***********加权线性回归***************/
	/****79分****/
/*	public static int vm_predict(int[] martix,long day,long interval)
	{
		int length=martix.length;
		List<Double> weight=new ArrayList<Double>();
		double[] pre_result=new double[(int) day];//预测每一天
		
		double total=0;
		
		for(int i=0;i<day;i++)
		{
			weight=get_weight(i+length);
			for(int k=0;k<length;k++)
			{
				pre_result[i]+=weight.get(k)*martix[k];
			}
			int x=0;
			while(x<i)
			{
				pre_result[i]+=weight.get(length+x)*pre_result[x];
				x++;
			}
			total+=pre_result[i];
		}
		
		return (int)Math.ceil(total);
	}
	
	
	
	
	public static List<Double> get_weight(int num)
	{
		List<Double> weight=new ArrayList<Double>();
		for(int i=0;i<num;i++)
		{
			//weight.add(1/(double)(1<<i));
			weight.add(Math.pow(1-Math.pow((double)(0.79),(double)2),2));
		}
		return weight;
	}
	*/
	
	
	/***局部加权******/
	/*public static int vm_predict(int[] mar, long day,long interval)
	{	
		
	
		
		
		int temp=1;
		

		List<Double> martix=new ArrayList<Double>();
		

		for(int i=0;i<mar.length/temp;i++)
		{	double temp_num=0;
				for(int j=0;j<temp;j++)
			{
					temp_num+=mar[temp*i+j];
		
			}
			martix.add(temp_num);	
			
			
		}
		int length=martix.size();
		*//*****计算有需求时的平均值********//*
		int sum=0;
		int num1=0;
		for(int i=0;i<length;i++)
		{
			if(martix.get(i)!=0)
				{
					sum+=martix.get(i);
					num1++;
				}
		}
		double ave=0;
		if(num1!=0)
			ave=sum/num1; //计算有需求时的平均值
		
		
		
		
		
		
		*//*******大于五倍平均值则为噪声,消除*********//*
		
		for(int i=0;i<mar.length/temp;i++)
		{
			if(martix.get(i)>5*ave)
				martix.add(i, martix.get(i)-ave);
		}
	
		
		
		

		
		int p=3; //参数
		double[][] X=new double[length][p+1]; //X矩阵
		for(int i=0;i<length;i++)
			for(int j=0;j<p+1;j++)	
		{
			X[i][j]=Math.pow(i+1, j);
			
			
		}
		
		double[][] transX=new double[p+1][length];//转置矩阵
		for(int i=0;i<p+1;i++)
			for(int j=0;j<length;j++)
		{
			transX[i][j]=X[j][i];
		}
		
		 
		
		double[] weight=new double[length];//权重
		
		
		
		double f=0.5;
		int r=(int) (f*martix.length);
	
		for(int i=martix.length-1;i>=0;i--)
		{	if(i>=martix.length-r)
			weight[i]=(Math.pow(1-Math.pow((double)(martix.length-i)/(martix.length),(double)5),8.6));
		else {
			weight[i]=0;
		}
		}
		
		
		
		double fin=0;
		double f=0.5;
		int r=(int) (f*length);
	double[] wi=new double[length];
	for(int t=0;t<length;t++)
		
	{	
		for(int i=0;i<length;i++)
		{	
			
			if(Math.abs(t-i)<=r/2)
			weight[i]=(Math.pow(1-Math.pow((double)(t-i)/r,(double)3),3));
			else
				weight[i]=0;
		}
		
		
		int number=3;
		double[][] alpha=new double[p+1][1];
			while(number>0)
			{
			double[][] W=new double[length][length];
			for(int i=0;i<length;i++)
			{
				for(int j=0;j<length;j++)
				{
					if(i==j)
					{	W[i][j]=weight[i];}
					else
					{
						W[i][j]=0;
					}
					
				}
			}
			
			double[][] Y=new double[length][1];//矩阵Y
			
			for(int i=0;i<length;i++)
			{
				Y[i][0]=martix.get(i);
			}
			
			
			//求解如下
			double[][] transX_W=new double[p+1][length];
			transX_W= multip(transX,W); //X'W
			double[][] transX_W_X=new double[p+1][p+1];
			transX_W_X=multip(transX_W, X);//X'WX
			
			 double[][]InMatrix=new double[p+1][p+1];
			 Inverse(transX_W_X,p,InMatrix); //(X'WX)^-1;
			 
			 double[][] transX_W_X_transX=new double[p+1][length];  
			 transX_W_X_transX=multip(InMatrix, transX); //X'WX)^-1*X'
			 
			 double[][] transX_W_X_transX_W=new double[p+1][length];  
			 transX_W_X_transX_W=multip(transX_W_X_transX, W); //X'WX)^-1*X'W
			 
			//double[][] alpha=new double[p+1][1];
			 alpha=multip(transX_W_X_transX_W, Y);//X'WX)^-1*X'WY
			 
			 
			 List<Double> list=new ArrayList<Double>();
			 double[] e=new double[length];
			 
			 *//*****强局部加权*********//*
		
			
			for(int i=0;i<length;i++)
			 {	double YI=0;
				 for(int j=0;j<alpha.length;j++)
				 {	if(i==0)
					 break;
					
					 YI+=alpha[j][0]*Math.pow(i, j);
					
				 }
				 if(YI<0)
					 	YI=-YI;
				
				 e[i]=martix.get(i)-YI;
				 
				 list.add(Math.abs(e[i]));	 
			 }
			
			 List<Double> list1=new ArrayList<Double>();
			 for(int i=0;i<list.size();i++)
			 { 
				 list1.add(list.get(i));
			 }
			 
			 Collections.sort(list);
			Double S=0.0;
			 if(e.length%2==0)
			 {	
				 S=(Double)(list.get(e.length/2)+list.get(e.length/2+1))/2;
				 
			 }
			 if(e.length%2!=0)
			 {
				 S=list.get(e.length/2);
				
			 }
			
			 double[] delta=new double[length];
		
			 for(int k=0;k<length;k++)
			 {
				 if(Math.abs((list1.get(k)/(6*S)))>1)
				 {
					 delta[k]=0;
				 }
				 else
				 {	 
					 delta[k]=Math.pow((1-Math.pow(list1.get(k)/(6*S),2)),2);
			
				 }
			 }
			 
			
			 
			 for(int i=0;i<length;i++)
				{
					weight[i]=delta[i]*weight[i];
			
		
				
				}
			 number--;
			 
			}
			
		wi[t]=weight[t];
		System.out.println(weight[t]);
	}
		
	
	double[][] W=new double[length][length];
	for(int i=0;i<length;i++)
	{
		for(int j=0;j<length;j++)
		{
			if(i==j)
			{	W[i][j]=wi[i];}
			else
			{
				W[i][j]=0;
			}
			
		}
	}
	
	double[][] Y=new double[length][1];//矩阵Y
	
	for(int i=0;i<length;i++)
	{
		Y[i][0]=martix.get(i);
	}
	
	
	//求解如下
	double[][] transX_W=new double[p+1][length];
	transX_W= multip(transX,W); //X'W
	double[][] transX_W_X=new double[p+1][p+1];
	transX_W_X=multip(transX_W, X);//X'WX
	
	 double[][]InMatrix=new double[p+1][p+1];
	 Inverse(transX_W_X,p,InMatrix); //(X'WX)^-1;
	 
	 double[][] transX_W_X_transX=new double[p+1][length];  
	 transX_W_X_transX=multip(InMatrix, transX); //X'WX)^-1*X'
	 
	 double[][] transX_W_X_transX_W=new double[p+1][length];  
	 transX_W_X_transX_W=multip(transX_W_X_transX, W); //X'WX)^-1*X'W
	 
	double[][] alpha=new double[p+1][1];
	 alpha=multip(transX_W_X_transX_W, Y);//X'WX)^-1*X'WY
	 
	 
	 List<Double> list=new ArrayList<Double>();

	
	 
		 double result=0;
		for(int t=length+1;t<=length+day;t++)
		{
			 for(int j=0;j<alpha.length;j++)
			 {
				  
				 result+=alpha[j][0]*Math.pow(t, j);
			 }
		} 
		 
		 if(result<0)
			 result=-result;
		
	
		    
		
	
	 return (int)Math.ceil(result);
	}
	
	
	
	
	*/
	
	
	
	
	
	
	
	
	/**********最小二乘法及局部加权********/
		/********86.577*******/
	
	public static int vm_predict(int[] mar, long day,long interval)
	{	
		
	
		
		
		int temp=1;
		

		double[] martix=new double[mar.length/temp];
		

		for(int i=0;i<mar.length/temp;i++)
		{
				for(int j=0;j<temp;j++)
			{
					martix[i]+=mar[temp*i+j];
		
			}
				
			
		}
		
		/*****计算有需求时的平均值********/
		int sum=0;
		int num1=0;
		for(int i=0;i<martix.length;i++)
		{
			if(martix[i]!=0)
				{
					sum+=martix[i];
					num1++;
				}
		}
		double ave=0;
		if(num1!=0)
			ave=sum/num1; //计算有需求时的平均值
		
		
		
	
	
		
		/*******大于五倍平均值则为噪声,消除*********/
		
		for(int i=0;i<mar.length/temp;i++)
		{
			if(martix[i]>5*ave)
				martix[i]=martix[i]-ave;
			
			
			
		}
	
		
		
		
	
		

		
		int p=3; //参数
		double[][] X=new double[martix.length][p+1]; //X矩阵
		for(int i=0;i<martix.length;i++)
			for(int j=0;j<p+1;j++)	
		{
			X[i][j]=Math.pow(i+1, j);
			
			
		}
		double[][] transX=new double[p+1][martix.length];//转置矩阵
		for(int i=0;i<p+1;i++)
			for(int j=0;j<martix.length;j++)
		{
			transX[i][j]=X[j][i];
		}
		 
		
		double[] weight=new double[martix.length];//权重
		
		int r=mar.length/temp;
		for(int i=martix.length-1;i>=0;i--)
		{	if(i>=martix.length-r)
			weight[i]=(Math.pow(1-Math.pow((double)(martix.length-i)/(martix.length),(double)5),7));
		else {
			weight[i]=0;
		}
		}
		
	
		
		/*double r=0.2;
		for(int i=0;i<martix.length;i++)
		{	
			
			//weight[i]=Math.pow(Math.E, Math.abs(martix.length-i)/(-1*r*r));
			weight[i]=(Math.pow(1-Math.pow((double)(martix.length-i)/(martix.length),(double)5),5));
			
			
	
		}
		*/
		
		//int number=3;
	//double[][] alpha=new double[p+1][1];
	//	while(number>0)
		//{
		double[][] W=new double[martix.length][martix.length];
		for(int i=0;i<martix.length;i++)
		{
			for(int j=0;j<martix.length;j++)
			{
				if(i==j)
				{	W[i][j]=weight[i];}
				else
				{
					W[i][j]=0;
				}
				
			}
		}
		
		double[][] Y=new double[martix.length][1];//矩阵Y
		
		for(int i=0;i<martix.length;i++)
		{
			Y[i][0]=martix[i];
		}
		
		
		//求解如下
		double[][] transX_W=new double[p+1][martix.length];
		transX_W= multip(transX,W); //X'W
		double[][] transX_W_X=new double[p+1][p+1];
		transX_W_X=multip(transX_W, X);//X'WX
		
		 double[][]InMatrix=new double[p+1][p+1];
		 Inverse(transX_W_X,p,InMatrix); //(X'WX)^-1;
		 
		 double[][] transX_W_X_transX=new double[p+1][martix.length];  
		 transX_W_X_transX=multip(InMatrix, transX); //X'WX)^-1*X'
		 
		 double[][] transX_W_X_transX_W=new double[p+1][martix.length];  
		 transX_W_X_transX_W=multip(transX_W_X_transX, W); //X'WX)^-1*X'W
		 
		double[][] alpha=new double[p+1][1];
		 alpha=multip(transX_W_X_transX_W, Y);//X'WX)^-1*X'WY
		 
		 
		 List<Double> list=new ArrayList<Double>();
		 double[] e=new double[martix.length];
		 
		 /*****强局部加权*********/
	
		
	/*	for(int i=0;i<martix.length;i++)
		 {	double YI=0;
			 for(int j=0;j<alpha.length;j++)
			 {	if(i==0)
				 break;
				
				 YI+=alpha[j][0]*Math.pow(i, j);
				
			 }
			 if(YI<0)
				 	YI=-YI;
			
			 e[i]=martix[i]-YI;
			 
			 list.add(Math.abs(e[i]));	 
		 }
		
		 List<Double> list1=new ArrayList<Double>();
		 for(int i=0;i<list.size();i++)
		 { 
			 list1.add(list.get(i));
		 }
		 
		 Collections.sort(list);
		Double S=0.0;
		 if(e.length%2==0)
		 {	
			 S=(Double)(list.get(e.length/2)+list.get(e.length/2+1))/2;
			 
		 }
		 if(e.length%2!=0)
		 {
			 S=list.get(e.length/2);
			
		 }
		
		 double[] delta=new double[martix.length];
	
		 for(int k=0;k<martix.length;k++)
		 {
			 if(Math.abs((list1.get(k)/(6*S)))>1)
			 {
				 delta[k]=0;
			 }
			 else
			 {	 
				 delta[k]=Math.pow((1-Math.pow(list1.get(k)/(6*S),2)),2);
		
			 }
		 }
		 
		
		 
		 for(int i=0;i<martix.length;i++)
			{
				weight[i]=delta[i]*weight[i];
		
	
			
			}
		 number--;
		 
		}
	
	*/
		 
		 
	 	 
		 double result=0;
		 

		 for(int i=martix.length+(int)interval/temp;i<=martix.length+(interval+day)/temp;i++)
		 {
			 for(int j=0;j<alpha.length;j++)
			 {
				  
				 result+=alpha[j][0]*Math.pow(i, j);
			 }
			  
		 }
		 if(result<0)
			 result=-result;
		
		result=result+Math.random()*15+5;
		 
		 return (int)Math.ceil(result);
		 
		    
		
		
	}
	

	

	
	/******矩阵相乘***/
	public static double[][] multip(double[][] a,double[][] b)
	{
		double[][] result=new double[a.length][b[0].length];
		for(int row=0;row<a.length;row++)
		{	for(int col=0;col<b[0].length;col++)
			{
				double num=0;
				for(int i=0;i<a[0].length;i++)
				{
					num+=a[row][i]*b[i][col];
				}
				result[row][col]=num;
			}
		}
		return result;
		
	}
	
	
	
	
	/*************矩阵行列式*********/
	  public static double Det(double [][]Matrix,int N)//计算n阶行列式(N=n-1)
	  {
	    int T0;
	    int T1;
	    int T2;
	    double Num;
	    int Cha;
	    double [][] B;
	    if(N>0)
	    {
	      Cha=0;
	      B=new double[N][N];
	      Num=0;
	      if(N==1)
	      {
	        return Matrix[0][0]*Matrix[1][1]-Matrix[0][1]*Matrix[1][0];
	      }
	      for (T0=0;T0<=N;T0++)//T0循环
	      {
	        for (T1=1;T1<=N;T1++)//T1循环
	        {
	          for (T2=0;T2<=N-1;T2++)//T2循环
	          {
	            if(T2==T0)
	            {
	              Cha=1;
	            }
	            B[T1-1][T2]=Matrix[T1][T2+Cha];
	          }
	          //T2循环
	          Cha=0;
	        }
	        //T1循环
	        Num=Num+Matrix[0][T0]*Det(B,N-1)*Math.pow((-1),T0);
	      }
	      //T0循环
	      return Num;
	    } else if(N==0)
	        {
	      return Matrix[0][0];
	    }
	    return 0;
	  }
	  
	  /*******矩阵求逆********/ 
	  public static double Inverse(double[][]Matrix,int N,double[][]MatrixC) //N为矩阵长度-1
	  {
	    int T0;
	    int T1;
	    int T2;
	    int T3;
	    double [][]B;
	    double Num=0;
	    int Chay=0;
	    int Chax=0;
	    B=new double[N][N];
	    double add;
	    add=1/Det(Matrix,N);
	    for ( T0=0;T0<=N;T0++)
	    {
	      for (T3=0;T3<=N;T3++)
	      {
	        for (T1=0;T1<=N-1;T1++)
	        {
	          if(T1<T0)
	          {
	            Chax=0;
	          } else
	          {
	            Chax=1;
	          }
	          for (T2=0;T2<=N-1;T2++)
	          {
	            if(T2<T3)
	            {
	              Chay=0;
	            } else
	            {
	              Chay=1;
	            }
	            B[T1][T2]=Matrix[T1+Chax][T2+Chay];
	          }
	          //T2循环
	        }//T1循环
	        Det(B,N-1);
	        MatrixC[T3][T0]=Det(B,N-1)*add*(Math.pow(-1, T0+T3));
	      }
	    }
	    return 0;
	  }
	  

	  
	  /**********************/
	
	  /***********基于灰色线性回归组合模型******/
	  /****由于0的个数过多导致这种方法并不好**/
/*	 public static int vm_predict(int[] mar,long day,long interval)
	  {
		  
		  int temp=1;
			int[] martix1=new int[mar.length/temp]; //temp时间段内的数量
			

			for(int i=0;i<mar.length/temp;i++)
			{
					for(int j=0;j<temp;j++)
				{
						martix1[i]+=mar[temp*i+j];
			
				}
					
				
			}
			double[] martix=new double[martix1.length];//temp时间段内的数量增量
			martix[0]=martix1[0];
			for(int i=1;i<martix.length;i++)
			{
				if((martix1[i]-martix1[i-1])<=0)
					martix[i]=0;
				else
					martix[i]=martix1[i]-martix1[i]-1;
			}
			
			
			
		  
		  
		  double[] X=new double[martix.length];//X矩阵
		  double[] X1=new double[martix.length];//X1矩阵为X矩阵的累加
		  for(int i=0;i<martix.length;i++)
		  {
			  X[i]=martix[i];
		  }
		  X1[0]=X[0];
		  for(int i=1;i<martix.length;i++)
		  {
			  X1[i]=X1[i-1]+X[i];
			
		  }
		  double[] Z=new double [X1.length-1];//Z矩阵,Z的长度为X.length-1
		  for(int i=0;i<Z.length;i++)
		  {
			  Z[i]=X1[i+1]-X1[i];
	
		  }
		  
		 
		  double V=0;
		  int m=X1.length-3;//Z.length-1
		  while(m>=0)
		  {
			  List<Double> ym=new ArrayList<Double>(); //ym的长度为X.length-2
			  for(int i=0;i<=X1.length-2-m;i++)
			  {
				  ym.add(Z[i+m]-Z[i]);
			  }
			 
			  for(int j=1;j<ym.size();j++)
			  {  if(ym.get(j)!=0 && ym.get(j-1)!=0)
				  V=V+Math.log(ym.get(j)/ym.get(j-1));
				
			  }
			  m--;
			  
		  }
		  V=V/((X1.length-2)*(X1.length-1)/2);
		 
		  
		  *//******最小二乘法求解*******//*
		  double[][] A=new double[X1.length][3];//A矩阵都一样X1的长度就是X的长度
		 for(int i=0;i<X1.length;i++)
		 {
			 A[i][0]=Math.pow(Math.E, -1*(i+1)*V);
			 A[i][1]=i+1;
			 A[i][2]=1;
			 
		 }
		  
		  double[][] transA=new double[3][X1.length];//A的转置矩阵
			for(int i=0;i<3;i++)
				for(int j=0;j<X1.length;j++)
			{
				transA[i][j]=A[j][i];
			}
			
			
			

		  double[][] transX1=new double[X1.length][1]; //X1的转置
		  for(int i=0;i<X1.length;i++)
		  {
			  transX1[i][0]=X1[i];
		  }
			
			//求解如下
			double[][] transA_A=new double[transA.length][A[0].length];//A转置矩阵的行和A的列,其实就是为3*3
			transA_A= multip(transA,A); //A'A
			
	
			
			 double[][]InMatrix=new double[3][3]; //后面的矩阵就直接简单写了
			 Inverse(transA_A,2,InMatrix); //(A'A)^-1;
			 
			 double[][] transA_A_transA=new double[3][X1.length];  
			 transA_A_transA=multip(InMatrix, transA); //A'A)^-1*A'
			 
			 double[][] C=new double[3][1];  
			 C=multip(transA_A_transA, transX1); //(A'A)^-1*A'X
			
			*//********预测的数量*******//*
			 double result1,result2,result;
			double time1=interval/temp;
			 double time=(day+interval)/temp;
			 
			 
			result1=C[0][0]*(Math.pow(Math.E, (-1*V*(martix.length+time1))))+C[1][0]*(martix.length+time1)+C[2][0];
			result2=C[0][0]*(Math.pow(Math.E, (-1*V*(martix.length+time))))+C[1][0]*(martix.length+time)+C[2][0];	
		//	result=Math.ceil(result2-(martix.length+day)-(result1-martix.length));
			
			result=Math.ceil(result2-result1);
			if(result<0)
				result=-result;
			System.out.println(result);
			return (int)result;
			
	  }*/
	
	
	
	
	
	/**************指数平滑法*****************/
	  /*****51分********/

/*	public static int vm_predict(int[] mar,long day,long interval)
	{	
		int temp=5; //参数
		
		int length=mar.length/temp;
		
		double[] martix1=new double[length];
		
	
			for(int i=0;i<length;i++)
			{
					for(int j=0;j<temp;j++)
				{
						martix1[i]+=mar[temp*i+j];
			
				}
					
				
			}
			double[] martix=new double[length];
			
			for(int i=0;i<length;i++) //时间间隔为length的数量增量
			{
				if(i==0)
					martix[i]=martix1[0];
				else if((martix1[i]-martix1[i-1])<=0)
				{	
					martix[i]=0;
				}
				else { 
					
					double temp1=martix1[i]-martix1[i-1];
					martix[i]=temp1;
				}

			}
			
		
		
		
		
		double[]  S=new double[martix.length];
		S[0]=0;

		for(int i=0;i<3;i++)//求平均值得S[0]
		{
			S[0]=S[0]+martix[i];
		}
			S[0]=S[0]/3;
			
			
			double alpha=0.8;
		
		//一次指数
		for(int j=1;j<martix.length;j++)
		{	
			S[j]=alpha*martix[j]+(1-alpha)*S[j-1];
		
		}
		//二次指数
		double[] S2=new double[martix.length];
		S2[0]=S[0];
		
		for(int k=1;k<martix.length;k++)
		{
			S2[k]=alpha*S[k]+(1-alpha)*S2[k-1];
			
			
		}
		
		double[] S3=new double[martix.length];
		S3[0]=S[0];
		for(int k=1;k<martix.length;k++)
		{
			S3[k]=alpha*S2[k]+(1-alpha)*S3[k-1];
			
		}
		
		
		//参数设置
		 double a_t=3*S[martix.length-1]-3*S2[martix.length-1]+S3[martix.length-1];
		 double b_t=alpha/(2*Math.pow((1-alpha),2))*((6-5*alpha)*S[martix.length-1]-2*(5-4*alpha)*S2[martix.length-1]+(4-3*alpha)*S3[martix.length-1]);
		double c_t=Math.pow(alpha, 2)/(2*Math.pow((1-alpha), 2))*(S[martix.length-1]-2*S2[martix.length-1]+S3[martix.length-1]);
		//double a_t=7*S[martix.length-1]-S2[martix.length-1];//原本为2
		//double b_t=alpha/(1-alpha)*(2*S[martix.length-1]-S2[martix.length-1]);
	
		
		double S_predict=0;
		
		
		int time=0;
		if(day%temp!=0)
			time=(int) (day/temp+1);
		else
			time=(int) (day/temp);
		double time=0;
		time=day/temp;
		
		
		for(int d=1;d<=time;d++)
		{	
			S_predict+=a_t+b_t*d+c_t*d*d;
			
			
			if(S_predict<0)
				S_predict=-S_predict;
			
		}
	
		for(int i=0;i<length;i++)
		{
			S_predict=S_predict+martix[i];
		}
		
		int result=(int)Math.ceil(S_predict);
		
		return result;
			
			
		
	}
	*/

	
}	

		


			
		
		
	
	
	
	

	
	
	
	