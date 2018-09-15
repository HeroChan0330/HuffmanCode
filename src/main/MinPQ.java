package main;

import java.util.concurrent.Exchanger;

public class MinPQ<Key extends Comparable<Key>> {
	private Key[] pq;
	private int N=0;
	public MinPQ(int n){
		pq=(Key[])new Comparable[n+1];
	}
	public boolean IsEmpty(){
		return N==0;
	} 
	public int Size(){
		return N;
	}
	public void Insert(Key k){
		pq[++N]=k;
		Swim(N);
	}
	
	public Key DeleteMin(){
		Key res=pq[1];
		pq[1]=pq[N--];
		pq[N+1]=null;
		Sink(1);
		return res;
	}
	private void Swim(int k){
		while(k>1&&!Less(k/2, k)){
			//Exchange
			Exch(k/2,k);
			k/=2;
		}
	}
	public void Sink(int k){
		while(2*k<=N){
			int j=2*k;
			if(j<N&&!Less(j, j+1))j++;
			if(Less(k, j))break;
			Exch(k, j);
			k=j;
		}
	}
	private void Exch(int i1,int i2){
		Key temp=pq[i1];
		pq[i1]=pq[i2];
		pq[i2]=temp;
	}
	private boolean Less(Key a,Key b){
		if(a.compareTo(b)<0)return true;
		return false;
	}
	private boolean Less(int index1,int index2){
		return Less(pq[index1],pq[index2]);
	}
}
