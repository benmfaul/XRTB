package com.xrtb.pojo;

import java.util.ArrayList;
import java.util.List;

import com.xrtb.nativead.Img;
import com.xrtb.nativead.Title;
import com.xrtb.nativead.Video;
import com.xrtb.nativead.Data;

public class NativePart {

	public int layout = -1;
	public Title title;
	public Img img;
	public Video video;
	public List<Data>data = new ArrayList();
	
	public NativePart() {
		
	}
}
