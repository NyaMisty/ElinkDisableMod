# ElinkDisableMod

A module that disable UFI001 firmware's Elink services (e.g. Auto AirplaneMode/Auto Reboot/Remote Control)

## Usage

Compile this repo, install, and enable it in Xposed modules.

## Internal

1. It hooks `Context.startServiceAsUser` in `system_server`, and prevents `ElinkAutoTestService` from loading
    - This disables the remote control function & simcard authentication & auto upgrade
2. It hooks `Context.sendBroadcast` in `com.android.systemui`, and prevents reboot broadcast intent
    - This prevents NetworkController from checking data status & reboot after 5 minutes
    - `TelephonyManager.set_all_ledoff` is also hook to prevent it closing all led lights before reboot

You can find more details in https://blog.csdn.net/jonhy_love/article/details/132757633