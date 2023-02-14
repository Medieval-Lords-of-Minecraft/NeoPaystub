package me.neoblade298.neopaystub;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import org.bukkit.Bukkit;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import me.neoblade298.neocore.bungee.BungeeCore;
import me.neoblade298.neocore.bungee.commands.SubcommandManager;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;
import me.neoblade298.neocore.shared.util.PaginatedList;
import me.neoblade298.neopaystub.commands.*;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.scheduler.TaskScheduler;

public class NeoPaystub extends Plugin {
	private static NeoPaystub inst;
	private static String token;
	
	private static PaginatedList<PayRequest> requests = new PaginatedList<PayRequest>();
	
	public void onEnable() {
		inst = this;
		// Bukkit.getPluginManager().registerEvents(this, this);
		initCommands();
		
		// Load paystub requests
		try (Connection con = BungeeCore.getConnection("NeoPaystub");
				Statement stmt = con.createStatement();
				ResultSet rs = stmt.executeQuery("SELECT * FROM paystub_requests ORDER BY id DESC;")) {
			while (rs.next()) {
				requests.add(new PayRequest(rs));
			}
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
	}
	
	private void initCommands() {
		SubcommandManager mngr = new SubcommandManager("paystub", "paystub.use", ChatColor.RED, this);
		mngr.register(new CmdPaystub("", "Checks your account", null, SubcommandRunner.BOTH));
		mngr.register(new CmdPaystubApprove("approve", "Approves a pay request", "paystub.admin", SubcommandRunner.BOTH));
		mngr.register(new CmdPaystubDeny("deny", "Denies a pay request", "paystub.admin", SubcommandRunner.BOTH));
		mngr.register(new CmdPaystubGive("give", "Gives points to a player (can be negative)", "paystub.admin", SubcommandRunner.BOTH));
		mngr.register(new CmdPaystubList("list", "Lists existing pay requests", "paystub.admin", SubcommandRunner.BOTH));
		mngr.register(new CmdPaystubRequest("request", "Requests payment for a job", "paystub.staff", SubcommandRunner.BOTH));
	}
	
	public static NeoPaystub inst() {
		return inst;
	}
	
	public static boolean pay(UUID uuid, int amount) {
		int id = -1;
		if (PaystubIO.hasAccount(uuid)) {
			id = PaystubIO.getAccount(uuid).getCodeId();
		}
		else {
			try (Connection con = BungeeCore.getConnection("NeoPaystub");
					Statement stmt = con.createStatement();
					ResultSet rs = stmt.executeQuery("SELECT * FROM paystub_accounts WHERE uuid = '" + uuid + "';")) {
				if (rs.next()) {
					id = rs.getInt("id");
				}
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
		}
		
		if (id == -1) {
			return createAccount(uuid, amount) != -1;
		}
		else {
			return payToId(id, amount);
		}
	}
	
	public static boolean pay(PaystubAccount acct, int amount) {
		return payToId(acct.getCodeId(), amount);
	}
	
	public static PaginatedList<PayRequest> getRequests() {
		return requests;
	}
	
	private static boolean payToId(int id, int amount) {
		try {
	        HttpClient client = HttpClient.newHttpClient();
	        HttpRequest request = HttpRequest.newBuilder()
	                .uri(URI.create("https://api.craftingstore.net/v7/gift-cards/" + id))
	                .header("token", token)
	                .header("content-type",  "application/json")
	                .PUT(HttpRequest.BodyPublishers.ofString("{\"amount\": " + amount + "}"))
	                .build();

	        HttpResponse<String> response = client.send(request,
	                HttpResponse.BodyHandlers.ofString());
	        if (response.statusCode() == 200) {
	        	JsonObject data = JsonParser.parseString(response.body()).getAsJsonObject().get("data").getAsJsonObject();
	        	int after = data.get("amount").getAsInt();
	        	Bukkit.getLogger().info("[NeoPaystub] Successfully paid " + amount + " to id " + id + ", now has " + after);
	        	return true;
	        }
	        else {
	        	Bukkit.getLogger().warning("[NeoPaystub] " + response.body());
	        	Bukkit.getLogger().warning("[NeoPaystub] Failed to pay " + amount + " to id " + id);
	        }
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	// Returns id of gift card
	public static int createAccount(UUID uuid, int amount) {
		try {
	        HttpClient client = HttpClient.newHttpClient();
	        HttpRequest request = HttpRequest.newBuilder()
	                .uri(URI.create("https://api.craftingstore.net/v7/gift-cards"))
	                .header("token", token)
	                .header("content-type",  "application/json")
	                .POST(HttpRequest.BodyPublishers.ofString("{\"amount\": " + amount + "}"))
	                .build();

	        HttpResponse<String> response = client.send(request,
	                HttpResponse.BodyHandlers.ofString());
	        if (response.statusCode() == 200) {
	        	JsonObject data = JsonParser.parseString(response.body()).getAsJsonObject().get("data").getAsJsonObject();
	        	int id = data.get("id").getAsInt();
	        	String code = data.get("code").getAsString();
	        	int actualAmount = data.get("amount").getAsInt();
	        	Bukkit.getLogger().info("[NeoPaystub] Successfully created account for user " + uuid + " with id " + id + ", code " + code + ", amount " + actualAmount);
	        	return id;
	        }
	        else {
	        	Bukkit.getLogger().warning("[NeoPaystub] " + response.body());
	        	Bukkit.getLogger().warning("[NeoPaystub] Failed to create account for user " + uuid);
	        }
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    	return -1;
	}
	
	public static JsonObject viewBalance(int id) {
		try {
	        HttpClient client = HttpClient.newHttpClient();
	        HttpRequest request = HttpRequest.newBuilder()
	                .uri(URI.create("https://api.craftingstore.net/v7/gift-cards/" + id))
	                .header("token", token)
	                .header("content-type",  "application/json")
	                .GET()
	                .build();

	        HttpResponse<String> response = client.send(request,
	                HttpResponse.BodyHandlers.ofString());
	        if (response.statusCode() == 200) {
	        	return JsonParser.parseString(response.body()).getAsJsonObject().get("data").getAsJsonObject();
	        }
	        else {
	        	Bukkit.getLogger().warning("[NeoPaystub] " + response.body());
	        	Bukkit.getLogger().warning("[NeoPaystub] Failed to receive gift card id " + id);
	        }
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    	return null;
	}
	
	public static void addRequest(PayRequest req) {
		requests.add(req);
	}
	
	public static TaskScheduler scheduler() {
		return inst.getProxy().getScheduler();
	}
	
	public static ProxyServer proxy() {
		return inst.getProxy();
	}
}
