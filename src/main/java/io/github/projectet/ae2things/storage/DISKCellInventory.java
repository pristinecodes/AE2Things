package io.github.projectet.ae2things.storage;

import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.config.IncludeExclude;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.cells.CellState;
import appeng.api.storage.cells.ISaveProvider;
import appeng.api.storage.cells.StorageCell;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.core.AELog;
import appeng.core.definitions.AEItems;
import appeng.util.ConfigInventory;
import appeng.util.prioritylist.FuzzyPriorityList;
import appeng.util.prioritylist.IPartitionList;
import io.github.projectet.ae2things.AE2Things;
import io.github.projectet.ae2things.item.DISKDrive;
import io.github.projectet.ae2things.util.Constants;
import io.github.projectet.ae2things.util.DataStorage;
import io.github.projectet.ae2things.util.StorageManager;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.Text;

;

public class DISKCellInventory implements StorageCell {

    private final IDISKCellItem cellType;
    public static final String ITEM_COUNT_TAG = "ic";
    public static final String STACK_KEYS = "keys";
    public static final String STACK_AMOUNTS = "amts";

    private final ISaveProvider container;
    private final AEKeyType keyType;
    private IPartitionList partitionList;
    private IncludeExclude partitionListMode;
    private int storedItems;
    private long storedItemCount;
    private Object2LongMap<AEKey> storedAmounts;
    private final ItemStack i;
    private boolean isPersisted = true;

    public DISKCellInventory(IDISKCellItem cellType, ItemStack stack, ISaveProvider saveProvider) {
        this.cellType = cellType;
        this.i = stack;
        this.container = saveProvider;
        this.keyType = cellType.getKeyType();
        this.storedAmounts = null;
        initData();

        updateFilter();
    }

    private void updateFilter() {
        var builder = IPartitionList.builder();

        var upgrades = getUpgradesInventory();
        var config = getConfigInventory();

        boolean hasInverter = upgrades.isInstalled(AEItems.INVERTER_CARD);
        if (upgrades.isInstalled(AEItems.FUZZY_CARD)) {
            builder.fuzzyMode(getFuzzyMode());
        }

        builder.addAll(config.keySet());

        partitionListMode = (hasInverter ? IncludeExclude.BLACKLIST : IncludeExclude.WHITELIST);
        partitionList = builder.build();
    }

    private DataStorage getDiskStorage() {
        if(getDiskUUID() != null)
            return getStorageInstance().getOrCreateDisk(getDiskUUID());
        else
            return DataStorage.EMPTY;
    }

    private void initData() {
        if(hasDiskUUID()) {
            this.storedItems = getDiskStorage().stackAmounts.length;
            this.storedItemCount = getDiskStorage().itemCount;
        }
        else {
            this.storedItems = 0;
            this.storedItemCount = 0;
            getCellItems();
        }
    }

    public IncludeExclude getPartitionListMode() {
        return partitionListMode;
    }

    public boolean isPreformatted() {
        return !partitionList.isEmpty();
    }

    public boolean isFuzzy() {
        return partitionList instanceof FuzzyPriorityList;
    }

    public ConfigInventory getConfigInventory() {
        return this.cellType.getConfigInventory(this.i);
    }

    public FuzzyMode getFuzzyMode() {
        return this.cellType.getFuzzyMode(this.i);
    }

    public IUpgradeInventory getUpgradesInventory() {
        return this.cellType.getUpgrades(this.i);
    }

    @Override
    public CellState getStatus() {
        if (this.getStoredItemCount() == 0) {
            return CellState.EMPTY;
        }
        if (this.canHoldNewItem()) {
            return CellState.NOT_EMPTY;
        }
        return CellState.FULL;
    }

    public CellState getClientStatus() {
        if (this.getNbtItemCount() == 0) {
            return CellState.EMPTY;
        }
        if ((getNbtItemCount() > 0 && getNbtItemCount() != getTotalBytes())) {
            return CellState.NOT_EMPTY;
        }
        return CellState.FULL;
    }

    @Override
    public double getIdleDrain() {
        return this.cellType.getIdleDrain();
    }

    @Override
    public void persist() {
        if (this.isPersisted) {
            return;
        }

        if(storedItemCount == 0) {
            if(hasDiskUUID()) {
                getStorageInstance().removeDisk(getDiskUUID());
                i.getNbt().remove(Constants.DISKUUID);
                i.getNbt().remove(DISKCellInventory.ITEM_COUNT_TAG);
                initData();
            }
            return;
        }

        long itemCount = 0;

        // add new pretty stuff...
        var amounts = new LongArrayList(storedAmounts.size());
        var keys = new NbtList();

        for (var entry : this.storedAmounts.object2LongEntrySet()) {
            long amount = entry.getLongValue();

            if (amount > 0) {
                itemCount += amount;
                keys.add(entry.getKey().toTagGeneric());
                amounts.add(amount);
            }
        }

        if (keys.isEmpty()) {
            getStorageInstance().updateDisk(getDiskUUID(), new DataStorage());
        } else {
            getStorageInstance().modifyDisk(getDiskUUID(), keys, amounts.toArray(new long[0]), itemCount);
        }

        this.storedItems = (short) this.storedAmounts.size();

        this.storedItemCount = itemCount;
        i.getOrCreateNbt().putLong(DISKCellInventory.ITEM_COUNT_TAG, itemCount);

        this.isPersisted = true;
    }

    @Override
    public Text getDescription() {
        return null;
    }

    public static DISKCellInventory createInventory(ItemStack stack, ISaveProvider saveProvider) {
        Objects.requireNonNull(stack, "Cannot create cell inventory for null itemstack");

        if (!(stack.getItem() instanceof IDISKCellItem cellType)) {
            return null;
        }

        if (!cellType.isStorageCell(stack)) {
            // This is not an error. Items may decide to not be a storage cell temporarily.
            return null;
        }

        // The cell type's channel matches, so this cast is safe
        return new DISKCellInventory(cellType, stack, saveProvider);
    }

    public boolean hasDiskUUID() {
        return i.hasNbt() && i.getOrCreateNbt().contains(Constants.DISKUUID);
    }

    public static boolean hasDiskUUID(ItemStack disk) {
        if(disk.getItem() instanceof IDISKCellItem) {
            return disk.hasNbt() && disk.getOrCreateNbt().contains(Constants.DISKUUID);
        }
        return false;
    }

    public UUID getDiskUUID() {
        if(hasDiskUUID())
            return i.getOrCreateNbt().getUuid(Constants.DISKUUID);
        else return null;
    }

    private boolean isStorageCell(AEItemKey key) {
        var type = getStorageCell(key);
        return type != null && !type.storableInStorageCell();
    }

    private static DISKDrive getStorageCell(AEItemKey itemKey) {
        if (itemKey.getItem() instanceof DISKDrive diskDrive) {
            return diskDrive;
        }

        return null;
    }

    private static boolean isCellEmpty(DISKCellInventory inv) {
        if (inv != null) {
            return inv.getAvailableStacks().isEmpty();
        }
        return true;
    }

    protected Object2LongMap<AEKey> getCellItems() {
        if (this.storedAmounts == null) {
            this.storedAmounts = new Object2LongOpenHashMap<>();
            this.loadCellItems();
        }

        return this.storedAmounts;
    }

    @Override
    public void getAvailableStacks(KeyCounter out) {
        for (var entry : this.getCellItems().object2LongEntrySet()) {
            out.add(entry.getKey(), entry.getLongValue());
        }
    }

    private void loadCellItems() {
        boolean corruptedTag = false;

        if(!i.hasNbt()) {
            return;
        }

        var amounts = getDiskStorage().stackAmounts;
        var tags = getDiskStorage().stackKeys;
        if (amounts.length != tags.size()) {
            AELog.warn("Loading storage cell with mismatched amounts/tags: %d != %d",
                    amounts.length, tags.size());
        }

        for (int i = 0; i < amounts.length; i++) {
            var amount = amounts[i];
            AEKey key = AEKey.fromTagGeneric(tags.getCompound(i));

            if (amount <= 0 || key == null) {
                corruptedTag = true;
            } else {
                storedAmounts.put(key, amount);
            }
        }

        if (corruptedTag) {
            this.saveChanges();
        }
    }

    private StorageManager getStorageInstance() {
        return AE2Things.STORAGE_INSTANCE;
    }

    protected void saveChanges() {
        // recalculate values
        this.storedItems = this.storedAmounts.size();
        this.storedItemCount = 0;
        for (var storedAmount : this.storedAmounts.values()) {
            this.storedItemCount += storedAmount;
        }

        this.isPersisted = false;
        if (this.container != null) {
            this.container.saveChanges();
        } else {
            // if there is no ISaveProvider, store to NBT immediately
            this.persist();
        }
    }

    public long getRemainingItemCount() {
        return this.getFreeBytes() > 0 ? this.getFreeBytes() : 0;
    }

    @Override
    public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {

        if (amount == 0 || !keyType.contains(what)) {
            return 0;
        }

        if (!this.partitionList.matchesFilter(what, this.partitionListMode)) {
            return 0;
        }

        if (this.cellType.isBlackListed(this.i, what)) {
            return 0;
        }

        // This is slightly hacky as it expects a read-only access, but fine for now.
        // TODO: Guarantee a read-only access. E.g. provide an isEmpty() method and
        // ensure CellInventory does not write
        // any NBT data for empty cells instead of relying on an empty IAEStackList
        if (what instanceof AEItemKey itemKey && this.isStorageCell(itemKey)) {
            // TODO: make it work for any cell, and not just BasicCellInventory!
            var meInventory = createInventory(itemKey.toStack(), null);
            if (!isCellEmpty(meInventory)) {
                return 0;
            }
        }

        if(!hasDiskUUID()) {
            i.getOrCreateNbt().putUuid(Constants.DISKUUID, UUID.randomUUID());
            getStorageInstance().getOrCreateDisk(getDiskUUID());
            loadCellItems();
        }

        var currentAmount = this.getCellItems().getLong(what);
        long remainingItemCount = getRemainingItemCount();


        if (amount > remainingItemCount) {
            amount = remainingItemCount;
        }

        if (mode == Actionable.MODULATE) {
            getCellItems().put(what, currentAmount + amount);
            this.saveChanges();
        }

        return amount;
    }

    @Override
    public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
        // To avoid long-overflow on the extracting callers side
        var extractAmount = Math.min(Integer.MAX_VALUE, amount);

        var currentAmount = getCellItems().getLong(what);
        if (currentAmount > 0) {
            if (extractAmount >= currentAmount) {
                if (mode == Actionable.MODULATE) {
                    getCellItems().remove(what, currentAmount);
                    this.saveChanges();
                }

                return currentAmount;
            } else {
                if (mode == Actionable.MODULATE) {
                    getCellItems().put(what, currentAmount - extractAmount);
                    this.saveChanges();
                }

                return extractAmount;
            }
        }

        return 0;
    }

    public long getTotalBytes() {
        return this.cellType.getBytes(this.i);
    }

    public long getFreeBytes() {
        return this.getTotalBytes() - this.getStoredItemCount();
    }

    public long getNbtItemCount() {
        if(hasDiskUUID()) {
            return i.getNbt().getLong(DISKCellInventory.ITEM_COUNT_TAG);
        }
        return 0;
    }

    public long getStoredItemCount() {
        return this.storedItemCount;
    }

    public long getStoredItemTypes() {
        return this.storedItems;
    }

    public boolean canHoldNewItem() {
        return (getFreeBytes() > 0 && getFreeBytes() != getTotalBytes());
    }
}
