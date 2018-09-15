package main;

public class MergeSort {
	public static void sort(Comparable arr[]){
		int len=arr.length;
		Comparable temp[]=new Comparable[len];
		sort(arr, 0, len-1, temp);
	}
	public static void sort(Comparable arr[],int l,int r,Comparable temp[]){
		if(l<r){
			int mid=(l+r)/2;
			sort(arr,l,mid,temp);
			sort(arr,mid+1,r,temp);
			merge(arr, l, mid, r, temp);
		}
	}
	public static void merge(Comparable arr[],int l,int mid,int r,Comparable temp[]){
		int p1=l,p2=mid+1;
		int index=0;
		while (p1<=mid&&p2<=r) {
			if(arr[p1].compareTo(arr[p2])<0){
				temp[index++]=arr[p1++];
			}else{
				temp[index++]=arr[p2++];
			}
		}
		while (p1<=mid) {
			temp[index++]=arr[p1++];
		}
		while (p2<=r) {
			temp[index++]=arr[p2++];
		}
		index=0;
		while (l<=r) {
			arr[l++]=temp[index++];
		}
	}
}
