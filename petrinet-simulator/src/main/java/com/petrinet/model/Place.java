package com.petrinet.model;

import static com.petrinet.model.ModelDefaultStrings.DEFAULT_PLACE_NAME_PREFIX;

public class Place {
	private int id;
	private int initialTokens;
	private int tokens;
	private String name;
	
	
	public Place(int id, int tokens) {
		this.id = id;
		this.initialTokens = tokens;
		this.tokens = tokens;
		this.name = DEFAULT_PLACE_NAME_PREFIX + id;
	}
	
	public Place(int id, int tokens, String name) {
		this.id = id;
		this.initialTokens = tokens;
		this.tokens = tokens;
		this.name = name;
	}
	
	public int getId(){
		return this.id;
	}
	
	public String getName() {
		return this.name;
	}
	
	public int getTokens(){
		return this.tokens;
	}

	public int getInitialTokens() {
		return this.initialTokens;
	}
	
	public void setTokens(int tokens){
		this.tokens = tokens;
	}

	public void resetTokens() {
		this.tokens = this.initialTokens;
	}

}
