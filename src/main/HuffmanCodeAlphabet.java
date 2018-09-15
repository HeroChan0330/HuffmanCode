package main;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class HuffmanCodeAlphabet {
	public HuffmanTree alphabetTree=null;
	public Bits alphabetArr[]=new Bits[256];
	public static int srcLen;
	public HuffmanCodeAlphabet(int charArr[]) {
		// TODO Auto-generated constructor stub
		MinPQ<HuffmanTree> minPQ=new MinPQ<>(256);
		for(int i=0;i<charArr.length;i++){
			if(charArr[i]>0){
				HuffmanTree huffmanTree=new HuffmanTree(i, charArr[i]);
				minPQ.Insert(huffmanTree);
			}
		}
		while (minPQ.Size()>1) {
			HuffmanTree minTree1=minPQ.DeleteMin(),minTree2=minPQ.DeleteMin();
			HuffmanTree fatherTree=new HuffmanTree(0, 0);
			fatherTree.addChild(minTree1);
			fatherTree.addChild(minTree2);
			minPQ.Insert(fatherTree);
		}
		this.alphabetTree=minPQ.DeleteMin();
		alphabetTreeToArr(alphabetTree,0,0);
	}
	public HuffmanCodeAlphabet(){
		
	}
	public static HuffmanCodeAlphabet fromStream(InputStream inputStream) throws Exception{
		HuffmanCodeAlphabet ret=new HuffmanCodeAlphabet();
		ret.alphabetTree=new HuffmanTree(0, 0);
		byte buffer[]=new byte[4];
		inputStream.read(buffer, 0, 4);
		srcLen=Utils.byte2Int(buffer, 4);//源数据总共字节长
//		System.out.println(String.format("len:%d", srcLen));
		inputStream.read(buffer, 0, 2);
		int keyCnt=Utils.byte2Int(buffer, 2);//键值总数
		inputStream.read(buffer, 0, 2);
		int keyValBytesLen=Utils.byte2Int(buffer, 2);//一个键值对应的压缩编码占的最大字节长
		for(int i=0;i<keyCnt;i++){
			inputStream.read(buffer, 0, 1);
			int key=(int)(buffer[0]&0xff);
			inputStream.read(buffer, 0, 1);
			byte bitsLen=buffer[0];
			inputStream.read(buffer, 0, keyValBytesLen);
			int bitsVal=Utils.byte2Int(buffer, keyValBytesLen);
//			System.out.println(String.format("key:%d val:%d", key,bitsVal));
			Bits bits=new Bits(key, bitsVal, bitsLen);
//			System.out.println(bits);
			ret.alphabetArr[key]=bits;
			HuffmanTree temp=ret.alphabetTree;
			int bit=0;
//			System.out.print(String.format("key:\t%d\t", key));
			for(int b=0;b<bitsLen;b++){
				bit=(bitsVal>>(bitsLen-b-1))&0x01;
				
				if(bit==0){
					if(temp.left==null){
						temp.left=new HuffmanTree(0, 0);
					}
					temp=temp.left;
				}
				else{
					if(temp.right==null){
						temp.right=new HuffmanTree(0, 0);
					}
					temp=temp.right;
				}
			}
//			System.out.print('\n');
			temp.key=key;
			
		}
//		System.out.println(ret);
		return ret;
	}
	public Bits decode(byte byteArr[],int bitPos){
		HuffmanTree temp=alphabetTree;
		while (true) {
			int bit=(byteArr[bitPos>>3]>>(7-(bitPos&0x07)))&0x01;
			bitPos++;
			if(bit==0){
				temp=temp.left;
			}
			else{
				temp=temp.right;
			}
			if(!temp.hasChild()){
				return alphabetArr[temp.key];
			}
		}
	}
	public String toString(){
//		return "alphabetTree";
		return alphabetToStr(alphabetTree,"");
	}
	public void compress(InputStream inputStream,OutputStream outputStream,int inputBytesLen) throws Exception{		
		byte outputBuffer[]=new byte[1000];
		byte inputBuffer[]=new byte[1000];
		int bitsPos=0;
		int byteLen=0;

		//写文件头
		byte header[]=constructFileHeader(inputBytesLen);
		outputStream.write(header);
		while ((byteLen=inputStream.read(inputBuffer))>=0) {
			for(int i=0;i<byteLen;i++){
//				System.out.print((char)inputBuffer[i]);
				int key=inputBuffer[i]&0xff;
				Bits bits=alphabetArr[key];
				if(bits==null) throw new Exception("alphabet error occur!");
				bitsPos=bits.writeToArray(outputBuffer, bitsPos);
				
				if(bitsPos>>3>500){//500字节写一次
					outputStream.write(outputBuffer, 0, 500);
					for(int b=0;b<500;b++){
						outputBuffer[b]=outputBuffer[b+500];
						outputBuffer[b+500]=0;
					}
					bitsPos-=500<<3;
				}
			}
		}
		int pos=((bitsPos%8)>0)?(bitsPos>>3)+1:bitsPos>>3;
		outputStream.write(outputBuffer, 0, pos);
	}
	public byte[]constructFileHeader(int inputBytesLen){
		int keyCnt=0,maxKeyValBitsLen=0;
		for(int i=0;i<256;i++){
			if(alphabetArr[i]!=null){
				keyCnt++;
				maxKeyValBitsLen=(alphabetArr[i].bitsLen>maxKeyValBitsLen)?alphabetArr[i].bitsLen:maxKeyValBitsLen;
			}
		}
		int maxKeyValBytesLen=((maxKeyValBitsLen%8)>0)?(maxKeyValBitsLen>>3)+1:maxKeyValBitsLen>>3;
		System.out.println(String.format("maxKeyValBitsLen %d", maxKeyValBitsLen));
		int keyInfoUseByte=2+maxKeyValBytesLen;
		byte ret[]=new byte[keyCnt*keyInfoUseByte+8];
		Utils.intToByte(inputBytesLen, ret, 0, 4);
		Utils.intToByte(keyCnt, ret, 4, 2);
		Utils.intToByte(maxKeyValBytesLen, ret, 6, 2);
		int keyIndex=0;
		for(int i=0;i<256;i++){
			if(alphabetArr[i]==null) continue;
			ret[keyIndex*keyInfoUseByte+8]=(byte)alphabetArr[i].key;
			ret[keyIndex*keyInfoUseByte+9]=(byte)alphabetArr[i].bitsLen;
			Utils.intToByte(alphabetArr[i].val, ret, keyIndex*keyInfoUseByte+10, maxKeyValBytesLen);
			keyIndex++;
		}
//		System.out.println(String.format("%d %d %d %d",ret[0],ret[1],ret[2],ret[3]));
		return ret;
	}
	public void uncompress(InputStream inputStream,OutputStream outputStream,int srcLen) throws Exception{
		byte inputBuffer[]=new byte[1000];
		byte outputBuffer[]=new byte[1000];
		int bitsPos=0;
		int inputByteLen=0;
		int outputBytePos=0;
		int writtenBytes=0;
		inputByteLen=inputStream.read(inputBuffer, 0, 1000);
		while (writtenBytes<srcLen) {
			
			Bits bits=decode(inputBuffer,bitsPos);
//			System.out.print(writtenBytes);
//			System.out.print(' ');
//			System.out.print(bitsPos>>3);
//			System.out.print(' ');
//			System.out.println(inputByteLen);
			bitsPos+=bits.bitsLen;
			writtenBytes++;
//			System.out.println(bitsPos>>3);
			outputBuffer[outputBytePos++]=(byte)bits.key;

			if(outputBytePos>=1000){
				outputStream.write(outputBuffer,0, 1000);
				outputBytePos=0;
			}

			if(bitsPos>>3>500){
				for(int b=0;b<inputByteLen-500;b++){
					inputBuffer[b]=inputBuffer[b+500];
				}
//				System.out.println(inputByteLen);
				bitsPos-=(500<<3);
				inputByteLen-=500;
				inputByteLen=inputByteLen+inputStream.read(inputBuffer, inputByteLen, 1000-inputByteLen);
			}

//			System.out.println(bitsPos);

			
		}
		outputStream.write(outputBuffer, 0, outputBytePos);
	}
	private String alphabetToStr(HuffmanTree tree,String code){
		
		if(tree.hasChild()){
			StringBuilder builder=new StringBuilder();
			builder.append(alphabetToStr(tree.left,code+'0'));
//			System.out.println(String.format("left is null %b", tree.left==null));
			builder.append(alphabetToStr(tree.right,code+'1'));
//			System.out.println(String.format("right is null %b", tree.right==null));
			return builder.toString();
		}
		else{
			return String.format("Key:\t%d\tcnt:\t%d\tcode:\t%s\n", tree.key,tree.cnt,(code));
		}
	}
	private void alphabetTreeToArr(HuffmanTree tree,int bits,int bitsLen){
		if(tree.hasChild()){
			StringBuilder builder=new StringBuilder();
			alphabetTreeToArr(tree.left,bits<<1,bitsLen+1);
			alphabetTreeToArr(tree.right,bits<<1|1,bitsLen+1);
			return;
		}
		else{
			Bits newBits=new Bits(tree.key,bits, bitsLen);
			alphabetArr[tree.key]=newBits;
			return;
//			return String.format("Key:%c cnt:%d code:%s\n", (char)tree.key,tree.cnt,(code));
		}
	}

	public static class HuffmanTree implements Comparable<HuffmanTree>{
		public HuffmanTree left=null,right=null;
		public int key;
		public int cnt;
		public HuffmanTree(int key,int cnt) {
			// TODO Auto-generated constructor stub
			this.key=key;
			this.cnt=cnt;
		}
		public boolean addChild(HuffmanTree child){
			if(left==null){
				left=child;
				cnt+=child.cnt;
				return true;
			}
			else if(right==null){
				right=child;
				cnt+=child.cnt;
				return true;
			}
			return false;
		}
		public boolean hasChild(){
			return !(left==null&&right==null);
		}
		@Override
		public int compareTo(HuffmanTree o) {
			// TODO Auto-generated method stub
			if(cnt>o.cnt) return 1;
			else if(cnt<o.cnt) return -1;
			return 0;
		}
		public String toString(){
			StringBuilder builder=new StringBuilder();
			System.out.println("sss");
			if(left!=null&&right!=null){//正常情况是完全二叉树
				builder.append(left.toString());
				builder.append(right.toString());
			}
			else{
				builder.append("Key:");
				builder.append((char)key);
				builder.append(" cnt:");
				builder.append(cnt);
				builder.append('\n');
			}
			return builder.toString();
		}
	}
	public static class Bits{
		int val,bitsLen;
		int key;
		public Bits(int key,int val,int bitsLen) {
			// TODO Auto-generated constructor stub
			this.key=key;
			this.val=val;
			this.bitsLen=bitsLen;
		}
		public int writeToArray(byte arr[],int bitPos){
			for(int i=0;i<bitsLen;i++){
				arr[(bitPos+i)>>3]|=((val>>(bitsLen-i-1))&0x01)<<(0x07-(bitPos+i)&0x07);
			}
			return bitPos+bitsLen;
		}
		public String toString(){
			return String.format("Key:\t%d\tcode:\t%s", key,(val));
		}
	}
}
