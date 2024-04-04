package io.github.projectet.ae2things.item;

import appeng.api.client.StorageCellModels;
import appeng.core.definitions.AEItems;
import appeng.items.materials.StorageComponentItem;
import appeng.items.materials.UpgradeCardItem;
import io.github.projectet.ae2things.AE2Things;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class AETItems {

    public static final Identifier MODEL_DISK_DRIVE_1K = AE2Things.id("model/drive/cells/disk_1k");
    public static final Identifier MODEL_DISK_DRIVE_4K = AE2Things.id("model/drive/cells/disk_4k");
    public static final Identifier MODEL_DISK_DRIVE_16K = AE2Things.id("model/drive/cells/disk_16k");
    public static final Identifier MODEL_DISK_DRIVE_64K = AE2Things.id("model/drive/cells/disk_64k");

    public static final FabricItemSettings DEFAULT_SETTINGS = new FabricItemSettings();

    public static final List<Pair<Identifier, ? extends Item>> ITEMS = new ArrayList<>();

    public static final Item DISK_HOUSING = item(new Item(DEFAULT_SETTINGS.maxCount(64).fireproof()),"disk_housing");
    public static final DISKDrive DISK_DRIVE_1K = registerCell(AETItems.MODEL_DISK_DRIVE_1K, new DISKDrive(AEItems.CELL_COMPONENT_1K, 1, 0.5f), "disk_drive_1k");
    public static final DISKDrive DISK_DRIVE_4K = registerCell(AETItems.MODEL_DISK_DRIVE_4K, new DISKDrive(AEItems.CELL_COMPONENT_4K, 4, 1.0f), "disk_drive_4k");
    public static final DISKDrive DISK_DRIVE_16K = registerCell(AETItems.MODEL_DISK_DRIVE_16K, new DISKDrive(AEItems.CELL_COMPONENT_16K, 16, 1.5f), "disk_drive_16k");
    public static final DISKDrive DISK_DRIVE_64K = registerCell(AETItems.MODEL_DISK_DRIVE_64K, new DISKDrive(AEItems.CELL_COMPONENT_64K, 64, 2.0f), "disk_drive_64k");
    public static final Item PORTABLE_DISK_1K = registerPortableDISK("portable_disk_1k", AEItems.CELL_COMPONENT_1K.asItem());
    public static final Item PORTABLE_DISK_4K = registerPortableDISK("portable_disk_4k", AEItems.CELL_COMPONENT_4K.asItem());
    public static final Item PORTABLE_DISK_16K = registerPortableDISK("portable_disk_16k", AEItems.CELL_COMPONENT_16K.asItem());
    public static final Item PORTABLE_DISK_64K = registerPortableDISK("portable_disk_64k", AEItems.CELL_COMPONENT_64K.asItem());

    public static final UpgradeCardItem FORTUNE_CARD = item(new UpgradeCardItem(DEFAULT_SETTINGS.maxCount(64)), "fortune_upgrade");

    public static void init() {
        for(Pair<Identifier, ? extends Item> pair : ITEMS) {
            Registry.register(Registries.ITEM, pair.getLeft(), pair.getRight());
        }
    }

    private static <T extends Item> T item(T item, String id) {
        ITEMS.add(new Pair<>(AE2Things.id(id), item));

        return item;
    }

    private static <T extends Item> T registerCell(Identifier model, T item, String id) {
        StorageCellModels.registerModel(item, model);

        return item(item, id);
    }

    private static Item registerPortableDISK(String id, StorageComponentItem sizeComponent) {
        return item(new PortableDISKItem(sizeComponent, DEFAULT_SETTINGS.maxCount(1).fireproof()), id);
    }

}
