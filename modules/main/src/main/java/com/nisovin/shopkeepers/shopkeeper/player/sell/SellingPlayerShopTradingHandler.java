package com.nisovin.shopkeepers.shopkeeper.player.sell;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.Nullable;

import com.nisovin.shopkeepers.api.internal.util.Unsafe;
import com.nisovin.shopkeepers.api.shopkeeper.TradingRecipe;
import com.nisovin.shopkeepers.api.shopkeeper.offers.PriceOffer;
import com.nisovin.shopkeepers.api.util.UnmodifiableItemStack;
import com.nisovin.shopkeepers.config.Settings;
import com.nisovin.shopkeepers.currency.Currencies;
import com.nisovin.shopkeepers.currency.Currency;
import com.nisovin.shopkeepers.lang.Messages;
import com.nisovin.shopkeepers.shopkeeper.player.PlayerShopTradingHandler;
import com.nisovin.shopkeepers.ui.trading.Trade;
import com.nisovin.shopkeepers.util.bukkit.TextUtils;
import com.nisovin.shopkeepers.util.inventory.InventoryUtils;

public class SellingPlayerShopTradingHandler extends PlayerShopTradingHandler {

	protected SellingPlayerShopTradingHandler(SKSellingPlayerShopkeeper shopkeeper) {
		super(shopkeeper);
	}

	@Override
	public SKSellingPlayerShopkeeper getShopkeeper() {
		return (SKSellingPlayerShopkeeper) super.getShopkeeper();
	}

	@Override
	protected boolean prepareTrade(Trade trade) {
		if (!super.prepareTrade(trade)) return false;
		SKSellingPlayerShopkeeper shopkeeper = this.getShopkeeper();
		Player tradingPlayer = trade.getTradingPlayer();
		TradingRecipe tradingRecipe = trade.getTradingRecipe();

		// Get offer for this type of item:
		UnmodifiableItemStack soldItem = tradingRecipe.getResultItem();
		PriceOffer offer = shopkeeper.getOffer(soldItem);
		if (offer == null) {
			// Unexpected, because the recipes were created based on the shopkeeper's offers.
			TextUtils.sendMessage(tradingPlayer, Messages.cannotTradeUnexpectedTrade);
			this.debugPreventedTrade(
					tradingPlayer,
					"Could not find the offer corresponding to the trading recipe!"
			);
			return false;
		}

		// Validate the found offer:
		int expectedSoldItemAmount = offer.getItem().getAmount();
		if (expectedSoldItemAmount != soldItem.getAmount()) {
			// Unexpected, because the recipe was created based on this offer.
			TextUtils.sendMessage(tradingPlayer, Messages.cannotTradeUnexpectedTrade);
			this.debugPreventedTrade(
					tradingPlayer,
					"The offer does not match the trading recipe!"
			);
			return false;
		}

		@Nullable ItemStack[] newContainerContents = Unsafe.assertNonNull(this.newContainerContents);

		// Remove result items from container contents:
		if (InventoryUtils.removeItems(newContainerContents, soldItem) != 0) {
			TextUtils.sendMessage(tradingPlayer, Messages.cannotTradeInsufficientStock);
			this.debugPreventedTrade(
					tradingPlayer,
					"The shop's container does not contain the required items."
			);
			return false;
		}

		// Add earnings to container contents:
		// TODO Maybe add the actual items the trading player gave, instead of creating new currency
		// items?
		int amountAfterTaxes = this.getAmountAfterTaxes(offer.getPrice());
		if (amountAfterTaxes > 0) {
			// TODO Always store the currency in the most compressed form possible, regardless of
			// 'highCurrencyMinCost'?
			int remaining = amountAfterTaxes;
			if (Currencies.isHighCurrencyEnabled() && remaining > Settings.highCurrencyMinCost) {
				Currency highCurrency = Currencies.getHigh();
				int highCurrencyAmount = (remaining / highCurrency.getValue());
				if (highCurrencyAmount > 0) {
					ItemStack currencyItems = highCurrency.getItemData().createItemStack(highCurrencyAmount);
					int remainingHighCurrency = InventoryUtils.addItems(newContainerContents, currencyItems);
					remaining -= (highCurrencyAmount - remainingHighCurrency) * highCurrency.getValue();
				}
			}
			if (remaining > 0) {
				ItemStack currencyItems = Currencies.getBase().getItemData().createItemStack(remaining);
				if (InventoryUtils.addItems(newContainerContents, currencyItems) != 0) {
					TextUtils.sendMessage(tradingPlayer, Messages.cannotTradeInsufficientStorageSpace);
					this.debugPreventedTrade(
							tradingPlayer,
							"The shop's container cannot hold the traded items."
					);
					return false;
				}
			}
		}
		return true;
	}
}
