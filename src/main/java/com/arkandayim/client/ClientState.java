package com.arkandayim.client;

public class ClientState {

    public static final int SHATTER_LEN = 60;

    public static float anger = 0.0F;
    public static float displayAnger = 0.0F;
    public static boolean shattered = false;
    public static int shatterAnim = 0;
    public static float flash = 0.0F;
    public static int shakeTicks = 0;
    public static boolean locked = false;
    public static boolean lastLocked = false;
    public static float camYaw = 0.0F;
    public static float camPitch = 0.0F;
    public static double zoom = 70.0D;
    public static int vanishCooldown = 0;

    // A forced gaze at a point in the world, used while the creature is forming.
    public static int gazeTicks = 0;
    public static double gazeX = 0.0D;
    public static double gazeY = 0.0D;
    public static double gazeZ = 0.0D;
}
