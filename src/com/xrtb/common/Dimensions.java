package com.xrtb.common;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Dimensions extends ArrayList<Dimension> {

	public Dimensions() {
		
	}
	
	@JsonIgnore
	public Dimension getBestFit(Integer x, Integer y) {
		for (int i=0;i<size();i++) {
			Dimension d = get(i);
			if (d.fits(x,y))
				return d;
		}
		return null;
	}
}
