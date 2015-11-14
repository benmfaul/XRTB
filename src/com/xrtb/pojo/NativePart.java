package com.xrtb.pojo;

import java.util.ArrayList;
import java.util.List;

import com.xrtb.nativeads.creative.Data;
import com.xrtb.nativeads.creative.Img;
import com.xrtb.nativeads.creative.Title;
import com.xrtb.nativeads.creative.Video;

public class NativePart {

	public int layout = -1;
	public Title title;
	public Img img;
	public Video video;
	public List<Data>data = new ArrayList();
	
	public NativePart() {
		
	}
}
