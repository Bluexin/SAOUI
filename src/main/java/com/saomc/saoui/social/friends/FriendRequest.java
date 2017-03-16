package com.saomc.saoui.social.friends;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class FriendRequest {

    private final String friendName;
    public int ticks;

    public FriendRequest(String name, int maxTicks) {
        friendName = name;
        ticks = maxTicks;
    }

    private boolean equals(FriendRequest request) {
        return equals(request == null ? null : request.friendName);
    }

    public final boolean equals(String name) {
        return friendName.equals(name);
    }

    @Override
    public final boolean equals(Object object) {
        return object instanceof FriendRequest ? equals((FriendRequest) object) : equals(String.valueOf(object));
    }

}
