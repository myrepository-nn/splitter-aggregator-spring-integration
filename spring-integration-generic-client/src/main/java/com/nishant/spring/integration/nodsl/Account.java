package com.nishant.spring.integration.nodsl;

import java.util.List;

public class Account {
	private String accountNo;
	private String accountType;
	private List<AccountHolders> accountHolders;
	public String getAccountNo() {
		return accountNo;
	}
	public void setAccountNo(String accountNo) {
		this.accountNo = accountNo;
	}
	public String getAccountType() {
		return accountType;
	}
	public void setAccountType(String accountType) {
		this.accountType = accountType;
	}
	public List<AccountHolders> getAccountHolders() {
		return accountHolders;
	}
	public void setAccountHolders(List<AccountHolders> accountHolders) {
		this.accountHolders = accountHolders;
	}


}
