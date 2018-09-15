package main;

public class Utils {
	public static int byte2Int(byte[] arr,int len){
		int res=0;
		for(int i=0;i<len;i++){
			res=res|((arr[i]<<8*i)&(0xff<<8*i));
		}
		return res;
	}
	public static void intToByte(int num,byte arr[],int offset,int len){
		for(int i=0;i<len;i++){
			arr[offset+i]=(byte)((num>>(8*i))&0xff);
		}
	}
	public static String intToBinStr(int num){
		String string="";
		while (num>0) {
			string=String.valueOf(num&0x01)+string;
			num>>=1;
		}
		return string;
	}
}
