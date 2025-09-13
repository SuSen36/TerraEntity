package org.confluence.terraentity.utils;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 简化的区块管理类，只处理强制区块加载和清理功能
 */
public class ChunkManager {
    private static final Map<ServerLevel, WorldChunkData> WORLD_DATA = new HashMap<>();
    private static final int DEFAULT_CHUNK_LOAD_DURATION = 30; // 默认区块加载持续时间（秒）
    
    /**
     * 表示单个区块强制加载信息的数据类
     */
    public static class ChunkData {
        public final ChunkPos pos;
        public long expiryTick; // 区块过期的时间戳
        
        public ChunkData(ChunkPos pos, long expiryTick) {
            this.pos = pos;
            this.expiryTick = expiryTick;
        }
    }
    
    /**
     * 表示特定世界所有区块数据的数据类
     */
    public static class WorldChunkData {
        private final ConcurrentHashMap<ChunkPos, ChunkData> chunkData = new ConcurrentHashMap<>();
        private final ServerLevel world;
        
        public WorldChunkData(ServerLevel world) {
            this.world = world;
        }
        
        public Optional<ChunkData> getChunkData(ChunkPos pos) {
            return Optional.ofNullable(chunkData.get(pos));
        }
        
        public Collection<ChunkData> getChunks() {
            return chunkData.values();
        }
        
        public ChunkData addForcedChunk(ChunkData chunkData) {
            return this.chunkData.put(chunkData.pos, chunkData);
        }
        
        public ChunkData unloadChunk(ChunkPos pos) {
            ChunkData data = this.chunkData.remove(pos);
            if (data != null) {
                this.world.setChunkForced(pos.x, pos.z, false);
            }
            return data;
        }
        
        public boolean hasChunk(ChunkPos pos) {
            return chunkData.containsKey(pos);
        }
        
        public int getChunkCount() {
            return chunkData.size();
        }
        
        public void clearAllChunks() {
            for (ChunkData data : chunkData.values()) {
                world.setChunkForced(data.pos.x, data.pos.z, false);
            }
            chunkData.clear();
        }
    }
    
    /**
     * 设置区块强制加载默认持续时间
     * @param world 目标世界
     * @param pos 区块位置
     */
    public static void forceLoadChunk(ServerLevel world, ChunkPos pos) {
        forceLoadChunk(world, pos, DEFAULT_CHUNK_LOAD_DURATION);
    }
    
    /**
     * 设置区块强制加载指定持续时间
     * @param world 目标世界
     * @param pos 区块位置
     * @param durationSeconds 持续时间（秒）
     */
    public static void forceLoadChunk(ServerLevel world, ChunkPos pos, int durationSeconds) {
        if (!WORLD_DATA.containsKey(world)) {
            WORLD_DATA.put(world, new WorldChunkData(world));
        }

        WorldChunkData worldData = WORLD_DATA.get(world);
        long currentTick = world.getServer().getTickCount();

        long expiryTick = currentTick + durationSeconds * 20; // 将秒转换为时间戳
        if (worldData.addForcedChunk(new ChunkData(pos, expiryTick)) == null) {
            world.setChunkForced(pos.x, pos.z, true);
        }
    }

    /**
     * 卸载已过期的强制加载区块
     * @param world 目标世界
     * @return 卸载的区块数量
     */
    public static int freeExpiredChunks(ServerLevel world) {
        if (!WORLD_DATA.containsKey(world)) return 0;
        
        WorldChunkData worldData = WORLD_DATA.get(world);
        long currentTick = world.getServer().getTickCount();
        int unloadedCount = 0;

        Iterator<ChunkData> iterator = worldData.getChunks().iterator();
        while (iterator.hasNext()) {
            ChunkData data = iterator.next();
            if (currentTick > data.expiryTick) {
                world.setChunkForced(data.pos.x, data.pos.z, false);
                iterator.remove();
                unloadedCount++;
            }
        }
        
        return unloadedCount;
    }
    
    /**
     * 兼容性方法：与原有的 freeChunks 方法保持兼容
     * @param world 目标世界
     */
    public static void freeChunks(ServerLevel world) {
        freeExpiredChunks(world);
    }
}
