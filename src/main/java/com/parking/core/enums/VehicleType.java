package com.parking.core.enums;

/**
 * Classification of vehicles that determines parking pricing.
 * <ul>
 *   <li>{@link #OFICIAL}        – official vehicles, exempt from charges</li>
 *   <li>{@link #RESIDENT}       – resident vehicles, flat monthly rate</li>
 *   <li>{@link #NON_RESIDENT}   – non-resident vehicles, per-minute rate</li>
 * </ul>
 */
public enum VehicleType {
    OFICIAL,
    RESIDENT,
    NON_RESIDENT
}
