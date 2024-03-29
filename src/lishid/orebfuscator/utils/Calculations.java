//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package lishid.orebfuscator.utils;

import gnu.trove.set.hash.TByteHashSet;

import java.lang.reflect.Field;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.zip.Deflater;

import lishid.orebfuscator.Orebfuscator;
import net.minecraft.server.NetServerHandler;
import net.minecraft.server.NetworkManager;
import net.minecraft.server.Packet;
import net.minecraft.server.Packet51MapChunk;
import net.minecraft.server.TileEntity;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class Calculations {
    private static final int CHUNK_SIZE = 81920;
    private static final int REDUCED_DEFLATE_THRESHOLD = 20480;
    private static final int DEFLATE_LEVEL_CHUNKS = 6;
    private static final int DEFLATE_LEVEL_PARTS = 1;
    private static Deflater deflater = new Deflater();
    private static byte[] deflateBuffer = new byte[82020];

    public Calculations() {
    }

    public static void UpdateBlocksNearby(Block block) {
        if (OrebfuscatorConfig.Enabled() && !OrebfuscatorConfig.isTransparent((byte) block.getTypeId())) {
            HashSet<Block> blocks = GetAjacentBlocks(block.getWorld(), new HashSet(), block, OrebfuscatorConfig.UpdateRadius());
            UpdateBlock(block);
            Iterator var2 = blocks.iterator();

            while (var2.hasNext()) {
                Block nearbyBlock = (Block) var2.next();
                UpdateBlock(nearbyBlock);
            }

        }
    }

    public static HashSet<Block> GetAjacentBlocks(World world, HashSet<Block> allBlocks, Block block, int countdown) {
        AddBlockCheck(allBlocks, block);
        if (countdown == 0) {
            return allBlocks;
        } else {
            GetAjacentBlocks(world, allBlocks, block.getRelative(BlockFace.UP), countdown - 1);
            GetAjacentBlocks(world, allBlocks, block.getRelative(BlockFace.DOWN), countdown - 1);
            GetAjacentBlocks(world, allBlocks, block.getRelative(BlockFace.NORTH), countdown - 1);
            GetAjacentBlocks(world, allBlocks, block.getRelative(BlockFace.SOUTH), countdown - 1);
            GetAjacentBlocks(world, allBlocks, block.getRelative(BlockFace.EAST), countdown - 1);
            GetAjacentBlocks(world, allBlocks, block.getRelative(BlockFace.WEST), countdown - 1);
            return allBlocks;
        }
    }

    public static void AddBlockCheck(HashSet<Block> allBlocks, Block block) {
        if (block != null) {
            if (OrebfuscatorConfig.isObfuscated((byte) block.getTypeId()) || OrebfuscatorConfig.isDarknessObfuscated((byte) block.getTypeId())) {
                allBlocks.add(block);
            }

        }
    }

    public static void UpdateBlock(Block block) {
        if (block != null) {
            HashSet<CraftPlayer> players = new HashSet();
            Iterator var2 = block.getWorld().getPlayers().iterator();

            while (var2.hasNext()) {
                Player player = (Player) var2.next();
                if (Math.abs(player.getLocation().getX() - (double) block.getX()) < 176.0D && Math.abs(player.getLocation().getZ() - (double) block.getZ()) < 176.0D) {
                    players.add((CraftPlayer) player);
                }
            }

            var2 = players.iterator();

            while (var2.hasNext()) {
                CraftPlayer player2 = (CraftPlayer) var2.next();
                player2.sendBlockChange(block.getLocation(), block.getTypeId(), block.getData());
            }

        }
    }

    public static boolean GetAjacentBlocksTypeID(BlockInfo info, TByteHashSet IDPool, int index, int x, int y, int z, int countdown) {
        byte id = 0;
        if (y > 126) {
            return true;
        } else {
            if (y < info.sizeY && y >= 0 && x < info.sizeX && x >= 0 && z < info.sizeZ && z >= 0 && index > 0 && info.original.length > index) {
                id = info.original[index];
            } else if (info.startY >= 0) {
                id = (byte) info.world.getTypeId(x + info.startX, y + info.startY, z + info.startZ);
            }

            if (!IDPool.contains(id) && OrebfuscatorConfig.isTransparent(id)) {
                return true;
            } else {
                if (!IDPool.contains(id)) {
                    IDPool.add(id);
                }

                return countdown != 0 && (GetAjacentBlocksTypeID(info, IDPool, index + 1, x, y + 1, z, countdown - 1) || GetAjacentBlocksTypeID(info, IDPool, index - 1, x, y - 1, z, countdown - 1) || GetAjacentBlocksTypeID(info, IDPool, index + info.sizeY * info.sizeZ, x + 1, y, z, countdown - 1) || GetAjacentBlocksTypeID(info, IDPool, index - info.sizeY * info.sizeZ, x - 1, y, z, countdown - 1) || GetAjacentBlocksTypeID(info, IDPool, index + info.sizeY, x, y, z + 1, countdown - 1) || GetAjacentBlocksTypeID(info, IDPool, index - info.sizeY, x, y, z - 1, countdown - 1));
            }
        }
    }

    public static boolean GetAjacentBlocksHaveLight(BlockInfo info, int index, int x, int y, int z, int countdown) {
        return info.world.getLightLevel(x + info.startX, y + info.startY, z + info.startZ) > 0 || countdown != 0 && (GetAjacentBlocksHaveLight(info, index + 1, x, y + 1, z, countdown - 1) || GetAjacentBlocksHaveLight(info, index - 1, x, y - 1, z, countdown - 1) || GetAjacentBlocksHaveLight(info, index + info.sizeY * info.sizeZ, x + 1, y, z, countdown - 1) || GetAjacentBlocksHaveLight(info, index - info.sizeY * info.sizeZ, x - 1, y, z, countdown - 1) || GetAjacentBlocksHaveLight(info, index + info.sizeY, x, y, z + 1, countdown - 1) || GetAjacentBlocksHaveLight(info, index - info.sizeY, x, y, z - 1, countdown - 1));
    }

    public static void Obfuscate(Packet51MapChunk packet, CraftPlayer player) {
        NetServerHandler handler = player.getHandle().netServerHandler;
        if (!Orebfuscator.usingSpout) {
            packet.k = false;
        }

        BlockInfo info = new BlockInfo();
        info.world = player.getHandle().world.getWorld().getHandle();
        info.startX = packet.a;
        info.startY = packet.b;
        info.startZ = packet.c;
        info.sizeX = packet.d;
        info.sizeY = packet.e;
        info.sizeZ = packet.f;
        TByteHashSet blockList = new TByteHashSet();
        int index;
        int x;
        int y;
        if (info.world.getWorld().getEnvironment() == Environment.NORMAL && !OrebfuscatorConfig.worldDisabled(info.world.getServer().getName()) && OrebfuscatorConfig.Enabled()) {
            info.original = new byte[packet.rawData.length];
            System.arraycopy(packet.rawData, 0, info.original, 0, packet.rawData.length);
            if (info.sizeY > 1) {
                index = 0;

                for (x = 0; x < info.sizeX; ++x) {
                    for (int z = 0; z < info.sizeZ; ++z) {
                        for (y = 0; y < info.sizeY; ++y) {
                            boolean Obfuscate = false;
                            blockList.clear();
                            if (OrebfuscatorConfig.isObfuscated(info.original[index])) {
                                Obfuscate = OrebfuscatorConfig.InitialRadius() == 0 || !GetAjacentBlocksTypeID(info, blockList, index, x, y, z, OrebfuscatorConfig.InitialRadius());
                            }

                            if (!Obfuscate && OrebfuscatorConfig.DarknessHideBlocks() && OrebfuscatorConfig.isDarknessObfuscated(info.original[index])) {
                                if (OrebfuscatorConfig.InitialRadius() == 0) {
                                    Obfuscate = true;
                                } else if (!GetAjacentBlocksHaveLight(info, index, x, y, z, OrebfuscatorConfig.InitialRadius())) {
                                    Obfuscate = true;
                                }
                            }

                            if (Obfuscate) {
                                if (OrebfuscatorConfig.EngineMode() == 1) {
                                    packet.rawData[index] = 1;
                                } else if (OrebfuscatorConfig.EngineMode() == 2) {
                                    packet.rawData[index] = OrebfuscatorConfig.GenerateRandomBlock();
                                }
                            }

                            ++index;
                        }
                    }
                }
            }
        }

        index = packet.rawData.length;
        if (deflateBuffer.length < index + 100) {
            deflateBuffer = new byte[index + 100];
        }

        deflater.reset();
        deflater.setLevel(index < 20480 ? 1 : 6);
        deflater.setInput(packet.rawData);
        deflater.finish();
        x = deflater.deflate(deflateBuffer);
        if (x == 0) {
            x = deflater.deflate(deflateBuffer);
        }

        packet.g = new byte[x];
        packet.h = x;
        System.arraycopy(deflateBuffer, 0, packet.g, 0, x);

        while (!GetNetworkManagerQueue(handler.networkManager, 1048576 - 2 * (18 + packet.h))) {
            try {
                Thread.sleep(5L);
            } catch (Exception var11) {
            }
        }

        handler.networkManager.queue(packet);
        Bukkit.getServer().getScheduler().callSyncMethod(Orebfuscator.mainPlugin, () -> Orebfuscator.lastSentPacket = (System.currentTimeMillis() / 1000L));

        Object[] list = info.world.getTileEntities(info.startX, info.startY, info.startZ, info.startX + info.sizeX, info.startY + info.sizeY, info.startZ + info.sizeZ).toArray();

        for (y = 0; y < list.length; ++y) {
            TileEntity tileentity = (TileEntity) list[y];
            if (tileentity != null) {
                Packet p = tileentity.f();
                if (p != null) {
                    handler.sendPacket(p);
                }
            }
        }

    }

    public static boolean GetNetworkManagerQueue(NetworkManager networkManager, int number) {
        try {
            Field p = networkManager.getClass().getDeclaredField("x");
            p.setAccessible(true);
            return Integer.parseInt(p.get(networkManager).toString()) < number;
        } catch (Exception var3) {
            var3.printStackTrace();
            return false;
        }
    }

    public static void LightingUpdate(Block block, boolean skipCheck) {
    }

    public static String MD5(byte[] data) {
        try {
            MessageDigest algorithm = MessageDigest.getInstance("MD5");
            algorithm.reset();
            algorithm.update(data);
            byte[] messageDigest = algorithm.digest();
            StringBuffer hexString = new StringBuffer();

            for (int i = 0; i < messageDigest.length; ++i) {
                hexString.append(Integer.toHexString(255 & messageDigest[i]));
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException var5) {
            var5.printStackTrace();
            return "";
        }
    }

    public int getIndex(int x, int y, int z) {
        return (x & 15) << 11 | (z & 15) << 7 | y & 127;
    }
}
