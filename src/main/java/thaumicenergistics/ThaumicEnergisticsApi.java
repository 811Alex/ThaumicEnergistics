package thaumicenergistics;

import thaumicenergistics.api.*;
import thaumicenergistics.api.wireless.IThEWireless;
import thaumicenergistics.init.ThEBlocks;
import thaumicenergistics.init.ThEItems;
import thaumicenergistics.lang.ThELang;
import thaumicenergistics.upgrade.ThEUpgrades;
import thaumicenergistics.wireless.ThEWireless;

/**
 * @author BrockWS
 */
public class ThaumicEnergisticsApi implements IThEApi {

    private static IThEApi INSTANCE;
    private IThEItems items;
    private IThEBlocks blocks;
    private IThEUpgrades upgrades;
    private IThEWireless wireless;
    private IThEConfig config;
    private IThELang lang;

    private ThaumicEnergisticsApi() {
        this.items = new ThEItems();
        this.blocks = new ThEBlocks();
        this.upgrades = new ThEUpgrades(this.items());
        this.wireless = new ThEWireless();
        this.config = new ThEConfig();
        this.lang = new ThELang();
    }

    public static IThEApi instance() {
        if (INSTANCE == null)
            INSTANCE = new ThaumicEnergisticsApi();
        return INSTANCE;
    }

    @Override
    public IThEItems items() {
        return this.items;
    }

    @Override
    public IThEBlocks blocks() {
        return this.blocks;
    }

    @Override
    public IThEUpgrades upgrades() {
        return this.upgrades;
    }

    @Override
    public IThEWireless wireless() {
        return this.wireless;
    }

    @Override
    public IThEConfig config() {
        return this.config;
    }

    @Override
    public IThELang lang() {
        return this.lang;
    }
}
