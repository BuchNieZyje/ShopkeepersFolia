package com.nisovin.shopkeepers.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.InventoryClickEvent;

import com.nisovin.shopkeepers.api.shopkeeper.Shopkeeper;
import com.nisovin.shopkeepers.api.shopkeeper.TradingRecipe;
import com.nisovin.shopkeepers.api.util.UnmodifiableItemStack;

/**
 * This event is called whenever a player is about to trade with a shopkeeper. Canceling it will cause the trade to not
 * get applied.
 * <p>
 * Depending on the inventory action, a single inventory click of a player might trigger several successive trades
 * (possibly even using different trading recipes). Canceling a trade will also cancel all successive trades.
 * <p>
 * All other preconditions regarding the trade have already been checked before this event gets called. So if this event
 * does not get cancelled you can assume that the trade is going to get applied.
 * <p>
 * DO NOT modify the corresponding {@link InventoryClickEvent}, any affected inventories (player, merchant, shop
 * container, ..), or any other state which might be affected by the trade during the handling of this event!
 */
public class ShopkeeperTradeEvent extends ShopkeeperEvent implements Cancellable {

	private final Player player;
	private final InventoryClickEvent clickEvent;
	private final TradingRecipe tradingRecipe;
	private final UnmodifiableItemStack offeredItem1;
	private final UnmodifiableItemStack offeredItem2; // Can be null
	private final boolean swappedItemOrder;
	private boolean cancelled = false;

	// The offered items are expected to be immutable and their stack sizes match the trading recipe items.
	public ShopkeeperTradeEvent(Shopkeeper shopkeeper, Player player, InventoryClickEvent clickEvent, TradingRecipe tradingRecipe,
								UnmodifiableItemStack offeredItem1, UnmodifiableItemStack offeredItem2, boolean swappedItemOrder) {
		super(shopkeeper);
		this.player = player;
		this.clickEvent = clickEvent;
		this.tradingRecipe = tradingRecipe;
		this.offeredItem1 = offeredItem1;
		this.offeredItem2 = offeredItem2;
		this.swappedItemOrder = swappedItemOrder;
	}

	/**
	 * Gets the trading player.
	 * 
	 * @return the trading player
	 */
	public Player getPlayer() {
		return player;
	}

	/**
	 * Gets the {@link InventoryClickEvent} which originally triggered this trade.
	 * 
	 * <p>
	 * Do not modify the click event (it has to be kept cancelled) or any of the involved items!
	 * 
	 * @return the original {@link InventoryClickEvent}
	 */
	public InventoryClickEvent getClickEvent() {
		return clickEvent;
	}

	/**
	 * Gets the trading recipe used by this trade.
	 * 
	 * @return the used trading recipe
	 */
	public TradingRecipe getTradingRecipe() {
		return tradingRecipe;
	}

	/**
	 * Gets an unmodifiable view on the item offered by the player that matches the first required item of the used
	 * trading recipe. This is not necessarily the item in the first slot.
	 * <p>
	 * The type and stack size equal those of the required item of the trading recipe. The metadata however can differ,
	 * but still be accepted for the trade depending on the item matching rules of the used Minecraft version and the
	 * shopkeeper settings (i.e. with strict item comparisons being disabled).
	 * 
	 * @return an unmodifiable view on the offered item that matches the first required item, not <code>null</code> or
	 *         empty
	 */
	public UnmodifiableItemStack getOfferedItem1() {
		return offeredItem1;
	}

	/**
	 * Gets an unmodifiable view on the item offered by the player that matches the second required item of the used
	 * trading recipe. This is not necessarily the item in the second slot.
	 * <p>
	 * The type and stack size equal those of the required item of the trading recipe. The metadata however can differ,
	 * but still be accepted for the trade depending on the item matching rules of the used Minecraft version and the
	 * shopkeeper settings (i.e. with strict item comparisons being disabled).
	 * 
	 * @return an unmodifiable view on the offered item that matches the second required item, can be <code>null</code>
	 */
	public UnmodifiableItemStack getOfferedItem2() {
		return offeredItem2;
	}

	/**
	 * Checks whether this is a trade with two input items.
	 * <p>
	 * This is a shortcut for checking if {@link #getOfferedItem2()} is not <code>null</code>.
	 * 
	 * @return <code>true</code> if this is a trade with two input items
	 */
	public boolean hasOfferedItem2() {
		return offeredItem2 != null;
	}

	/**
	 * Whether the offered items are placed in reverse or regular order inside the trading slots of the merchant
	 * inventory.
	 * <p>
	 * Minecraft checks for matching trading recipes for both combinations.
	 * 
	 * @return <code>true</code> if the offered items are placed in reverse order
	 */
	public boolean isItemOrderSwapped() {
		return swappedItemOrder;
	}

	/**
	 * If cancelled the trade will not take place.
	 */
	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	/**
	 * If cancelled the trade will not take place.
	 */
	@Override
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}

	private static final HandlerList handlers = new HandlerList();

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
