package com.pgis.bus.data.orm;

public class StringValue {
    public Integer id;
    public Integer key_id;
	public String lang_id;
	public String value;
	
	public StringValue(){
		
		
	}
	public StringValue(String lang_id,String value){
		this.lang_id = lang_id;
		this.value = value;
		
	}
	@Override
	public String toString() {
		return "StringValue [id=" + id + ", key_id=" + key_id + ", lang_id="
				+ lang_id + ", value=" + value + "]";
	}
	
	
}
