package me.neoblade298.neopaystub;

import java.util.UUID;

public class PaystubAccount {
	private UUID uuid;
	private int codeId;
	private String code;
	public PaystubAccount(UUID uuid, int codeId, String code) {
		this.uuid = uuid;
		this.codeId = codeId;
		this.code = code;
	}
	public UUID getUuid() {
		return uuid;
	}
	public int getCodeId() {
		return codeId;
	}
	public String getCode() {
		return code;
	}
}
