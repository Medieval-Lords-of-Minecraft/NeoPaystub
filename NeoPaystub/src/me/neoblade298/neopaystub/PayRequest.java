package me.neoblade298.neopaystub;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import me.neoblade298.neocore.bungee.BungeeCore;
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
		
		NeoPaystub.scheduler().runAsync(NeoPaystub.inst(), () -> {
        	try (Connection con = BungeeCore.getConnection("NeoPaystub");
        			Statement stmt = con.createStatement()) {
        		stmt.executeUpdate("INSERT INTO paystub_requests VALUES('" + id + "','" + uuid + "','" + display + "',"
        			+ amount + "," + requesttime + ",'" +
        			note + "',0,0);");
        	} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
	}
	
	public PayRequest(ResultSet rs) throws SQLException {
		this.uuid = UUID.fromString(rs.getString("uuid"));
		this.amount = rs.getInt("amount");
		this.id = rs.getInt("id");
		this.note = rs.getString("note");
		this.requesttime = rs.getLong("requesttime");
		this.processtime = rs.getLong("processtime");
		this.isApproved = rs.getBoolean("isApproved");
		this.display = rs.getString("display");
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
		return processtime != 0;
	}
	public void process(boolean approve) {
		processtime = System.currentTimeMillis();
		isApproved = approve;
		
		NeoPaystub.scheduler().runAsync(NeoPaystub.inst(), () -> {
			try (Connection con = BungeeCore.getConnection("NeoPaystub");
					Statement stmt = con.createStatement()) {
				stmt.executeUpdate("UPDATE paystub_requests SET processtime = " + processtime + ", isApproved = " + (approve ? 1 : 0) + " WHERE id = " + id + ";");
			} catch (SQLException e) {
				e.printStackTrace();
			}
		});
	}
	
	public void revertProcess() {
		processtime = 0;
		isApproved = false;
		
		NeoPaystub.scheduler().runAsync(NeoPaystub.inst(), () -> {
			try (Connection con = BungeeCore.getConnection("NeoPaystub");
					Statement stmt = con.createStatement()) {
				stmt.executeUpdate("UPDATE paystub_requests SET processtime = " + 0 + ", isApproved = 0" + " WHERE id = " + id + ";");
			} catch (SQLException e) {
				e.printStackTrace();
			}
		});
	}
	
	public static void setNextId(int id) {
		nextId = id;
	}
}
