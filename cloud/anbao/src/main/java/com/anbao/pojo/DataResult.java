package com.anbao.pojo;

import java.util.List;
/**
 * 分页数据显示
 * @author zzm
 *
 */
public class DataResult {


	private Integer code;
	private String msg;

	private List<?> data;
	private Long total;
	public Long getTotal() {
		return total;
	}
	public void setTotal(Long total) {
		this.total = total;
	}
	public List<?> getData() {
		return data;
	}
	public void setData(List<?> data) {
		this.data = data;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	public Integer getCode() {
		return code;
	}
	public void setCode(Integer code) {
		this.code = code;
	}

	
	

}
