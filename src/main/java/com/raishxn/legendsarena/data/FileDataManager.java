package com.raishxn.legendsarena.data;

import com.raishxn.legendsarena.LegendsArena;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer; // ADICIONE ESTE IMPORT
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileDataManager {

    // Retorna a pasta onde os dados dos jogadores serão salvos (ex: world/playerdata/legendsarena/)
    private static File getPlayerDataDir() {
        // --- INÍCIO DA CORREÇÃO ---
        // Pega a instância do servidor
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();

        // Pega o save handler do mundo principal (Overworld, dimensão 0) e, a partir dele, o diretório do mundo.
        // Este é o método mais seguro e garantido pela API.
        File worldDir = server.getWorld(0).getSaveHandler().getWorldDirectory();
        // --- FIM DA CORREÇÃO ---

        File playerDataDir = new File(worldDir, "playerdata");
        File modDataDir = new File(playerDataDir, "legendsarena");

        // Cria a pasta se ela não existir
        if (!modDataDir.exists()) {
            modDataDir.mkdirs();
        }
        return modDataDir;
    }

    // Retorna o arquivo específico de um jogador (ex: .../UUID.dat)
    private static File getPlayerFile(EntityPlayer player) {
        return new File(getPlayerDataDir(), player.getUniqueID().toString() + ".dat");
    }

    // Função para SALVAR os dados de um jogador no arquivo dele
    public static void saveData(EntityPlayer player, IPlayerData data) {
        File playerFile = getPlayerFile(player);
        try {
            if (!playerFile.exists()) {
                playerFile.createNewFile();
            }

            NBTTagCompound compound = new NBTTagCompound();
            compound.setInteger("elo", data.getElo());
            compound.setInteger("wins", data.getWins());
            compound.setInteger("losses", data.getLosses());

            // Escreve o NBT no arquivo
            FileOutputStream fos = new FileOutputStream(playerFile);
            CompressedStreamTools.writeCompressed(compound, fos);
            fos.close();

            LegendsArena.LOGGER.info("[SAVE SUCCESS] Dados salvos para " + player.getName() + " (ELO: " + data.getElo() + ")");

        } catch (IOException e) {
            LegendsArena.LOGGER.error("[SAVE FAILED] Falha ao salvar dados para " + player.getName(), e);
        }
    }

    // Função para CARREGAR os dados de um jogador a partir do arquivo dele
    public static void loadData(EntityPlayer player, IPlayerData data) {
        File playerFile = getPlayerFile(player);
        if (playerFile.exists()) {
            try {
                // Lê o NBT do arquivo
                FileInputStream fis = new FileInputStream(playerFile);
                NBTTagCompound compound = CompressedStreamTools.readCompressed(fis);
                fis.close();

                // Carrega os valores, se existirem
                if (compound.hasKey("elo")) {
                    data.setElo(compound.getInteger("elo"));
                }
                if (compound.hasKey("wins")) {
                    data.setWins(compound.getInteger("wins"));
                }
                if (compound.hasKey("losses")) {
                    data.setLosses(compound.getInteger("losses"));
                }

                LegendsArena.LOGGER.info("[LOAD SUCCESS] Dados carregados para " + player.getName() + " (ELO: " + data.getElo() + ")");

            } catch (IOException e) {
                LegendsArena.LOGGER.error("[LOAD FAILED] Falha ao carregar dados para " + player.getName(), e);
            }
        } else {
            LegendsArena.LOGGER.info("[LOAD INFO] Nenhum arquivo de dados encontrado para " + player.getName() + ". Usando valores padrão.");
        }
    }
}