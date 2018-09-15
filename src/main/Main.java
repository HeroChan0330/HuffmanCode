package main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class Main {
	static int mode=0;//0±àÂë 1½âÂë
	static String targetFile=null,srcFile=null;
	public static void main(String []args) {
		if(args.length==0) return;
		
		if(!cmdDecoder(args))return;
		
//		String src=args[0],temp="output.dat",target="2.mp3";
		if(mode==0){
			try {
				FileInputStream inputStream=new FileInputStream(new File(srcFile));
				FileOutputStream outputStream=new FileOutputStream(new File(targetFile));
				int byteCnt[]=new int[256];
				byte buffer[]=new byte[1024];
				int bufferLen=0,sourseLen=0;
				while ((bufferLen=inputStream.read(buffer))>0) {
					sourseLen+=bufferLen;
					for(int i=0;i<bufferLen;i++){
						int index=buffer[i]&0xff;
						byteCnt[index]++;
					}
				}

				HuffmanCodeAlphabet alphabet=new HuffmanCodeAlphabet(byteCnt);
	//			System.out.println(alphabet);
				inputStream.close();
				inputStream=new FileInputStream(new File(srcFile));
				alphabet.compress(inputStream, outputStream,sourseLen);
				outputStream.close();
			}
			catch (Exception e) {
				// TODO: handle exception
				System.out.println(e);
			}
		}else{
			try{
				FileInputStream inputStream2=new FileInputStream(new File(srcFile));
				FileOutputStream outputStream2=new FileOutputStream(new File(targetFile));
				HuffmanCodeAlphabet alphabet2=HuffmanCodeAlphabet.fromStream(inputStream2);
				System.out.println(alphabet2);
				alphabet2.uncompress(inputStream2, outputStream2,HuffmanCodeAlphabet.srcLen);
			} catch (Exception e) {
				// TODO: handle exception
				System.out.println(e);
			}
		}
	}
	
	private static boolean cmdDecoder(String args[]){

		for(int i=0;i<args.length;i++){
			String cmd=args[i];
//			System.out.println(cmd);
			if(cmd.compareTo("-d")==0){
				mode=1;
			}else if(cmd.compareTo("-e")==0){
				mode=0;
			}else if(cmd.compareTo("-o")==0){
				targetFile=args[++i];
			}else{
				srcFile=cmd;
			}
		}
		if(targetFile==null&&srcFile==null){
			return false;
		}else if(targetFile==null){
			targetFile="_"+srcFile;
			System.out.println(targetFile);
		}
		return true;
	}
}
