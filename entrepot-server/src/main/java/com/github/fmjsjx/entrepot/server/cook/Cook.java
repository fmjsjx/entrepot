package com.github.fmjsjx.entrepot.server.cook;

public interface Cook {

    byte[] cook(byte[] raw, String remoteIp);

}
