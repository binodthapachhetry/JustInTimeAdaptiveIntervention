package edu.neu.android.wocketslib.json.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.SerializedName;

public class Swapping implements Serializable {

	private static final long serialVersionUID = 1L;

	// *****************for GSON
	@SerializedName("someSwaps")
	public List<SwapEvent> someSwap;

	public Swapping() {
		super();
		this.someSwap = new ArrayList<SwapEvent>();
	}

}