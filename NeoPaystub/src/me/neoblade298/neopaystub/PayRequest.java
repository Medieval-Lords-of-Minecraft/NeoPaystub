package me.neoblade298.neopaystub;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import net.md_5.bungee.api.connection.ProxiedPlayer;

public class PayRequest {
	public static int nextId;
	
	private UUID uuid;
	private int id, amount;
	private String display, note;
	private long requesttime, processtime;
	private boolean isApproved = false;
	
	// Used for new requests
	public PayRequest(ProxiedPlayer p, int amount, String note) {
		this.uuid = p.getUniqueId();
		this.display = p.getName();
		this.amount = amount;
		this.note = note;
		requesttime = System.currentTimeMillis();
		
		this.id = nextId++;
	}
	
	public PayRequest(ResultSet rs) throws SQLException {
		this.uuid = UUID.fromString(rs.getString("uuid"));
		this.amount = rs.getInt("amount");
		this.id = rs.getInt("id");
		this.note = rs.getString("note");
		this.requesttime = rs.getLong("requesttime");
		this.processtime = rs.getLong("processtime");
		this.isApproved = rs.getBoolean("isApproved");
	}
	public UUID getUniqueId() {
		return uuid;
	}
	public String getDisplay() {
		return display;
	}
	public int getId() {
		return id;
	}
	public int getAmount() {
		return amount;
	}
	public String getNote() {
		return note;
	}
	public long getRequestTime() {
		return requesttime;
	}
	public long getProcessTime() {
		return processtime;
	}
	public boolean isApproved() {
		return isApproved;
	}
	public boolean isProcessed() {
		return processtime != -1;
	}
}
