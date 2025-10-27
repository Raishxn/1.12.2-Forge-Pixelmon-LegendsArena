package com.raishxn.legendsarena.data;

// Esta interface define os "métodos" que nossos dados terão.
public interface IPlayerData {
    int getElo();
    void setElo(int elo);

    int getWins();
    void setWins(int wins);

    int getLosses();
    void setLosses(int losses);

    // Adicionaremos mais coisas aqui depois (skins, relíquias, etc.)
}