package com.petrinet.model;

import static com.petrinet.model.ModelDefaultStrings.DEFAULT_PLACE_NAME_PREFIX;

public class Place {
	private static int nPlaces = 1;
	private int id;
	private int tokens;
	private String name;
	
	public Place(){
		this.id = nPlaces++;
	}
	
	public Place(int tokens){
		this.id = nPlaces++;
		this.tokens = tokens;
		this.name = DEFAULT_PLACE_NAME_PREFIX + id;
	}
	
	public Place(int id, int tokens) {
		this.id = id;
		this.tokens = tokens;
		this.name = DEFAULT_PLACE_NAME_PREFIX + id;
	}
	
	public Place(int id, int tokens, String name) {
		this.id = id;
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
	
	public void setTokens(int tokens){
		this.tokens = tokens;
	}

}
