package com.nisovin.shopkeepers.shopobjects.sign;

import java.util.Collections;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import com.nisovin.shopkeepers.api.internal.util.Unsafe;
import com.nisovin.shopkeepers.api.shopkeeper.ShopCreationData;
import com.nisovin.shopkeepers.api.shopobjects.sign.SignShopObjectType;
import com.nisovin.shopkeepers.config.Settings;
import com.nisovin.shopkeepers.lang.Messages;
import com.nisovin.shopkeepers.shopkeeper.AbstractShopkeeper;
import com.nisovin.shopkeepers.shopobjects.block.AbstractBlockShopObjectType;
import com.nisovin.shopkeepers.util.bukkit.BlockFaceUtils;
import com.nisovin.shopkeepers.util.bukkit.TextUtils;

public final class SKSignShopObjectType
		extends AbstractBlockShopObjectType<@NonNull SKSignShopObject>
		implements SignShopObjectType<@NonNull SKSignShopObject> {

	private final SignShops signShops;

	public SKSignShopObjectType(SignShops signShops) {
		super("sign", Collections.emptyList(), "shopkeeper.sign", SKSignShopObject.class);
		this.signShops = signShops;
	}

	@Override
	public boolean isEnabled() {
		return Settings.enableSignShops;
	}

	@Override
	public String getDisplayName() {
		return Messages.shopObjectTypeSign;
	}

	@Override
	public boolean mustBeSpawned() {
		return true; // Despawn signs on chunk unload, and spawn them again on chunk load
	}

	@Override
	public boolean validateSpawnLocation(
			@Nullable Player creator,
			@Nullable Location spawnLocation,
			@Nullable BlockFace attachedBlockFace
	) {
		if (!super.validateSpawnLocation(creator, spawnLocation, attachedBlockFace)) {
			return false;
		}
		assert spawnLocation != null;
		Unsafe.assertNonNull(spawnLocation);

		// Block has to be empty:
		Block spawnBlock = spawnLocation.getBlock();
		if (!spawnBlock.isEmpty()) {
			if (creator != null) {
				TextUtils.sendMessage(creator, Messages.spawnBlockNotEmpty);
			}
			return false;
		}

		// If sign posts are disabled, only wall sign block faces (or null) are allowed:
		if (attachedBlockFace == BlockFace.DOWN
				|| (attachedBlockFace == BlockFace.UP && !Settings.enableSignPostShops)
				|| (attachedBlockFace != null && !BlockFaceUtils.isBlockSide(attachedBlockFace))) {
			if (creator != null) {
				TextUtils.sendMessage(creator, Messages.invalidSpawnBlockFace);
			}
			return false;
		}

		return true;
	}

	@Override
	public SKSignShopObject createObject(
			AbstractShopkeeper shopkeeper,
			@Nullable ShopCreationData creationData
	) {
		return new SKSignShopObject(signShops, shopkeeper, creationData);
	}
}
