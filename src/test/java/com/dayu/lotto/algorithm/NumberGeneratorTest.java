package com.dayu.lotto.algorithm;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class NumberGeneratorTest {
	@Test
	public void testWriteAllNumber() throws IOException
	{
		String filename = getClass().getResource("/results").getPath() + "/AllLottoNumber.csv";
		
		FileWriter fw = new FileWriter(filename, true);
		PrintWriter pw = new PrintWriter(fw);
		
		
		List<Integer> l1 = new ArrayList<Integer>();
		List<Integer> l2 = new ArrayList<Integer>();
		List<Integer> l3 = new ArrayList<Integer>();
		List<Integer> l4 = new ArrayList<Integer>();
		List<Integer> l5 = new ArrayList<Integer>();
		List<Integer> l6 = new ArrayList<Integer>();
		
		
		
		for (int i = 1; i <= 45; i++)
		{
			l1.add(i);
			l2.add(i);
			l3.add(i);
			l4.add(i);
			l5.add(i);
			l6.add(i);
		}
		
		List<Integer> temp = new ArrayList<Integer>(6);
		for (int i1 = 0; i1 < 40; i1++)
		{
			int n1 = l1.get(i1);
			
			// remove n1 from other list
			l2.remove(Integer.valueOf(n1));
			l3.remove(Integer.valueOf(n1));
			l4.remove(Integer.valueOf(n1));
			l5.remove(Integer.valueOf(n1));
			l6.remove(Integer.valueOf(n1));
			
			
			temp.add(n1);
			
			for (int i2 = 0; i2<l2.size(); i2++)
			{
				int n2 = l2.get(i2);
				if (n2 > n1 && !temp.contains(n2))
				{
					temp.add(n2);
					
					for (int i3 =0; i3<l3.size(); i3++)
					{
						int n3 = l3.get(i3);
						if (n3 > n2 && !temp.contains(n3))
						{
							temp.add(n3);
							for (int i4=0;i4<l4.size();i4++)
							{
								int n4 = l4.get(i4);
								if (n4 > n3 && !temp.contains(n4))
								{
									temp.add(n4);
									for (int i5=0; i5<l5.size();i5++)
									{
										int n5 = l5.get(i5);
										if (n5 > n4 && !temp.contains(n5))
										{
											temp.add(n5);
											for (int i6=0; i6<l6.size();i6++)
											{
												int n6 = l6.get(i6);
												if (n6 > n5 && !temp.contains(n6))
												{
													temp.add(n6);
													// print temp
													pw.println(temp.get(0) + " " + temp.get(1) + " " + temp.get(2) + " " + temp.get(3) + " " + temp.get(4) + " " + temp.get(5));
													pw.flush();
													
													temp.remove(5);
												}
											}
											temp.remove(4);
										}
									}
									temp.remove(3);
								}
							}
							temp.remove(2);
						}
					}
					temp.remove(1);
				}
			}
			temp.remove(0);
		}
		
		
		/*int[] array = {1,2,3,4,5,6};
		
		pw.println(array[0] + " " + array[1] + " " + array[2] + " " + array[3] + " " + array[4] + " " + array[5]);
		*/
		/*for (int k= 0; k < 40; k++)
		{
			int[] arr = new int[] {1+k, 2+k,3+k,4+k,5+k,6+k};
			pw.println(arr[0] + " " + arr[1] + " " + arr[2] + " " + arr[3] + " " + arr[4] + " " + arr[5]);
			
			for (int i = 0; i<arr.length; i++)
			{
				int[] temp = new int[6];
				temp[0]=arr[0];
				temp[1]=arr[1];
				temp[2]=arr[2];
				temp[3]=arr[3];
				temp[4]=arr[4];
				temp[5]=arr[5];
				
				
				
				for (int j=7+k; j<46; j++)
				{
					temp[i] = j;
					
					pw.println(temp[0] + " " + temp[1] + " " + temp[2] + " " + temp[3] + " " + temp[4] + " " + temp[5]);
				}
				
			}
		}*/
		
		pw.close();
	}
	
	/*private void addNumberToTemp(List<Integer> numbers, List <Integer> temp, PrintWriter pw)
	{
		for (int i = 0; i < numbers.size(); i++)
		{
			if (!temp.contains(numbers.get(i)))
			{
				temp.add(numbers.get(i));
				
				if (temp.size() <6)
				{
					addNumberToTemp(numbers, temp, pw);
				}
				else
				{
					for (Integer n : temp)
					{
						pw.print(n);
						pw.print(" ");
					}
					pw.println();
				}
			}

			
			
			
			List<Integer> temp = new ArrayList<Integer>(6);
			int n1 = numbers.get(i);
			temp.add(n1);
			
			for (int i2 = 0; i2<45; i2++)
			{
				if (!temp.contains(l2.get(i2)))
				{
					temp.add(l2.get(i2));
					
					for (int i3 =0; i3<45; i3++)
				}
			}
		}
		
	}*/
}
