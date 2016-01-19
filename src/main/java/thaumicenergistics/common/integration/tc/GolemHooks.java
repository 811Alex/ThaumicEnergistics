package thaumicenergistics.common.integration.tc;

import java.util.HashMap;
import java.util.HashSet;
import net.minecraft.entity.DataWatcher;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import thaumcraft.common.entities.golems.EntityGolemBase;
import thaumicenergistics.api.entities.IGolemHookHandler;
import thaumicenergistics.api.entities.IGolemHookSyncRegistry;
import thaumicenergistics.common.utils.EffectiveSide;
import thaumicenergistics.common.utils.ThELog;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class GolemHooks
{
	private static class DummyHookHandler
		implements IGolemHookHandler
	{

		public DummyHookHandler()
		{
		}

		@Override
		public void addDefaultSyncEntries( final IGolemHookSyncRegistry syncRegistry )
		{

		}

		@Override
		public boolean canHandleInteraction( final EntityGolemBase golem, final Object handlerData, final EntityPlayer player, final Side side )
		{
			return false;
		}

		@Override
		public Object customInteraction( final EntityGolemBase golem, final Object handlerData, final EntityPlayer player, final Side side )
		{
			return null;
		}

		@Override
		public boolean needsRenderer()
		{
			return false;
		}

		@Override
		public void onBellLeftClick( final EntityGolemBase golem, final Object handlerData, final ItemStack itemGolemPlacer,
										final EntityPlayer player, final boolean dismantled,
										final Side side )
		{

		}

		@Override
		public Object onSyncDataChanged( final IGolemHookSyncRegistry syncData, final Object clientHandlerData )
		{
			return null;
		}

		@Override
		public Object readEntityFromNBT( final EntityGolemBase golem, final IGolemHookSyncRegistry syncData, final NBTTagCompound nbtTag )
		{
			return null;
		}

		@Override
		public void renderGolem( final EntityGolemBase golem, final Object clientHandlerData, final double x, final double y, final double z,
									final float partialElaspsedTick )
		{

		}

		@Override
		public Object setupGolem( final EntityGolemBase golem, final Object handlerData, final IGolemHookSyncRegistry syncData, final Side side )
		{
			return null;
		}

		@Override
		public Object spawnGolemFromItemStack( final EntityGolemBase golem, final ItemStack itemGolemPlacer, final Side side )
		{
			return null;
		}

		@Override
		public void writeEntityNBT( final EntityGolemBase golem, final Object serverHandlerData, final NBTTagCompound nbtTag )
		{

		}

	}

	/**
	 * ID of the datawatcher field.
	 */
	private static final int DATAWATCHER_ID = 4;

	/**
	 * Default sync values.
	 */
	private static final GolemSyncRegistry defaultSyncRegistry = new GolemSyncRegistry();

	/**
	 * Internal handler used to track sync data.
	 */
	private static final DummyHookHandler internalHandler = new DummyHookHandler();

	/**
	 * All hook handlers
	 */
	protected static final HashSet<IGolemHookHandler> registeredHandlers = new HashSet<IGolemHookHandler>();

	/**
	 * Handlers that need to be called during render.
	 */
	protected static final HashSet<IGolemHookHandler> renderHandlers = new HashSet<IGolemHookHandler>();

	/**
	 * Logs an exception when it occurs.
	 * 
	 * @param hook
	 * @param handler
	 * @param e
	 */
	private static void logCaughtException( final String hook, final IGolemHookHandler handler, final Exception e )
	{
		if( handler != null )
		{
			if( e != null )
			{
				ThELog.warning( "Caught Exception During call to '" + hook + "' for handler '%s':", handler.getClass().getCanonicalName() );
				ThELog.warning( e.toString() );
			}
			else
			{
				ThELog.warning( "Caught Unknown Exception During call to '" + hook + "' for handler '%s'", handler.getClass().getCanonicalName() );
			}
		}
		else if( e != null )
		{
			ThELog.warning( "Caught Exception During call to null handler '" + hook + "':" );
			ThELog.warning( e.toString() );
		}
		else
		{
			ThELog.warning( "Caught Unknown Exception During call to null handler '" + hook + "'" );
		}

	}

	/**
	 * Hook for ItemGolemBell.onLeftClickEntity
	 * 
	 * @param golem
	 * @param dropped
	 * @param player
	 * @param golemHandlerData
	 */
	public static void hook_Bell_OnLeftClickGolem( final EntityGolemBase golem, final ItemStack dropped, final EntityPlayer player,
													final HashMap<IGolemHookHandler, Object> golemHandlerData )
	{
		// Get the dismantled status
		boolean dismantled = player.isSneaking();
		Side side = EffectiveSide.side();

		for( IGolemHookHandler handler : registeredHandlers )
		{
			try
			{
				// Get the current handler data
				Object handlerData = golemHandlerData.getOrDefault( handler, null );

				// Call handler

				handler.onBellLeftClick( golem, handlerData, dropped, player, dismantled, side );
			}
			catch( Exception e )
			{
				logCaughtException( "onBellLeftClick", handler, e );
			}

		}

	}

	/**
	 * Hook for EntityGolemBase.customInteraction
	 * 
	 * @param golem
	 * @param player
	 * @param golemHandlerData
	 * @return
	 */
	public static boolean hook_CustomInteraction( final EntityGolemBase golem, final EntityPlayer player,
													final HashMap<IGolemHookHandler, Object> golemHandlerData )
	{
		boolean interactionHandled = false;
		Side side = EffectiveSide.side();

		for( IGolemHookHandler handler : registeredHandlers )
		{
			try
			{
				// Get the current handler data
				Object handlerData = golemHandlerData.getOrDefault( handler, null );
				boolean hadData = ( handlerData != null );

				// Call handler
				interactionHandled |= handler.canHandleInteraction( golem, handlerData, player, side );
				if( interactionHandled )
				{
					try
					{
						handlerData = handler.customInteraction( golem, handlerData, player, side );

						// Update golem
						if( handlerData == null )
						{
							if( hadData )
							{
								golemHandlerData.remove( handler );
							}
						}
						else
						{
							golemHandlerData.put( handler, handlerData );
						}
					}
					catch( Exception e )
					{
						logCaughtException( "customInteraction", handler, e );
					}
				}
			}
			catch( Exception e )
			{
				logCaughtException( "canHandleInteraction", handler, e );
			}

		}
		if( interactionHandled && side == Side.SERVER )
		{
			golem.setupGolem();
		}

		return interactionHandled;
	}

	/**
	 * Hook for EntityGolemBase.entityInit
	 * 
	 * @param golem
	 * @param golemHandlerData
	 */
	public static void hook_EntityInit( final EntityGolemBase golem, final HashMap<IGolemHookHandler, Object> golemHandlerData )
	{
		// Create a new sync registry
		GolemSyncRegistry localRegistry = new GolemSyncRegistry();
		localRegistry.copyDefaults( defaultSyncRegistry );

		// Add to the handlers
		golemHandlerData.put( internalHandler, localRegistry );

		// Get the datawatcher
		DataWatcher watcher = golem.getDataWatcher();

		// Add datawatcher field.
		watcher.addObject( DATAWATCHER_ID, localRegistry.mappingsToString() );
	}

	/**
	 * Hook for EntityGolemBase.onEntityUpdate.
	 * Keeps client handlers updated.
	 * 
	 * @param golem
	 * @param golemHandlerData
	 */
	public static void hook_onEntityUpdate( final EntityGolemBase golem, final HashMap<IGolemHookHandler, Object> golemHandlerData )
	{
		if( EffectiveSide.isServerSide() )
		{
			return;
		}

		// Get the sync registry
		GolemSyncRegistry syncRegistry = (GolemSyncRegistry)golemHandlerData.get( internalHandler );

		// Update sync ticks
		++syncRegistry.clientSyncTicks;

		// Have 20 ticks passed? (Roughly a full second if the game is not lagging)
		if( syncRegistry.clientSyncTicks >= 20.0f )
		{
			// Reset the counter
			syncRegistry.clientSyncTicks = 0.0f;

			// Is the data out of sync?
			String watcherString = golem.getDataWatcher().getWatchableObjectString( DATAWATCHER_ID );
			if( !syncRegistry.hasChanged() && ( watcherString == syncRegistry.lastUpdatedFrom ) )
			{
				// Data is in sync
				return;
			}

			// Read the sync data, and get the list of handlers to update
			HashSet<IGolemHookHandler> handlersToUpdate = syncRegistry.readFromString( watcherString );
			if( handlersToUpdate == null )
			{
				// No handlers to update
				return;
			}

			// Inform each handler
			for( IGolemHookHandler handler : handlersToUpdate )
			{
				try
				{
					// Get the current handler data
					Object handlerData = golemHandlerData.getOrDefault( handler, null );
					boolean hadData = ( handlerData != null );

					// Call handler
					handlerData = handler.onSyncDataChanged( syncRegistry, handlerData );

					// Update golem
					if( handlerData == null )
					{
						if( hadData )
						{
							golemHandlerData.remove( handler );
						}
					}
					else
					{
						golemHandlerData.put( handler, handlerData );
					}
				}
				catch( Exception e )
				{
					logCaughtException( "onSyncDataChanged", handler, e );
				}
			}
		}

	}

	/**
	 * Hook for ItemGolemPlacer.spawnCreature
	 * 
	 * @param golem
	 * @param itemGolemPlacer
	 * @param golemHandlerData
	 */
	public static void hook_Placer_SpawnGolem( final EntityGolemBase golem, final ItemStack itemGolemPlacer,
												final HashMap<IGolemHookHandler, Object> golemHandlerData )
	{
		// Ensure the stack has an NBT tag
		if( !itemGolemPlacer.hasTagCompound() )
		{
			return;
		}
		Side side = EffectiveSide.side();

		// Inform each handler
		for( IGolemHookHandler handler : registeredHandlers )
		{
			try
			{
				// Call handler
				Object handlerData = handler.spawnGolemFromItemStack( golem, itemGolemPlacer, side );

				// Update golem
				if( handlerData != null )
				{
					golemHandlerData.put( handler, handlerData );
				}
			}
			catch( Exception e )
			{
				logCaughtException( "spawnGolemFromItemStack", handler, e );
			}
		}
	}

	/**
	 * Hook for EntityGolemBase.readEntityFromNBT
	 * 
	 * @param golem
	 * @param golemHandlerData
	 * @param nbt
	 */
	public static void hook_ReadEntityFromNBT( final EntityGolemBase golem, final HashMap<IGolemHookHandler, Object> golemHandlerData,
												final NBTTagCompound nbt )
	{
		// Get the sync data
		GolemSyncRegistry localSyncRegistry = (GolemSyncRegistry)golemHandlerData.get( internalHandler );

		// Inform each handler
		for( IGolemHookHandler handler : registeredHandlers )
		{
			try
			{
				// Get the current handler data
				Object handlerData = golemHandlerData.getOrDefault( handler, null );

				// Call handler
				handlerData = handler.readEntityFromNBT( golem, localSyncRegistry, nbt );

				// Update golem
				if( handlerData == null )
				{
					golemHandlerData.remove( handler );
				}
				else
				{
					golemHandlerData.put( handler, handlerData );
				}
			}
			catch( Exception e )
			{
				logCaughtException( "readEntityFromNBT", handler, e );
			}
		}
	}

	/**
	 * Hook for RenderGolemBase.render
	 * 
	 * @param golem
	 * @param x
	 * @param y
	 * @param z
	 * @param partialElaspsedTick
	 */
	@SideOnly(Side.CLIENT)
	public static void hook_RenderGolem( final EntityGolemBase golem, final HashMap<IGolemHookHandler, Object> golemHandlerData, final double x,
											final double y, final double z, final float partialElaspsedTick )
	{

		// Call each render handler
		for( IGolemHookHandler handler : renderHandlers )
		{
			try
			{
				handler.renderGolem( golem, golemHandlerData.get( handler ), x, y, z, partialElaspsedTick );
			}
			catch( Exception e )
			{
				logCaughtException( "renderGolem", handler, e );
			}
		}
	}

	/**
	 * Hook for EntityGolemBase.setupGolem
	 * 
	 * @param golem
	 * @param golemHandlerData
	 */
	public static void hook_SetupGolem( final EntityGolemBase golem, final HashMap<IGolemHookHandler, Object> golemHandlerData )
	{
		// Get the sync data
		GolemSyncRegistry localRegistry = (GolemSyncRegistry)golemHandlerData.get( internalHandler );
		Side side = EffectiveSide.side();

		// Inform each handler
		for( IGolemHookHandler handler : registeredHandlers )
		{
			try
			{
				// Get the current handler data
				Object handlerData = golemHandlerData.getOrDefault( handler, null );
				boolean hadData = ( handlerData != null );

				// Call handler
				handlerData = handler.setupGolem( golem, handlerData, localRegistry, side );

				// Update golem
				if( handlerData == null )
				{
					if( hadData )
					{
						golemHandlerData.remove( handler );
					}
				}
				else
				{
					golemHandlerData.put( handler, handlerData );
				}
			}
			catch( Exception e )
			{
				logCaughtException( "setupGolem", handler, e );
			}
		}

		// Update data watcher
		if( localRegistry.hasChanged() && side == Side.SERVER )
		{
			golem.getDataWatcher().updateObject( DATAWATCHER_ID, localRegistry.mappingsToString() );
		}
	}

	/**
	 * Hook for EntityGolemBase.writeEntityToNBT
	 * 
	 * @param golem
	 * @param golemHandlerData
	 * @param nbt
	 */
	public static void hook_WriteEntityToNBT( final EntityGolemBase golem, final HashMap<IGolemHookHandler, Object> golemHandlerData,
												final NBTTagCompound nbt )
	{
		// Inform each handler
		for( IGolemHookHandler handler : registeredHandlers )
		{
			try
			{
				// Get the current handler data
				Object handlerData = golemHandlerData.getOrDefault( handler, null );

				// Call handler
				handler.writeEntityNBT( golem, handlerData, nbt );
			}
			catch( Exception e )
			{
				logCaughtException( "writeEntityNBT", handler, e );
			}
		}
	}

	/**
	 * Registers a handler.
	 * 
	 * @param handler
	 */
	public static void registerHandler( final IGolemHookHandler handler )
	{
		// Add the handler
		registeredHandlers.add( handler );

		// Needs render?
		if( handler.needsRenderer() )
		{
			renderHandlers.add( handler );
		}

		// Register sync data
		defaultSyncRegistry.canRegister = true;
		handler.addDefaultSyncEntries( defaultSyncRegistry );
		defaultSyncRegistry.canRegister = false;
	}

}