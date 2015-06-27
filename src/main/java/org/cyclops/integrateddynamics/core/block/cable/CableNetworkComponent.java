package org.cyclops.integrateddynamics.core.block.cable;

import net.minecraft.block.Block;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.helper.TileHelpers;
import org.cyclops.integrateddynamics.core.network.Network;
import org.cyclops.integrateddynamics.core.path.CablePathElement;
import org.cyclops.integrateddynamics.core.tileentity.ITileCable;
import org.cyclops.integrateddynamics.core.tileentity.ITileCableNetwork;

/**
 * A component for {@link ICableNetwork}.
 * @author rubensworks
 */
public class CableNetworkComponent<C extends Block & ICableNetwork<CablePathElement>> implements ICableNetwork<CablePathElement> {

    private final C cable;

    public CableNetworkComponent(C cable) {
        this.cable = cable;
    }

    @Override
    public CablePathElement createPathElement(World world, BlockPos blockPos) {
        return new CablePathElement(cable, DimPos.of(world, blockPos));
    }

    @Override
    public void initNetwork(World world, BlockPos pos) {
        Network.initiateNetworkSetup(cable, world, pos).initialize();
    }

    @Override
    public boolean canConnect(World world, BlockPos pos, ICable connector, EnumFacing side) {
        ITileCable tile = TileHelpers.getSafeTile(world, pos, ITileCable.class);
        return tile != null && tile.canConnect(connector, side);
    }

    @Override
    public void updateConnections(World world, BlockPos pos) {
        ITileCable tile = TileHelpers.getSafeTile(world, pos, ITileCable.class);
        if(tile != null) {
            tile.updateConnections();
            world.markBlockRangeForRenderUpdate(pos, pos);
        }
    }

    @Override
    public boolean isConnected(World world, BlockPos pos, EnumFacing side) {
        ITileCable tile = TileHelpers.getSafeTile(world, pos, ITileCable.class);
        return tile != null && tile.isConnected(side);
    }

    @Override
    public void disconnect(World world, BlockPos pos, EnumFacing side) {
        ITileCable tile = TileHelpers.getSafeTile(world, pos, ITileCable.class);
        if(tile != null) {
            tile.disconnect(side);
        }
    }

    @Override
    public void resetCurrentNetwork(World world, BlockPos pos) {
        ITileCableNetwork tile = TileHelpers.getSafeTile(world, pos, ITileCableNetwork.class);
        if(tile != null) {
            tile.resetCurrentNetwork();
        }
    }

    @Override
    public void setNetwork(Network network, World world, BlockPos pos) {
        ITileCableNetwork tile = TileHelpers.getSafeTile(world, pos, ITileCableNetwork.class);
        if(tile != null) {
            tile.setNetwork(network);
        }
    }

    @Override
    public Network getNetwork(World world, BlockPos pos) {
        ITileCableNetwork tile = TileHelpers.getSafeTile(world, pos, ITileCableNetwork.class);
        if(tile != null) {
            return tile.getNetwork();
        }
        return null;
    }

    /**
     * Request to update the cable connections at the given position.
     * @param world The world.
     * @param pos The position of this block.
     */
    public void requestConnectionsUpdate(World world, BlockPos pos) {
        Block block = world.getBlockState(pos).getBlock();
        if(block instanceof ICable) {
            ((ICable) block).updateConnections(world, pos);
        }
    }

    /**
     * Trigger a connections update for all neighbours.
     * @param world The world.
     * @param blockPos The center positions.
     */
    public void triggerNeighbourConnections(World world, BlockPos blockPos) {
        for(EnumFacing side : EnumFacing.VALUES) {
            requestConnectionsUpdate(world, blockPos.offset(side));
        }
    }

    /**
     * Add this block to a network.
     * @param world The world.
     * @param pos The position.
     */
    public void addToNetwork(World world, BlockPos pos) {
        triggerNeighbourConnections(world, pos);
        if(!world.isRemote) {
            initNetwork(world, pos);
        }
    }

    /**
     * Remove this block from its current network.
     * @param world The world.
     * @param pos The position.
     */
    public void removeFromNetwork(World world, BlockPos pos) {
        removeFromNetwork(world, pos, true);
        removeFromNetwork(world, pos, false);
    }

    /**
     * Remove this block from its current network.
     * @param world The world.
     * @param pos The position.
     * @param preDestroy At which stage of the block destruction this is being called.
     */
    public void removeFromNetwork(World world, BlockPos pos, boolean preDestroy) {
        if(preDestroy) {
            // Remove the cable from this network if it exists
            Network network = getNetwork(world, pos);
            if(network != null) {
                network.removeCable(cable, createPathElement(world, pos));
            }
        } else {
            triggerNeighbourConnections(world, pos);
            // Reinit neighbouring networks.
            for(EnumFacing side : EnumFacing.VALUES) {
                if(!world.isRemote) {
                    BlockPos sidePos = pos.offset(side);
                    Block block = world.getBlockState(sidePos).getBlock();
                    if(block instanceof ICableNetwork) {
                        ((ICableNetwork<CablePathElement>) block).initNetwork(world, sidePos);
                    }
                }
            }
        }
    }

    /**
     * Called before this block is destroyed.
     * @param world The world.
     * @param pos The position.
     */
    public void onPreBlockDestroyed(World world, BlockPos pos) {
        removeFromNetwork(world, pos, true);
    }

    /**
     * Called before after block is destroyed.
     * @param world The world.
     * @param pos The position.
     */
    public void onPostBlockDestroyed(World world, BlockPos pos) {
        removeFromNetwork(world, pos, false);
    }
}