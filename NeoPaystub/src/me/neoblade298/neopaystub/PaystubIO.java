package me.neoblade298.neopaystub;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.UUID;

import me.neoblade298.neocore.bungee.BungeeCore;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class PaystubIO implements Listener {
	private static HashMap<UUID, PaystubAccount> accounts = new HashMap<UUID, PaystubAccount>();
	
	@EventHandler
	public void onJoin(PostLoginEvent e) {
		UUID uuid = e.getPlayer().getUniqueId();
		
		// Load account
		NeoPaystub.inst().getProxy().getScheduler().runAsync(NeoPaystub.inst(), () -> {
			try (Connection con = BungeeCore.getConnection("NeoPaystub");
					Statement stmt = con.createStatement();
					ResultSet rs = stmt.executeQuery("SELECT * FROM paystub_accounts WHERE uuid = '" + uuid + "';")) {
				if (rs.next()) {
					accounts.put(uuid, new PaystubAccount(rs));
				}
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
		});
	}
	
	public static PaystubAccount getAccount(UUID uuid) {
		return accounts.get(uuid);
	}
	
	public static boolean hasAccount(UUID uuid) {
		return accounts.get(uuid) != null;
	}
	
	public static void addAccount(UUID uuid, PaystubAccount acct) {
		accounts.put(uuid, acct);
	}
}
