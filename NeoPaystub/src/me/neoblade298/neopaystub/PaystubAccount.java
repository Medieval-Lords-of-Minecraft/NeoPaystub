package me.neoblade298.neopaystub;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import me.neoblade298.neocore.bungee.BungeeCore;

public class PaystubAccount {
	private UUID uuid;
	private int codeId;
	private String code;
	public PaystubAccount(UUID uuid, int codeId, String code) {
		this.uuid = uuid;
		this.codeId = codeId;
		this.code = code;

    	try (Connection con = BungeeCore.getConnection("NeoPaystub");
    			Statement stmt = con.createStatement()) {
    		stmt.executeUpdate("INSERT INTO paystub_accounts VALUES('" + uuid + "'," + codeId + ",'" + code + "');");
    	} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	public PaystubAccount(ResultSet rs) throws SQLException {
		this.uuid = UUID.fromString(rs.getString("uuid"));
		this.codeId = rs.getInt("id");
		this.code = rs.getString("code");
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
