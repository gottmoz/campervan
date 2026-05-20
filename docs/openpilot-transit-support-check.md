# Openpilot Transit Support Check

Checked: 2026-05-19T13:45:12

openpilot commit: `6941a913a39ebff27489063a1bd6bde66f208459`
opendbc commit: `a5276670833fb5c2feeac6b81baaac2814eff173`

## transit

```text
opendbc/car/car.capnp:# ******* events causing controls state machine transition *******
opendbc/car/gm/carstate.py:    # Don't add event if transitioning from INIT, unless it's to an actual button
opendbc/car/gm/values.py:      # Camera transitions to MAX_ACC_REGEN from zero gas and uses friction brakes instantly
opendbc/car/toyota/values.py:PEDAL_TRANSITION = 10. * CV.MPH_TO_MS
opendbc/car/uds.py:  VERIFY_BAUDRATE_TRANSITION_WITH_FIXED_BAUDRATE = 1
opendbc/car/uds.py:  VERIFY_BAUDRATE_TRANSITION_WITH_SPECIFIC_BAUDRATE = 2
opendbc/car/uds.py:  TRANSITION_BAUDRATE = 3
opendbc/car/uds.py:    if link_control_type == LINK_CONTROL_TYPE.VERIFY_BAUDRATE_TRANSITION_WITH_FIXED_BAUDRATE:
opendbc/car/uds.py:    elif link_control_type == LINK_CONTROL_TYPE.VERIFY_BAUDRATE_TRANSITION_WITH_SPECIFIC_BAUDRATE:
opendbc/dbc/bmw_e9x_e8x.dbc:CM_ SG_ 502 TurnSignalActive "Turn signal on or transitioning";
opendbc/dbc/cadillac_ct6_object.dbc:VAL_TABLE_ ExptNxtTrnstnDir 7 "Reserved4" 6 "Reserved3" 5 "Reserved2" 4 "Reserved1" 3 "Traversing Middle Transition" 2 "Traversing Next Transition Right" 1 "Traversing Next Transition Left" 0 "Unknown" ;
opendbc/dbc/ford_lincoln_base_pt.dbc:VAL_TABLE_ TrnPrkSys_D_Actl 15 "Faulty" 14 "NotUsed_5" 13 "NotUsed_4" 12 "NotUsed_3" 11 "NotUsed_2" 10 "NotUsed_1" 9 "FrequencyError" 8 "OutOfRangeHigh" 7 "OutOfRangeLow" 6 "Override" 5 "OutOfPark" 4 "TransitionCloseToOutOfPark" 3 "AtNoSpring" 2 "TransitionCloseToPark" 1 "Park" 0 "NotKnown";
opendbc/dbc/ford_lincoln_base_pt.dbc:VAL_TABLE_ VehStab_D_Stat 15 "Faulty" 14 "No_Data_Exists" 13 "NotUsed6" 12 "NotUsed5" 11 "NotUsed4" 10 "NotUsed3" 9 "NotUsed2" 8 "NotUsed1" 7 "High_SSRA_no_OS" 6 "Straight" 5 "Post_Oversteer" 4 "Post_Transition" 3 "Countersteer" 2 "Understeer" 1 "Oversteer" 0 "Linear";
opendbc/dbc/ford_lincoln_base_pt.dbc:VAL_TABLE_ PrkBrk_D_Stat 7 "Gen_Faults_or_Maint_Mode" 6 "ECD_by_Brake_ECU_Active" 5 "EPB_Limphome_Active" 4 "Rear_Caliper_Open" 3 "RWU_by_EPB_Active" 2 "Rear_Caliper_Transition" 1 "Rear_Caliper_Closed" 0 "Not_Supported";
opendbc/dbc/ford_lincoln_base_pt.dbc:VAL_TABLE_ TrnGear_D_Actl 7 "Fault" 6 "NotUsed" 5 "Manual" 4 "Drive" 3 "Neutral" 2 "Reverse" 1 "Park" 0 "Transition_State";
opendbc/dbc/ford_lincoln_base_pt.dbc:VAL_TABLE_ PrkBrkStatus 7 "GeneralFault_MaintenceMode" 6 "ECD_by_Brake_ECU_Active" 5 "EPB_Limphome_Active" 4 "Rear_Caliper_Open" 3 "RWU_By_EPB_Active" 2 "Rear_Caliper_Transition" 1 "Rear_Caliper_Closed" 0 "Not_Supported";
opendbc/dbc/ford_lincoln_base_pt.dbc:VAL_TABLE_ Veh_Lock_Requestor 31 "Unused8" 30 "Unused7" 29 "Unused6" 28 "Rgtm_Shutlock_Switch" 27 "Boundary_Alert" 26 "Transit_Vehicle_Relock" 25 "Transit_Cargo_Relock" 24 "Transit_Ajar_Lock" 23 "Diagnostics" 22 "Console_Lock" 21 "Child_Lock" 20 "Double_Lock" 19 "Passive_Pasenger" 18 "Passive_Driver" 17 "Slam_Lock_Protect" 16 "SYNC" 15 "Passport" 14 "Passive_Smart_Unlock" 13 "Autorelock" 12 "RemoteStart" 0 "Null" 1 "Autolock" 11 "Smart_Unlock" 10 "Sliding_Door" 9 "Passive" 8 "Remote" 7 "Programming" 6 "Powerslide" 5 "Keypad" 4 "Key_Cylinder" 3 "Interior" 2 "Auto_Unlock";
opendbc/dbc/ford_lincoln_base_pt.dbc:VAL_TABLE_ TrnCnvtClu_D_Actl 3 "Transition_or_Micro_Slip" 2 "Lock_Up_Fixed" 1 "Lock_Up_Slip" 0 "No_Lock_Up";
opendbc/dbc/ford_lincoln_base_pt.dbc:VAL_ 817 Veh_Lock_Requestor 31 "Unused8" 30 "Unused7" 29 "Unused6" 28 "Rgtm_Shutlock_Switch" 27 "Boundary_Alert" 26 "Transit_Vehicle_Relock" 25 "Transit_Cargo_Relock" 24 "Transit_Ajar_Lock" 23 "Diagnostics" 22 "Console_Lock" 21 "Child_Lock" 20 "Double_Lock" 19 "Passive_Pasenger" 18 "Passive_Driver" 17 "Slam_Lock_Protect" 16 "SYNC" 15 "Passport" 14 "Passive_Smart_Unlock" 13 "Autorelock" 12 "RemoteStart" 0 "Null" 1 "Autolock" 11 "Smart_Unlock" 10 "Sliding_Door" 9 "Passive" 8 "Remote" 7 "Programming" 6 "Powerslide" 5 "Keypad" 4 "Key_Cylinder" 3 "Interior" 2 "Auto_Unlock";
opendbc/dbc/ford_lincoln_base_pt.dbc:VAL_ 1045 VehStab_D_Stat 15 "Faulty" 14 "No_Data_Exists" 13 "NotUsed6" 12 "NotUsed5" 11 "NotUsed4" 10 "NotUsed3" 9 "NotUsed2" 8 "NotUsed1" 7 "High_SSRA_no_OS" 6 "Straight" 5 "Post_Oversteer" 4 "Post_Transition" 3 "Countersteer" 2 "Understeer" 1 "Oversteer" 0 "Linear";
opendbc/dbc/ford_lincoln_base_pt.dbc:VAL_ 531 PrkBrkStatus 7 "GeneralFault_MaintenceMode" 6 "ECD_by_Brake_ECU_Active" 5 "EPB_Limphome_Active" 4 "Rear_Caliper_Open" 3 "RWU_By_EPB_Active" 2 "Rear_Caliper_Transition" 1 "Rear_Caliper_Closed" 0 "Not_Supported";
opendbc/dbc/ford_lincoln_base_pt.dbc:VAL_ 374 TrnPrkSys_D_Actl 15 "Faulty" 14 "NotUsed_5" 13 "NotUsed_4" 12 "NotUsed_3" 11 "NotUsed_2" 10 "NotUsed_1" 9 "FrequencyError" 8 "OutOfRangeHigh" 7 "OutOfRangeLow" 6 "Override" 5 "OutOfPark" 4 "TransitionCloseToOutOfPark" 3 "AtNoSpring" 2 "TransitionCloseToPark" 1 "Park" 0 "NotKnown";
opendbc/dbc/hyundai_kia_generic.dbc:CM_ SG_ 1056 SCCInfoDisplay "Goes to 1 for a second while transitioning from Cruise Control to No Message";
opendbc/dbc/mazda_2017.dbc: SG_ TJA_TRANSITION : 27|2@0+ (1,0) [0|63] "" XXX
opendbc/dbc/mazda_2017.dbc:CM_ SG_ 1088 TJA_TRANSITION "3: if TJA signal is 3, otherwise set to 0";
opendbc/dbc/rivian_primary_actuator.dbc:VAL_ 929 VDM_AchDriveMode4x4 0 "VDM_AchDriveMode4x4_A4x4Auto" 1 "VDM_AchDriveMode4x4_P4x4Permenant" 6 "VDM_AchDriveMode4x4_I_Transition" 7 "VDM_AchDriveMode4x4_Fault";
opendbc/dbc/rivian_primary_actuator.dbc:VAL_ 929 VDM_AchDriveModeAbs 0 "VDM_AchDriveModeAbs_OnRoad" 1 "VDM_AchDriveModeAbs_OffRoad" 6 "VDM_AchDriveModeAbs_InTransition" 7 "VDM_AchDriveModeAbs_Fault";
opendbc/dbc/rivian_primary_actuator.dbc:VAL_ 929 VDM_AchDriveModeDrivability 0 "VDM_AchDriveModeDrivability_Eco" 1 "VDM_AchDriveModeDrivability_Sport" 2 "VDM_AchDriveModeDrivability_OffRoad" 6 "VDM_AchDriveModeDrivability_InTransition" 7 "VDM_AchDriveModeDrivability_Null";
opendbc/dbc/rivian_primary_actuator.dbc:VAL_ 929 VDM_AchDriveModeDamping 0 "VDM_AchDriveModeDamping_Kneel" 1 "VDM_AchDriveModeDamping_Aero" 2 "VDM_AchDriveModeDamping_Design" 3 "VDM_AchDriveModeDamping_High" 4 "VDM_AchDriveModeDamping_AirSuspensionMax" 6 "VDM_AchDriveModeDamping_InTransition" 7 "VDM_AchDriveModeDamping_Fault";
opendbc/dbc/rivian_primary_actuator.dbc:VAL_ 929 VDM_AchDriveModeRollControl 0 "VDM_AchDriveModeRollControl_Low" 1 "VDM_AchDriveModeRollControl_Middle" 2 "VDM_AchDriveModeRollControl_High" 6 "VDM_AchDriveModeRollControl_InTransition" 7 "VDM_AchDriveModeRollControl_Null";
opendbc/dbc/rivian_primary_actuator.dbc:VAL_ 929 VDM_AchDriveModeEsp 0 "VDM_AchDriveModeEsp_EspOn" 1 "VDM_AchDriveModeEsp_EspReduced" 2 "VDM_AchDriveModeEsp_EspOff" 6 "VDM_AchDriveModeEsp_InTransition" 7 "VDM_AchDriveModeEsp_Null";
opendbc/dbc/tesla_model3_party.dbc:VAL_ 925 IBST_internalState 5 "TRANSITION_TO_IDLE" 0 "NO_MODE_ACTIVE" 4 "DIAGNOSTIC" 6 "POST_DRIVE_CHECK" 1 "PRE_DRIVE_CHECK" 3 "EXTERNAL_BRAKE_REQUEST" 2 "LOCAL_BRAKE_REQUEST" ;
opendbc/safety/safety.h:  // allow 1s of transition timeout after relay changes state before assessing malfunctioning
opendbc/safety/tests/hyundai_common.py:        # should enter controls allowed on falling edge and not transitioning to cancel
opendbc/safety/tests/test_honda.py:          # should enter controls allowed on falling edge and not transitioning to cancel or main
```

## transit custom

```text
(no matches)
```

## tourneo

```text
(no matches)
```

## FORD_TRANSIT

```text
(no matches)
```

## FORD.*TRANSIT

```text
(no matches)
```

## ford docs CARS

```text
docs/CARS.md:|Ford|Bronco Sport 2021-24|Co-Pilot360 Assist+|[Upstream](#upstream)|
docs/CARS.md:|Ford|Escape 2020-22|Co-Pilot360 Assist+|[Upstream](#upstream)|
docs/CARS.md:|Ford|Escape 2023-24|Co-Pilot360 Assist+|[Upstream](#upstream)|
docs/CARS.md:|Ford|Escape Hybrid 2020-22|Co-Pilot360 Assist+|[Upstream](#upstream)|
docs/CARS.md:|Ford|Escape Hybrid 2023-24|Co-Pilot360 Assist+|[Upstream](#upstream)|
docs/CARS.md:|Ford|Escape Plug-in Hybrid 2020-22|Co-Pilot360 Assist+|[Upstream](#upstream)|
docs/CARS.md:|Ford|Escape Plug-in Hybrid 2023-24|Co-Pilot360 Assist+|[Upstream](#upstream)|
docs/CARS.md:|Ford|Expedition 2022-24|Co-Pilot360 Assist 2.0|[Upstream](#upstream)|
docs/CARS.md:|Ford|Explorer 2020-24|Co-Pilot360 Assist+|[Upstream](#upstream)|
docs/CARS.md:|Ford|Explorer Hybrid 2020-24|Co-Pilot360 Assist+|[Upstream](#upstream)|
docs/CARS.md:|Ford|F-150 2021-23|Co-Pilot360 Assist 2.0|[Upstream](#upstream)|
docs/CARS.md:|Ford|F-150 Hybrid 2021-23|Co-Pilot360 Assist 2.0|[Upstream](#upstream)|
docs/CARS.md:|Ford|Focus 2018|Adaptive Cruise Control with Lane Centering|[Upstream](#upstream)|
docs/CARS.md:|Ford|Focus Hybrid 2018|Adaptive Cruise Control with Lane Centering|[Upstream](#upstream)|
docs/CARS.md:|Ford|Kuga 2020-23|Adaptive Cruise Control with Lane Centering|[Upstream](#upstream)|
docs/CARS.md:|Ford|Kuga Hybrid 2020-23|Adaptive Cruise Control with Lane Centering|[Upstream](#upstream)|
docs/CARS.md:|Ford|Kuga Hybrid 2024|All|[Upstream](#upstream)|
docs/CARS.md:|Ford|Kuga Plug-in Hybrid 2020-23|Adaptive Cruise Control with Lane Centering|[Upstream](#upstream)|
docs/CARS.md:|Ford|Kuga Plug-in Hybrid 2024|All|[Upstream](#upstream)|
docs/CARS.md:|Ford|Maverick 2022|LARIAT Luxury|[Upstream](#upstream)|
docs/CARS.md:|Ford|Maverick 2023-24|Co-Pilot360 Assist|[Upstream](#upstream)|
docs/CARS.md:|Ford|Maverick Hybrid 2022|LARIAT Luxury|[Upstream](#upstream)|
docs/CARS.md:|Ford|Maverick Hybrid 2023-24|Co-Pilot360 Assist|[Upstream](#upstream)|
docs/CARS.md:|Ford|Mustang Mach-E 2021-24|All|[Upstream](#upstream)|
docs/CARS.md:|Ford|Ranger 2024|Adaptive Cruise Control with Lane Centering|[Upstream](#upstream)|
```

## class CAR ford values

```text
opendbc/car/ford/values.py:class CarControllerParams:
opendbc/car/ford/values.py:class CAR(Platforms):
```

## FORD enum lines

```text
opendbc/car/ford/values.py:58:  DELPHI_MRR = 'FORD_CADS'
opendbc/car/ford/values.py:82:    if CP.carFingerprint in (CAR.FORD_F_150_MK14, CAR.FORD_F_150_LIGHTNING_MK1, CAR.FORD_EXPEDITION_MK4):
opendbc/car/ford/values.py:124:  FORD_BRONCO_SPORT_MK1 = FordPlatformConfig(
opendbc/car/ford/values.py:128:  FORD_ESCAPE_MK4 = FordPlatformConfig(
opendbc/car/ford/values.py:135:  FORD_ESCAPE_MK4_5 = FordCANFDPlatformConfig(
opendbc/car/ford/values.py:143:  FORD_EXPLORER_MK6 = FordPlatformConfig(
opendbc/car/ford/values.py:150:  FORD_EXPEDITION_MK4 = FordCANFDPlatformConfig(
opendbc/car/ford/values.py:154:  FORD_F_150_MK14 = FordCANFDPlatformConfig(
opendbc/car/ford/values.py:158:  FORD_F_150_LIGHTNING_MK1 = FordF150LightningPlatform(
opendbc/car/ford/values.py:162:  FORD_FOCUS_MK4 = FordPlatformConfig(
opendbc/car/ford/values.py:166:  FORD_MAVERICK_MK1 = FordPlatformConfig(
opendbc/car/ford/values.py:173:  FORD_MUSTANG_MACH_E_MK1 = FordCANFDPlatformConfig(
opendbc/car/ford/values.py:177:  FORD_RANGER_MK2 = FordCANFDPlatformConfig(
opendbc/car/ford/values.py:253:DATA_IDENTIFIER_FORD_ASBUILT = 0xDE00
opendbc/car/ford/values.py:273:  return bytes([uds.SERVICE_TYPE.READ_DATA_BY_IDENTIFIER]) + p16(DATA_IDENTIFIER_FORD_ASBUILT + block_id - 1)
opendbc/car/ford/values.py:277:  return bytes([uds.SERVICE_TYPE.READ_DATA_BY_IDENTIFIER + 0x40]) + p16(DATA_IDENTIFIER_FORD_ASBUILT + block_id - 1)
```

## Summary

- Transit found as upstream Ford platform: no, based on the grep results above.
- Tourneo found as upstream Ford platform: no, based on the grep results above.
- Ford port files are under `external/opendbc/opendbc/car/ford/`.
